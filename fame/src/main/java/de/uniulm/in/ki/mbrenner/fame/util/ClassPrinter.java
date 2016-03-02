package de.uniulm.in.ki.mbrenner.fame.util;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/**
 * Utility class which can be used to print diverse OWL objects
 * @author spellmaker
 *
 */
public class ClassPrinter {
	/**
	 * Prints a complex OWL class
	 * @param oce The OWL class
	 * @return A text representation of the class
	 */
	public static String printClass(Object oce){
		String s = "";
		
		if(oce instanceof OWLObjectIntersectionOf){
			for(OWLClassExpression e : ((OWLObjectIntersectionOf)oce).getOperands()){
				s = s + ((s.equals(""))? "" : " ^ ") + printClass(e);
			}
		}
		else if(oce instanceof OWLObjectSomeValuesFrom){
			OWLObjectSomeValuesFrom ex = (OWLObjectSomeValuesFrom) oce;			
			s = "€" + printIRI(ex.getProperty().asOWLObjectProperty().getIRI().toString()) + "." + printClass(ex.getFiller());
		}
		else if(oce instanceof OWLClass){
			s = printIRI(((OWLClass)oce).getIRI().toString());
		}
		else if(oce instanceof OWLProperty){
			s = printIRI(((OWLProperty) oce).getIRI().toString());
		}
		else{
			System.out.println("Not sure what to do with " + oce.getClass());
			throw new IllegalArgumentException("");
		}
		return s;
	}
	
	/**
	 * Prints an OWL axiom
	 * @param ax An OWL axiom
	 * @return A text representation of the axiom
	 */
	public static String printAxiom(OWLAxiom ax){
		String s = "";
		if(ax instanceof OWLEquivalentClassesAxiom){
			Iterator<OWLClassExpression> iter = ((OWLEquivalentClassesAxiom) ax).getClassExpressions().iterator();
			
			s = printClass(iter.next()) + " = " + printClass(iter.next());
		}
		else if(ax instanceof OWLSubClassOfAxiom){
			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom) ax;
			s = printClass(sub.getSubClass()) + " [ " + printClass(sub.getSuperClass());
		}
		else if(ax instanceof OWLClassAssertionAxiom){
			OWLClassAssertionAxiom ass = (OWLClassAssertionAxiom) ax;
			s = "(" + printClass(ass.getClassExpression()) + ")(" + ass.getIndividual() + ")";
		}
		else if(ax instanceof OWLObjectPropertyAssertionAxiom){
			OWLObjectPropertyAssertionAxiom ass = (OWLObjectPropertyAssertionAxiom) ax;
			Set<OWLNamedIndividual> ind = ass.getIndividualsInSignature();
			Iterator<OWLNamedIndividual> iter = ind.iterator();
			s = "(" + printIRI(ass.getProperty().asOWLObjectProperty().getIRI().toString()) + ")(" + printIRI(iter.next().getIRI().toString()) + ", " + printIRI(iter.next().getIRI().toString()) + ")";
		}
		else if(ax instanceof OWLSubObjectPropertyOfAxiom){
			OWLSubObjectPropertyOfAxiom sub = (OWLSubObjectPropertyOfAxiom) ax;
			s = printClass(sub.getSubProperty()) + "[ " + printClass(sub.getSuperProperty());
		}
		else if(ax instanceof OWLDeclarationAxiom){
			OWLDeclarationAxiom decl = (OWLDeclarationAxiom) ax;
			s = "Declare " + printClass(decl.getEntity());
		}
		else if(ax instanceof OWLTransitiveObjectPropertyAxiom){
			OWLTransitiveObjectPropertyAxiom trans = (OWLTransitiveObjectPropertyAxiom) ax;
			s = "Trans(" + printClass(trans.getProperty()) + ")";
		}
		else{
			System.out.println("Not sure what to do with " + ax.getClass());
			s += ax.toString();
			//throw new IllegalArgumentException("");
		}
		return s;
	}
	
	/**
	 * Prints an IRI in a readable format
	 * @param iri An IRI
	 * @return A text representation of the IRI
	 */
	public static String printIRI(String iri){
		return iri.substring(iri.lastIndexOf("/") + 1);
	}
}
