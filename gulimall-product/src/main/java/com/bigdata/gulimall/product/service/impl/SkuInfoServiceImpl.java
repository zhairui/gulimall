package com.bigdata.gulimall.product.service.impl;

import com.bigdata.gulimall.product.entity.ProductAttrValueEntity;
import com.bigdata.gulimall.product.entity.SkuImagesEntity;
import com.bigdata.gulimall.product.entity.SpuInfoDescEntity;
import com.bigdata.gulimall.product.service.*;
import com.bigdata.gulimall.product.vo.SkuItemSaleAttrVo;
import com.bigdata.gulimall.product.vo.SkuItemVo;
import com.bigdata.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.product.dao.SkuInfoDao;
import com.bigdata.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService imagesService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key=(String)params.get("key");
        if(StringUtils.isNotEmpty(key)){
            queryWrapper.and(item ->{
                item.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId=(String)params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && ! "0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }


        String brandId=(String)params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && ! "0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }


        String min=(String)params.get("min");
        if(StringUtils.isNotEmpty(min)){
            queryWrapper.ge("price",min);
        }

        String max=(String)params.get("max");
        if(StringUtils.isNotEmpty(max)){

            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if( bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    queryWrapper.le("price",max);
                }
            } catch (Exception e) {

            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId);
        List<SkuInfoEntity> skuInfoEntities = this.list(queryWrapper);
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        //1. SKU基本信息获取，pms_sku_info
        SkuInfoEntity byId = this.getById(skuId);
        skuItemVo.setInfo(byId);

        //2.SKU的图片信息获取，pms_sku_images
        List<SkuImagesEntity> skuImagesEntities=imagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(skuImagesEntities);

        //3. 获取SPU销售属性组合 pms_product_attr_value
        List<SkuItemSaleAttrVo> skuItemSaleAttrVos=skuSaleAttrValueService.getSaleAttrsBySpuId(byId.getSpuId());
        skuItemVo.setSaleAttr(skuItemSaleAttrVos);


        //4. 获取SPU的介绍 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(byId.getSpuId());
        skuItemVo.setDesp(spuInfoDescEntity);


        //5. 获取SPU的规格参数信息
        List<SpuItemAttrGroupVo> spuItemAttrGroupVos=attrGroupService.getAttrGroupWithAttrsBySpuId(byId.getSpuId(),byId.getCatalogId());
        skuItemVo.setGroupAttrs(spuItemAttrGroupVos);


        return skuItemVo;
    }

}