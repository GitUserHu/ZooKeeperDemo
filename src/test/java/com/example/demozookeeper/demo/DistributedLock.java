package com.example.demozookeeper.demo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooDefs.Ids;

/**
   * 一、分布式锁问题
   *       共享锁在同一个进程中很容易实现，可以靠Java本身提供的同步机制解决，但是在跨进程或者在不同 Server 之间就不好实现了，
   *       这时候就需要一个中间人来协调多个Server之间的各种问题，比如如何获得锁/释放锁、谁先获得锁、谁后获得锁等。
 *       
   * 二、利用ZooKeeper解决
   *      借助Zookeeper 可以实现这种分布式锁：需要获得锁的 Server 创建一个 EPHEMERAL_SEQUENTIAL 目录节点，
   *      然后调用 getChildren()方法获取列表中最小的目录节点，如果最小节点就是自己创建的目录节点，那么它就获得了这个锁，
   *      如果不是那么它就调用 exists() 方法并监控前一节点的变化，一直到自己创建的节点成为列表中最小编号的目录节点，从而获得锁。
   *      释放锁很简单，只要删除它自己所创建的目录节点就行了。    
 * @author HuJiaB
 *
 */
public class DistributedLock {

	final static CountDownLatch connectedSignal=new CountDownLatch(1);
	public static int resource =0;
	public  static void plus() {
		
		resource++;
		System.err.println(Thread.currentThread().getName() +": " + resource);
		int sleepMillis=(int) (Math.random()*100);
		 try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
	
	static class CountPlus extends Thread{
		private final static String LOCK_ROOT_PATH="/Locks";
		private final static String LOCK_NODE_NAME="Lock";
		private ZooKeeper client;
		public void run() {
			for(int i =0 ;i < 1;i++) {
				String lockPath=getLock();
				plus();
				releaseLock(lockPath);
			}
			closeClient(client);
			System.err.println(Thread.currentThread().getName() + "执行完毕：" + resource);
		}
		
		public String getLock() {
			try {
				String lockPath=this.client.create(LOCK_ROOT_PATH+"/"+LOCK_NODE_NAME, 
									Thread.currentThread().getName().getBytes(), 
									Ids.OPEN_ACL_UNSAFE, 
									CreateMode.EPHEMERAL_SEQUENTIAL);
				System.err.println(Thread.currentThread().getName()+" create Path: "+ lockPath);
				/*try to Lock*/
				tryLock(lockPath);
				return lockPath;
			} catch (KeeperException | InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public boolean tryLock(String lockPath) {
			try {
				List<String> lockPaths=this.client.getChildren(LOCK_ROOT_PATH, false);
				//sort child node by asc
				Collections.sort(lockPaths);
				int index=lockPaths.indexOf(lockPath.substring(LOCK_ROOT_PATH.length()+1));
				//lockPath is mix node. 
				if(index==0) {
					System.err.println(Thread.currentThread().getName()+" get lock,lockPath:"+lockPath);
					return true;
				}else {
					//create a watcher to monitor the previous node.
					Watcher watcher=new Watcher() {
						
						@Override
						public void process(WatchedEvent event) {
							System.err.println(event.getType()+" has benn deleted.");
							synchronized (this) {
								notify();
							}
						}
					};
					String preLockPath=lockPaths.get(index-1);
					Stat stat=client.exists(LOCK_ROOT_PATH+"/"+preLockPath, watcher);
					if(stat==null) {
						return tryLock(lockPath);
					}else {
						System.err.println(Thread.currentThread().getName() + " wait for " + preLockPath);
						synchronized (watcher) {
							watcher.wait();
						}
						System.err.println(Thread.currentThread().getName()+" "+lockPath+" cancel wait");
						return tryLock(lockPath);
					}
				}
			} catch (KeeperException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		/**
		 * 释放锁：删除节点
		 * @param lockPath
		 */
		public void releaseLock(String lockPath) {
			try {
				this.client.delete(lockPath, -1);
			} catch (InterruptedException | KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public CountPlus(String threadName) {
			super(threadName);
		}
		public void setClient(ZooKeeper client) {
			this.client=client;
		}
		public void closeClient(ZooKeeper client) {
			try {
				client.close();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void setClientForThread(CountPlus thread) throws IOException, InterruptedException {
		ZooKeeper zoo=new ZooKeeper("127.0.0.1:2181", 5000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				System.err.println("event triggered, type:"+event.getType());
				if(event.getState()==KeeperState.SyncConnected) {
					//invoke function countDown() 1 time, so main progress can process
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
		thread.setClient(zoo);
	}
	public static void main(String[] args) throws IOException, InterruptedException {
		//Date startTime=new Date();
		CountPlus threadA=new CountPlus("Thread A");
		setClientForThread(threadA);
		threadA.start();
		
		CountPlus threadB=new CountPlus("Thread B");
		setClientForThread(threadB);
		threadB.start();
		
		CountPlus threadC=new CountPlus("Thread C");
		setClientForThread(threadC);
		threadC.start();
		
		CountPlus threadD=new CountPlus("Thread D");
		setClientForThread(threadD);
		threadD.start();
		
		CountPlus threadE=new CountPlus("Thread E");
		setClientForThread(threadE);
		threadE.start();

		//System.err.println("Total:"+-new Date().- startTime);
	}

}
