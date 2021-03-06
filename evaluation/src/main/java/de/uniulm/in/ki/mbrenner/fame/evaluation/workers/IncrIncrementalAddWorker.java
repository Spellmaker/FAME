package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.ModificationResult;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrIncrementalAddWorker implements Callable<IncrTimeResult> {
    public File f;
    private int change;
    private int iterations;
    private Random r;
    private int id;
    private boolean naive;
    private boolean half;

    private void message(String msg){

        EvaluationMain.out.println("[Task " + id + "](" + (naive ? "NAIV" : "INCR") + "): " + msg);
    }

    public IncrIncrementalAddWorker(File f, int change, int iterations, int id, boolean naive, boolean half){
        this.f = f;
        this.change = change;
        this.iterations = iterations;
        r = new Random();
        this.id = id;
        this.naive = naive;
        this.half = half;
    }

    private List<OWLAxiom> currentList;
    private Set<OWLAxiom> currentSet;
    private Set<OWLAxiom> removedAxioms;
    private Map<OWLClass, Set<OWLClass>> hierarchy;
    private OWLClass bottom;
    private OWLClass top;

    private List<String> debugRemove;
    private List<Integer> debugCount;
    private Iterator<String> debugIterStr;
    private Iterator<Integer> debugIterInt;

    private void initDebug(){
        debugRemove = new LinkedList<>();
        debugCount = new LinkedList<>();

        debugCount.add(1);
        debugCount.add(1);
        debugCount.add(1);
        debugCount.add(1);

        debugRemove.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_185> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_70>)");
        debugRemove.add("SubClassOf(<http://www.ifomis.org/bfo/1.0/snap#FiatObjectPart> <http://www.ifomis.org/bfo/1.0/snap#IndependentContinuant>)");
        debugRemove.add("SubClassOf(<http://obi.sourceforge.net/ontology/OBI.owl#OBI_138> <http://obi.sourceforge.net/ontology/OBI.owl#OBI_137>)");
        debugRemove.add("SubClassOf(<http://www.ifomis.org/bfo/1.0/snap#IndependentContinuant> <http://www.ifomis.org/bfo/1.0/snap#Continuant>)");

        debugIterStr = debugRemove.iterator();
        debugIterInt = debugCount.iterator();
    }

    private void choseDebug(){
        if(debugRemove == null) initDebug();

        Set<OWLAxiom> nrem = new HashSet<>();
        int cnt = debugIterInt.next();
        for(int i = 0; i < cnt; i++){
            String search = debugIterStr.next();
            List<OWLAxiom> remList = new LinkedList<>();
            OWLAxiom found = null;
            for(OWLAxiom a : currentList){
                if(a.toString().equals(search)){
                    found = a;
                }
            }
            currentList.remove(found);
            currentSet.remove(found);
            nrem.add(found);
        }
        currentSet.addAll(removedAxioms);
        currentList.addAll(removedAxioms);
        EvaluationMain.out.println("adding " + removedAxioms);
        removedAxioms = nrem;
        EvaluationMain.out.println("removing " + removedAxioms);
    }

    private List<OWLAxiom> chooseList;
    private Iterator<OWLAxiom> nextAxiom;

    private void initList(){
        chooseList = new LinkedList<>();
        for(int i = 0; i < change * (iterations + 1); i++){
            OWLAxiom c = currentList.get(r.nextInt(currentList.size()));
            currentSet.remove(c);
            currentList.remove(c);
            chooseList.add(c);
        }
        nextAxiom = chooseList.iterator();
    }


    private void choseNew(){
        for(int i = 0; i < change; i++){
            OWLAxiom n = nextAxiom.next();
            currentSet.add(n);
            currentList.add(n);
        }
    }

    private Map<OWLClass, Set<OWLClass>> hierarchyFromScratch(OWLReasoner reasoner, OWLOntology ontology){
        Map result = new HashMap<>();
        for(OWLClass c : ontology.getClassesInSignature()){
            addHierarchy(c, result, reasoner);
        }
        OWLDataFactory fact = new OWLDataFactoryImpl();
        OWLClass bottom = fact.getOWLNothing();
        OWLClass top = fact.getOWLThing();
        //same for bot and top
        addHierarchy(bottom, result, reasoner);
        addHierarchy(top, result, reasoner);

        return hierarchy;
    }

    private void addHierarchy(OWLClass clazz, Map<OWLClass, Set<OWLClass>> hierarchy, OWLReasoner reasoner){
        Set<OWLClass> sup = new HashSet<>();
        NodeSet<OWLClass> s = reasoner.getSuperClasses(clazz, false);
        s.forEach(x -> sup.addAll(x.getEntities()));
        sup.addAll(reasoner.getEquivalentClasses(clazz).getEntities());
        sup.remove(clazz);
        hierarchy.put(clazz, sup);
    }

    private void getNewHierarchy(OWLClass a, Set<OWLClass> addAffected, Set<OWLClass> delAffected, Map<OWLClass, Set<OWLClass>> nHierarchy, OWLReasoner reasoner, OWLOntology workingOntology){
        boolean addAff = addAffected.contains(a);
        boolean delAff = delAffected.contains(a);

        Set<OWLClass> supClasses = hierarchy.get(a);
        if(!addAff && !delAff){
            nHierarchy.put(a, supClasses);
            return;
        }

        Set<OWLClass> nSupClasses = new HashSet<>();
        if(!addAff){
            //only check previous super classes
            for(OWLClass b : supClasses){
                OWLAxiom ax = new OWLSubClassOfAxiomImpl(a, b, Collections.emptySet());
                if(reasoner.isEntailed(ax)) nSupClasses.add(b);
            }
            nHierarchy.put(a, nSupClasses);
            return;
        }

        for(OWLClass b : workingOntology.getClassesInSignature()) {
            if(b.equals(a)){
                continue;
            }
            if(supClasses.contains(b) && !delAff){
                nSupClasses.add(b);
                continue;
            }
            OWLAxiom ax = new OWLSubClassOfAxiomImpl(a, b, Collections.emptySet());
            if(reasoner.isEntailed(ax)){
                nSupClasses.add(b);
            }
        }
        if(supClasses.contains(bottom) && !delAff){
            nSupClasses.add(bottom);
        }
        else{
            OWLAxiom ax = new OWLSubClassOfAxiomImpl(a, bottom, Collections.emptySet());
            if(reasoner.isEntailed(ax)) nSupClasses.add(bottom);
        }
        /*if(supClasses.contains(top) && !delAff){
            nSupClasses.add(top);
        }
        else{
            OWLAxiom ax = new OWLSubClassOfAxiomImpl(a, top, Collections.emptySet());
            if(reasoner.isEntailed(ax)) nSupClasses.add(top);
        }*/
        nHierarchy.put(a, nSupClasses);
    }

    public static int basemodaffected = 0;

    @Override
    public IncrTimeResult call() throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

        message("Ontology loaded");
        message(o.getAxiomCount() + " axioms, " + o.getLogicalAxiomCount() + " logical axioms and " + o.getClassesInSignature().size() + " concepts in ontology");
        //create initial
        currentList = new ArrayList<>(o.getLogicalAxioms());
        currentSet = new HashSet<>(currentList);
        removedAxioms = new HashSet<>();

        OWLDataFactory fact = new OWLDataFactoryImpl();
        bottom = fact.getOWLNothing();
        top = fact.getOWLThing();
        //make initial configuration
        initList();
        choseNew();
        message("Initial configuration chosen");
        OWLOntology workingOntology = m.createOntology(currentSet);
        IncrementalExtractor ie = new IncrementalExtractor(workingOntology);
        //get modules for all symbols
        workingOntology.getClassesInSignature().forEach(ie::getModule);

        message("initial modules extracted");
        //initial classification
        ReasonerFactory rf = new ReasonerFactory();
        OWLReasoner reasoner = rf.createNonBufferingReasoner(workingOntology);
        hierarchy = new HashMap<>();
        for(OWLClass c : workingOntology.getClassesInSignature()){
            addHierarchy(c, hierarchy, reasoner);
        }
        //same for bot and top
        addHierarchy(bottom, hierarchy, reasoner);
        addHierarchy(top, hierarchy, reasoner);
        message("initial hierarchy determined");

        Set<OWLClass> oldSignature = workingOntology.getClassesInSignature();
        long start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++){
            //remove old ontology
            m.removeOntology(workingOntology);
            Set<OWLAxiom> add = removedAxioms;
            choseNew();
            workingOntology = m.createOntology(currentSet);

            ModificationResult modRes;
            if(half)
                modRes = ie.modifyOntologyHalfNaive(add, removedAxioms);
            else if(naive)
                modRes = ie.modifyOntologyNaive(add, removedAxioms);
            else
                modRes = ie.modifyOntology(add, removedAxioms);

            /*reasoner = rf.createNonBufferingReasoner(workingOntology);
            //find new symbols
            Set<OWLClass> newSignature = workingOntology.getClassesInSignature();
            newSignature.removeAll(oldSignature);
            oldSignature = workingOntology.getClassesInSignature();
            for(OWLClass c : newSignature){
                //superclasses of top
                Set<OWLClass> sups = new HashSet<>();
                sups.addAll(hierarchy.get(top));
                hierarchy.put(c, sups);
                //subclasses of bot
                for(Map.Entry<OWLClass, Set<OWLClass>> entry : hierarchy.entrySet()){
                    if(entry.getValue().contains(bottom)){
                        entry.getValue().add(c);
                    }
                }
            }

            Set<OWLClass> addAffected = new HashSet<>();
            for(IncrementalModule x : modRes.additionAffected){
                OWLClass e = (OWLClass) ie.getObject(x.getBaseEntity());
                addAffected.add(e);
            }

            Set<OWLClass> delAffected = modRes.deletionAffected.stream().map(x -> (OWLClass) ie.getObject(x.getBaseEntity())).collect(Collectors.toSet());

            Map<OWLClass, Set<OWLClass>> nHierarchy = new HashMap<>();
            for(OWLClass a : workingOntology.getClassesInSignature()){
                getNewHierarchy(a, addAffected, delAffected, nHierarchy, reasoner, workingOntology);
            }
            getNewHierarchy(top, addAffected, delAffected, nHierarchy, reasoner, workingOntology);

            hierarchy = nHierarchy;
            //determine actual hierarchy
            if(!compareHierarchy(workingOntology, reasoner)){
                EvaluationMain.out.println("error: wrong hierarchy after " + i + " step(s)");
                System.exit(0);
                return null;
            }*/
        }
        long end = System.currentTimeMillis();
        if(naive) basemodaffected = ie.basemodaffected;
        return new IncrTimeResult(end - start);
    }

    private void printHierarchy(Map<OWLClass, Set<OWLClass>> hierarchy){
        EvaluationMain.out.println(hierarchy.size() + " classes in hierarchy");
        for(OWLClass c : hierarchy.keySet()){
            EvaluationMain.out.print("class " + c + ": ");
            for(OWLClass d : hierarchy.get(c)){
                EvaluationMain.out.print(d + " ");
            }
            EvaluationMain.out.println();
        }
    }

    private boolean compareHierarchy(OWLOntology workingOntology, OWLReasoner reasoner){
        boolean failed = false;
        Set<OWLClass> check = new HashSet<>(workingOntology.getClassesInSignature());
        check.add(top);

        for(OWLClass c : check){
            Set<OWLClass> compare = hierarchy.get(c);
            Set<OWLClass> correct = new HashSet<>();
            reasoner.getSuperClasses(c, false).forEach(x -> correct.addAll(x.getEntities()));
            correct.addAll(reasoner.getEquivalentClasses(c).getEntities());
            correct.remove(c);

            for(OWLClass d : correct){
                if(!compare.contains(d)){
                    boolean cin = (new RuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(c);
                    boolean din = (new RuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(d);


                    EvaluationMain.out.println("does not contain subsumption " + c + " C " + d + "(" + cin + ", " + din + ")");
                    failed = true;
                }
            }
            for(OWLClass d : compare){
                if(!correct.contains(d)){
                    boolean cin = (new RuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(c);
                    boolean din = (new RuleBuilder()).buildRules(workingOntology).getBaseSignature().contains(d);
                    EvaluationMain.out.println("contains additional subsumption " + c + " C " + d + "(" + cin + ", " + din + ")");
                    failed = true;
                }
            }
        }
        return !failed;
    }
}
