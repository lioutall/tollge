# tollge

##### [中文版](https://github.com/lioutall/tollge/blob/master/README_zh.md)
#### Introduction
Tollge is a microservice development framework based on Vert.x, providing a set of standardized specifications and tools for building high-performance, asynchronous applications. The framework follows a modular design philosophy, allowing developers to selectively import required modules to implement specific functionalities. (The project is continuously improving...)

Inspired by Spring Boot's configuration approach, Tollge enables you to directly use features by simply importing the corresponding module dependencies. For example, to build an HTTP web project, import web-http; if you need database access, import data-hikari. You have many other options as modules are provided, and you can use them as needed. For specific usage, please refer to the documentation of each module. The demo makes it easier to understand.

#### Core Features
- **Event-driven Architecture**: Built on Vert.x, providing high-performance asynchronous processing capabilities
- **Modular Design**: Supports on-demand module import, reducing unnecessary dependencies
- **Annotation-based Development**: Simplifies development through rich annotations
- **Unified Data Access**: Provides abstract data access layer, supporting multiple databases
- **Automatic Service Discovery**: Automatically discovers and registers services through annotations
- **Flexible Configuration**: Supports YAML configuration files with hierarchical management
- **Built-in Validation**: Provides comprehensive parameter validation and data conversion capabilities
- **User Authentication**: Includes user authentication and permission management

#### Architecture
![image](https://github.com/lioutall/tollge/blob/master/de.png)

The framework architecture consists of the following core components:
- **Core Layer**: Provides basic Vert.x integration and global configuration management
- **Annotation Layer**: Provides various annotations for service discovery, parameter validation, and data conversion
- **Service Layer**: Implements business logic processing through BizVerticle
- **Data Access Layer**: Provides unified database access through AbstractDao
- **Web Layer**: Provides HTTP request processing through AbstractRouter
- **Module Layer**: Extensible module system supporting various functional requirements

#### Dependence

Requires Java 21+ support.
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

#### Core Dependencies
- Vert.x 4.5.9: Core framework providing event-driven, non-blocking I/O
- Logback 1.5.6: Logging framework
- Reflections 0.10.2: Runtime reflection library for class scanning
- ReflectASM 1.11.9: High-performance Java reflection library
- FastJSON2 2.0.52: High-performance JSON processor
- SnakeYAML 2.2: YAML processor for configuration files
- Lombok 1.18.34: Java annotation library for reducing boilerplate code
- Guava 33.2.1: Google's core Java libraries
- Jackson 2.16.1: JSON processor

#### User Guide

Tollge provides basic specifications and tools for building applications. Since it's based on Vert.x, please understand the basics of Vert.x first. The following are the currently implemented features:

##### 1. Global Vertx Object
Use `MyVertx.vertx()` to get the global `Vertx` object. This is a singleton instance that can be accessed anywhere in your application.

```java
// Get the global Vertx instance
Vertx vertx = MyVertx.vertx();

// Use Vertx for various operations
vertx.deployVerticle(new MyVerticle());
```

##### 2. Global Configuration
The framework loads all `modules/tollge.yml` files and the project's `tollge.yml` file, merging them into a `<String, String>` map. Later loaded configurations will override earlier ones, with the project's `tollge.yml` being loaded last.

Configuration loading order:
1. Load `tollge.yml` from all modules
2. Load `tollge.yml` from the user project (overrides module configurations)

Example configuration:
```yaml
# tollge.yml
application:
  baseScan: com.yourcompany # Base package for scanning

verticles:
  myVerticle: com.yourcompany.verticle.MyVerticle, 10 # Deploy 10 instances
  anotherVerticle: com.yourcompany.verticle.AnotherVerticle, HALF # Deploy half CPU core instances
```

##### 3. Verticle Deployment
You can add `verticles.xxx: com.xxx.xxx.xxVerticle` in `tollge.yml` to deploy verticles. Define the number of instances to be deployed. For example, `verticles.xxxx: com.xxxx.xxxx.xxxVerticle, 10` deploys 10 xxxVerticle instances.

In addition to using numbers directly, Tollge defines two keywords:
- `ALL`: Deploys instances equal to the number of available CPU cores
- `HALF`: Deploys instances equal to half the number of available CPU cores

Example:
```yaml
verticles:
  httpVerticle: com.yourcompany.HttpVerticle, ALL # Deploy instances equal to CPU cores
  workerVerticle: com.yourcompany.WorkerVerticle, HALF # Deploy half CPU core instances
  singleVerticle: com.yourcompany.SingleVerticle # Deploy 1 instance
```

##### 4. Biz Discovery
By default, all Biz classes under the package `com.tollge.modules.**` are loaded. You can load all Biz classes under the `com.xxx` package by adding `application.baseScan: com.xxx` to `tollge.yml`.

What is Biz? It is a provider in Vert.x that handles business logic. In Tollge, it's written like this:

```java
@Biz("biz://user")
public class UserBiz extends BizVerticle {
    /**
     * Get user by ID
     */
    @Path("/getById")
    @NotNull(key="userId")
    public void getById(Message<JsonObject> msg) {
        String userId = msg.body().getString("userId");
        // Business logic here
        JsonObject user = new JsonObject().put("id", userId).put("name", "John Doe");
        msg.reply(user);
    }

    /**
     * Create user
     */
    @Path("/create")
    @NotNull(key="name")
    @LengthValid(key="name", min=2, max=50, msg="Name length must be between 2 and 50")
    public void create(Message<JsonObject> msg) {
        JsonObject user = msg.body();
        // Business logic to create user
        msg.reply(new JsonObject().put("success", true).put("id", "new-user-id"));
    }
}
```

##### 5. Annotation-based Simplification
Tollge provides various annotations to simplify code development:

|Annotation Type|Name|Function|Parameters|
|-|-|-|-|
|Validation|NotNull|Null check|key: key to check. msg: error message|
|Validation|RegexValid|Regex validation|key: key to check. regex: regular expression. msg: error message|
|Validation|LengthValid|Length validation for String and JsonArray|key: key to check. min: minimum length. max: maximum length. msg: error message|
|Data Modification|InitIfNull|Initialize if the key is empty|key: key to check. value: initialization value|
|Data Conversion|ChangeType|Change data type|key: key to check. from: source type. to: target type|

Example usage:
```java
@Path("/update")
@NotNull(key="userId", msg="User ID cannot be null")
@RegexValid(key="email", regex="^[A-Za-z0-9+_.-]+@(.+)$", msg="Invalid email format")
@LengthValid(key="username", min=3, max=20, msg="Username length must be between 3 and 20")
@InitIfNull(key="status", value="active")
@ChangeType(key="age", from=Type.STRING, to=Type.INTEGER)
public void update(Message<JsonObject> msg) {
    JsonObject user = msg.body();
    // Business logic here
    msg.reply(new JsonObject().put("success", true));
}
```

##### 6. Data Access Layer Simplification
By extending `BizVerticle`, you can greatly simplify database operations using methods like `page`, `list`, `one`, `count`, etc.

```java
@Biz("biz://product")
public class ProductBiz extends BizVerticle {
    /**
     * Get product by ID
     */
    @Path("/getById")
    @NotNull(key="id")
    public void getById(Message<JsonObject> msg) {
        String id = msg.body().getString("id");
        SqlAndParams sqlAndParams = new SqlAndParams("product.getById").putParam("id", id);
        one(msg, sqlAndParams, Product.class);
    }

    /**
     * Get product list with pagination
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
     * Get products with pagination
     */
    @Path("/page")
    public void page(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.page")
            .putParam("name", params.getString("name"))
            .putParam("category", params.getString("category"));
        // The pageNum and pageSize will be automatically extracted from the message body
        list(msg, sqlAndParams, Product.class);
    }

    /**
     * Count products
     */
    @Path("/count")
    public void count(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("product.count")
            .putParam("category", params.getString("category"));
        count(msg, sqlAndParams);
    }

    /**
     * Create product
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
     * Transaction example
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
                msg.fail(500, "Transfer failed: " + res.cause().getMessage());
            }
        });
    }
}
```

##### 7. HTTP Request Handling
Tollge provides `AbstractRouter` for handling HTTP requests with annotations:

```java
public class UserRouter extends AbstractRouter {
    /**
     * Get user by ID
     */
    @Path("/api/user/:id")
    @Method(Method.GET)
    public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.getById", new JsonObject().put("userId", userId));
    }

    /**
     * Create user
     */
    @Path("/api/user")
    @Method(Method.POST)
    public Future<JsonObject> createUser(RoutingContext ctx, @Body User user) {
        return sendBiz("biz://user.create", JsonObject.mapFrom(user));
    }

    /**
     * Update user
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
     * Get user list with pagination
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

##### 8. Multi-level Cache
Import [JetCache](https://github.com/alibaba/jetcache), the utility class CacheUtil has been initially packaged. It is recommended to customize it yourself, as the original implementation might not be easy to use.

```java
// Example of using cache
@Biz("biz://cachedUser")
public class CachedUserBiz extends BizVerticle {
    private Cache<String, JsonObject> userCache;
    
    @Override
    public void start() {
        // Initialize cache
        userCache = CacheUtil.createCache("userCache", 1000, Duration.ofMinutes(30));
    }
    
    @Path("/getCachedUser")
    @NotNull(key="userId")
    public void getCachedUser(Message<JsonObject> msg) {
        String userId = msg.body().getString("userId");
        
        // Try to get from cache first
        JsonObject cachedUser = userCache.get(userId);
        if (cachedUser != null) {
            msg.reply(cachedUser);
            return;
        }
        
        // If not in cache, get from database
        SqlAndParams sqlAndParams = new SqlAndParams("user.getById").putParam("id", userId);
        one(msg, sqlAndParams, res -> {
            if (res.succeeded()) {
                JsonObject user = res.result().body();
                // Put into cache
                userCache.put(userId, user);
                msg.reply(user);
            } else {
                msg.fail(500, "Failed to get user: " + res.cause().getMessage());
            }
        });
    }
}
```


#### Core Function Modules

##### 1. Annotation System
Tollge provides a comprehensive annotation system for parameter validation, data conversion, and service discovery.

###### Validation Annotations
- `@NotNull`: Validates that a parameter is not null
- `@NotNulls`: Multiple @NotNull annotations
- `@LengthValid`: Validates the length of String or JsonArray parameters
- `@LengthValids`: Multiple @LengthValid annotations
- `@RegexValid`: Validates parameters using regular expressions
- `@RegexValids`: Multiple @RegexValid annotations

###### Data Conversion Annotations
- `@ChangeType`: Converts data between types (String, Integer, Double)
- `@ChangeTypes`: Multiple @ChangeType annotations
- `@InitIfNull`: Initializes a parameter if it's null
- `@InitIfNulls`: Multiple @InitIfNull annotations

###### Service Discovery Annotations
- `@Biz`: Marks a class as a business service provider
- `@Path`: Defines a service endpoint with HTTP method and content type

###### HTTP Request Annotations
- `@QueryParam`: Binds a query parameter to a method parameter
- `@PathParam`: Binds a path parameter to a method parameter
- `@Body`: Binds the request body to a method parameter
- `@HeaderParam`: Binds a header parameter to a method parameter
- `@FormParam`: Binds a form parameter to a method parameter
- `@CookieParam`: Binds a cookie parameter to a method parameter

##### 2. Data Access Layer
The data access layer provides a unified interface for database operations through `AbstractDao` and `SqlAndParams`.

###### AbstractDao
Abstract base class for database operations, providing the following standard operations:
- `COUNT`: Returns a numerical value (count query)
- `OPERATE`: Returns JsonArray or operation result count
- `BATCH`: Batch operations, returns operation count
- `ONE`: Returns a single JsonObject
- `LIST`: Returns a JsonArray
- `PAGE`: Returns paginated results
- `TRANSACTION`: Executes operations in a transaction

###### SqlAndParams
A class that encapsulates SQL statements and parameters, supporting:
- SQL key identification
- Parameter binding
- Batch parameter support
- Pagination parameters (limit and offset)

Example:
```java
// Create SqlAndParams with SQL key
SqlAndParams sqlAndParams = new SqlAndParams("user.getById")
    .putParam("id", userId)
    .putParam("status", "active");

// Create SqlAndParams with pagination
SqlAndParams pageSql = new SqlAndParams("user.list", 1, 10)
    .putParam("name", "John%")
    .putParam("department", "IT");
```

##### 3. Pagination Support
Tollge provides comprehensive pagination support through `Page` and `PageRequest` classes.

###### Page
Represents a paginated result set with the following properties:
- `pageNum`: Current page number (1-based)
- `pageSize`: Number of items per page
- `startRow`: Starting row index
- `endRow`: Ending row index
- `total`: Total number of items
- `pages`: Total number of pages
- `result`: List of items in the current page

###### PageRequest
Represents a pagination request with the following properties:
- `pageNum`: Current page number (1-based)
- `pageSize`: Number of items per page (default: 10)

###### Example
```java
// In BizVerticle
@Path("/page")
public void page(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    SqlAndParams sqlAndParams = new SqlAndParams("product.page")
        .putParam("name", params.getString("name"));
    
    // The pageNum and pageSize will be automatically extracted from the message body
    list(msg, sqlAndParams, Product.class);
}

// In AbstractRouter
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

##### 4. User Authentication
Tollge provides user authentication and authorization support through the `LoginUser` class.

###### LoginUser
Represents an authenticated user with the following properties:
- `userId`: User ID
- `nickname`: User nickname
- `avatar`: User avatar URL
- `realname`: User real name
- `mobile`: User mobile number
- `loginTime`: Last login time
- `roleIdList`: List of role IDs
- `paramJson`: Additional user parameters

###### Example
```java
// In BizVerticle
@Path("/updateProfile")
public void updateProfile(Message<JsonObject> msg) {
    LoginUser loginUser = msg.headers().get("loginUser");
    if (loginUser == null) {
        msg.fail(401, "Unauthorized");
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

// In AbstractRouter
@Path("/api/profile")
@Method(Method.PUT)
public Future<JsonObject> updateProfile(RoutingContext ctx, @Body ProfileRequest request) {
    LoginUser loginUser = ctx.get("loginUser");
    if (loginUser == null) {
        return Future.failedFuture("Unauthorized");
    }
    
    JsonObject params = JsonObject.mapFrom(request);
    params.put("userId", loginUser.getUserId());
    
    return sendBizWithUser(loginUser, "biz://user.updateProfile", params);
}
```

##### 5. Custom Map Implementation
Tollge provides a custom `TollgeMap` implementation that extends `LinkedHashMap` with special features.

###### Features
- Automatic conversion of List<Map> to List<TollgeMap>
- Custom JSON serialization
- Special handling for nested structures

###### Example
```java
// Create TollgeMap
TollgeMap<String, Object> map = new TollgeMap<>();
map.put("name", "John");
map.put("age", 30);
map.put("addresses", Arrays.asList(
    new TollgeMap<String, Object>()
        .put("type", "home")
        .put("city", "New York"),
    new TollgeMap<String, Object>()
        .put("type", "work")
        .put("city", "Boston")
));

// Automatic JSON serialization
String json = map.toString(); // Custom JSON format

// Automatic conversion when retrieving
List<TollgeMap<String, Object>> addresses = map.get("addresses");
```

##### 6. Status Code Management
Tollge provides a comprehensive status code management system through the `StatusCodeMsg` enum.

###### Features
- Standard HTTP status codes
- Custom business status codes
- User authentication and authorization status codes
- Parameter validation status codes
- Server error status codes

###### Example
```java
// In BizVerticle
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

// In AbstractRouter
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

#### Implemented modules

See [modules](https://github.com/lioutall/tollge-modules)

#### Participation contribution

Welcome everyone to provide a new module implementation. For contribution guidelines, please refer to the project's code of conduct and submit pull requests following the established patterns.

