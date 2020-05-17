package com.bigdata.gulimall.search.service;

import com.bigdata.gulimall.search.vo.SearchParam;
import com.bigdata.gulimall.search.vo.SearchReult;

public interface MallSearchService {
    /**
     *
     * @param param 检索的所有参数
     * @return  检索的结果
     */
    SearchReult search(SearchParam param);
}
