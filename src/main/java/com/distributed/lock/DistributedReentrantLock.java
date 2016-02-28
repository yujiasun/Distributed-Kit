package com.distributed.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by sunyujia@aliyun.com on 2016/2/26.
 */
public interface DistributedReentrantLock {
    public boolean tryLock(Long timeout, TimeUnit unit) throws Exception;

    public void unlock();
}
