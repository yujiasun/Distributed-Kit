package com.distributed.lock;

/**
 * Created by sunyujia@aliyun.com on 2016/2/23.
 */
public interface DistributedLockTemplate {

    public Object execute(String lockId,int timeout,Callback callback);
}
