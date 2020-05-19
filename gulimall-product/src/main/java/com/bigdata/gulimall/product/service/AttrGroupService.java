package com.bigdata.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.gulimall.product.entity.AttrGroupEntity;
import com.bigdata.gulimall.product.vo.AttrGroupWithAttrVo;
import com.bigdata.gulimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 18:50:19
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long cateLogId);

    List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

