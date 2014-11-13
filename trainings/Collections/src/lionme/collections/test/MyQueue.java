package lionme.collections.test;


public class MyQueue<T>  {
	private Elem<T> header;
	private int size;
	
	public MyQueue(){
		this.header = new Elem<T>(null,null,null); 
		header.next = header.prev = header;
		this.size = 0;
	}
	
	public void add(T value){
		Elem<T> newElem = new Elem<T>(value,header,header.prev);
		newElem.prev.next = newElem;
		newElem.next.prev = newElem;
		size++;
	}
	
	public T getQueueValue(){
		return header.prev.value;
	}
	
	public T getStackValue(){
		return header.next.value;
	}
	
	private static class Elem <T> {
		T value;
		Elem<T> next;
		Elem<T> prev;
		Elem(T value, Elem<T> next, Elem<T> prev){
			 this.value = value;
		     this.next = next;
		     this.prev = prev;
		}
	}

	public int getSize() {
		return size;
	}

}

