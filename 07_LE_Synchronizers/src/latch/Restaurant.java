package latch;

import java.util.concurrent.CountDownLatch;

public class Restaurant {

	private final static CountDownLatch cookLatch = new CountDownLatch(1);
	private static CountDownLatch guestLatch;

	public static void main(String[] args) {
		int nrGuests = 2;
		guestLatch = new CountDownLatch(nrGuests);
				
		new Cook(cookLatch).start();
		
		for(int i = 0; i < nrGuests; i++) {
			new Guest(cookLatch, guestLatch).start();
		}
		
		new DishWasher(guestLatch).start();
	}
	
	
	static class Cook extends Thread {
		private CountDownLatch cookLatch;

		public Cook(CountDownLatch cooklatch) {
			this.cookLatch = cooklatch;
		}
		
		@Override
		public void run() {
			System.out.println("Start Cooking..");
			try {
				sleep(5000);
			} catch (InterruptedException e) {}
			System.out.println("Meal is ready");
			cookLatch.countDown();
		}
	}
	
	
	static class Guest extends Thread {
		private CountDownLatch cookLatch;
		private CountDownLatch guestLatch;

		public Guest(CountDownLatch cookLatch, CountDownLatch guestLatch) {
			this.cookLatch = cookLatch;
			this.guestLatch = guestLatch;
		}
		
		@Override
		public void run() {
			try {
				sleep(1000);
				System.out.println("Entering restaurant and placing order.");
				cookLatch.await();
				System.out.println("Enjoying meal.");
				sleep(5000);
				System.out.println("Meal was excellent!");
				guestLatch.countDown();
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class DishWasher extends Thread {

		private CountDownLatch guestLatch;
		
		public DishWasher(CountDownLatch guestLatch) {
			this.guestLatch = guestLatch;
		}
		
		@Override
		public void run() {
			try {
				System.out.println("Waiting for dirty dishes.");
				guestLatch.await();
				System.out.println("Washing dishes.");
				sleep(0);
			} catch (InterruptedException e) {}
		}
	}
}
