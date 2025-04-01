package com.tollge.common;

import java.util.*;

public class TollgeMap<K, V> extends LinkedHashMap<K, V> {

  @Override
  public V get(Object key) {
    if (key == null) return null;


    V value = super.get(key);

    // 检查获取的值是否为List类型
    if (value instanceof List) {
      List<?> originalList = (List<?>) value;
      List<TollgeMap<String, Object>> convertedList = new ArrayList<>();

      for (Object item : originalList) {
        if (item instanceof Map) {
          Map<?, ?> mapItem = (Map<?, ?>) item;
          TollgeMap<String, Object> customMapItem = new TollgeMap<>();

          // 遍历原Map的条目，复制到CustomMap中
          for (Map.Entry<?, ?> entry : mapItem.entrySet()) {
            Object entryKey = entry.getKey();
            Object entryValue = entry.getValue();

            // 确保键和值的类型正确
            if (entryKey instanceof String) {
              customMapItem.put((String) entryKey, entryValue);
            } else {
              throw new IllegalArgumentException("Map entry has invalid types. Expected Integer key and String value.");
            }
          }
          convertedList.add(customMapItem);
        }
      }

      // 由于类型擦除，此处进行强制转换
      return (V) convertedList;
    }

    return value;
  }

  @Override
  public String toString() {
    return mapToJson(this);
  }

  private static String mapToJson(Map<?, ?> map) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!first) sb.append(",");
      first = false;

      // 处理Key（强制转为字符串）
      sb.append('"')
        .append(escapeJson(entry.getKey().toString()))
        .append("\":");

      // 处理Value
      sb.append(valueToJson(entry.getValue()));
    }

    return sb.append("}").toString();
  }

  private static String valueToJson(Object value) {
    if (value == null) return "null";
    if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
    if (value instanceof Number) return numberToString((Number) value);
    if (value instanceof Boolean) return value.toString();
    if (value instanceof Map) return mapToJson((Map<?, ?>) value);
    if (value instanceof Collection) return collectionToJson((Collection<?>) value);
    return "\"" + escapeJson(value.toString()) + "\""; // 其他对象转为字符串
  }

  private static String collectionToJson(Collection<?> coll) {
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (Object item : coll) {
      if (!first) sb.append(",");
      first = false;
      sb.append(valueToJson(item));
    }
    return sb.append("]").toString();
  }

  // 处理特殊字符转义
  private static String escapeJson(String input) {
    return input.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }

  // 处理科学计数法
  private static String numberToString(Number number) {
    if (number instanceof Double) {
      Double d = (Double) number;
      if (d.isNaN() || d.isInfinite()) return "null";
    } else if (number instanceof Float) {
      Float f = (Float) number;
      if (f.isNaN() || f.isInfinite()) return "null";
    }
    return number.toString();
  }
}
