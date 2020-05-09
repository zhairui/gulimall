package com.bigdata.gulimall.product.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.product.dao.SkuInfoDao;
import com.bigdata.gulimall.product.entity.SkuInfoEntity;
import com.bigdata.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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

}