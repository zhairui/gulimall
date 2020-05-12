package com.bigdata.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.bigdata.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bigdata.gulimall.ware.entity.WareSkuEntity;
import com.bigdata.gulimall.ware.service.WareSkuService;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.R;



/**
 * 商品库存
 *
 * @author cosmoswong
 * @email cosmoswong@sina.com
 * @date 2020-04-23 23:42:50
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 根据skuId查询是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> skuHasStockVos=wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(skuHasStockVos);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
