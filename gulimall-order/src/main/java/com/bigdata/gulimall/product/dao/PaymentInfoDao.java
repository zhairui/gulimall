package com.bigdata.gulimall.order.dao;

import com.bigdata.gulimall.order.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 23:37:37
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
