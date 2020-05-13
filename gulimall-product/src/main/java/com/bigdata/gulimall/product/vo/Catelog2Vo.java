package com.bigdata.gulimall.product.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
/**
 * 二级分类VO
 */
public class Catelog2Vo {

    private String catalog1Id;
    private List<Catalog3List> catalog3List;
    private String id;
    private String name;
}