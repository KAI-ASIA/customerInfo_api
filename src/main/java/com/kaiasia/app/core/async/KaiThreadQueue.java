package com.kaiasia.app.core.async;

import java.util.concurrent.ConcurrentLinkedQueue;

// Hàng đợi xử lý thread
public class KaiThreadQueue<T> {
    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue();

    public KaiThreadQueue() {
    }

    public void add(T t) {
        this.queue.add(t);
    }

    public T poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }
}
