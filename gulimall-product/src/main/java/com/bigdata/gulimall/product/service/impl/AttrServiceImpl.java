package com.bigdata.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bigdata.common.constant.ProductConstant;
import com.bigdata.gulimall.product.dao.*;
import com.bigdata.gulimall.product.entity.AttrGroupEntity;
import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.bigdata.gulimall.product.service.CategoryService;
import com.bigdata.gulimall.product.vo.AttrGroupRelationVo;
import com.bigdata.gulimall.product.vo.AttrResponseVo;
import com.bigdata.gulimall.product.vo.AttrVo;
import com.bigdata.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.product.entity.AttrEntity;
import com.bigdata.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //保存基本数据
        this.save(attrEntity);

        if(ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode() == attrEntity.getAttrType() && attr.getAttrGroupId() != null ){
            //保存关联关系
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }


    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long cateLogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType)?ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (cateLogId != 0) {
            queryWrapper.eq("catelog_id", cateLogId);
        }

        String key = (String) params.get("key");

        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params)
                , queryWrapper
        );

        List<AttrEntity> records = page.getRecords();

        //由于AttrEntity中不包含有catelogName和groupName字段，因此需要使用AttrResponseVo来封装最终返回页面的结果
        List<AttrResponseVo> responseVos = records.stream().map(attrEntity -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            if("base".equalsIgnoreCase(attrType)){
                //这里之所以没有使用多表关联查询，是因为在大数据量的情况下，产生的笛卡尔积很庞大
                //所以暂时只能通过多次查询的方式来获取到结果
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrResponseVo.getAttrId()));

                if (null != relationEntity && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrResponseVo.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }

            return attrResponseVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrEntity byId = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(byId, attrResponseVo);

        AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", byId.getAttrId()));

        if(ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode() == byId.getAttrType()){
            //1. 设置分组信息
            if (null != relationEntity) {
                Long attrGroupId = relationEntity.getAttrGroupId();
                attrResponseVo.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                if (null != attrGroupEntity) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        //2. 设置分类信息
        Long catelogId = attrResponseVo.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (null != categoryEntity) {
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }
        return attrResponseVo;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        if(ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode() == attrEntity.getAttrType()){
            //修改关联分组
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrVo.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            UpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId());

            Integer count = relationDao.selectCount(updateWrapper);
            if( count > 0){
                relationDao.update(relationEntity,updateWrapper);
            }else {
                relationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组ID查找关联的所有属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAtr(Long attrgroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId);
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(queryWrapper);
        List<AttrEntity> entityList = relationEntities.stream().map(attrAttrgroupRelationEntity -> {
            Long attrId = attrAttrgroupRelationEntity.getAttrId();

            return this.getById(attrId);
        }).filter(attrEntity -> {
            return attrEntity != null;
        }).collect(Collectors.toList());

        return entityList;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> entityList = Arrays.asList(attrGroupRelationVos).stream().map(param -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(param, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());



        relationDao.deleteBatchRelation(entityList);
    }

    /**获取属性分组中，没有关联被其他属性分组和自身所关联的其他属性
     * 总的思想是：
     * （1）根据属性分组id获取到对应的分类ID
     * （2）在pms_attr_group表中，找到所有具有相同分类ID的所有记录，并获取到它们的属性分组id
     * （3）根据这些属性分组ID，到pms_attr_attrgroup_relation中间表中查询出所关联的所有属性id
     * （4）拿到这些属性id，根据分类ID和属性类型（基本/销售），在pms_attr中查询所有不在这些属性id范围内的其他属性记录
     * （5）将结果封装到pageUtil中并返回
     *
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {

        //1. 当前分组只能关联自己所属的分类的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2. 当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的其他属性分组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
//                .ne("attr_group_id", attrgroupId));
        List<Long> collect = groupEntities.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2)、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id",collect));
        List<Long> attrIds = entities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3)、从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if(null != attrIds && attrIds.size() > 0){
            queryWrapper.notIn("attr_id", attrIds);
        }

        String key = (String)params.get("key");
        if(StringUtils.isNotEmpty(key)){
            queryWrapper.and(w ->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        PageUtils pageUtils = new PageUtils(page);


        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {

        return this.baseMapper.selectSearchAttrIds(attrIds);
    }
}