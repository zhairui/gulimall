package com.bigdata.gulimall.search.vo;

import com.bigdata.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * 查询的响应数据
 */
@Data
public class SearchReult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    //当前页面
    private Integer pageNum;

    //总记录数
    private Long total;

    //总页码数
    private Integer totalPages;

    //导航页
    private List<Integer> pageNavs;

    //查询到的所有品牌信息
    private List<BrandVo> brands;

    //查询所涉及到的所有属性
    private List<AttrVo> attrs;

    //查询所涉及到的所有分类信息
    private List<CatelogVo> catelogs;


    //面包屑导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

}
