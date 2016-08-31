package de.uniulm.in.ki.mbrenner.fame.localityframe.oneentitychecker;

import de.uniulm.in.ki.mbrenner.fame.localityframe.LocalityChecker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 13.06.2016.
 */
public class OneEntityChecker implements LocalityChecker {
    OECAxiom axiomVisitor = new OECAxiom(this);
    OECClass classVisitor = new OECClass(this);
    OECProperty propertyVisitor = new OECProperty(this);
    Set<OWLEntity> signature;
    Map<OWLObjectProperty, Set<OWLClassExpression>> roleMapping;

    public static OWLReasoner reasoner;

    @Override
    public boolean isLocal(OWLAxiom axiom, Set<OWLEntity> signature) {
        this.signature = signature;
        roleMapping = new HashMap<>();
        return axiom.accept(axiomVisitor);
    }

    public boolean isBot(OWLObjectProperty prop, OWLClassExpression expression){
        Set<OWLClassExpression> s = roleMapping.get(prop);
        if(s == null) return true;

        for(OWLClassExpression oce : s){
            if(reasoner.isEntailed(new OWLSubClassOfAxiomImpl(oce, expression, Collections.emptySet())) ||
                    reasoner.isEntailed(new OWLSubClassOfAxiomImpl(expression, oce, Collections.emptySet()))){
                return false;
            }
        }
        return true;
    }
}

enum OECValue{
    BOT,
    TOP,
    X,
    AT_LEAST_ONE,
    UNKNOWN
}