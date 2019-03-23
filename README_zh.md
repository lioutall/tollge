# tollge

#### 项目介绍
基于vertx实现的一组框架的规范, 需要使用module才能运行起来, 简单拼装一下能实现常用的功能.(项目在持续完善中...)   
借鉴springboot的配置可用思想; 你需要什么功能, 只需要引入对应的module依赖就直接可用. 比如你需要搭建一个http的web工程, 就依赖web-http; 如果你需要调用数据库, 就依赖data-hikari; 你有很多其他选择, 只要module里提供了, 你都可用. 具体的使用请参考各个module的说明文档. 当然, demo可能更直观一点.   
如果看不上module的实现, 我非常欢迎你贡献一个合适的module.

#### 软件架构
![image](https://github.com/lioutall/tollge/blob/master/de.png)

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge</groupId>
    <artifactId>tollge</artifactId>
    <version>0.1.1</version>
</dependency>
```
Gradle
```
compile 'com.tollge:tollge:0.1.1'
```

#### 使用说明

tollge提供基础规范, 由于基于vertx, 请先了解下vertx的基础知识, 目前实现的规范有:   

1. 全局的vertx对象   
你可以使用 MyVertx.vertx()获取vertx对象
2. 全局参数的方案   
第一步加载moudules/tollge.yml, 把所有配置加载成 `<String, String>`的map   
后加载覆盖先加载的, 最后加载用户project中的tollge.yml
3. verticle服务发布   
你可以在tollge.yml里添加`verticles.xxx: com.xxx.xxx.xxVerticle`来deploy verticle.   
你也可以定义需要deploy的实例个数. 比如, `verticles.xxx: com.xxx.xxx.xxVerticle,10` deploy 10个xxVerticle实例.   
Tollge 除了直接使用数字外,另外定义了两个关键字, `ALL` 和 `HALF`. `HALF`是CPU可用核心数的一半.
4. Biz group自动发现   
默认加载package为`com.tollge.modules.**`下的所有Biz   
可以通过在tollge.yml里添加`application.baseScan: com.xxx` 来加载`com.xxx`包下的所有Biz   
什么是Biz? 它就是vertx里一个provider. tollge它是这样写的:
```
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {
    /**
     * 测试
     */
    @Path("/one")
    @NotNull(key="key")
    public void one(Message<JsonObject> msg) {
        String key = msg.body().getString("key");
        msg.reply(key+" response");
    }

}
```
5. annotation简化代码   
目前提供两类注解   

|注解类型|注解名|作用|参数|
|-|-|-|-|
|校验|NotNull|空校验|key: 校验的关键字 msg:错误提示|
|校验|RegexValid|正则校验|key: 校验的关键字 regex:表达式 msg:错误提示|
|校验|LengthValid|长度校验,只能校验String和JsonArray|key: 校验的关键字 min:最小长度 max:最大长度 msg:错误提示|
|数据改变|InitIfNull|如果key为空,则初始化|key: 关键字 value:初始化字段|
|数据改变|ChangeType|改变数据类型|key: 关键字 from:从什么类型 to:变成什么类型|

6. 对数据层封装   
extends BizVerticle后, 你可以使用page, list, one, count等方法来大大简化代码.
7. 多级缓存   
引入了JetCache, 工具类CacheUtil做了初步包装. 建议自己定制一下, 原生的不好用.

持续优化中...

#### 已实现的模块

请参看[module说明](https://github.com/lioutall/tollge-modules)

#### 参与贡献

欢迎大家提供module新实现. 方便自己, 方便大家!

