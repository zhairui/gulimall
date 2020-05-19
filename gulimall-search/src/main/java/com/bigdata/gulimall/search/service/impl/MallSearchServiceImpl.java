package com.bigdata.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bigdata.common.to.es.SkuEsModel;
import com.bigdata.common.utils.R;
import com.bigdata.gulimall.search.config.GulimallElasticSearchConfig;
import com.bigdata.gulimall.search.constant.EsConstant;
import com.bigdata.gulimall.search.feign.ProductFeignService;
import com.bigdata.gulimall.search.service.MallSearchService;
import com.bigdata.gulimall.search.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;


    @Autowired
    ProductFeignService productFeignService;


    //去es中进行检索
    @Override
    public SearchReult search(SearchParam param) {
        //动态构建出查询需要的dsl（dsk参考doc/json/gulimall_product_dsl.json）

        SearchReult result=null;
        //1. 创建检索请求
        SearchRequest searchRequest =buildSearchRequest(param);
        try {
            //2.执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            //3.分析响应数据封装成为我们想要的格式
            result=buildSearchResult(param,response);
        } catch (IOException e) {

        }


        return result;
    }

    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchReult buildSearchResult(SearchParam param,SearchResponse response) {
        SearchReult result = new SearchReult();
        SearchHits hits = response.getHits();

        SearchHit[] subHits = hits.getHits();
        List<SkuEsModel> skuEsModels=null;
        if(subHits != null && subHits.length > 0){

            skuEsModels = Arrays.asList(subHits).stream().map(subHit -> {
                String sourceAsString = subHit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = subHit.getHighlightFields().get("skuTitle");
                    String skuTitleHighLight = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitleHighLight);
                }
                return skuEsModel;
            }).collect(Collectors.toList());

        }

        //1.返回所查询到的所有商品
        result.setProducts(skuEsModels);


        //2.当前所有商品所涉及到的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<AttrVo> attrVos = attr_id_agg.getBuckets().stream().map(item -> {
            AttrVo attrVo = new AttrVo();
            //1.获取属性的id
            long attrId = item.getKeyAsNumber().longValue();

            //2.获取属性名
            String attrName = ((ParsedStringTerms) item.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();

            //3.获取属性的所有值
            List<String> attrValues = ((ParsedStringTerms) item.getAggregations().get("attr_value_agg")).getBuckets().stream().map(bucket -> {
                return bucket.getKeyAsString();
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);


            return attrVo;
        }).collect(Collectors.toList());

        result.setAttrs(attrVos);


        //3.当前所有商品所涉及到的所有品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<BrandVo> brandVos = brand_agg.getBuckets().stream().map(item -> {
            BrandVo brandVo = new BrandVo();
            //1.获取id
            long brandId = item.getKeyAsNumber().longValue();
            //2.获取品牌名
            String brandName = ((ParsedStringTerms) item.getAggregations().get("brand_Name_agg")).getBuckets().get(0).getKeyAsString();

            //3.获取品牌图片
            String brandImag = ((ParsedStringTerms) item.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImag);
            return brandVo;
        }).collect(Collectors.toList());

        result.setBrands(brandVos);

        //4.当前所有商品所涉及到的所有分类信息
        ParsedLongTerms catelog_agg = response.getAggregations().get("catelog_agg");
        List<CatelogVo> catelogVos = catelog_agg.getBuckets().stream().map(item -> {
            CatelogVo catelogVo = new CatelogVo();
            //获取分类ID
            String catelogId = item.getKeyAsString();
            catelogVo.setCatelogId(Long.parseLong(catelogId));

            //获取分类名
            ParsedStringTerms catelog_name_agg = item.getAggregations().get("catelog_name_agg");
            String catelogName = catelog_name_agg.getBuckets().get(0).getKeyAsString();
            catelogVo.setCatelogName(catelogName);
            return catelogVo;
        }).collect(Collectors.toList());

        result.setCatelogs(catelogVos);

        //=========以上从聚合信息中获取===========
        //5.分页信息-页码

        result.setPageNum(param.getPageNum());
        //5.分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5.分页信息-总页码
        boolean flag=total%EsConstant.PRODUCT_PAGESIZE == 0;
        int totalPage=flag?(int)total/EsConstant.PRODUCT_PAGESIZE:((int)total/EsConstant.PRODUCT_PAGESIZE)+1;
        result.setTotalPages(totalPage);

        ArrayList<Integer> page = new ArrayList<>();
        for (int i=1;i<=totalPage;i++){
            page.add(i);
        }

        result.setPageNavs(page);


//        //构建面包屑导航功能
//        List<SearchReult.NavVo> navVos = param.getAttrs().stream().map(item -> {
//            SearchReult.NavVo navVo = new SearchReult.NavVo();
//            String[] s = item.split("_");
//            navVo.setNavValue(s[1]);
//
//            R info = productFeignService.info(Long.parseLong(s[0]));
//            if(info.getCode() == 0){
//                AttrResponseVo data = info.getData("attr", new TypeReference<AttrResponseVo>() {
//                });
//                navVo.setNavName(data.getAttrName());
//            }else {
//                navVo.setNavName(s[0]);
//            }
//
//            //2.取消了面包屑后，要跳转到什么地方，将请求地址的URL置空
//            return navVo;
//        }).collect(Collectors.toList());
//
//        result.setNavs(navVos);


        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */
        //1. 构建bool-query
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        //1.1 bool-must
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        //1.2 bool-fiter
        //1.2.1 catelogId
        if(null != param.getCatalog3Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId",param.getCatalog3Id()));
        }

        //1.2.2 brandId
        if(null != param.getBrandId() && param.getBrandId().size() >0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        //1.2.3 attrs
        if(param.getAttrs() != null && param.getAttrs().size() > 0){


            param.getAttrs().forEach(item -> {
                //attrs=1_5寸:8寸&2_16G:8G
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


                //attrs=1_5寸:8寸
                String[] s = item.split("_");
                String attrId=s[0];
                String[] attrValues = s[1].split(":");//这个属性检索用的值
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs",boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });



        }

        //1.2.4 hasStock
        if(null != param.getHasStock()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));
        }


        //1.2.5 skuPrice
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            //skuPrice形式为：1_500或_500或500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] price = param.getSkuPrice().split("_");
            if(price.length==2){
                rangeQueryBuilder.gte(price[0]).lte(price[1]);
            }else if(price.length == 1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQueryBuilder.lte(price[1]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQueryBuilder.gte(price[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        //封装所有的查询条件
        searchSourceBuilder.query(boolQueryBuilder);


        /**
         * 排序，分页，高亮
         */

        //排序
        //形式为sort=hotScore_asc/desc
        if(!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            String[] sortFileds = sort.split("_");

            SortOrder sortOrder="asc".equalsIgnoreCase(sortFileds[1])?SortOrder.ASC:SortOrder.DESC;

            searchSourceBuilder.sort(sortFileds[0],sortOrder);
        }

        //分页
        searchSourceBuilder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //高亮
        if(!StringUtils.isEmpty(param.getKeyword())){

            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            searchSourceBuilder.highlighter(highlightBuilder);
        }



        /**
         * 聚合分析
         */
        //1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);


        //1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_Name_agg")
                .field("brandName").size(1));
        //1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg")
                .field("brandImg").size(1));

        searchSourceBuilder.aggregation(brand_agg);

        //2. 按照分类信息进行聚合
        TermsAggregationBuilder catelog_agg = AggregationBuilders.terms("catelog_agg");
        catelog_agg.field("catelogId").size(20);

        catelog_agg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catelogName").size(1));

        searchSourceBuilder.aggregation(catelog_agg);

        //2. 按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //2.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        //2.1.1 在每个属性ID下，按照属性名进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //2.1.1 在每个属性ID下，按照属性值进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        searchSourceBuilder.aggregation(attr_agg);

        log.debug("构建的DSL语句 {}",searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);


        return searchRequest;
    }
}
