package com.xinzhuxiansheng.common.utils;

/**
 * 过滤器 ， 通过匿名类实现，自定义accept实现
 * @param <T>
 */
public interface Filter<T> {

    boolean accept(T t);
}
