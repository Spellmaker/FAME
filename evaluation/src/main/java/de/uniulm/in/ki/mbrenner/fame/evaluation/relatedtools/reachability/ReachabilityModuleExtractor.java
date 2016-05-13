package de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.reachability;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

public class ReachabilityModuleExtractor {
	private Map<OWLEntity, List<OWLAxiom>> mapping;
	
	private void addAxiom(OWLAxiom ax, OWLClassExpression expr){
		for(OWLEntity e : expr.getSignature()){
			List<OWLAxiom> m = mapping.get(e);
			if(m == null){
				m = new LinkedList<>();
				mapping.put(e, m);
			}
			m.add(ax);
		}
	}
	
	public ReachabilityModuleExtractor(OWLOntology ontology){
		mapping = new HashMap<>();
		for(OWLAxiom a : ontology.getAxioms()){
			if(a instanceof OWLEquivalentClassesAxiom){
				OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) a;
				for(OWLClassExpression cl : eq.getClassExpressions()){
					addAxiom(a, cl);
				}
			}
			else if(a instanceof OWLSubClassOfAxiom){
				addAxiom(a, ((OWLSubClassOfAxiom)a).getSubClass());
			}
			else if(a instanceof OWLDeclarationAxiom){
				continue;
			}
			else{
				System.out.println("unknown axiom: " + a.getClass());
			}
		}
		/*System.out.println("preprocessing completed");
		for(Entry<OWLEntity, List<OWLAxiom>> entry : mapping.entrySet()){
			System.out.println("for entity " + entry.getKey());
			for(OWLAxiom a : entry.getValue()){
				System.out.println("\t" + ClassPrinter.printAxiom(a));
			}
		}*/
	}
	
	public Set<OWLAxiom> extractModule(Set<OWLEntity> signature){
		Set<OWLEntity> csig = new HashSet<>(signature);
		Set<OWLAxiom> module = new HashSet<>();
		Queue<OWLAxiom> process = new LinkedList<>();
		for(OWLEntity e : signature){
			List<OWLAxiom> l = mapping.get(e);
			if(l != null) process.addAll(l);
		}
		
		while(!process.isEmpty()){
			OWLAxiom front = process.poll();
			if(front instanceof OWLSubClassOfAxiom){
				OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom) front;
				check(process, csig, module, front, ax.getSubClass(), ax.getSuperClass());
			}
			else{
				OWLEquivalentClassesAxiom ax = (OWLEquivalentClassesAxiom) front;
				if(!check(process, csig, module, front, ax.getClassExpressionsAsList().get(0), ax.getClassExpressionsAsList().get(1))){
					check(process, csig, module, front, ax.getClassExpressionsAsList().get(1), ax.getClassExpressionsAsList().get(0));
				}
			}
		}
		
		for(OWLEntity e : csig){
			module.add(new OWLDeclarationAxiomImpl(e, Collections.emptyList()));
		}
		
		return module;
	}
	
	private boolean check(Queue<OWLAxiom> process, Set<OWLEntity> csig, Set<OWLAxiom> module, OWLAxiom front, OWLClassExpression sub, OWLClassExpression sup){
		boolean retval = false;
		if(csig.containsAll(sub.getSignature())){
			module.add(front);
			csig.addAll(sup.getSignature());
			retval = true;
			for(OWLEntity e : sup.getSignature()){
				List<OWLAxiom> l = mapping.get(e);
				if(l != null){
					for(OWLAxiom la : l){
						if(!module.contains(la)){
							process.add(la);
						}
					}
				}
			}
		}
		return retval;
	}
}
