package com.bigdata.gulimall.member.dao;

import com.bigdata.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 23:42:09
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
