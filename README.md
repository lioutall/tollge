# tollge

#### Introduction
Based on the specification of a set of frameworks implemented by vertx (3.5.2), you need to use the modules to run it. Simply assemble it to implement common functions. (The project is in continuous improvement...)
Learn from the springboot configuration, what features you need, just need to import the corresponding module dependencies are directly available. For example, you need to build a http web project, import web-http; if you need to call the database, import data-hikari. You have a lot of other options, as the modules provided, you can use them. For specific use, please refer to the documentation of each module. The demo make easier understand.   
If you don't like the module implementation, welcome to contribute a suitable module.

#### Architecture
![image](http://p8ilcqqyk.bkt.clouddn.com/design.png)

#### Dependence

Requires JDK1.8+ support.   
maven
```
<dependency>
    <groupId>com.tollge</groupId>
    <artifactId>tollge</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge:tollge:0.1.0'
```

#### Instruction manual

tollge provides the basic specification, because based on vertx, please understand the basics of vertx first, the currently implemented modules below:   

1. 全局的vertx对象
2. verticle服务发布
3. Biz group自动发现
4. annotation简化代码
5. 多级缓存
6. 全局参数的方案

持续优化中...

#### Implemented modules

|group|modules|
|-|-|
|auth 鉴权|auth-localstorge 本地存储的个性实现|
|data 数据源|data-hikari 基于hikari连接的个性实现|
|job 任务| 暂无|
|oss 对象存储|oss-qiniu 七牛对象存储|
|sms 短信|sms-dayu 大鱼短信|
|web 网站服务|web-http http服务|
|wechat 微信|wechat-gzh 公众号实现|

#### Participation contribution

Welcome everyone to provide a new module implementation.

