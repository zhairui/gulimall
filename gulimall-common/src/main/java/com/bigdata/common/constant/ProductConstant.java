package com.bigdata.common.constant;

public class   ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(0,"销售属性"),ATTR_TYPE_SALE(1,"基本属性");
        private int code;
        private String message;

        AttrEnum(int code, String message) {
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
    public enum StatusEnum{
        NEA_SPU(0,"新建"),SPU_UP(1,"商品上架"),SPU_DOWN(2,"商品下架");
        private int code;
        private String message;

        StatusEnum(int code, String message) {
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

}
