package de.uniulm.in.ki.mbrenner.fame.debug.oldtestcode;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.locality.SyntacticLocalityEvaluator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;

/**
 * Created by spellmaker on 23.03.2016.
 */
public class TestIncrementalPerformance {
    public static void test() throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        System.out.println("loading ontology");
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.contest1));//"C:\\Users\\spellmaker\\git\\rbme\\RuleBasedModuleExtraction\\addComplicated.owl"));//new File(OntologiePaths.medical));////new File("C:\\Users\\spellmaker\\git\\rbme\\RuleBasedModuleExtraction\\EL-GALEN.owl"));

        System.out.println("ontology:");
        for(OWLAxiom a : o.getLogicalAxioms()) System.out.println(a);
        int removeSize = o.getLogicalAxiomCount() / 2; int iterations = 100; int different = 10;

        long fametime = 0;
        long inctime = 0;

        Random r = new Random();
        for(int i = 0; i < different; i++){
            System.out.println("selecting new removal set");
            Set<OWLAxiom> rem = new HashSet<>();
            List<OWLAxiom> allAxioms = new ArrayList<>(o.getLogicalAxioms());

            for(int j = 0; j < removeSize; j++){
                rem.add(allAxioms.get(r.nextInt(allAxioms.size())));
            }
            System.out.println("determining remaining ontology");
            Set<OWLAxiom> remaining = new HashSet<>();
            for(OWLAxiom a : o.getAxioms()){
                if(rem.contains(a)) continue;

                remaining.add(a);
            }
            OWLOntology o2 = m.createOntology(remaining);

            //collect symbols
            Set<OWLEntity> seeds = new HashSet<>();
            seeds.addAll(o2.getClassesInSignature());
            seeds.addAll(o2.getObjectPropertiesInSignature());
            //extract all modules
            RuleSet rs = (new BottomModeRuleBuilder()).buildRules(o2);
            RBMExtractorNoDef ndef = new RBMExtractorNoDef(false);
            List<IncrementalExtractor> incrextractors = new ArrayList<>(iterations);
            for(int j = 0; j < iterations; j++){
                IncrementalExtractor current = new IncrementalExtractor(o2);
                incrextractors.add(current);
                for(OWLEntity e : seeds){
                    IncrementalModule im = current.extractModule(e);
                    Set<OWLAxiom> mod = ndef.extractModule(rs, Collections.singleton(e));
                }
            }
            List<Set<OWLAxiom>> modules = new ArrayList<>();
            for(OWLEntity e : seeds){
                Set<OWLAxiom> mod = ndef.extractModule(rs, Collections.singleton(e));
                modules.add(mod);
            }



            //reextract modules for Incremental case
            long start = System.currentTimeMillis();
            for(int j = 0; j < iterations; j++){
                //incrextractors.get(j).addAxioms(rem);
            }
            long end = System.currentTimeMillis();

            System.out.println("time: " + (end - start));
            inctime += end - start;

            RuleSet rs2 = (new BottomModeRuleBuilder()).buildRules(o);
            SyntacticLocalityEvaluator eval = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
            start = System.currentTimeMillis();
            for(int j = 0; j < iterations; j++) {
                for (Set<OWLAxiom> mod : modules) {
                    Set<OWLEntity> signature = new HashSet<>();
                    mod.forEach(x -> signature.addAll(x.getSignature()));
                    boolean allLocal = true;
                    for (OWLAxiom a : mod) {
                        if (!eval.isLocal(a, signature)) {
                            allLocal = false;
                        }
                    }

                    if (!allLocal) {
                        ndef.setBase(mod);
                        Set<OWLAxiom> nmod = ndef.extractModule(rs2, signature);
                    }
                }
                end = System.currentTimeMillis();
            }
            System.out.println("time: " + (end - start));
            fametime += end - start;
            //final correctness check
            /*for(OWLEntity e : seeds){
                IncrementalModule im = incremental.getModule(e);
                Set<OWLAxiom> mod = ndef.extractModule(rs2, Collections.singleton(e));

                boolean allOk = true;
                for(OWLAxiom a : im.getOWLModule()){
                    if(!mod.contains(a)){
                        allOk = false;
                        System.out.println("additional axiom " + a);
                    }
                }

                for(OWLAxiom a : mod){
                    if(!im.getOWLModule().contains(a)){
                        allOk = false;
                        System.out.println("missing axiom " + a);
                    }
                }

                if(!allOk){
                    System.out.println("module should be ");
                    for(OWLAxiom a : mod) System.out.println(a);
                    System.out.println("module is: ");
                    for(OWLAxiom a : im.getOWLModule()) System.out.println(a);

                    System.out.println("error: wrong final module for entity " + e + " and removal set " + rem);
                    System.exit(0);
                }
            }*/
        }
        System.out.println("total time incremental: " + inctime);
        System.out.println("total time fame: " + fametime);

        System.exit(0);
    }
}
