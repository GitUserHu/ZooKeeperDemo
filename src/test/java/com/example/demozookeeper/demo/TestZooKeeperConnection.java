package com.example.demozookeeper.demo;

import java.util.List;
import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

import com.example.demozookeeper.zookeeper.ZooKeeperUtil;

public class TestZooKeeperConnection {

	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
		// TODO Auto-generated method stub
		String path="/FirstZnode";
		String path_second="/SecondZnode";
		String host="127.0.0.1:2181";
		ZooKeeper zoo=ZooKeeperUtil.connect(host);
		byte[] data=ZooKeeperUtil.getData(zoo,path);
		System.out.println("---test ZooKeeper getData---\n path:"+path+" data:"+new String(data,"UTF-8"));
		
		ZooKeeperUtil.setData(zoo, "dataToSet".getBytes(), path);
		data=ZooKeeperUtil.getData(zoo,path);
		System.out.println("---test ZooKeeper setData---\n after setData path:"+path+" data:"+new String(data,"UTF-8"));
		
		Stat stat = ZooKeeperUtil.exists(zoo, path);
		System.out.println("---test ZooKeeper exists---\n stat:"+stat );
		
		List<String> children= ZooKeeperUtil.getChildren(zoo, path_second);
		System.out.println("---test ZooKeeper getChildren---\n children of "+path+" are "+children );
		
		
		ZooKeeperUtil.deleteZnode(zoo, path_second);
		if (ZooKeeperUtil.exists(zoo, path_second)==null) {
			System.out.println("after deleting, "+path_second+" node does not exist.");
		}
		else System.out.println("---test ZooKeeper deleteChild---\n after deleting, this node still exists." );
		
		ZooKeeperUtil.close(zoo);
		
	}

}
