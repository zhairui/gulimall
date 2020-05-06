package com.bigdata.common.constant;

public enum  ProductConstant {
    ATTR_TYPE_BASE(0,"销售属性"),ATTR_TYPE_SALE(1,"基本属性");
    private int code;
    private String message;

    ProductConstant(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
