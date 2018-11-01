package com.example.demozookeeper.demo;

import java.util.Date;

/**
   *一、 分布式锁问题：
 *   plus()方法涉及到对公共资源(resource(int))的改动，但是并没有对它进行同步控制，
   *       可能会造成多个线程同时对公共资源发起改动，进而出现并发问题。问题的根源在于，没有保证同一时刻只能有一个线程可以改动公共资源。
   *       例子 ：出现的问题->如果不出现多个线程同时操作resource的情况，那么resource最后的值应该是100，但是  该值最后不是100，说明出现了多个线程同时操作该资源的情况。
 * 
   * 二、解决资源锁问题
 * 	为了解决上诉问题，给plus方法加上synchronized关键字，synchronized关键字将plus方法加入锁，即控制一个线程想要执行该方法，
 * 	首先需要获得锁（锁是唯一的），执行完毕后，再释放锁。如果得不到锁，该线程会进入等待池中等待，直到抢到锁才能继续执行。
 * 	这样就保证了同一时刻只能有一个线程可以改动公共资源，避免了并发问题。
 * 
 * @author P1306792
 *
 */
public class MultiThreadTest {

	public static int resource =0;
	public synchronized static void plus() {
		
		resource++;
		System.out.println(Thread.currentThread().getName() +": " + resource);
		int sleepMillis=(int) (Math.random()*100);
		 try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
	
	static class CountPlus extends Thread{
		public void run() {
			for(int i =0 ;i < 20;i++) {
				plus();
				
			}
			//System.out.println(Thread.currentThread().getName() + "执行完毕：" + resource);
		}
		
		public CountPlus(String threadName) {
			super(threadName);
		}
	}
	public static void main(String[] args) {
		Date startTime=new Date();
		CountPlus threadA=new CountPlus("Thread A");
		threadA.start();
		CountPlus threadB=new CountPlus("Thread B");
		threadB.start();
		CountPlus threadC=new CountPlus("Thread C");
		threadC.start();
		CountPlus threadD=new CountPlus("Thread D");
		threadD.start();
		CountPlus threadE=new CountPlus("Thread E");
		threadE.start();
		//System.out.println("Total:"+-new Date().- startTime);
	}

}
