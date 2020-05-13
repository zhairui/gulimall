package com.bigdata.gulimall.product.app;

import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.R;
import com.bigdata.gulimall.product.entity.ProductAttrValueEntity;
import com.bigdata.gulimall.product.service.AttrService;
import com.bigdata.gulimall.product.service.ProductAttrValueService;
import com.bigdata.gulimall.product.vo.AttrResponseVo;
import com.bigdata.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 21:08:55
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /* 功能：根据spuId信息查询出对应的规格参数信息
       API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/GhhJhkg7
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entityList=productAttrValueService.baseAttrListForSpu(spuId);
        return  R.ok().put("data",entityList);
    };



    /**
     * 获取分类规格参数或获取分类销售属性
     * API:https://easydoc.xyz/doc/75716633/ZUqEdvA4/Ld1Vfkcd
     * API:https://easydoc.xyz/doc/75716633/ZUqEdvA4/FTx6LRbR
     * 分类规格参数：/product/attr/base/list/{catelogId}
     * 分类销售属性：/product/attr/sale/list/{catelogId}
     * @param params
     * @param cateLogId
     * @param attrType 属性类型[0-销售属性，1-基本属性，
     * @return
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("attrType") String attrType,
                          @PathVariable("catelogId") Long cateLogId){
        PageUtils page = attrService.queryBaseAttrPage(params,cateLogId,attrType);
         return R.ok().put("page",page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 功能：查询属性详情
     * API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/7C3tMIuF
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        AttrResponseVo responseVo=attrService.getAttrInfo(attrId);

        return R.ok().put("attr", responseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);
        return R.ok();
    }

    /**
     * 功能：修改商品规格
     * API：https://easydoc.xyz/doc/75716633/ZUqEdvA4/GhnJ0L85
     * @param spuId
     * @param entities
     * @return
     */
    @PostMapping("/update/{spuId}")
    public R update(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
