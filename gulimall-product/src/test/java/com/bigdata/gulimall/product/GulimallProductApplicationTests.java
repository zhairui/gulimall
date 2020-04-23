package com.bigdata.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigdata.gulimall.product.entity.BrandEntity;
import com.bigdata.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
         brandEntity.setName("huawei");
         brandService.save(brandEntity);
        System.out.println("success.");
    }
    @Test
    void updateValue() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("huawei honor8");
        brandService.updateById(brandEntity);
        System.out.println("success.");
    }
    @Test
    void listBrand(){
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>(new BrandEntity()));
        list.forEach(t-> System.out.println(t));
    }
}
