package com.bigdata.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private String attrValues;
}
