package de.uniulm.in.ki.mbrenner.fame.util.printer;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Iterator;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class DefaultClassPrinter extends OWLClassExpressionVisitorAdapter implements OWLClassPrinter {
    private String result;

    @Override
    public String getString(OWLClassExpression oce) {
        oce.accept(this);
        return result;
    }

    @Override
    public void visit(OWLClass c){
        result = OWLPrinter.getString(c.getIRI());
    }

    @Override
    public void visit(OWLObjectIntersectionOf c){
        result = "";
        Iterator<OWLClassExpression> iter = c.getOperands().iterator();
        result += OWLPrinter.getString(iter.next());
        while(iter.hasNext()){
            result += OWLChars.and + OWLPrinter.getString(iter.next());
        }
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom c){
        result = OWLChars.exists + OWLPrinter.getString(c.getProperty()) + "." + OWLPrinter.getString(c.getFiller());
    }
}
