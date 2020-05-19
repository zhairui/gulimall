package com.bigdata.gulimall.product.service.impl;

import com.bigdata.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.bigdata.gulimall.product.entity.AttrEntity;
import com.bigdata.gulimall.product.service.AttrService;
import com.bigdata.gulimall.product.vo.AttrGroupWithAttrVo;
import com.bigdata.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrService attrService;

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

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //查询分类下所关联的所有分组信息
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
        //根据分组信息，得到所有的属性信息
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrVo);
            List<AttrEntity> relationAtr = attrService.getRelationAtr(attrGroupWithAttrVo.getAttrGroupId());
            attrGroupWithAttrVo.setAttrs(relationAtr);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());


        return attrGroupWithAttrVos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //1.查询出当前SPU对应的所有属性的分组信息，以及当前分组下的所有属性对应的值
        return this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
    }

}