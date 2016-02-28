package com.distributed.lock.zk;

import com.distributed.lock.DistributedReentrantLock;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.LockInternalsDriver;
import org.apache.curator.framework.recipes.locks.StandardLockInternalsDriver;
import org.apache.zookeeper.KeeperException;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于Zookeeper的可重入互斥锁(关于重入:仅限于持有zk锁的jvm内重入)
 * Created by sunyujia@aliyun.com on 2016/2/24.
 */
public class ZkReentrantLock implements DistributedReentrantLock {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ZkReentrantLock.class);

    /**
     * 线程池
     */
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    /**
     * 所有PERSISTENT锁节点的根位置
     */
    public static final String ROOT_PATH = "/ROOT_LOCK/";

    /**
     * 每次延迟清理PERSISTENT节点的时间  Unit:MILLISECONDS
     */
    private long delayTimeForClean = 1000;

    /**
     * zk 共享锁实现
     */
    private InterProcessMutex interProcessMutex = null;


    /**
     * 锁的ID,对应zk一个PERSISTENT节点,下挂EPHEMERAL节点.
     */
    private String path;


    /**
     * zk的客户端
     */
    private CuratorFramework client;



    public ZkReentrantLock(CuratorFramework client, String lockId) {
        init(client, lockId);
    }

    public void init(CuratorFramework client, String lockId) {
        this.client = client;
        this.path = ROOT_PATH + lockId;
        interProcessMutex = new InterProcessMutex(client, this.path);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            return interProcessMutex.acquire(timeout, unit);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    @Override
    public void unlock() {
        try {
            interProcessMutex.release();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            executorService.schedule(new Cleaner(client, path), delayTimeForClean, TimeUnit.MILLISECONDS);
        }
    }

    static class Cleaner implements Runnable {
        CuratorFramework client;
        String path;

        public Cleaner(CuratorFramework client, String path) {
            this.client = client;
            this.path = path;
        }

        public void run() {
            try {
                List list = client.getChildren().forPath(path);
                if (list == null || list.isEmpty()) {
                    client.delete().forPath(path);
                }
            } catch (KeeperException.NoNodeException e1) {
                //nothing
            } catch (KeeperException.NotEmptyException e2) {
                //nothing
            } catch (Exception e) {
                log.error(e.getMessage(), e);//准备删除时,正好有线程创建锁
            }
        }
    }
}
