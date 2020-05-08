/**
  * Copyright 2020 bejson.com 
  */
package com.bigdata.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Auto-generated: 2020-05-08 14:3:53
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class MemberPrice {
    //会员等级ID
    private Long id;
    //会员等级名
    private String name;
    //会员价格
    private BigDecimal price;
}