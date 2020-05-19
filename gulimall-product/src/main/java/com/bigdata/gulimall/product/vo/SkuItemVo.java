package com.bigdata.gulimall.product.vo;

import com.bigdata.gulimall.product.entity.SkuImagesEntity;
import com.bigdata.gulimall.product.entity.SkuInfoEntity;
import com.bigdata.gulimall.product.entity.SpuInfoDescEntity;
import com.bigdata.gulimall.product.service.SpuInfoDescService;
import lombok.Data;

import java.util.List;

/**
 * 商品详情信息，包含商品基本属性，图片信息，分类信息，描述信息，规格
 */
@Data
public class SkuItemVo {
    //1. SKU基本信息获取，pms_sku_info
    SkuInfoEntity info;

    //2.SKU的图片信息获取，pms_sku_images
    List<SkuImagesEntity> images;


    //3. 获取SPU销售信属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4. 获取SPU的介绍
    SpuInfoDescEntity desp;


    //5. 获取SPU的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

}
