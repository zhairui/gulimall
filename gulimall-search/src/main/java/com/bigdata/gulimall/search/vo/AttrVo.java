package com.bigdata.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 查询的属性信息
 */
@Data
public class AttrVo {
    private Long attrId;

    private String attrName;

    private List<String> attrValue;
}
