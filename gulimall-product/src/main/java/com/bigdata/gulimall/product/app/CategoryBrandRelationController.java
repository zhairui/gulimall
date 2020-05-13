package com.bigdata.gulimall.product.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.R;
import com.bigdata.gulimall.product.entity.BrandEntity;
import com.bigdata.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bigdata.gulimall.product.service.CategoryBrandRelationService;
import com.bigdata.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 品牌分类关联
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 21:08:55
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId",required = true) Long catId){
        List<BrandEntity> vos=categoryBrandRelationService.getBrandById(catId);
        List<BrandVo> brandVos = vos.stream().map(item -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data",brandVos);
    }


    /**
     * 获取当前分类所关联的所有分类
     * API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/HgVjlzWV
     * @param brandId
     * @return
     */
    @GetMapping("/catelog/list")
    public R cateLoglist(@RequestParam("brandId") Long brandId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("brand_id",brandId);
        List list = categoryBrandRelationService.list(queryWrapper);

        return R.ok().put("data", list);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存关联关系
     * https://easydoc.xyz/doc/75716633/ZUqEdvA4/7jWJki5e
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
        categoryBrandRelationService.saveDetails(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
