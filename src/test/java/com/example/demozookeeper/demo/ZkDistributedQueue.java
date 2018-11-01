package com.example.demozookeeper.demo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZkDistributedQueue {
	final static CountDownLatch connectedSignal=new CountDownLatch(1);
	private final static String MAIL_ROOT_PATH="/Mail";
	private final static String MAIL_PATH="Letter";
	/**
	 * 
	 * @author P1306792
	 *
	 */
	static class Consumer extends Thread{
		
		private ZooKeeper zoo;

		public Consumer(String name) {
			super(name);
		}
		public void consume() throws KeeperException, InterruptedException {
			if(getMailNum()==0) {
				System.err.println(Thread.currentThread().getName()+": Mail Box is empty...");
				Watcher watcher=new Watcher() {
					
					@Override
					public void process(WatchedEvent event) {
						System.err.println(Thread.currentThread().getName()+": mail box is not empty...");
						synchronized (this) {
							this.notify();
						}
					}
				};
				zoo.getChildren(MAIL_ROOT_PATH, watcher);
				synchronized (watcher) {
					watcher.wait();
				}
			}else {
				int sleepMillis = (int) (Math.random() * 1000);
				Thread.sleep(sleepMillis);
				List<String> children=zoo.getChildren(MAIL_ROOT_PATH, false);
				Collections.sort(children);
				System.err.println(MAIL_ROOT_PATH+"/"+children.get(0));
				zoo.delete(MAIL_ROOT_PATH+"/"+children.get(0), -1);
				System.err.println(Thread.currentThread().getName()+": a mail has been deleted...");
			}
		}
		
		public void run() {
			while(true){
				try {
					consume();
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		public void setZoo(ZooKeeper zoo) {
			this.zoo = zoo;
		}
		public int getMailNum() throws KeeperException, InterruptedException {
			Stat stat	=zoo.exists(MAIL_ROOT_PATH, false);
			if(stat!=null) {
				return stat.getNumChildren();
			}
			return 0;
		}
		
	}
	
	static class Producer extends Thread{
		private ZooKeeper zoo;
		public void producerMail() throws KeeperException, InterruptedException {
			if(getMailNum()==10) {
				System.err.println(Thread.currentThread().getName()+": Mail Box is full...");
				Watcher watcher =new Watcher() {
					
					@Override
					public void process(WatchedEvent event) {
						System.err.println(Thread.currentThread().getName()+": Mail box is not full...(a mail has been deleted...)");
						synchronized (this) {
							this.notify();
						}
						
					}
				};
					this.zoo.getChildren(MAIL_ROOT_PATH, watcher);
					synchronized (watcher) {
						watcher.wait();
					}
			}else {
					int sleepMillis = (int) (Math.random() * 1000);
					Thread.sleep(sleepMillis);
					String path=this.zoo.create(MAIL_ROOT_PATH+"/"+MAIL_PATH, Thread.currentThread().getName().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
					System.err.println(Thread.currentThread().getName()+":("+path+") a new mail has been created...");
			}	
		}
		public void run() {
			while(true) {
				try {
					producerMail();
				} catch (KeeperException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public Producer(String name) {
			super(name);
		}
		public void setZoo(ZooKeeper zoo) {
			this.zoo = zoo;
		}
		
		public int getMailNum() throws KeeperException, InterruptedException {
			Stat stat	=zoo.exists(MAIL_ROOT_PATH, false);
			if(stat!=null) {
				return stat.getNumChildren();
			}
			return 0;
		}
	}
	public static ZooKeeper getZoo() throws IOException {
		
		return new ZooKeeper("127.0.0.1:2181",3000,null);
	}
	
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		
		Producer producer=new Producer("Producer");
		ZooKeeper zooKeeper=getZoo();
		//zooKeeper.create(MAIL_ROOT_PATH, "MAIL_BOX".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		producer.setZoo(zooKeeper);
		producer.start();
		Consumer consumer=new Consumer("Consumer");
		consumer.setZoo(getZoo());
		consumer.start();
		
		
	}

}
