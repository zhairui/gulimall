package com.bigdata.gulimall.product.app;

import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.R;
import com.bigdata.gulimall.product.entity.AttrEntity;
import com.bigdata.gulimall.product.entity.AttrGroupEntity;
import com.bigdata.gulimall.product.service.AttrAttrgroupRelationService;
import com.bigdata.gulimall.product.service.AttrGroupService;
import com.bigdata.gulimall.product.service.AttrService;
import com.bigdata.gulimall.product.service.CategoryService;
import com.bigdata.gulimall.product.vo.AttrGroupRelationVo;
import com.bigdata.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 21:08:55
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;

    /**
     * 功能描述：获取分类下所有分组&关联属性
     * API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/6JM6txHf
     */
     @GetMapping("{catelogId}/withattr")
     public R getAttrGroupWithAttr(@PathVariable("catelogId") Long catelogId){
         List<AttrGroupWithAttrVo> vos=attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
         return R.ok().put("data",vos);
    }

    /**功能：添加属性和属性分组的关联关系
     * API：<https://easydoc.xyz/doc/75716633/ZUqEdvA4/VhgnaedC
     * @param relationVo
     * @return
     */
    @PostMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrGroupRelationVo> relationVo){

        relationService.saveAttrRelations(relationVo);


        return  R.ok();
    }

    /**
     * 获取属性分组中，没有关联被其他属性分组和自身所关联的其他属性
     * API:https://easydoc.xyz/doc/75716633/ZUqEdvA4/d3EezLdO
     * /product/attrgroup/{attrgroupId}/noattr/relation
     * @param attrgroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,@RequestParam Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(attrgroupId,params);

        return R.ok().put("page",page);
    }



    /**
     * 删除属性与分组的关联关系
     * API: https://easydoc.xyz/doc/75716633/ZUqEdvA4/qn7A2Fht
     * @param attrGroupRelationVos
     * @return
     */
    ///product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R delAttrRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
        attrService.deleteRelation(attrGroupRelationVos);

         return R.ok();
    }

    /**
     * 获取属性分组的关联的所有属性
     * API:https://easydoc.xyz/doc/75716633/ZUqEdvA4/LnjzZHPj
     * //product/attrgroup/{attrgroupId}/attr/relation
     * @param attrgroupId 分组ID
     * @return
     */
    @GetMapping("{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entityList = attrService.getRelationAtr(attrgroupId);
        return R.ok().put("data",entityList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{cateLogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("cateLogId") Long cateLogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page =attrGroupService.queryPage(params,cateLogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path=categoryService.findCatelogPath(catelogId);

        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
