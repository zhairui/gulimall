package com.bigdata.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
/**
 * 三级分类VO
 */
public class Catalog3List {

    private String catalog2Id;
    private String id;
    private String name;
}