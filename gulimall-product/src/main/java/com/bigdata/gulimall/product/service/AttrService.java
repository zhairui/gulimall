package com.bigdata.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.gulimall.product.vo.AttrGroupRelationVo;
import com.bigdata.gulimall.product.vo.AttrResponseVo;
import com.bigdata.gulimall.product.vo.AttrVo;
import com.bigdata.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 18:50:20
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long cateLogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAtr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params);

    /**
     * 在指定属性集合中，筛选出支持检索的属性的属性ID
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

