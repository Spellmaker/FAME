import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by spellmaker on 30.03.2016.
 */
public class IncrementalTest {
    private boolean useDefined = false;


    private List<String> removalOrder = new LinkedList<>();
    private List<Integer> removalCount = new LinkedList<>();
    private String checkEntity = null;

    private void defineRemovals(){
        //removed axiom strings in the order of removals
        removalOrder.add("SubClassOf(<http://schema.org/Place> <http://purl.org/goodrelations/v1#Location>)");
        removalOrder.add("SubObjectPropertyOf(<http://purl.org/goodrelations/v1#height> <http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty>)");
        removalOrder.add("SubObjectPropertyOf(<http://purl.org/goodrelations/v1#hasMPN> <http://schema.org/productID>)");
        removalOrder.add("SubClassOf(<http://purl.org/goodrelations/v1#License> <http://purl.org/goodrelations/v1#BusinessFunction>)");
        removalOrder.add("SubClassOf(ObjectSomeValuesFrom(<http://purl.org/goodrelations/v1#predecessorOf> owl:Thing) <http://purl.org/goodrelations/v1#ProductOrServiceModel>)");
        //number of removed elements in each iteration
        removalCount.add(2);
        removalCount.add(0);
        removalCount.add(0);
        //entity on which the error occurred
        checkEntity = "<http://schema.org/Place>";
    }

    @Test
    public void testNaiveIncremental() throws Exception{
        if(useDefined) defineRemovals();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.contest1));

        List<OWLAxiom> allAxioms = new ArrayList<>(ontology.getLogicalAxioms());
        Random r = new Random();
        int rsize = 2;
        List<OWLAxiom> removed = new ArrayList<>(rsize);
        List<OWLAxiom> removalOrder = new ArrayList<>(this.removalOrder.size());
        if(useDefined){
            for(int i = 0; i < this.removalOrder.size(); i++) removalOrder.add(null);
            for(OWLAxiom a : ontology.getAxioms()){
                for(int i = 0; i < this.removalOrder.size(); i++){
                    if(a.toString().equals(this.removalOrder.get(i))){
                        removalOrder.set(i, a);
                        break;
                    }
                }
            }
        }

        Iterator<Integer> nextCount = removalCount.iterator();
        Iterator<OWLAxiom> nextAx = removalOrder.iterator();

        if(!useDefined) {
            for (int i = 0; i < rsize; i++) {
                int pos = r.nextInt(allAxioms.size());
                removed.add(allAxioms.get(pos));
                allAxioms.remove(pos);
            }
        }
        else{
            int j = nextCount.next();
            for(int i = 0; i < j; i++){
                OWLAxiom a = nextAx.next();
                removed.add(a);
                allAxioms.remove(a);
            }
        }
        System.out.println("removed axioms:");
        for(OWLAxiom a : removed){
            System.out.println(a);
        }

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);

        //System.out.println("partition is: ");
        //System.out.println("remaining:");
        //for(OWLAxiom a : allAxioms) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);
        //System.out.println("removed:");
        //for(OWLAxiom a : removed) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);


        IncrementalExtractor ie = new IncrementalExtractor(new HashSet<>(allAxioms));
        for(OWLEntity e : workingOntology.getClassesInSignature()){
            IncrementalModule im = ie.extractModule(e);
            Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));
            assertTrue("initialization failed for entity " + e + " expected size " + mod.size() + " got " + im.size(), mod.equals(im.getOWLModule()));
        }
        System.out.println("> done with normal extractions");

        //pick a number of axioms to remove from the ontology, remove them and add new axioms
        for(int i = 0; i < 100; i++){
            System.out.println("iteration: " + i);

            List<OWLAxiom> nremoved = new ArrayList<>();
            if(!useDefined) {
                int rem = r.nextInt(5);
                System.out.println("> removing " + rem + " additional axioms, adding back in " + removed.size() + " axioms");
                for (int j = 0; j < rem; j++) {
                    int pos = r.nextInt(allAxioms.size());
                    nremoved.add(allAxioms.get(pos));
                    System.out.println("chose " + allAxioms.get(pos));
                    allAxioms.remove(pos);
                }
            }
            else{
                int j = nextCount.next();
                System.out.println("> removing " + j + " additional axioms, adding back in " + removed.size() + " axioms");
                for(int k = 0; k < j; k++){
                    OWLAxiom ca = nextAx.next();
                    nremoved.add(ca);
                    System.out.println("chose " + ca);
                    allAxioms.remove(ca);
                }
            }
            allAxioms.addAll(removed);
            workingOntology = m.createOntology(new HashSet<>(allAxioms));
            ie.modifyOntologyNaive(new HashSet<>(removed), new HashSet<>(nremoved));
            removed = nremoved;


            rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);
            //check base modules

            Set<OWLAxiom> baseMod = rs.getBaseModule();
            Set<OWLAxiom> iBaseMod = ie.getModule(null).getOWLModule();

            if(!rs.getBaseModule().equals(ie.getModule(null).getOWLModule())){
                System.out.println("wrong base module");
                System.out.println("missing:");
                for(OWLAxiom a : baseMod){
                    if(iBaseMod.contains(a)) continue;
                    System.out.println(a);
                }
                System.out.println("additional:");
                for(OWLAxiom a : iBaseMod){
                    if(baseMod.contains(a)) continue;
                    System.out.println(a);
                    System.out.println("is in base set: " + ie.isInBaseSet(a));
                }
            }
            assertTrue("Base module does not match expected size " + baseMod.size() + " got " + iBaseMod.size(), baseMod.equals(iBaseMod));
            for(OWLEntity e : workingOntology.getClassesInSignature()){
                if(useDefined && !e.toString().equals(checkEntity)) continue;
                IncrementalModule im = ie.getModule(e);
                Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));

                if(!mod.equals(im.getOWLModule())) {
                    System.out.println("entity is " + e);
                    System.out.println("missing axioms:");
                    for(OWLAxiom a : mod){
                        if(im.getOWLModule().contains(a)) continue;

                        System.out.println(a);
                        if(iBaseMod.contains(a)){
                            System.out.println("axiom is part of the base module");
                        }
                        else{
                            System.out.println("axiom is not part of the base module");
                        }
                    }
                    System.out.println("additional axioms:");
                    for(OWLAxiom a : im.getOWLModule()){
                        if(mod.contains(a)) continue;

                        System.out.println(a);
                    }
                }
                assertTrue("failed after " + i + " iterations: expected size " + mod.size() + " got " + im.size() + " for entity " + e, mod.equals(im.getOWLModule()));
            }
        }
    }

    @Test
    public void testIncremental() throws Exception{
        if(useDefined) defineRemovals();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.contest1));

        List<OWLAxiom> allAxioms = new ArrayList<>(ontology.getLogicalAxioms());
        Random r = new Random();
        int rsize = 2;
        List<OWLAxiom> removed = new ArrayList<>(rsize);
        List<OWLAxiom> removalOrder = new ArrayList<>(this.removalOrder.size());
        if(useDefined){
            for(int i = 0; i < this.removalOrder.size(); i++) removalOrder.add(null);
            for(OWLAxiom a : ontology.getAxioms()){
                for(int i = 0; i < this.removalOrder.size(); i++){
                    if(a.toString().equals(this.removalOrder.get(i))){
                        removalOrder.set(i, a);
                        break;
                    }
                }
            }
        }

        Iterator<Integer> nextCount = removalCount.iterator();
        Iterator<OWLAxiom> nextAx = removalOrder.iterator();

        if(!useDefined) {
            for (int i = 0; i < rsize; i++) {
                int pos = r.nextInt(allAxioms.size());
                removed.add(allAxioms.get(pos));
                allAxioms.remove(pos);
            }
        }
        else{
            int j = nextCount.next();
            for(int i = 0; i < j; i++){
                OWLAxiom a = nextAx.next();
                removed.add(a);
                allAxioms.remove(a);
            }
        }
        System.out.println("removed axioms:");
        for(OWLAxiom a : removed){
            System.out.println(a);
        }

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);

        //System.out.println("partition is: ");
        //System.out.println("remaining:");
        //for(OWLAxiom a : allAxioms) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);
        //System.out.println("removed:");
        //for(OWLAxiom a : removed) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);


        IncrementalExtractor ie = new IncrementalExtractor(new HashSet<>(allAxioms));
        for(OWLEntity e : workingOntology.getClassesInSignature()){
            IncrementalModule im = ie.extractModule(e);
            Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));
            assertTrue("initialization failed for entity " + e + " expected size " + mod.size() + " got " + im.size(), mod.equals(im.getOWLModule()));
        }
        System.out.println("> done with normal extractions");

        //pick a number of axioms to remove from the ontology, remove them and add new axioms
        for(int i = 0; i < 100; i++){
            System.out.println("iteration: " + i);

            List<OWLAxiom> nremoved = new ArrayList<>();
            if(!useDefined) {
                int rem = r.nextInt(5);
                System.out.println("> removing " + rem + " additional axioms, adding back in " + removed.size() + " axioms");
                for (int j = 0; j < rem; j++) {
                    int pos = r.nextInt(allAxioms.size());
                    nremoved.add(allAxioms.get(pos));
                    System.out.println("chose " + allAxioms.get(pos));
                    allAxioms.remove(pos);
                }
            }
            else{
                int j = nextCount.next();
                System.out.println("> removing " + j + " additional axioms, adding back in " + removed.size() + " axioms");
                for(int k = 0; k < j; k++){
                    OWLAxiom ca = nextAx.next();
                    nremoved.add(ca);
                    System.out.println("chose " + ca);
                    allAxioms.remove(ca);
                }
            }
            allAxioms.addAll(removed);
            workingOntology = m.createOntology(new HashSet<>(allAxioms));
            ie.modifyOntology(new HashSet<>(removed), new HashSet<>(nremoved));
            removed = nremoved;


            rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);
            //check base modules

            Set<OWLAxiom> baseMod = rs.getBaseModule();
            Set<OWLAxiom> iBaseMod = ie.getModule(null).getOWLModule();

            if(!rs.getBaseModule().equals(ie.getModule(null).getOWLModule())){
                System.out.println("wrong base module");
                System.out.println("missing:");
                for(OWLAxiom a : baseMod){
                    if(iBaseMod.contains(a)) continue;
                    System.out.println(a);
                }
                System.out.println("additional:");
                for(OWLAxiom a : iBaseMod){
                    if(baseMod.contains(a)) continue;
                    System.out.println(a);
                    System.out.println("is in base set: " + ie.isInBaseSet(a));
                }
            }
            assertTrue("Base module does not match expected size " + baseMod.size() + " got " + iBaseMod.size(), baseMod.equals(iBaseMod));
            for(OWLEntity e : workingOntology.getClassesInSignature()){
                if(useDefined && !e.toString().equals(checkEntity)) continue;
                IncrementalModule im = ie.getModule(e);
                Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));

                if(!mod.equals(im.getOWLModule())) {
                    System.out.println("entity is " + e);
                    System.out.println("missing axioms:");
                    for(OWLAxiom a : mod){
                        if(im.getOWLModule().contains(a)) continue;

                        System.out.println(a);
                        if(iBaseMod.contains(a)){
                            System.out.println("axiom is part of the base module");
                        }
                        else{
                            System.out.println("axiom is not part of the base module");
                        }
                    }
                    System.out.println("additional axioms:");
                    for(OWLAxiom a : im.getOWLModule()){
                        if(mod.contains(a)) continue;

                        System.out.println(a);
                    }
                }
                assertTrue("failed after " + i + " iterations: expected size " + mod.size() + " got " + im.size() + " for entity " + e, mod.equals(im.getOWLModule()));
            }
        }
    }
}
