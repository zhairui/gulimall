package com.bigdata.gulimall.product.dao;

import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 18:50:19
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
