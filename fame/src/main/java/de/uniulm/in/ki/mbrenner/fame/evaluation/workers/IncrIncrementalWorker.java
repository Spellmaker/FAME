package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.locality.SyntacticLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrIncrementalWorker implements Callable<IncrTimeResult> {
    public File f;
    private int change;
    private int iterations;
    private Random r;
    private int id;

    private void message(String msg){
        EvaluationMain.out.println("[Task " + id + "](INCR): " + msg);
    }

    public IncrIncrementalWorker(File f, int change, int iterations, int id){
        this.f = f;
        this.change = change;
        this.iterations = iterations;
        r = new Random();
        this.id = id;
    }

    private List<OWLAxiom> currentList;
    private Set<OWLAxiom> currentSet;
    private Set<OWLAxiom> removedAxioms;
    private Map<OWLClass, List<OWLClass>> hierarchy;

    private void choseNew(){
        Set<OWLAxiom> nrem = new HashSet<>();
        for(int i = 0; i < change; i++){
            OWLAxiom c = currentList.get(r.nextInt(currentList.size()));
            currentSet.remove(c);
            currentList.remove(c);
            nrem.add(c);
        }
        currentSet.addAll(removedAxioms);
        currentList.addAll(removedAxioms);
        removedAxioms = nrem;
    }

    private void refetchHierarchy(ReasonerFactory factory, IncrementalExtractor ie, OWLOntology workingOntology, OWLOntologyManager m, Set<IncrementalModule> affected) throws OWLOntologyCreationException{
        for(IncrementalModule im : affected){
            OWLClass c = (OWLClass) ie.getObject(im.getBaseEntity());
            OWLOntology nOnto = m.createOntology(im.getOWLModule());
            NodeSet<OWLClass> sup = factory.createNonBufferingReasoner(nOnto).getSuperClasses(c, false);
            List<OWLClass> s = new LinkedList<>();
            sup.forEach(x -> s.addAll(x.getEntities()));
            hierarchy.put(c, s);
        }
    }

    @Override
    public IncrTimeResult call() throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
        message("Ontology loaded");
        //create initial
        currentList = new ArrayList<>(o.getLogicalAxioms());
        currentSet = new HashSet<>(currentList);
        removedAxioms = new HashSet<>();

        choseNew();
        message("Initial configuration chosen");
        OWLOntology workingOntology = m.createOntology(currentSet);
        IncrementalExtractor ie = new IncrementalExtractor(workingOntology);

        workingOntology.getClassesInSignature().forEach(ie::getModule);

        message("initial modules extracted");
        //initial classificaton
        ReasonerFactory rf = new ReasonerFactory();
        OWLReasoner reasoner = rf.createNonBufferingReasoner(workingOntology);
        hierarchy = new HashMap<>();
        for(OWLClass c : workingOntology.getClassesInSignature()){
            List<OWLClass> sup = new LinkedList<>();
            NodeSet<OWLClass> s = reasoner.getSuperClasses(c, false);
            if(!s.isSingleton()) EvaluationMain.out.println("this shouldnt happen... it is not a singleton");
            s.forEach(x -> sup.addAll(x.getEntities()));
            hierarchy.put(c, sup);
        }
        OWLDataFactory fact = new OWLDataFactoryImpl();
        OWLClass bottom = fact.getOWLNothing();
        OWLClass top = fact.getOWLThing();
        List<OWLClass> all = new LinkedList<>(workingOntology.getClassesInSignature());
        all.add(top);
        hierarchy.put(bottom, all);

        message("initial hierarchy determined");


        Set<OWLClass> oldSignature = workingOntology.getClassesInSignature();
        long start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++){
            m.removeOntology(workingOntology);
            Set<OWLAxiom> add = removedAxioms;
            choseNew();
            workingOntology = m.createOntology(currentSet);
            Set<IncrementalModule> affected = ie.modifyOntology(add, removedAxioms);
            //find new symbols
            Set<OWLClass> newSignature = workingOntology.getClassesInSignature();
            newSignature.removeAll(oldSignature);

            for(OWLClass c : newSignature){
                IncrementalModule im = ie.getModule(c);


            }
        }
        long end = System.currentTimeMillis();
        return new IncrTimeResult(end - start);
    }
}
