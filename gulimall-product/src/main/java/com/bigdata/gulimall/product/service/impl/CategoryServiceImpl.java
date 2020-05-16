package com.bigdata.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.common.utils.PageUtils;
import com.bigdata.common.utils.Query;
import com.bigdata.gulimall.product.dao.CategoryDao;
import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.bigdata.gulimall.product.service.CategoryBrandRelationService;
import com.bigdata.gulimall.product.service.CategoryService;
import com.bigdata.gulimall.product.vo.Catalog3List;
import com.bigdata.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService relationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

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


    /**
     * （1）更新分类数据
     * （2）采用失效模式更新缓存
     * （3）可以有两种方式来实现同时更新缓存到redis的一级或三级分类数据
     * @param category
     */
//    @Caching(evict={
//       @CacheEvict(value = {"category"},key = "'level1Categorys'"),
//       @CacheEvict(value = {"category"},key = "'getCatelogJson'")
//    })
    @CacheEvict(value = {"category"},allEntries = true)
    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        relationService.updateCategory(category.getCatId(),category.getName());
    }

    @Cacheable(value = {"category"},key = "'level1Categorys'")
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        log.info("查询一级分类数据");
        //找出一级分类
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    /**
     * （1）根据一级分类，找到对应的二级分类
     * （2）将得到的二级分类，封装到Catelog2Vo中
     * （3）根据二级分类，得到对应的三级分类
     * （3）将三级分类封装到Catalog3List
     *  (4) 将执行结果放入到缓存中
     * @return
     */

    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        log.info("查询数据库");
        //一次性查询出所有的分类数据，减少对于数据库的访问次数，后面的数据操作并不是到数据库中查询，而是直接从这个集合中获取，
        // 由于分类信息的数据量并不大，所以这种方式是可行的
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getParentCid(categoryEntities,0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. 根据一级分类的id查找到对应的二级分类
            List<CategoryEntity> level2Categories = getParentCid(categoryEntities,level1.getCatId());

            //3. 根据二级分类，查找到对应的三级分类
            List<Catelog2Vo> catelog2Vos =null;

            if(null != level2Categories || level2Categories.size() > 0){
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //得到对应的三级分类
                    List<CategoryEntity> level3Categories = getParentCid(categoryEntities,level2.getCatId());
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

    @Deprecated
    public Map<String,List<Catelog2Vo>> getCatelogJsons(){
        //先从缓存中获取分类数据，如果没有再从数据库中查询，并且分类数据是以JSON的形式存放到Reids中的
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");

        //1. 空结果缓存：解决缓存穿透
        //2. 设置过期时间(加随机值)：解决缓存雪崩
        //3. 加锁：解决缓存击穿（使用分布式锁）
        if(StringUtils.isEmpty(catelogJson)){

            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedissonLock();

            return catelogJsonFromDb;

        }

        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        log.warn("缓存命中");

        return  stringListMap;
    }

    /**
     * 使用Redisson分布式锁来实现多个服务共享同一缓存中的数据
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {

        RLock lock = redissonClient.getLock("CatelogJson-lock");
        //该方法会阻塞其他线程向下执行，只有释放锁之后才会接着向下执行
        lock.lock();
        Map<String, List<Catelog2Vo>> catelogJsonFromDb;
        try {
            //从数据库中查询分类数据
            catelogJsonFromDb = getCatelogJsonFromDb();
        } finally {
           lock.unlock();
        }

        return catelogJsonFromDb;

    }


    /**
     * 使用分布式锁来实现多个服务共享同一缓存中的数据
     * （1）设置读写锁，失败则表明其他线程先于该线程获取到了锁，则执行自旋，成功则表明获取到了锁
     * （2）获取锁成功，查询数据库，获取分类数据
     * （3）释放锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {
        String uuid= UUID.randomUUID().toString();
        //设置redis分布式锁，成功则返回true，否则返回false，该操作是原子性的
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if(lock==null || !lock){
            //获取锁失败，重试
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            log.warn("获取锁失败，重新获取...");
           return getCatelogJsonFromDbWithRedisLock();
        }else{
            //获取锁成功
            log.warn("获取锁成功:)");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb;
            try {
                //从数据库中查询分类数据
                catelogJsonFromDb = getCatelogJsonFromDb();
            } finally {
                //确保一定会释放锁
                String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript(script,Long.class),Arrays.asList("lock"),uuid);
                log.warn("释放锁成功:)");
            }
            return catelogJsonFromDb;
        }

    }


    /**
     * 逻辑是
     * （0）首先查询Redis缓存中是否有分类数据信息，有则返回，否则继续执行
     * （1）根据一级分类，找到对应的二级分类
     * （2）将得到的二级分类，封装到Catelog2Vo中
     * （3）根据二级分类，得到对应的三级分类
     * （3）将三级分类封装到Catalog3List
     * （4）将查询结果放入到Redis中
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        //先从redis缓存中查询，如果有数据，则返回查询结果
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if(!StringUtils.isEmpty(catelogJson)){
            log.warn("从缓存中获取数据");
            return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        }
        log.warn("查询数据库");
        //一次性查询出所有的分类数据，减少对于数据库的访问次数，后面的数据操作并不是到数据库中查询，而是直接从这个集合中获取，
        // 由于分类信息的数据量并不大，所以这种方式是可行的
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

         //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getParentCid(categoryEntities,0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), level1 -> {
            //2. 根据一级分类的id查找到对应的二级分类
            List<CategoryEntity> level2Categories = getParentCid(categoryEntities,level1.getCatId());

            //3. 根据二级分类，查找到对应的三级分类
            List<Catelog2Vo> catelog2Vos =null;

            if(null != level2Categories || level2Categories.size() > 0){
                catelog2Vos = level2Categories.stream().map(level2 -> {
                    //得到对应的三级分类
                    List<CategoryEntity> level3Categories = getParentCid(categoryEntities,level2.getCatId());
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

        //将查询结果放入到redis中
        redisTemplate.opsForValue().set("catelogJson",JSON.toJSONString(parent_cid),1, TimeUnit.DAYS);


        return parent_cid;
    }

    /**
     * 在selectList中找到parentId等于传入的parentCid的所有分类数据
     * @param selectList
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return  collect;
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