package com.distributed.lock;

/**
 * 分布式锁模板类
 * Created by sunyujia@aliyun.com on 2016/2/23.
 */
public interface DistributedLockTemplate {

    /**
     *
     * @param lockId 锁id(对应业务唯一ID)
     * @param timeout 单位毫秒
     * @param callback 回调函数
     * @return
     */
    public Object execute(String lockId,int timeout,Callback callback);
}
