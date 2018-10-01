package io.tollge.common.verticle;

import io.tollge.common.UFailureHandler;
import io.tollge.common.annotation.mark.Router;
import io.tollge.common.util.Properties;
import io.tollge.common.util.ReflectionUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RouterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);

        // 过滤器初始化
        Map<String, Object> filters = Properties.getGroup("filters");
        filters.entrySet().forEach(c->{
            try {
                router.route().handler((Handler<RoutingContext>)Class.forName((String)c.getValue()).getMethod("create").invoke(null));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                log.error("加载过滤器[{}]失败", c.getValue(), e);
            }
        });

        // routers初始化
        Set<Class<?>> set = ReflectionUtil.getClassesWithAnnotated(Router.class);
        for (Class<?> c : set) {
            Router mark = c.getAnnotation(Router.class);
            try {
                AbstractRouter abstractRouter = (AbstractRouter)c.newInstance();
                abstractRouter.getMap().forEach((pathMark, routingContextConsumer) -> {
                    String path = mark.value().concat(pathMark.value());
                    String contextPath = Properties.getString("application","context.path", "") + path;
                    Route r = null;
                    switch (pathMark.method()) {
                        case ROUTE: r = router.route(contextPath);break;
                        case GET: r = router.get(contextPath);break;
                        case POST: r = router.post(contextPath);break;
                        case PUT: r = router.put(contextPath);break;
                        case DELETE: r = router.delete(contextPath);break;
                        case TRACE: r = router.trace(contextPath);break;
                        default: r = router.route(contextPath);break;
                    }
                    r.produces("application/json").handler(routingContextConsumer)
                    .failureHandler(rct -> {
                        log.error("调用Biz[{}]失败", contextPath, rct.failure());
                        rct.response().end(UFailureHandler.commonFailure(rct.failure()));
                    });
                    log.info("监听 {}:{}", pathMark.method().name(), path);
                });
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("初始化({})失败", c, e);
            }

        }

        int port = Properties.getInteger("application", "listen.port");
        log.info("服务监听端口:{}", port);
        vertx.createHttpServer().requestHandler(router::accept).listen(port);
    }

}
