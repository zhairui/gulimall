package com.bigdata.gulimall.product.service.impl;

import com.bigdata.gulimall.product.service.CategoryBrandRelationService;
import com.bigdata.gulimall.product.vo.Catalog3List;
import com.bigdata.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;

import com.bigdata.gulimall.product.dao.CategoryDao;
import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.bigdata.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = categoryDao.selectList(null);
        return categoryEntities;
    }

    @Override
    public   void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前的菜单是否被别的地方所引用
        categoryDao.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths=new ArrayList<>();
        List<Long> parentPaths = findParentPath(catelogId, paths);
        Collections.reverse(parentPaths);
        return parentPaths.toArray(new Long[parentPaths.size()]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        relationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        //找出一级分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    /**
     * 逻辑是
     * （1）根据一级分类，找到对应的二级分类
     * （2）将得到的二级分类，封装到Catelog2Vo中
     * （3）根据二级分类，得到对应的三级分类
     * （3）将三级分类封装到Catalog3List
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. 根据一级分类的id查找到对应的二级分类
            List<CategoryEntity> level2Categories = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", level1.getCatId()));

            //3. 根据二级分类，查找到对应的三级分类
            List<Catelog2Vo> catelog2Vos =null;

            if(null != level2Categories || level2Categories.size() > 0){
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //得到对应的三级分类
                        List<CategoryEntity> level3Categories = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", level2.getCatId()));
                        //封装到Catalog3List
                        List<Catalog3List> catalog3Lists = null;
                        if (null != level3Categories) {
                            catalog3Lists = level3Categories.stream().map(level3 -> {
                                Catalog3List catalog3List = new Catalog3List(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                                return catalog3List;
                            }).collect(Collectors.toList());
                        }
                        return new Catelog2Vo(level1.getCatId().toString(), catalog3Lists, level2.getCatId().toString(), level2.getName());
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        return parent_cid;
    }

    public List<Long> findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity id = this.getById(catelogId);
        if(id.getParentCid() != 0){
             findParentPath(id.getParentCid(),paths);
        }
        return paths;
    }
}