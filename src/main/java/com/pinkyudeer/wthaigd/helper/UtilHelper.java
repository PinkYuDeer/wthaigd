package com.pinkyudeer.wthaigd.helper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class UtilHelper {

    /**
     * 获取本类及其父类的字段属性
     *
     * @param clazz 当前类对象
     * @return 字段数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }

    /**
     * 获取本类及其父类的字段属性, 且父类字段属性在子类前
     *
     * @param clazz 当前类对象
     * @return 字段数组
     */
    public static Field[] getAllFieldsReverse(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(0, new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }

    /**
     * 使用Gson实现对象的深度克隆
     *
     * @param <T>    对象类型
     * @param object 要克隆的对象
     * @param clazz  对象的类型
     * @return 深度克隆后的对象
     */
    public static <T> T deepClone(T object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
        return gson.fromJson(gson.toJson(object), clazz);
    }

    /**
     * 浅度克隆对象
     *
     * @param object 要克隆的对象
     * @param <T>    对象类型
     * @return 克隆后的对象
     */
    public static <T> T shallowClone(T object) {
        try {
            // 如果对象为null，直接返回null
            if (object == null) {
                return null;
            }

            // 创建与原对象相同类型的新实例
            Class<?> clazz = object.getClass();
            @SuppressWarnings("unchecked")
            T clone = (T) clazz.getDeclaredConstructor()
                .newInstance();

            // 获取所有字段并复制值
            for (Field field : UtilHelper.getAllFieldsReverse(clazz)) {
                field.setAccessible(true);
                Object value = field.get(object);
                field.set(clone, value);
            }

            return clone;
        } catch (Exception e) {
            throw new RuntimeException("克隆对象失败", e);
        }
    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString()); // ISO-8601格式
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Duration.parse(in.nextString());
        }
    }
}
