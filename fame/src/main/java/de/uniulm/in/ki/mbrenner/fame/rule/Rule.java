package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.Iterator;

/**
 * Represents a module extraction rule
 * @author spellmaker
 *
 */
public class Rule implements Iterable<Integer>{
	private final Integer[] body;
	private final Integer head;
	private final Integer axiom;
	private final Integer define;
	private int size;
	
	public Rule(Integer head, Integer axiom, Integer define, Integer ...body){
		this.axiom = axiom;
		this.define = define;
		this.head = head;
		if(body != null){
			this.body = new Integer[body.length];
			for(int i = 0; i < body.length; i++) this.body[i] = body[i];
		}
		else{
			this.body = null;
		}
		this.size = (this.body == null) ? 0 : this.body.length;
	}
	
	public Integer getAxiom(){
		return axiom;
	}
	
	public Integer getDefinition(){
		return define;
	}
		
	/**
	 * Provides access to the rules head
	 * @return The head of the rule
	 */
	public Integer getHead(){
		return head;
	}
	
	public Integer get(int i){
		return body[i];
	}
	
	@Override
	public String toString(){
		String res = "";
		for(Object e : body){
			res = res + (res.equals("") ? "" : " & ") + e;
		}
		
		res += " -> ";
		if(head != null) res += head;
		else{
			res += axiom;
			if(define != null){
				res += " def " + define;
			}
		}
		
		//if(head instanceof OWLAxiom) 	res += " -> " + ClassPrinter.printAxiom((OWLAxiom) head);
		//else 							res += " -> " + ClassPrinter.printClass(head);
		return res;
	}
	
	@Override
	public int hashCode(){
		int res = 0;
		if(head != null) res += head.hashCode();
		if(axiom != null) res += axiom.hashCode();
		if(define != null) res += define.hashCode();
		for(Object o : body) res += o.hashCode();
		return res;
	}
	
	public int size(){
		return size;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Rule){
			Rule other = (Rule) o;
			
			boolean res = (other.head == null && head == null) || 
					(other.head != null && head != null && other.head.toString().equals(head.toString()));
			res = res && (other.axiom == null && axiom == null) ||
					(other.axiom != null && axiom != null && other.axiom.toString().equals(axiom.toString()));
			
			if(res && other.body.length == body.length){
				Iterator<Integer> otherIter = other.iterator();
				Iterator<Integer> myIter = iterator();
				while(myIter.hasNext()){
					if(!otherIter.next().toString().equals(myIter.next().toString())) return false;
				}
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new ArrayIterator<Integer>(body);
	}
}

class ArrayIterator<T> implements Iterator<T>{
	private final T[] array;
	private int position;
	
	public ArrayIterator(T[] array){
		this.array = array;
		this.position = -1;
	}
	
	@Override
	public boolean hasNext() {
		return position < array.length - 1;
	}

	@Override
	public T next() {
		return array[++position];
	}
	
}
