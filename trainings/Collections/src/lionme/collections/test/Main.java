package lionme.collections.test;


public class Main {

	public static void main(String[] args) {
		MyQueue<Integer> myQueue = new MyQueue<Integer>();
		myQueue.add(1);
		myQueue.add(8);
		myQueue.add(3);
		myQueue.add(3);
		myQueue.add(6);
		myQueue.add(3);
		
		System.out.println(myQueue.getQueueValue());
		System.out.println(myQueue.getStackValue());
	}

}
