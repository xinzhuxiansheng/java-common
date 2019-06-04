package com.xinzhuxiansheng.common.utils;

import java.util.Enumeration;
import java.util.Iterator;

// Iterable: 一个集合对象要表名自己支持迭代，能使用foreach语句的特权，就必须实现Iterable接口，标明是可迭代的，就必须为forearch语句
//提供一个迭代器。这个迭代器是用接口定义的iterator方法提供的。也就是iterator方法需要返回一个Iterator对象
public class EnumerationIter<E> implements Iterator<E>,Iterable<E> {
    private final Enumeration<E> e;

    public EnumerationIter(Enumeration<E> enumeration){
        this.e = enumeration;
    }

    @Override
    public Iterator<E> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.e.hasMoreElements();
    }

    @Override
    public E next() {
        return this.e.nextElement();
    }

    /**
     * 不支持 remove
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
