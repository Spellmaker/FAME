package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrCorrectnessResult;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleDiff;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrCorrectnessWorker implements Callable<IncrCorrectnessResult> {
    public File f;
    private int change;
    private int iterations;
    private Random r;
    private int id;

    private void message(String msg){
        EvaluationMain.out.println("[Task " + id + "]: " + msg);
    }

    public IncrCorrectnessWorker(File f, int change, int iterations, int id){
        this.f = f;
        this.change = change;
        this.iterations = iterations;
        r = new Random();
        this.id = id;
    }

    private List<OWLAxiom> currentList;
    private Set<OWLAxiom> currentSet;
    private Set<OWLAxiom> removedAxioms;

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

    private ModuleDiff checkModules(OWLOntology o, IncrementalExtractor ie){
        RuleSet rs = (new RuleBuilder()).buildRules(o);
        int size = o.getSignature().size();
        message("Signature size is " + size);
        int cnt = 0;
        long time = System.currentTimeMillis();
        for(OWLEntity e : o.getSignature()){
            cnt++;
            if(!(e instanceof OWLObjectProperty) && !(e instanceof OWLClass)) continue;

            IncrementalModule im = ie.getModule(e);
            Set<OWLAxiom> mod = new RBMExtractorNoDef(false).extractModule(rs, Collections.singleton(e));
            ModuleDiff diff = ModuleDiff.diff(im.getOWLModule(), mod);
            if(!diff.modulesEqual()) return diff;

            long tnow = System.currentTimeMillis();
            if(tnow - time > 1000){
                message("Entity " + cnt + " of " + size);
                time = tnow;
            }
        }
        return null;
    }

    @Override
    public IncrCorrectnessResult call() throws Exception {
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
        message("Incremental extractor initialized");
        ModuleDiff res = checkModules(workingOntology, ie);
        if(res != null){
            return new IncrCorrectnessResult(res, -1, ie.getIncrCases());
        }
        message("Initial modules are correct");

        for(int i = 0; i < iterations; i++){
            m.removeOntology(workingOntology);
            message("Performing iteration " + i + " of " + iterations);
            Set<OWLAxiom> add = removedAxioms;
            choseNew();
            workingOntology = m.createOntology(currentSet);
            ie.modifyOntology(add, removedAxioms);

            res = checkModules(workingOntology, ie);
            if(res != null){
                return new IncrCorrectnessResult(res, i, ie.getIncrCases());
            }
        }

        return new IncrCorrectnessResult(null, iterations, ie.getIncrCases());
    }
}
