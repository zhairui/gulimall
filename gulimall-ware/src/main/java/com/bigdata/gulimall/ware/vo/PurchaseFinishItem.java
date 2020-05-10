package com.bigdata.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseFinishItem {
    private Long itemId;
    private Integer status;
    private String reason;
}
