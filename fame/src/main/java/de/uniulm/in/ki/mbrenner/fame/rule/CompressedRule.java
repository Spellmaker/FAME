package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import org.semanticweb.owlapi.model.OWLObject;

public class CompressedRule implements Iterable<OWLObject>{
	Set<OWLObject> body;
	OWLAxiom head;
	
	public CompressedRule(OWLAxiom head, Set<OWLObject> body){
		this.head = head;
		this.body = body;
	}
	
	public int size(){
		return body.size();
	}
	
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