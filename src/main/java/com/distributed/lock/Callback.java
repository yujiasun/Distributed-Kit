package com.distributed.lock;

/**
 * Created by sunyujia@aliyun.com on 2016/2/23.
 */
public interface Callback {

    public Object onGetLock() throws InterruptedException;

    public Object onTimeout() throws InterruptedException;
}
