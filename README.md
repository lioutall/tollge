# tollge

##### [中文版](https://github.com/lioutall/tollge/blob/master/README_zh.md)
#### Introduction
Based on the specification of a set of frameworks implemented by vertx, you need to use the modules to run it. Simply assemble it to implement common functions. (The project is in continuous improvement...)
Learn from the springboot configuration, what features you need, just need to import the corresponding module dependencies are directly available. For example, you need to build a http web project, import web-http; if you need to call the database, import data-hikari. You have a lot of other options, as the modules provided, you can use them. For specific use, please refer to the documentation of each module. The demo make easier understand.   
If you don't like the module implementation, welcome to contribute a suitable module.

#### Architecture
![image](https://github.com/lioutall/tollge/blob/master/de.png)

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

#### User Guide

tollge provides the basic specification, because based on vertx, please understand the basics of vertx first, the currently implemented modules below:   

1. Global vertx object   
Use `MyVertx.vertx()` to get `Vertx` object.
2. Global parameter   
The first step is to load all moudules/tollge.yml and the configuration will save in a map of `<String, String>`   
The same key will be overwritten, the tollge.yml in the user project will be loaded at last.
3. deploy verticle   
You can add `verticles.xxx: com.xxx.xxx.xxVerticle` in tollge.yml to deploy verticle.   
Define the number of instances that need to be deployed. For example, `verticles.xxxx: com.xxxx.xxxx.xxxVerticle, 10`deploy 10 xxxVerticle instances.   
In addition to using numbers directly, Tollge defines two keywords, `ALL'and `HALF'. `HALF' are half the number of CPU cores available.
4. Biz discovery   
Default to load all Biz under package `com.tollge.modules.**`   
You can load all Biz under the `com.xxx` package by adding `application.baseScan: com.xxx` to tollge.yml.   
What is Biz? It is a [provider in vertx](https://vertx.io/docs/vertx-core/java/#_deploying_verticles_programmatically). tollge it is written like this:
```
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {
    /**
     * test
     */
    @Path("/one")
    @NotNull(key="key")
    public void one(Message<JsonObject> msg) {
        String key = msg.body().getString("key");
        msg.reply(key+" response");
    }

}
```
5. Annotation simplified code   
Currently providing types of annotations below   

|annotation type|name|function|parameters|
|-|-|-|-|
|check|NotNull|check null|key: key to check. msg:message to show|
|check|RegexValid|regex check|key: key to check. regex:Regular expression. msg:message to show|
|check|LengthValid|length check for String and JsonArray|key: key to check. min:xx max:xx msg:message to show|
|change content|InitIfNull|Initialize if the key is empty|key: key to check. value:Initialize value|
|change type|ChangeType|Change data type|key: key to check. from:from type. to:to type|

6. Data layer call simplification   
extends BizVerticle, You can greatly simplify the code by using methods such as page, list, one, count, etc..
7. Multi-level cache   
Import [JetCache](https://github.com/alibaba/jetcache), The tool class CacheUtil has been initially packaged. It is recommended to customize it yourself, the original is not easy to use..


#### Implemented modules

See [modules](https://github.com/lioutall/tollge-modules)

#### Participation contribution

Welcome everyone to provide a new module implementation.

