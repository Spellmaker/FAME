package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class OWLPrinter {
    public static OWLClassPrinter classPrinter = new DefaultClassPrinter();
    public static OWLPropertyPrinter propertyPrinter = new DefaultPropertyPrinter();
    public static OWLAxiomPrinter axiomPrinter = new DefaultAxiomPrinter();
    public static OWLEntityPrinter entityPrinter = new DefaultEntityPrinter();
    public static IRIPrinter iriPrinter = new DefaultIRIPrinter();

    public static void config(OWLClassPrinter cp, OWLPropertyPrinter pp, OWLAxiomPrinter ap, IRIPrinter iri, OWLEntityPrinter ep){
        classPrinter = cp;
        propertyPrinter = pp;
        axiomPrinter = ap;
        iriPrinter = iri;
        entityPrinter = ep;
    }

    public static String getString(OWLClassExpression oce){
        return classPrinter.getString(oce);
    }

    public static String getString(OWLPropertyExpression p){
        return propertyPrinter.getString(p);
    }

    public static String getString(OWLAxiom a){
        return axiomPrinter.getString(a);
    }

    public static String getString(IRI iri){
        return iriPrinter.getString(iri);
    }

    public static String getString(OWLEntity entity){
        return entityPrinter.getString(entity);
    }

    public static String getString(OWLObject o){
        if(o instanceof OWLAxiom)
            return axiomPrinter.getString((OWLAxiom) o);
        else if(o instanceof OWLPropertyExpression)
            return propertyPrinter.getString((OWLPropertyExpression) o);
        else if(o instanceof OWLClassExpression)
            return classPrinter.getString((OWLClassExpression) o);
        else if(o instanceof IRI)
            return iriPrinter.getString((IRI) o);
        else if(o instanceof OWLEntity)
            return entityPrinter.getString((OWLEntity) o);
        else
            return "ERROR";
    }

    public static String getString(Set<OWLEntity> set){
        if(set.isEmpty()) return "[]";
        else{
            Iterator<OWLEntity> e = set.iterator();
            String s = "[" + getString(e.next());
            while(e.hasNext()) s += ", " + getString(e.next());
            s += "]";
            return s;
        }
    }

    public static void print(OWLObject o){
        System.out.print(getString(o));
    }

    public static void println(OWLObject o){
        System.out.println(getString(o));
    }
}
