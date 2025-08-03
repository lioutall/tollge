# tollge

#### 项目介绍
Tollge是一个基于Vert.x的微服务开发框架，提供了一套标准化的规范和工具，用于构建高性能、异步的应用程序。该框架遵循模块化设计理念，允许开发者按需导入所需模块来实现特定功能。（项目正在持续完善中...）

借鉴Spring Boot的配置方法，Tollge使您只需导入相应的模块依赖即可直接使用所需功能。例如，要构建HTTP Web项目，导入web-http；如果需要数据库访问，导入data-hikari；您还有许多其他选择，因为提供了各种模块，您可以根据需要使用它们。具体使用方法请参考各模块的文档。Demo使理解更加直观。

如果您不满意模块的实现，我们非常欢迎您贡献一个合适的模块。

#### 核心特性
- **事件驱动架构**：基于Vert.x构建，提供高性能的异步处理能力
- **模块化设计**：支持按需模块导入，减少不必要的依赖
- **基于注解的开发**：通过丰富的注解简化开发
- **统一数据访问**：提供抽象数据访问层，支持多种数据库
- **自动服务发现**：通过注解自动发现和注册服务
- **灵活配置**：支持YAML配置文件的分层管理
- **内置验证**：提供全面的参数验证和数据转换功能
- **用户认证**：包括用户认证和权限管理

#### 软件架构
![image](https://github.com/lioutall/tollge/blob/master/de.png)

框架架构由以下核心组件组成：
- **核心层**：提供基本的Vert.x集成和全局配置管理
- **注解层**：提供各种注解用于服务发现、参数验证和数据转换
- **服务层**：通过BizVerticle实现业务逻辑处理
- **数据访问层**：通过AbstractDao提供统一的数据库访问
- **Web层**：通过AbstractRouter提供HTTP请求处理
- **模块层**：可扩展的模块系统，支持各种功能需求

#### 依赖

需要Java 21及以上版本支持。
maven
```
<dependency>
    <groupId>com.tollge</groupId>
    <artifactId>tollge</artifactId>
    <version>0.10.1</version>
</dependency>
```
Gradle
```
compile 'com.tollge:tollge:0.10.1'
```

#### 核心依赖
- Vert.x 4.5.9：核心框架，提供事件驱动、非阻塞I/O
- Logback 1.5.6：日志框架
- Reflections 0.10.2：运行时反射库，用于类扫描
- ReflectASM 1.11.9：高性能Java反射库
- FastJSON2 2.0.52：高性能JSON处理器
- SnakeYAML 2.2：YAML处理器，用于配置文件
- Lombok 1.18.34：Java注解库，减少样板代码
- Guava 33.2.1：Google核心Java库
- Jackson 2.16.1：JSON处理器

#### 使用说明

Tollge提供了基础规范和工具来构建应用程序。由于它基于Vert.x，请先了解Vert.x的基础知识。以下是当前实现的功能：

##### 1. 全局Vertx对象
使用`MyVertx.vertx()`获取全局`Vertx`对象。这是一个单例实例，可以在应用程序的任何地方访问。

```java
// 获取全局Vertx实例
Vertx vertx = MyVertx.vertx();

// 使用Vertx进行各种操作
vertx.deployVerticle(new MyVerticle());
```

##### 2. 全局配置
框架会加载所有`modules/tollge.yml`文件和项目的`tollge.yml`文件，将它们合并成一个`<String, String>`的map。后加载的配置会覆盖先加载的，项目的`tollge.yml`最后加载。

配置加载顺序：
1. 从所有模块加载`tollge.yml`
2. 从用户项目加载`tollge.yml`（覆盖模块配置）

示例配置：
```yaml
# tollge.yml
application:
  baseScan: com.yourcompany # 扫描的基础包

verticles:
  myVerticle: com.yourcompany.verticle.MyVerticle, 10 # 部署10个实例
  anotherVerticle: com.yourcompany.verticle.AnotherVerticle, HALF # 部署一半CPU核心数的实例
```

##### 3. Verticle部署
你可以在`tollge.yml`中添加`verticles.xxx: com.xxx.xxx.xxVerticle`来部署verticle。定义需要部署的实例数量。例如，`verticles.xxxx: com.xxxx.xxxx.xxxVerticle, 10`部署10个xxxVerticle实例。

除了直接使用数字外，Tollge还定义了两个关键字：
- `ALL`：部署实例数等于可用CPU核心数
- `HALF`：部署实例数等于可用CPU核心数的一半

示例：
```yaml
verticles:
  httpVerticle: com.yourcompany.HttpVerticle, ALL # 部署等于CPU核心数的实例
  workerVerticle: com.yourcompany.WorkerVerticle, HALF # 部署一半CPU核心数的实例
  singleVerticle: com.yourcompany.SingleVerticle # 部署1个实例
```

##### 4. Biz自动发现
默认情况下，会加载包`com.tollge.modules.**`下的所有Biz类。你可以通过在`tollge.yml`中添加`application.baseScan: com.xxx`来加载`com.xxx`包下的所有Biz类。

什么是Biz？它是Vert.x中处理业务逻辑的provider。在Tollge中，它是这样写的：

```java
@Biz("biz://user")
public class UserBiz extends BizVerticle {
    /**
     * 根据ID获取用户
     */
    @Path("/getById")
    @NotNull(key="userId")
    public void getById(Message<JsonObject> msg) {
        String userId = msg.body().getString("userId");
        // 业务逻辑
        JsonObject user = new JsonObject().put("id", userId).put("name", "张三");
        msg.reply(user);
    }

    /**
     * 创建用户
     */
    @Path("/create")
    @NotNull(key="name")
    @LengthValid(key="name", min=2, max=50, msg="姓名长度必须在2到50之间")
    public void create(Message<JsonObject> msg) {
        JsonObject user = msg.body();
        // 创建用户的业务逻辑
        msg.reply(new JsonObject().put("success", true).put("id", "new-user-id"));
    }
}
```

##### 5. 基于注解的简化
Tollge提供了各种注解来简化代码开发：

|注解类型|注解名|作用|参数|
|-|-|-|-|
|验证|NotNull|空值检查|key: 要检查的键。msg: 错误信息|
|验证|RegexValid|正则表达式验证|key: 要检查的键。regex: 正则表达式。msg: 错误信息|
|验证|LengthValid|String和JsonArray的长度验证|key: 要检查的键。min: 最小长度。max: 最大长度。msg: 错误信息|
|数据修改|InitIfNull|如果键为空则初始化|key: 要检查的键。value: 初始化值|
|数据转换|ChangeType|更改数据类型|key: 要检查的键。from: 源类型。to: 目标类型|

使用示例：
```java
@Path("/update")
@NotNull(key="userId", msg="用户ID不能为空")
@RegexValid(key="email", regex="^[A-Za-z0-9+_.-]+@(.+)$", msg="邮箱格式无效")
@LengthValid(key="username", min=3, max=20, msg="用户名长度必须在3到20之间")
@InitIfNull(key="status", value="active")
@ChangeType(key="age", from=Type.STRING, to=Type.INTEGER)
public void update(Message<JsonObject> msg) {
    JsonObject user = msg.body();
    // 业务逻辑
    msg.reply(new JsonObject().put("success", true));
}
```

##### 6. 数据访问层简化
通过扩展`BizVerticle`，你可以使用`page`、`list`、`one`、`count`等方法大大简化数据库操作。

```java
@Biz("biz://product")
public class ProductBiz extends BizVerticle {
    /**
     * 根据ID获取产品
     */
    @Path("/getById")
    @NotNull(key="id")
    public void getById(Message<JsonObject> msg) {
        String id = msg.body().getString("id");
        SqlAndParams sqlAndParams = new SqlAndParams("product.getById").putParam("id", id);
        one(msg, sqlAndParams, Product.class);
    }

    /**
     * 获取产品列表
     */
    @Path("/list")
    public void list(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.list")
            .putParam("name", params.getString("name"))
            .putParam("category", params.getString("category"));
        list(msg, sqlAndParams, Product.class);
    }

    /**
     * 获取分页产品
     */
    @Path("/page")
    public void page(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.page")
            .putParam("name", params.getString("name"))
            .putParam("category", params.getString("category"));
        // pageNum和pageSize将自动从消息体中提取
        list(msg, sqlAndParams, Product.class);
    }

    /**
     * 统计产品数量
     */
    @Path("/count")
    public void count(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.count")
            .putParam("category", params.getString("category"));
        count(msg, sqlAndParams);
    }

    /**
     * 创建产品
     */
    @Path("/create")
    @NotNull(key="name")
    @NotNull(key="price")
    public void create(Message<JsonObject> msg) {
        JsonObject product = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.create")
            .putParam("name", product.getString("name"))
            .putParam("price", product.getDouble("price"));
        operate(msg, sqlAndParams);
    }

    /**
     * 事务示例
     */
    @Path("/transfer")
    public void transfer(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        String fromAccountId = params.getString("fromAccountId");
        String toAccountId = params.getString("toAccountId");
        Double amount = params.getDouble("amount");
        
        List<SqlAndParams> sqlList = new ArrayList<>();
        sqlList.add(new SqlAndParams("account.subtract")
            .putParam("accountId", fromAccountId)
            .putParam("amount", amount));
        sqlList.add(new SqlAndParams("account.add")
            .putParam("accountId", toAccountId)
            .putParam("amount", amount));
            
        transaction(sqlList, res -> {
            if (res.succeeded()) {
                msg.reply(new JsonObject().put("success", true));
            } else {
                msg.fail(500, "转账失败: " + res.cause().getMessage());
            }
        });
    }
}
```

##### 7. HTTP请求处理
Tollge提供了`AbstractRouter`用于通过注解处理HTTP请求：

```java
public class UserRouter extends AbstractRouter {
    /**
     * 根据ID获取用户
     */
    @Path("/api/user/:id")
    @Method(Method.GET)
    public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.getById", new JsonObject().put("userId", userId));
    }

    /**
     * 创建用户
     */
    @Path("/api/user")
    @Method(Method.POST)
    public Future<JsonObject> createUser(RoutingContext ctx, @Body User user) {
        return sendBiz("biz://user.create", JsonObject.mapFrom(user));
    }

    /**
     * 更新用户
     */
    @Path("/api/user")
    @Method(Method.PUT)
    public Future<JsonObject> updateUser(RoutingContext ctx, @QueryParam("id") String userId,
                                          @QueryParam("name") String name,
                                          @QueryParam("email") String email) {
        JsonObject params = new JsonObject()
            .put("userId", userId)
            .put("name", name)
            .put("email", email);
        return sendBiz("biz://user.update", params);
    }

    /**
     * 获取用户列表（分页）
     */
    @Path("/api/users")
    @Method(Method.GET)
    public Future<JsonObject> getUserList(RoutingContext ctx,
                                          @QueryParam("pageNum") Integer pageNum,
                                          @QueryParam("pageSize") Integer pageSize,
                                          @QueryParam("name") String name) {
        JsonObject params = new JsonObject()
            .put("pageNum", pageNum)
            .put("pageSize", pageSize)
            .put("name", name);
        return sendBiz("biz://user.list", params);
    }
}
```

##### 8. 多级缓存
引入了[JetCache](https://github.com/alibaba/jetcache)，工具类CacheUtil已经做了初步封装。建议自己定制一下，原生的可能不好用。

```java
// 使用缓存的示例
@Biz("biz://cachedUser")
public class CachedUserBiz extends BizVerticle {
    private Cache<String, JsonObject> userCache;
    
    @Override
    public void start() {
        // 初始化缓存
        userCache = CacheUtil.createCache("userCache", 1000, Duration.ofMinutes(30));
    }
    
    @Path("/getCachedUser")
    @NotNull(key="userId")
    public void getCachedUser(Message<JsonObject> msg) {
        String userId = msg.body().getString("userId");
        
        // 先尝试从缓存获取
        JsonObject cachedUser = userCache.get(userId);
        if (cachedUser != null) {
            msg.reply(cachedUser);
            return;
        }
        
        // 如果缓存中没有，从数据库获取
        SqlAndParams sqlAndParams = new SqlAndParams("user.getById").putParam("id", userId);
        one(msg, sqlAndParams, res -> {
            if (res.succeeded()) {
                JsonObject user = res.result().body();
                // 放入缓存
                userCache.put(userId, user);
                msg.reply(user);
            } else {
                msg.fail(500, "获取用户失败: " + res.cause().getMessage());
            }
        });
    }
}
```

#### 核心功能模块

##### 1. 注解系统
Tollge提供了全面的注解系统，用于参数验证、数据转换和服务发现。

###### 验证注解
- `@NotNull`：验证参数不为空
- `@NotNulls`：多个@NotNull注解
- `@LengthValid`：验证String或JsonArray参数的长度
- `@LengthValids`：多个@LengthValid注解
- `@RegexValid`：使用正则表达式验证参数
- `@RegexValids`：多个@RegexValid注解

###### 数据转换注解
- `@ChangeType`：在类型之间转换数据（String、Integer、Double）
- `@ChangeTypes`：多个@ChangeType注解
- `@InitIfNull`：如果参数为空则初始化
- `@InitIfNulls`：多个@InitIfNull注解

###### 服务发现注解
- `@Biz`：将类标记为业务服务提供者
- `@Path`：定义带有HTTP方法和内容类型的服务端点

###### HTTP请求注解
- `@QueryParam`：将查询参数绑定到方法参数
- `@PathParam`：将路径参数绑定到方法参数
- `@Body`：将请求体绑定到方法参数
- `@HeaderParam`：将头参数绑定到方法参数
- `@FormParam`：将表单参数绑定到方法参数
- `@CookieParam`：将Cookie参数绑定到方法参数

##### 2. 数据访问层
数据访问层通过`AbstractDao`和`SqlAndParams`为数据库操作提供了统一的接口。

###### AbstractDao
数据库操作的抽象基类，提供以下标准操作：
- `COUNT`：返回数值（计数查询）
- `OPERATE`：返回JsonArray或操作结果计数
- `BATCH`：批量操作，返回操作计数
- `ONE`：返回单个JsonObject
- `LIST`：返回JsonArray
- `PAGE`：返回分页结果
- `TRANSACTION`：在事务中执行操作

###### SqlAndParams
封装SQL语句和参数的类，支持：
- SQL键标识
- 参数绑定
- 批量参数支持
- 分页参数（limit和offset）

示例：
```java
// 创建带有SQL键的SqlAndParams
SqlAndParams sqlAndParams = new SqlAndParams("user.getById")
    .putParam("id", userId)
    .putParam("status", "active");

// 创建带有分页的SqlAndParams
SqlAndParams pageSql = new SqlAndParams("user.list", 1, 10)
    .putParam("name", "张三%")
    .putParam("department", "IT");
```

##### 3. 分页支持
Tollge通过`Page`和`PageRequest`类提供了全面的分页支持。

###### Page
表示分页结果集，具有以下属性：
- `pageNum`：当前页码（从1开始）
- `pageSize`：每页项目数
- `startRow`：起始行索引
- `endRow`：结束行索引
- `total`：总项目数
- `pages`：总页数
- `result`：当前页中的项目列表

###### PageRequest
表示分页请求，具有以下属性：
- `pageNum`：当前页码（从1开始）
- `pageSize`：每页项目数（默认：10）

###### 示例
```java
// 在BizVerticle中
@Path("/page")
public void page(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    SqlAndParams sqlAndParams = new SqlAndParams("product.page")
        .putParam("name", params.getString("name"));
    
    // pageNum和pageSize将自动从消息体中提取
    list(msg, sqlAndParams, Product.class);
}

// 在AbstractRouter中
@Path("/api/products")
@Method(Method.GET)
public Future<JsonObject> getProducts(RoutingContext ctx,
                                      @QueryParam("page") Integer page,
                                      @QueryParam("size") Integer size) {
    PageRequest request = new PageRequest();
    request.setPageNum(page != null ? page : 1);
    request.setPageSize(size != null ? size : 10);
    
    return sendBiz("biz://product.page", request);
}
```

##### 4. 用户认证
Tollge通过`LoginUser`类提供用户认证和授权支持。

###### LoginUser
表示已认证的用户，具有以下属性：
- `userId`：用户ID
- `nickname`：用户昵称
- `avatar`：用户头像URL
- `realname`：用户真实姓名
- `mobile`：用户手机号
- `loginTime`：最后登录时间
- `roleIdList`：角色ID列表
- `paramJson`：附加用户参数

###### 示例
```java
// 在BizVerticle中
@Path("/updateProfile")
public void updateProfile(Message<JsonObject> msg) {
    LoginUser loginUser = msg.headers().get("loginUser");
    if (loginUser == null) {
        msg.fail(401, "未授权");
        return;
    }
    
    JsonObject profile = msg.body();
    profile.put("userId", loginUser.getUserId());
    
    SqlAndParams sqlAndParams = new SqlAndParams("user.updateProfile")
        .putParam("userId", loginUser.getUserId())
        .putParam("nickname", profile.getString("nickname"))
        .putParam("avatar", profile.getString("avatar"));
    
    operate(msg, sqlAndParams);
}

// 在AbstractRouter中
@Path("/api/profile")
@Method(Method.PUT)
public Future<JsonObject> updateProfile(RoutingContext ctx, @Body ProfileRequest request) {
    LoginUser loginUser = ctx.get("loginUser");
    if (loginUser == null) {
        return Future.failedFuture("未授权");
    }
    
    JsonObject params = JsonObject.mapFrom(request);
    params.put("userId", loginUser.getUserId());
    
    return sendBizWithUser(loginUser, "biz://user.updateProfile", params);
}
```

##### 5. 自定义Map实现
Tollge提供了自定义的`TollgeMap`实现，它扩展了`LinkedHashMap`并具有特殊功能。

###### 功能
- List<Map>到List<TollgeMap>的自动转换
- 自定义JSON序列化
- 嵌套结构的特殊处理

###### 示例
```java
// 创建TollgeMap
TollgeMap<String, Object> map = new TollgeMap<>();
map.put("name", "张三");
map.put("age", 30);
map.put("addresses", Arrays.asList(
    new TollgeMap<String, Object>()
        .put("type", "home")
        .put("city", "北京"),
    new TollgeMap<String, Object>()
        .put("type", "work")
        .put("city", "上海")
));

// 自动JSON序列化
String json = map.toString(); // 自定义JSON格式

// 检索时自动转换
List<TollgeMap<String, Object>> addresses = map.get("addresses");
```

##### 6. 状态码管理
Tollge通过`StatusCodeMsg`枚举提供了全面的状态码管理系统。

###### 功能
- 标准HTTP状态码
- 自定义业务状态码
- 用户认证和授权状态码
- 参数验证状态码
- 服务器错误状态码

###### 示例
```java
// 在BizVerticle中
@Path("/delete")
public void delete(Message<JsonObject> msg) {
    String id = msg.body().getString("id");
    if (id == null) {
        msg.fail(StatusCodeMsg.C412.getCode(), StatusCodeMsg.C412.getMsg());
        return;
    }
    
    SqlAndParams sqlAndParams = new SqlAndParams("user.delete").putParam("id", id);
    operate(msg, sqlAndParams);
}

// 在AbstractRouter中
@Path("/api/users/:id")
@Method(Method.DELETE)
public Future<JsonObject> deleteUser(RoutingContext ctx, @PathParam("id") String id) {
    if (id == null || id.isEmpty()) {
        return Future.failedFuture(new TollgeException(StatusCodeMsg.C412.getMsg()));
    }
    
    return sendBiz("biz://user.delete", new JsonObject().put("id", id))
        .recover(error -> Future.failedFuture(
            new TollgeException(StatusCodeMsg.C501.getCode(), error.getMessage())));
}
```

#### 已实现的模块

请参看[module说明](https://github.com/lioutall/tollge-modules)

#### 参与贡献

欢迎大家提供module新实现。有关贡献指南，请参考项目的行为准则并按照既定模式提交拉取请求。方便自己，方便大家！

