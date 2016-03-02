package de.uniulm.in.ki.mbrenner.fame.rule;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;

public class CompressedRule{
	Set<OWLEntity> body;
	OWLAxiom head;
	
	public CompressedRule(OWLAxiom head, Set<OWLEntity> body){
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
		for(OWLEntity e : body){
			s += ClassPrinter.printClass(e) + ", ";
		}
		s = s.substring(0, s.length() - 2);
		s += " -> " + ClassPrinter.printAxiom(head);
		return s;
	}
}