package com.bigdata.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigdata.gulimall.product.entity.BrandEntity;
import com.bigdata.gulimall.product.service.BrandService;
import com.bigdata.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    public void getCatelogPath(){
        Long[] catelogPath = categoryService.findCatelogPath(227L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }


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
    void listBrand() {
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>(new BrandEntity()));
        list.forEach(t -> System.out.println(t));
    }
}
