# tollge

#### 项目介绍
基于vertx(3.5.2)实现的一组框架的规范, 需要使用module才能运行起来, 简单拼装一下能实现常用的功能.(项目在持续完善中...)   
借鉴springboot的配置可用思想; 你需要什么功能, 只需要引入对应的module依赖就直接可用. 比如你需要搭建一个http的web工程, 就依赖web-http; 如果你需要调用数据库, 就依赖data-hikari; 你有很多其他选择, 只要module里提供了, 你都可用. 具体的使用请参考各个module的说明文档. 当然, demo可能更直观一点.   
如果看不上module的实现, 我非常欢迎你贡献一个合适的module.

#### 软件架构
![image](http://p8ilcqqyk.bkt.clouddn.com/design.png)

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>io.tollge</groupId>
    <artifactId>tollge</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
Gradle
```
compile 'io.tollge:tollge:0.1.0-SNAPSHOT'
```

#### 使用说明

tollge提供基础规范, 由于基于vertx, 请先了解下vertx的基础知识, 目前实现的规范有:   

1. 全局的vertx对象
2. verticle服务发布
3. Biz group自动发现
4. annotation简化代码
5. 多级缓存
6. 全局参数的方案

持续优化中...

#### 已实现的模块

|组|具体实现|
|-|-|
|auth 鉴权|localstorge 本地存储的个性实现|
|data 数据源|data-hikari 基于hikari连接的个性实现|
|job 任务| 暂无|
|oss 对象存储|oss-qiniu 七牛对象存储|
|sms 短信|sms-dayu 大鱼短信|
|web 网站服务|web-http http服务|
|wechat 微信|wechat-gzh 公众号实现|

#### 参与贡献

欢迎大家提供module新实现. 方便自己, 方便大家!

