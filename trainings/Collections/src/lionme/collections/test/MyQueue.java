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
		T oldValue = header.next.value;
		if(header.next!=header){
			size--;
		}
		header.next = header.next.next;
		header.next.prev = header; 
		return oldValue;
	}
	
	public T getStackValue(){
		T oldValue = header.prev.value;
		if(header.prev!=header){
			size--;
		}
		header.prev = header.prev.prev;
		header.prev.next = header; 
		return oldValue;
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
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("[");
		Elem<T> e = header.next; 
		while(e != header){
			s.append(e.value.toString());
			s.append(",");
			e = e.next;
		}
		if(s.lastIndexOf(",")>=0){
			s.deleteCharAt(s.lastIndexOf(","));
		}
		s.append("]");
		return s.toString();
	}

	
}

