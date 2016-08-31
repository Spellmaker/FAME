package de.uniulm.in.ki.mbrenner.fame.abox.islands.snf;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Set;

/**
 * Created by spellmaker on 16.06.2016.
 */
public class ShallowNormalForm {
    ShallowAxiom shallowAxiom = new ShallowAxiom(this);
    ShallowClass shallowClass = new ShallowClass(this);
    ShallowProperty shallowProperty = new ShallowProperty(this);
    OWLDataFactory fact;

    public ShallowNormalForm(OWLDataFactory fact){
        this.fact = fact;
    }

    public Set<OWLClassExpression> getSNF(OWLAxiom axiom){
        //System.out.println("generating snf for " + axiom + "(" + OWLPrinter.getString(axiom) + ")");
        return axiom.accept(shallowAxiom);
    }
}
