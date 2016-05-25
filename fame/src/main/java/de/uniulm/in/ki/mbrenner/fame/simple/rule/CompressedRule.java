package de.uniulm.in.ki.mbrenner.fame.simple.rule;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * A compressed extraction rule
 *
 * In contrast to normal rules, each compressed rule has only atomic concepts and properties in its body and
 * its execution results in the addition of an axiom
 */
public class CompressedRule implements Iterable<OWLObject>{
	final Set<OWLObject> body;
	final OWLAxiom head;

	/**
	 * Default constructor
	 * @param head The axiom of the rule
	 * @param body The body of the rule
     */
	public CompressedRule(OWLAxiom head, Set<OWLObject> body){
		this.head = head;
		this.body = body;
	}

	/**
	 * Provides the number of elements in the body
	 * @return The body size
     */
	public int size(){
		return body.size();
	}

	/**
	 * Provides access to the rule head
	 * @return The head axiom
     */
	public OWLAxiom getHead(){
		return head;
	}
	
	@Override
	public String toString(){
		String s = "";
		for(OWLObject e : body){
			s += ClassPrinter.printClass(e) + ", ";
		}
		s = s.substring(0, s.length() - 2);
		s += " -> " + ClassPrinter.printAxiom(head);
		return s;
	}

	@Override
	public Iterator<OWLObject> iterator() {
		return body.iterator();
	}
}