package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.util.locality.SyntacticLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class NaiveIncrementalWorker implements Callable<IncrTimeResult> {
    public File f;
    private int change;
    private int iterations;
    private Random r;
    private int id;

    private void message(String msg){
        EvaluationMain.out.println("[Task " + id + "](NORMAL): " + msg);
    }

    public NaiveIncrementalWorker(File f, int change, int iterations, int id){
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
        RuleBuilder bmrb = new RuleBuilder();
        RBMExtractorNoDef fame = new RBMExtractorNoDef(false);
        RuleSet rs = bmrb.buildRules(workingOntology);

        SyntacticLocalityEvaluator locality = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
        Map<OWLEntity, Set<OWLAxiom>> modules = new HashMap<>();
        for(OWLClass c : workingOntology.getClassesInSignature()){
            modules.put(c, fame.extractModule(rs, Collections.singleton(c)));
        }

        message("initial modules extracted");

        long start = System.currentTimeMillis();
        for(int i = 0; i < iterations; i++){
            m.removeOntology(workingOntology);
            Set<OWLAxiom> add = removedAxioms;
            choseNew();
            workingOntology = m.createOntology(currentSet);
            rs = bmrb.buildRules(workingOntology); //TODO: Fix this by making rulesets a bit more incremental for better comparison
            for(OWLClass e : workingOntology.getClassesInSignature()){
                Set<OWLAxiom> cmodule = modules.get(e);
                if(cmodule == null){
                    modules.put(e, fame.extractModule(rs, Collections.singleton(e)));
                    continue;
                }
                boolean isDeletionAffected = false;
                for(OWLAxiom a : removedAxioms){
                    if(cmodule.contains(a)){
                        isDeletionAffected = true;
                        break;
                    }
                }
                if (isDeletionAffected) {
                    modules.put(e, fame.extractModule(rs, Collections.singleton(e)));
                }

                boolean isAdditionAffected = false;
                for(OWLAxiom a : add){
                    Set<OWLEntity> signature = new HashSet<>();
                    cmodule.forEach(x -> signature.addAll(x.getSignature()));
                    if(!locality.isLocal(a, signature)){
                        isAdditionAffected = true;
                        break;
                    }
                }
                if(isAdditionAffected){
                    modules.put(e, fame.extractModule(rs, Collections.singleton(e)));
                }
            }
        }
        long end = System.currentTimeMillis();
        return new IncrTimeResult(end - start);
    }
}
