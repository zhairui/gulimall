**项目简介**：尚硅谷**《谷粒商城》**2020最新版，源码是根据视频手敲，难免有纰漏，望斧正。

**视频地址**：https://www.bilibili.com/video/av967612226/

**笔      记**：[笔记参考](http://www.jayh.club/#/02.PassJava%E6%9E%B6%E6%9E%84%E7%AF%87/03.%E6%90%AD%E5%BB%BA%E7%AE%A1%E7%90%86%E5%90%8E%E5%8F%B0)

 项目GITEE地址： https://gitee.com/cosmoswong/mall ，如果代码拉取速度慢，图片不加载请移步到Gitee。

## 1. 概览

微服务架构图

![谷粒商城-微服务架构图.jpg](doc/images/谷粒商城-微服务架构图.jpg)

项目微服务：

![image-20200512182015906](doc/images/image-20200512182015906.png)



项目的前端页面：

![image-20200511010829465](doc/images/image-20200511010829465.png)

后台管理页面：

![image-20200511010920710](doc/images/image-20200511010920710.png)



项目所创建的微服务

![image-20200509122426436](doc/images/image-20200509122426436.png)

调用端口：

| 服务名               | 端口  |
| -------------------- | ----- |
| gulimall-ware        | 11000 |
| gulimall-member      | 8000  |
| gulimall-product     | 10000 |
| gulimall-gateway     | 88    |
| gulimall-coupon      | 7000  |
| gulimall-third-party | 30000 |
| renren-fast          | 8080  |
| gulimall-order       | 9000  |
| gulimall-search      | 12000  |

## FAQ

### 1、[微服务太多，管理起来不方便](https://shimo.im/docs/WTPpdrDt66HxCQx6/)

### 2、[SPU管理中，修改商品规格，显示400错误状态码](https://shimo.im/docs/wpTxdcHpYRRV3dc6/)



### 笔记

#### 1）分布式基础

#### 2）[分布式高级](https://shimo.im/docs/WWhRX8trqVtRckGj/ )

#### 3）集群

