package de.uniulm.in.ki.mbrenner.fame.debug.incremental;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Created by spellmaker on 26.04.2016.
 */
public class HierarchyTools {
    public static void printHierarchy(Hierarchy hierarchy){
        EvaluationMain.out.println(hierarchy.size() + " classes in hierarchy");
        for(OWLClass c : hierarchy.keySet()){
            EvaluationMain.out.print("class " + c + ": ");
            for(OWLClass d : hierarchy.get(c)){
                EvaluationMain.out.print(d + " ");
            }
            EvaluationMain.out.println();
        }
    }

    public static boolean checkHierarchy(OWLOntology workingOntology, OWLReasoner reasoner, Hierarchy hierarchy){
        Set<OWLClass> c = new HashSet<>(workingOntology.getClassesInSignature());
        OWLDataFactory dFact = new OWLDataFactoryImpl();
        OWLClass top = dFact.getOWLThing();
        c.add(top);

        return checkHierarchy(workingOntology, reasoner, hierarchy, c);
    }

    public static Hierarchy initialHierarchy(OWLOntology ontology, OWLReasoner reasoner){
        OWLDataFactory fact = new OWLDataFactoryImpl();
        Set<OWLClass> allClasses = new HashSet<>(ontology.getClassesInSignature());
        allClasses.add(fact.getOWLNothing());
        allClasses.add(fact.getOWLThing());
        return initialHierarchy(reasoner, allClasses);
    }

    public static boolean checkHierarchy(OWLOntology workingOntology, OWLReasoner reasoner, Hierarchy hierarchy, Collection<OWLClass> classesToCheck){
        boolean failed = false;
        for(OWLClass c : classesToCheck){
            Set<OWLClass> compare = hierarchy.get(c);
            Set<OWLClass> correct = new HashSet<>();
            reasoner.getSuperClasses(c, false).forEach(x -> correct.addAll(x.getEntities()));
            correct.addAll(reasoner.getEquivalentClasses(c).getEntities());
            correct.remove(c);

            for(OWLClass d : correct){
                if(!compare.contains(d)){
                    boolean cin = (new BottomModeRuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(c);
                    boolean din = (new BottomModeRuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(d);


                    EvaluationMain.out.println("does not contain subsumption " + c + " C " + d + "(" + cin + ", " + din + ")");
                    failed = true;
                }
            }
            for(OWLClass d : compare){
                if(!correct.contains(d)){
                    boolean cin = (new BottomModeRuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(c);
                    boolean din = (new BottomModeRuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(d);
                    EvaluationMain.out.println("contains additional subsumption " + c + " C " + d + "(" + cin + ", " + din + ")");
                    failed = true;
                }
            }
        }
        return !failed;
    }

    public static Hierarchy initialHierarchy(OWLReasoner reasoner, Collection<OWLClass> classesToCheck){
        Hierarchy result = new Hierarchy();
        for(OWLClass c : classesToCheck){
            Set<OWLClass> sup = new HashSet<>();
            NodeSet<OWLClass> s = reasoner.getSuperClasses(c, false);
            s.forEach(x -> sup.addAll(x.getEntities()));
            sup.addAll(reasoner.getEquivalentClasses(c).getEntities());
            sup.remove(c);
            result.put(c, sup);
        }
        return result;
    }
}
