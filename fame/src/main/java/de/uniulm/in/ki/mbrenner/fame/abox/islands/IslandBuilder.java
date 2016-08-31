package de.uniulm.in.ki.mbrenner.fame.abox.islands;

import de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure.InfoStructure;
import de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure.InfoStructureBuilder;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 17.06.2016.
 */
public class IslandBuilder {
    private InfoStructure infoStructure;
    private OWLOntologyManager m;
    private OWLOntology ontology;

    private Map<OWLIndividual, Set<OWLObjectPropertyAssertionAxiom>> indToPropSubj;
    private Map<OWLIndividual, Set<OWLObjectPropertyAssertionAxiom>> indToPropObj;
    private Map<OWLIndividual, Set<OWLClassAssertionAxiom>> indToClass;
    private Map<OWLClassExpression, Set<OWLClass>> subClasses;

    public IslandBuilder(OWLOntologyManager m, OWLOntology ontology){
        this.m = m;
        this.ontology = ontology;
        infoStructure = InfoStructureBuilder.build(ontology, m);

        indToPropSubj = new HashMap<>();
        indToPropObj = new HashMap<>();
        indToClass = new HashMap<>();

        for(OWLIndividual i : ontology.getIndividualsInSignature()) {
            Set<OWLClassAssertionAxiom> s = new HashSet<>();
            s.addAll(ontology.getClassAssertionAxioms(i));
            indToClass.put(i, s);
            Set<OWLObjectPropertyAssertionAxiom> s2 = new HashSet<>();
            Set<OWLObjectPropertyAssertionAxiom> s3 = new HashSet<>();
            for(OWLObjectPropertyAssertionAxiom ax : ontology.getObjectPropertyAssertionAxioms(i)){
                if(ax.getSubject().equals(i)) s2.add(ax);
                else s3.add(ax);
            }

            indToPropSubj.put(i, s2);
            indToPropObj.put(i, s3);
        }


        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
        subClasses = new HashMap<>();

        infoStructure.values().stream().
                filter(x -> x != InfoStructureBuilder.STAR).
                forEach(x -> x.
                        forEach(y -> subClasses.put(y, reasoner.getSubClasses(y, false).getFlattened())));
    }

    private OWLObjectPropertyExpression getInv(OWLObjectPropertyExpression prop){
        return m.getOWLDataFactory().getOWLObjectInverseOf(prop);
    }

    private int counter = 0;
    private OWLIndividual newIndividual(){
        return m.getOWLDataFactory().getOWLNamedIndividual(IRI.create("new" + counter++));
    }

    private OWLIndividual getOther(OWLObjectPropertyAssertionAxiom axiom, OWLIndividual i){
        return (i.equals(axiom.getSubject())) ? axiom.getObject() : axiom.getSubject();
    }

    private void oneLoop(OWLObjectPropertyAssertionAxiom propAssertion, Set<OWLAxiom> result, Set<OWLIndividual> seen, boolean direction){
        OWLIndividual a = direction ? propAssertion.getSubject() : propAssertion.getObject();
        OWLIndividual b = direction ? propAssertion.getObject() : propAssertion.getSubject();
        OWLObjectPropertyExpression r = propAssertion.getProperty();
        OWLObjectPropertyExpression rinv = getInv(r);

        if(infoStructure.get(r) == InfoStructureBuilder.STAR ||
                infoStructure.get(rinv) == InfoStructureBuilder.STAR){
            result.add(propAssertion);
            //System.out.println("continuing with " + b + "(other is " + a + ")");
            result.addAll(getIsland(b, seen));
        }
        Set<OWLClassExpression> set = infoStructure.get(r);
        Set<OWLClassExpression> setinv = infoStructure.get(rinv);
        if((set == null || test(set, a,  b)) && (setinv == null || test(setinv, b, a))){
            if(direction)
                result.add(m.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(r, a, newIndividual()));
            else
                result.add(m.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(r, newIndividual(), a));
        }
        else{
            result.add(propAssertion);
            //System.out.println("continuing with " + b + "(other is " + a + ")");
            result.addAll(getIsland(b, seen));
        }
    }

    public Set<OWLAxiom> getIsland(OWLIndividual a, Set<OWLIndividual> seen){
        if(seen.contains(a)) return Collections.emptySet();
        seen.add(a);


        Set<OWLAxiom> result = new HashSet<>();
        result.addAll(indToClass.get(a));
        for(OWLObjectPropertyAssertionAxiom propAssertion : indToPropSubj.get(a)){
            oneLoop(propAssertion, result, seen, true);
        }
        for(OWLObjectPropertyAssertionAxiom propAssertion : indToPropObj.get(a)){
            oneLoop(propAssertion, result, seen, false);
        }

        return result;
    }

    public boolean test(Set<OWLClassExpression> classesToTest, OWLIndividual subject, OWLIndividual object){
        for(OWLClassExpression oce : classesToTest){
            if(oce.isBottomEntity()) continue;
            if(indToClass.get(object).stream().
                    filter(x -> subClasses.get(oce).contains(x)).findAny().isPresent()) continue;
            if(indToClass.get(subject).contains(m.getOWLDataFactory().
                    getOWLClassAssertionAxiom(m.getOWLDataFactory().getOWLObjectComplementOf(oce), subject)))
                continue;

            return false;
        }
        return true;
    }
}
