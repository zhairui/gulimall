package com.bigdata.gulimall.coupon.dao;

import com.bigdata.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 23:38:48
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
