package com.tollge.common.util;

import com.google.common.collect.Maps;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Slf4j
public class Properties {
    private Properties() {
    }

    public static Properties getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    @SuppressWarnings("unchecked")
    private enum Singleton {
        /**
         * 单例
         */
        INSTANCE;

        private final Properties single;

        private Singleton() {
            single = new Properties();
            Yaml yaml = new Yaml();
            Map<String, Object> proMap = Maps.newHashMap();

            // 先加载modules中的配置
            try {
                String modulesDir = "modules";

                Enumeration<URL> resources = Properties.class.getClassLoader().getResources(modulesDir);
                if (resources != null) {
                    while (resources.hasMoreElements()) {
                        URL resource = resources.nextElement();
                        String modulesPath = resource.getPath();
                        if(!modulesPath.contains("jar!")){
                            continue;
                        }

                        String jarPath = modulesPath.substring(5, modulesPath.length() - modulesDir.length() - 2);

                        try(JarFile jarFile = new JarFile(jarPath)) {
                            Enumeration<JarEntry> dd = jarFile.entries();
                            while (dd.hasMoreElements()) {
                                JarEntry entry = dd.nextElement();
                                if (!entry.isDirectory() && entry.getName().startsWith(modulesDir + "/tollge-") && entry.getName().endsWith("yml")) {
                                    log.debug("遍历yml文件:{}", entry.getName());
                                    try (InputStream fileIS = jarFile.getInputStream(entry)) {
                                        Map<Object, Object> loadMap = yaml.load(fileIS);
                                        proMap.putAll(flatRead("", loadMap));
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (IOException e) {
                log.error("加载modules/tollge-x.yml失败", e);
            }

            // 后加载当前项目, 如有相同则覆盖
            Map<Object, Object> loadMap = yaml.load(this.getClass().getClassLoader().getResourceAsStream("tollge.yml"));
            proMap.putAll(flatRead("", loadMap));

            single.pros = proMap;
        }

        private Map<? extends String, ?> flatRead(String prefix, Map<Object, Object> loadMap) {
            Map<String, Object> result = Maps.newHashMap();

            for (Map.Entry<Object, Object> m : loadMap.entrySet()) {
                if (m.getValue() instanceof Map) {
                    if(prefix.isEmpty()) {
                        result.putAll(flatRead(m.getKey().toString(), (Map<Object, Object>) m.getValue()));
                    } else {
                        result.putAll(flatRead(prefix + "." + m.getKey(), (Map<Object, Object>) m.getValue()));
                    }
                } else {
                    result.put(prefix + "." + m.getKey(), m.getValue());
                }
            }

            return result;
        }

        public Properties getInstance() {
            return single;
        }
    }

    private Map<String, Object> pros;

    public static String getString(String group, String key, String defaultStr) {
        Object o = Singleton.INSTANCE.getInstance().pros.get(group + "." + key);
        if(o == null) {
            return defaultStr;
        }
        String str = (String) o;
        if (StringUtil.isNullOrEmpty(str)) {
            return defaultStr;
        }
        return str;
    }

    public static String getString(String group, String key) {
        Object o = Singleton.INSTANCE.getInstance().pros.get(group + "." + key);
        if(o == null) {
            return null;
        }
        return (String) o;
    }

    public static Boolean getBoolean(String group, String key) {
        Object o = Singleton.INSTANCE.getInstance().pros.get(group + "." + key);
        if(o == null) {
            return null;
        }
        return (Boolean) o;
    }

    public static Integer getInteger(String group, String key) {
        Object o = Singleton.INSTANCE.getInstance().pros.get(group + "." + key);
        if(o == null) {
            return null;
        }
        return (Integer) o;
    }

    public static Map<String, Object> getGroup(String group) {
        return Singleton.INSTANCE.getInstance().pros.entrySet()
                .stream().filter(e -> e.getKey().startsWith(group + "."))
                .collect(Collectors.toMap(entry -> entry.getKey().substring(group.length() + 1), Map.Entry::getValue));
    }
}
