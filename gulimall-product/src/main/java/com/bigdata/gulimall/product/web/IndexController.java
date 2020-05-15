package com.bigdata.gulimall.product.web;

import com.bigdata.gulimall.product.entity.CategoryEntity;
import com.bigdata.gulimall.product.service.CategoryService;
import com.bigdata.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping(value = {"/","/index.html"})
    public String indexPage(Model model){
        //1. 查询出所有的一级分类
        List<CategoryEntity> categoryEntityList=categoryService.getLevel1Categories();
        model.addAttribute("categories",categoryEntityList);
        return  "index";
    }


    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){

        Map<String, List<Catelog2Vo>> map=categoryService.getCatelogJson();
        return map;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        //1.获取一把锁，只要名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        //2.加锁和解锁


        try {
            lock.lock();
            System.out.println("加锁成功，执行业务方法..."+Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e){

        }finally {
            lock.unlock();
            System.out.println("释放锁..."+Thread.currentThread().getId());
        }
        return "hello";
    }

    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        RReadWriteLock writeLock=redisson.getReadWriteLock("rw-loc");
        String uuid = null;
        RLock lock = writeLock.writeLock();
        lock.lock();
        try {
            log.info("写锁加锁成功");
            uuid = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue",uuid);
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            log.info("写锁释放");

        }
        return uuid;
    }

    @GetMapping("/read")
    @ResponseBody
    public String redValue(){
        String uuid = null;
        RReadWriteLock readLock=redisson.getReadWriteLock("rw-loc");
        RLock lock = readLock.readLock();
        lock.lock();
        try {
            log.info("读锁加锁成功");
             uuid = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            log.info("读锁释放");
        }
        return uuid;
    }
}
