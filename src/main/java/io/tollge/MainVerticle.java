package io.tollge;

import io.tollge.common.annotation.mark.Biz;
import io.tollge.common.util.MyVertx;
import io.tollge.common.util.Properties;
import io.tollge.common.util.ReflectionUtil;
import io.tollge.common.verticle.RouterVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * 启动入口
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        // 全局化
        MyVertx.vertx(vertx);

        Future<String> future = Future.<String>future(dao -> {
            log.debug("启动Verticle...");
            dao.complete();
        });

        // 启动模块verticle
        Map<String, Object> filters = Properties.getGroup("verticles");
        Set<Map.Entry<String, Object>> list = filters.entrySet();
        for (Map.Entry<String, Object> entry : list) {
            future = future.compose(res -> Future.future(dao -> {
                String value = (String) entry.getValue();
                log.debug("启动[{}]...", value);
                vertx.deployVerticle(value, dao);
            }));
        }

        // 启动Biz
        Set<Class<?>> set = ReflectionUtil.getClassesWithAnnotated(Biz.class);
        for (Class<?> c : set) {
            future = future.compose(res -> Future.<String>future(biz -> {
                Biz mark = c.getAnnotation(Biz.class);
                boolean isWorker = mark.worker();
                int instances = mark.instances();
                log.debug("启动 {}[worker={},instances={}]", c.getName(), isWorker, instances);
                DeploymentOptions options = new DeploymentOptions();
                if (isWorker) {
                    options.setWorker(true);
                }

                if (instances > 0) {
                    options.setInstances(instances);
                }
                vertx.deployVerticle(c.getName(), options, biz);
            }));
        }

        future.compose(res -> Future.<String>future(router -> {
            log.debug("启动RouterVerticle...");
            vertx.deployVerticle(new RouterVerticle(), router);
        })).setHandler(res -> {
            if (res.succeeded()) {
                log.info("启动完成...");
                fut.complete();
            } else {
                log.error("启动失败");
                fut.fail(res.cause());
            }
        });
    }

}
