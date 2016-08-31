package de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 17.06.2016.
 */
public class RoleHierarchyBuilder {
    public static Map<OWLPropertyExpression, Set<OWLPropertyExpression>> getHierarchy(OWLOntologyManager m, OWLOntology ontology, Set<OWLObjectPropertyExpression> roleNames){
        OWLOntology rbox = null;
        try {
            rbox = m.createOntology(ontology.getRBoxAxioms(Imports.INCLUDED));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(rbox);
        Map<OWLPropertyExpression, Set<OWLPropertyExpression>> result = new HashMap<>();
        for(OWLObjectPropertyExpression a : roleNames){
            Set<OWLPropertyExpression> s = new HashSet<>();
            for(OWLObjectPropertyExpression b : roleNames){
                if(reasoner.isEntailed(m.getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(a, b))){
                    s.add(b);
                }
            }
            if(!s.isEmpty()){
                result.put(a, s);
            }
        }

        return result;
    }

}
