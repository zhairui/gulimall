package com.bigdata.gulimall.search.controller;

import com.bigdata.common.exception.BizCodeEnum;
import com.bigdata.common.to.es.SkuEsModel;
import com.bigdata.common.utils.R;
import com.bigdata.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){

        boolean status=false;
        try {
            status = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("商品上架错误{}",e);
            return R.error(BizCodeEnum.PRODUCT_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_EXCEPTION.getMsg());
        }

        if(status){
            return R.error(BizCodeEnum.PRODUCT_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_EXCEPTION.getMsg());
        }else {
            return R.ok();
        }

    }
}
