package blockingQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OrderProcessing {

	private final static int MAX = 100;
	private final static BlockingQueue<Order> orders = new ArrayBlockingQueue<OrderProcessing.Order>(MAX, true);
	private final static BlockingQueue<Order> validOrders = new ArrayBlockingQueue<OrderProcessing.Order>(MAX, true);
	
	public static void main(String[] args) {
		int nCustomers = 10;
		int nValidators = 2;
		int nProcessors = 3;

		for (int i = 0; i < nCustomers; i++) {
			new Customer("" + i, orders).start();
		}

		for (int i = 0; i < nValidators; i++) {
			new OrderValidator(orders, validOrders).start();
		}

		for (int i = 0; i < nProcessors; i++) {
			new OrderProcessor(validOrders).start();
		}
	}
	
	static class Order {
		public final String customerName;
		public final int itemId;
		public Order(String customerName, int itemId) {
			this.customerName = customerName;
			this.itemId = itemId;
		}
		
		@Override
		public String toString() {
			return "Order: [name = " + customerName + " ], [item = " + itemId +" ]";  
		}
	}
	
	
	static class Customer extends Thread {

		private final BlockingQueue<Order> orders;

		public Customer(String name, BlockingQueue<Order> orders) {
			super(name);
			this.orders = orders;
		}
		
		private Order createOrder() {
			Order o = new Order(getName(), (int) (Math.random()*100));
			System.out.println("Created:   " + o);
			return o;
		}
		
		private void handOverToValidator(Order o) throws InterruptedException {
			orders.put(o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = createOrder();
					handOverToValidator(o);
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class OrderValidator extends Thread {

		private final BlockingQueue<Order> orders;
		private final BlockingQueue<Order> validOrders;

		public OrderValidator(BlockingQueue<Order> orders, BlockingQueue<Order> validOrders) {
			this.orders = orders;
			this.validOrders = validOrders;
		}
		
		public Order getNextOrder() throws InterruptedException {
			return orders.take();
		}
		
		public boolean isValid(Order o) {
			return o.itemId < 50;
		}
		
		public void handOverToProcessor(Order o) throws InterruptedException {
			validOrders.put(o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = getNextOrder();
					if(isValid(o)) {
						handOverToProcessor(o);
					} else {
						System.err.println("Destroyed: " + o);
					}
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class OrderProcessor extends Thread {

		private final BlockingQueue<Order> validorders;

		public OrderProcessor(BlockingQueue<Order> validorders) {
			this.validorders = validorders;
		}
		
		public Order getNextOrder() throws InterruptedException {
			return validorders.take();
		}
		
		public void processOrder(Order o) {
			System.out.println("Processed: " + o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = getNextOrder();
					processOrder(o);
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
}
