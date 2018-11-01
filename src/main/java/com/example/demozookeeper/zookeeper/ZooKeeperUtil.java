package com.example.demozookeeper.zookeeper;

import java.util.List;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * ZooKeeper工具类
 * @author P1306792
 *
 */
public class ZooKeeperUtil {
	
	//CountDownLatch 用于停止（等待）主进程，直到客户端与ZooKeeper集合连接：即 在主进程进行之前需要调用countDown（）方法1次
	
	public static ZooKeeper connect(String host) throws IOException, InterruptedException {
		final CountDownLatch connectedSignal=new CountDownLatch(1);
		ZooKeeper zoo=new ZooKeeper(host, 5000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				System.out.println("event triggered, type:"+event.getType());
				if(event.getState()==KeeperState.SyncConnected) {
					//invoke function countDown() 1 time, so main progress can process
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
		return zoo;
	}
	/**
	 * delete the node with the specified path 
	 * @param zoo
	 * @param path
	 * @throws KeeperException 
	 * @throws InterruptedException 
	 */
	public static void deleteZnode(ZooKeeper zoo,String path) throws InterruptedException, KeeperException {
		if(ZooKeeperUtil.exists(zoo, path).getNumChildren()!=0) {
			System.out.println("Existing Children in the path to be deleted !");
		}else {
			zoo.delete(path,ZooKeeperUtil.exists(zoo, path).getVersion());//-1 can match all version, so will delete all data of this znode.
		}
	}
	
	/**
	 * get all childs
	 * @param zoo
	 * @param path
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static List<String> getChildren(ZooKeeper zoo,String path) throws KeeperException, InterruptedException{
		return zoo.getChildren(path, false);
	}
	
	/**
	 * set data and update version
	 * @param zoo
	 * @param dataToSet
	 * @param path
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static void setData(ZooKeeper zoo,byte[] dataToSet,String path) throws KeeperException, InterruptedException {
		zoo.setData(path, dataToSet, ZooKeeperUtil.exists(zoo, path).getVersion());
	}
	/**
	 * getData
	 * @param zoo
	 * @param path
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static byte[] getData(ZooKeeper zoo,String path) throws KeeperException, InterruptedException {
		return zoo.getData(path, false, null);
	}
	/**
	 * check if exists
	 * @param zoo
	 * @param path
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static Stat exists(ZooKeeper zoo,String path) throws KeeperException, InterruptedException {
		return zoo.exists(path, true);
	}
	/**
	 * create Znode
	 * @param path
	 * @param data
	 * @param zoo
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static void create(String path,byte[] data,ZooKeeper zoo) throws KeeperException, InterruptedException {
		zoo.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		
	}
	
	/**
	 * close zookeeper
	 * @throws InterruptedException
	 */
	public static void close(ZooKeeper zoo) throws InterruptedException {
		zoo.close();
	}
}
