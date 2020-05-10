package com.bigdata.gulimall.ware.service.impl;

import com.bigdata.common.constant.WareConstant;
import com.bigdata.gulimall.ware.entity.PurchaseDetailEntity;
import com.bigdata.gulimall.ware.entity.WareSkuEntity;
import com.bigdata.gulimall.ware.service.PurchaseDetailService;
import com.bigdata.gulimall.ware.service.WareSkuService;
import com.bigdata.gulimall.ware.vo.MergeVo;
import com.bigdata.gulimall.ware.vo.PurchaseFinishItem;
import com.bigdata.gulimall.ware.vo.PurchaseFinishVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.ware.dao.PurchaseDao;
import com.bigdata.gulimall.ware.entity.PurchaseEntity;
import com.bigdata.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        //取得采购单中状态为0或1的采购单
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status",0).or().eq("status",1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     *
     * @param mergeVo
     */
    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        //如果purchaseId不存在，保存PurchaseEntity并设置状态和时间戳
        //保存到wms_purchase
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId=purchaseEntity.getId();

        }else{
            //如果采购单的状态不是新建或已分配，则返回
            PurchaseEntity purchaseEntity = this.baseMapper.selectById(mergeVo.getPurchaseId());
            boolean flage=(purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                    (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            if(!flage){
                return;
            }

        }
        //更新wms_purchase_detail
        Long finalPurchaseId =purchaseId;
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

    @Override
    @Transactional
    public void received(List<Long> ids) {
        //1. 确认当前采购单是新建或已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(item -> {
            PurchaseEntity byId = this.getById(item);
            return byId;
        }).filter(item -> {
            //过滤出新建或已分配的采购单
            boolean flage=(item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                          (item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            return flage;
        }).map(item -> {
            //将采购单状态设置为已接收，并且只更新状态和时间字段
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(item.getId());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            purchaseEntity.setUpdateTime(new Date());
            //item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            //item.setUpdateTime(new Date());
            item = null;//GC
            return  purchaseEntity;
        }).collect(Collectors.toList());


        //2. 改变采购单的状态
        this.updateBatchById(purchaseEntities);


        //3. 改变采购项的状态
        purchaseEntities.stream().forEach(item ->{
            List<PurchaseDetailEntity> entities=purchaseDetailService.listDetailByPurchaseId(item.getId());
            //只更新状态字段即可
            List<PurchaseDetailEntity> collect = entities.stream().map(detailEntity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(detailEntity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                detailEntity=null;//GC
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        });

    }

    /**
     * 更新商品的采购状态，库存状态和更新采购单的状态
     * （1）如果商品采购成功，则更新采购状态为已完成，更新库存值，更新采购单已经完成
     * （2）如果商品采购失败，则更新采购状态为采购失败，更新采购单状态位失败
     * @param finishVo
     */
    @Transactional
    @Override
    public void done(PurchaseFinishVo finishVo) {


        //1. 改变采购项的状态
        List<PurchaseFinishItem> finishVoItems = finishVo.getItems();

        AtomicBoolean flag= new AtomicBoolean(true);
        //设置采购项的状态
        List<PurchaseDetailEntity> detailEntities = finishVoItems.stream().map(item -> {

            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            //是否采购失败
            boolean failFlag = item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASEERROR.getCode();
            if (failFlag) {
                detailEntity.setStatus(item.getStatus());
                flag.set(false);
            } else {
                //采购成功

                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //2. 将成功的采购进行入库
                //采购成功的商品ID
                Long itemId = item.getItemId();
                //取得sku_id，sku_num和ware_id
                PurchaseDetailEntity byId = purchaseDetailService.getById(itemId);

                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());

            }
            item = null;//GC
            return detailEntity;
        }).collect(Collectors.toList());


        purchaseDetailService.updateBatchById(detailEntities);

        //3. 改变采购单的状态
        Long id = finishVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag.get() ? WareConstant.PurchaseStatusEnum.FINISH.getCode():
                WareConstant.PurchaseStatusEnum.HASEERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);

    }

}