package com.bigdata.gulimall.search.vo;

import lombok.Data;

/**
 * 查询的品牌信息
 */
@Data
public class BrandVo {

    private Long brandId;
    private String brandName;
    private String brandImg;
}
