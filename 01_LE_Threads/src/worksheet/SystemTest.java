package worksheet;

public class SystemTest {

	private static long counter = 0;

	public static void main(String[] args) {
		while(true){
			new Thread(() -> run()).start();
		}
	}

	public static void run(){
		System.out.println(++counter);
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
