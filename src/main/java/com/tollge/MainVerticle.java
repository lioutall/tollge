package com.tollge;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.util.MyVertx;
import com.tollge.common.util.Properties;
import com.tollge.common.util.ReflectionUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * 启动入口
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> fut) {
        // 全局化
        MyVertx.vertx(vertx);

        Future<String> future = Future.<String>future(dao -> {
            log.debug("启动Verticle...");
            dao.complete();
        });

        // 启动模块verticle
        Map<String, Object> verticles = Properties.getGroup("verticles");
        Set<Map.Entry<String, Object>> list = verticles.entrySet();
        for (Map.Entry<String, Object> entry : list) {
            future = future.compose(res -> Future.future(dao -> {
                String value = (String) entry.getValue();
                log.debug("启动[{}]...", value);
                // 如果带了instance值, 则按规则启动
                int commaIndex = value.indexOf(",");
                if(commaIndex > -1) {
                    String verticleName = value.substring(0, commaIndex);
                    String instanceStr = value.substring(commaIndex+1);
                    if("HALF".equals(instanceStr)) {
                        double cupCoresNum = (double) CpuCoreSensor.availableProcessors();
                        vertx.deployVerticle(verticleName, new DeploymentOptions().setInstances((int) Math.ceil(cupCoresNum / 2)), dao);
                    } else if("ALL".equals(instanceStr)) {
                        vertx.deployVerticle(verticleName, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors()), dao);
                    } else {
                        if(instanceStr.matches("\\d+")) {
                            vertx.deployVerticle(verticleName, new DeploymentOptions().setInstances(Integer.valueOf(instanceStr)), dao);
                        } else {
                            vertx.deployVerticle(value, dao);
                        }
                    }
                } else {
                    vertx.deployVerticle(value, dao);
                }
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

        future.setHandler(res -> {
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
