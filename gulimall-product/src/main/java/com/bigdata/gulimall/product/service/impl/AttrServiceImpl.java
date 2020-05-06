package com.bigdata.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bigdata.common.constant.ProductConstant;
import com.bigdata.gulimall.product.dao.*;
import com.bigdata.gulimall.product.entity.AttrGroupEntity;
import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.bigdata.gulimall.product.service.CategoryService;
import com.bigdata.gulimall.product.vo.AttrResponseVo;
import com.bigdata.gulimall.product.vo.AttrVo;
import com.bigdata.gulimall.product.entity.AttrAttrgroupRelationEntity;
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

        if(ProductConstant.ATTR_TYPE_SALE.getCode() == attrEntity.getAttrType()){
            //保存关联关系
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }


    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long cateLogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType)?ProductConstant.ATTR_TYPE_SALE.getCode():ProductConstant.ATTR_TYPE_BASE.getCode());
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

            if("base".equals(attrType)){
                //这里之所以没有使用多表关联查询，是因为在大数据量的情况下，产生的笛卡尔积很庞大
                //所以暂时只能通过多次查询的方式来获取到结果
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrResponseVo.getAttrId()));

                if (null != relationEntity) {
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

        if(ProductConstant.ATTR_TYPE_SALE.getCode() == byId.getAttrType()){
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

        if(ProductConstant.ATTR_TYPE_SALE.getCode() == attrEntity.getAttrType()){
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
}