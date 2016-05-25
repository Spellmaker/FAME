import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.incremental.ModificationResult;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Tests incremental module extraction
 *
 * Created by spellmaker on 30.03.2016.
 */
public class IncrementalTest {
    private final boolean useDefined = false;
    private final String file = "C:\\Users\\spellmaker\\Downloads\\oboFoundry\\fix.owl";


    private final List<String> removalOrder = new LinkedList<>();
    private final List<Integer> removalCount = new LinkedList<>();
    private final String checkEntity = null;

    private void defineRemovals(){
        //removed axiom strings in the order of removals
        removalOrder.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_356> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_66>)");
        removalOrder.add("SubClassOf(<http://chen.moe/onto/med/DEFBI_Gene> ObjectIntersectionOf(<http://chen.moe/onto/med/Immuno_Protein_Gene> ObjectSomeValuesFrom(<http://chen.moe/onto/med/associated_With> <http://chen.moe/onto/med/Cystic_Fibrosis>)))");
        removalOrder.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_169> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_250>)");
        removalOrder.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_33> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_15>)");
        removalOrder.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_296> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_69>)");
        removalOrder.add("SubClassOf(ObjectSomeValuesFrom(<http://protege.stanford.edu/plugins/owl/protege#PAL-NAME> owl:Thing) <http://protege.stanford.edu/plugins/owl/protege#PAL-CONSTRAINT>)");

        //number of removed elements in each iteration
        removalCount.add(1);
        removalCount.add(0);
        removalCount.add(0);
        removalCount.add(0);
        //entity on which the error occurred
        //checkEntity = "<http://www.ifomis.org/bfo/1.0/snap#Function>";
    }

    /**
     * Tests the correctness of the naive incremental algorithm
     * @throws Exception If an error occurred
     */
    @Test
    public void testNaiveIncremental() throws Exception{
        test(true);
    }

    /**
     * Tests the correctness of the full incremental algorithm
     * @throws Exception If an error occurred
     */
    @Test
    public void testIncremental() throws Exception{
        test(false);
    }

    /**
     * Tests the correctness of the incremental classification
     * @throws Exception If an error occurred
     */
    @Test
    public void testModRes() throws Exception{
        if(useDefined) defineRemovals();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(file));

        List<OWLAxiom> allAxioms = new ArrayList<>(ontology.getLogicalAxioms());
        Random r = new Random();
        int rsize = 1;
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
        removed.forEach(System.out::println);

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new RuleBuilder()).buildRules(workingOntology);

        //System.out.println("partition is: ");
        //System.out.println("remaining:");
        //for(OWLAxiom a : allAxioms) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);
        //System.out.println("removed:");
        //for(OWLAxiom a : removed) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);

        Map<OWLEntity, Set<OWLAxiom>> bmrbModules = new HashMap<>();

        IncrementalExtractor ie_naive = new IncrementalExtractor(new HashSet<>(allAxioms));
        IncrementalExtractor ie_incr = new IncrementalExtractor(new HashSet<>(allAxioms));

        for(OWLEntity e : workingOntology.getClassesInSignature()){
            IncrementalModule im1 = ie_naive.extractModule(e);
            IncrementalModule im2 = ie_incr.extractModule(e);
            Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));
            bmrbModules.put(e, mod);
        }
        System.out.println("> done with normal extractions");

        //pick a number of axioms to remove from the ontology, remove them and add new axioms
        for(int i = 0; i < 100; i++){
            System.out.println("iteration: " + i);

            List<OWLAxiom> nremoved = new ArrayList<>();
            if(!useDefined) {
                int rem = r.nextInt(1);
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
            System.out.println("naive");
            ModificationResult modRes_naive = ie_naive.modifyOntologyNaive(new HashSet<>(removed), new HashSet<>(nremoved));
            System.out.println("incr");
            ModificationResult modRes_incr = ie_incr.modifyOntology(new HashSet<>(removed), new HashSet<>(nremoved));

            removed = nremoved;


            rs = (new RuleBuilder()).buildRules(workingOntology);
            //check base modules

            Map<OWLEntity, Set<OWLAxiom>> newModules = new HashMap<>();

            for(OWLEntity e : workingOntology.getClassesInSignature()){
                if(useDefined && checkEntity != null && !e.toString().equals(checkEntity)) continue;
                newModules.put(e, (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e)));
            }

            System.out.println("naive:");
            long[] res_naive = compare(bmrbModules, newModules, modRes_naive, ie_naive);
            System.out.println("incr:");
            long[] res_incr = compare(bmrbModules, newModules, modRes_incr, ie_incr);

            System.out.println("missing add naiv: " + res_naive[0] + " incr: " + res_incr[0]);
            System.out.println("additional add naiv: " + res_naive[1] + " incr: " + res_incr[1]);
            System.out.println("missing del naiv: " + res_naive[2] + " incr: " + res_incr[2]);
            System.out.println("additional del naiv: " + res_naive[3] + " incr: " + res_incr[3]);

            assertTrue("different failures", res_naive[0]==res_incr[0] && res_naive[1]==res_incr[1] && res_naive[2]==res_incr[2] && res_naive[3]==res_incr[3]);

            bmrbModules = newModules;
        }
    }

    private void test(boolean naive) throws Exception{
        if(useDefined) defineRemovals();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(file));

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
        removed.forEach(System.out::println);

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new RuleBuilder()).buildRules(workingOntology);

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
            ModificationResult modRes;
            if(naive)
                modRes = ie.modifyOntologyNaive(new HashSet<>(removed), new HashSet<>(nremoved));
            else
                modRes = ie.modifyOntology(new HashSet<>(removed), new HashSet<>(nremoved));

            removed = nremoved;


            rs = (new RuleBuilder()).buildRules(workingOntology);
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
                if(useDefined && checkEntity != null && !e.toString().equals(checkEntity)) continue;
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
                if(!mod.equals(im.getOWLModule())){
                    im.getOWLModule().forEach(System.out::println);
                }
                assertTrue("failed after " + i + " iterations: expected size " + mod.size() + " got " + im.size() + " for entity " + e, mod.equals(im.getOWLModule()));
            }
        }
    }

    private long[] compare(Map<OWLEntity, Set<OWLAxiom>> previousModules, Map<OWLEntity, Set<OWLAxiom>> currentModules, ModificationResult modRes, IncrementalExtractor ie){
        Set<OWLEntity> additions = modRes.additionAffected.stream().map(x -> (OWLEntity) ie.getObject(x.getBaseEntity())).collect(Collectors.toSet());
        Set<OWLEntity> deletions = modRes.additionAffected.stream().map(x -> (OWLEntity) ie.getObject(x.getBaseEntity())).collect(Collectors.toSet());

        Set<OWLEntity> realAdditions = new HashSet<>();
        Set<OWLEntity> realDeletions = new HashSet<>();

        //check additions
        for(Map.Entry<OWLEntity, Set<OWLAxiom>> entry : currentModules.entrySet()){
            Set<OWLAxiom> prev = previousModules.get(entry.getKey());
            if(prev == null){
                realAdditions.add(entry.getKey());
                continue;
            }
            for(OWLAxiom a : entry.getValue()){
                if(!entry.getValue().contains(a)){
                    realAdditions.add(entry.getKey());
                    break;
                }
            }
        }
        //check deletions
        for(Map.Entry<OWLEntity, Set<OWLAxiom>> entry : previousModules.entrySet()){
            Set<OWLAxiom> curr = currentModules.get(entry.getKey());
            if(curr == null){
                realDeletions.add(entry.getKey());
                continue;
            }
            for(OWLAxiom a : entry.getValue()){
                if(!curr.contains(a)){
                    realDeletions.add(entry.getKey());
                    break;
                }
            }
        }

        //compare
        long[] res = new long[4];
        res[0] = realAdditions.stream().filter(x -> !additions.contains(x)).count();
        res[1] = additions.stream().filter(x -> !realAdditions.contains(x)).count();
        res[2] = realDeletions.stream().filter(x -> !deletions.contains(x)).count();
        res[3] = deletions.stream().filter(x -> !realDeletions.contains(x)).count();

        int err = 0;
        realAdditions.stream().filter(x -> !additions.contains(x)).forEach(y -> System.out.println("missing addition for entity " + y));
        additions.stream().filter(x -> !realAdditions.contains(x)).forEach(y -> System.out.println("additional addition for entity " + y));
        realDeletions.stream().filter(x -> !deletions.contains(x)).forEach(y -> System.out.println("missing deletion for entity " + y));
        deletions.stream().filter(x -> !realDeletions.contains(x)).forEach(y -> System.out.println("additional deletion for entity " + y));
        err += realAdditions.stream().filter(x -> !additions.contains(x)).count();
        err += additions.stream().filter(x -> !realAdditions.contains(x)).count();
        err += realDeletions.stream().filter(x -> !deletions.contains(x)).count();
        err += deletions.stream().filter(x -> !realDeletions.contains(x)).count();

        return res;
    }
}
