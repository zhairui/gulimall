package com.bigdata.gulimall.product.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.product.dao.AttrGroupDao;
import com.bigdata.gulimall.product.entity.AttrGroupEntity;
import com.bigdata.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long cateLogId) {

            String queryKey = (String) params.get("key");

            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
            //catelog_id == 0时，按照attr_group_id和attr_group_name进行模糊查询，否则要带上catelog_id
            if(cateLogId != 0){
                queryWrapper.eq("catelog_id", cateLogId);
            }
            //select * from pms_attr_group WHERE catelog_id = 1 AND (attr_group_id =key or attr_group_name LIKE '%key%');
            if (StringUtils.isNotEmpty(queryKey)) {

                queryWrapper.and((param) -> {
                    param.eq("attr_group_id", queryKey)
                            .or()
                            .like("attr_group_name", queryKey);
                });
            }

            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );

            return new PageUtils(page);
    }

}