package com.tollge.common.util;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

public class ReflectionUtil {

    /**
     * 模块里的默认加载
     */
    private static final String TOLLGE_MODULES = "com.tollge.modules";

    private ReflectionUtil(){}

    private static final String PACKAGES = Properties.getString("application", "baseScan", "-nowhere-");

    public static Set<Method> getMethodsWithAnnotated(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages(PACKAGES, TOLLGE_MODULES)
                        .filterInputsBy(new FilterBuilder().includePackage(PACKAGES).includePackage(TOLLGE_MODULES))
                        .addScanners(Scanners.ConstructorsAnnotated));
        return reflections.getMethodsAnnotatedWith(annotation);
    }

    public static Set<Class<?>> getClassesWithAnnotated(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages(PACKAGES, TOLLGE_MODULES)
                        .filterInputsBy(new FilterBuilder().includePackage(PACKAGES).includePackage(TOLLGE_MODULES)));
        return reflections.getTypesAnnotatedWith(annotation);
    }

    public static <T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages(PACKAGES, TOLLGE_MODULES)
                        .filterInputsBy(new FilterBuilder().includePackage(PACKAGES).includePackage(TOLLGE_MODULES)));
        return reflections.getSubTypesOf(type);
    }
}
