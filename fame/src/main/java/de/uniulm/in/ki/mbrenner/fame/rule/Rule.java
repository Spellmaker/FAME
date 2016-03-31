package de.uniulm.in.ki.mbrenner.fame.rule;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

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
	private int id = -1;
	public int occurences;

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public Rule(Integer head, Integer axiom, Integer define, Integer ...body){
		this.axiom = axiom;
		this.define = define;
		this.head = head;
		if(body != null){
			//this.body = new Integer[body.length];
			//for(int i = 0; i < body.length; i++){
			//	if(body[i] == null) throw new IllegalArgumentException("body elements are not allowed to be null");
			//	this.body[i] = body[i];
			//}
			this.body = new Integer[body.length];
			System.arraycopy(body, 0, this.body, 0, body.length);
		}
		else{
			this.body = null;
		}
		this.size = (this.body == null) ? 0 : this.body.length;
	}

	public Rule(Integer head, Integer axiom, Integer define, List<Integer> body){
		this.axiom = axiom;
		this.head = head;
		this.define = define;
		if(body != null){
			this.body = body.toArray(new Integer[1]);
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

	public Integer getHeadOrAxiom(){
		return (head == null) ? axiom : head;
	}
	
	public Integer get(int i){
		return body[i];
	}
	
	@Override
	public String toString(){
		String res = "";
		if(body != null)
			for(Integer e : body){
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

	public String toDebugString(RuleSet rs){
		String res = "";
		if(body != null)
			for(Integer e : body){
				res = res + (res.equals("") ? "" : " & ") + rs.debugLookup(e);
			}

		res += " -> ";
		if(head != null) res += rs.debugLookup(head);
		else{
			res += rs.debugLookup(axiom);
			if(define != null){
				res += " def " + rs.debugLookup(define);
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
		if(body != null)
			for(Object o : body){
				res += o.hashCode();
				if(o == null) System.out.println(this);
			}
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

			if(res && ((body == null && other.body == null) || other.body.length == body.length)){
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
		if(array == null) return false;
		return position < array.length - 1;
	}

	@Override
	public T next() {
		return array[++position];
	}
	
}
