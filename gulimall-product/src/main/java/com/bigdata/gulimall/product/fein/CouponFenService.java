package com.bigdata.gulimall.product.fein;

import com.bigdata.common.to.SkuReductionTo;
import com.bigdata.common.to.SpuBoundTo;
import com.bigdata.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "gulimall-coupon")
public interface CouponFenService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);


}
