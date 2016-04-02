package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.EqCorrectnessChecker;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleIO;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleSetup;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by spellmaker on 11.03.2016.
 */
public class PreparationWorker implements Runnable {
    private File file;
    private OWLEntity entity;
    private OWLOntology ontology;
    private OWLOntologyManager m;
    private RuleSet ruleSet;
    private Path outDir;
    private ExecutorService pool;
    private int count;

    private boolean done = false;

    public PreparationWorker(ExecutorService pool, File f, Path outDir, int count){
        this.pool = pool;
        this.file = f;
        this.outDir = outDir;
        this.count = count;
    }

    public PreparationWorker(OWLOntologyManager m, RuleSet rs, OWLOntology ontology, Path outDir, OWLEntity entity){
        this.ontology = ontology;
        this.outDir = outDir;
        this.entity = entity;
        this.m = m;
        this.ruleSet = rs;
    }

    private void makeChildWorkers(){
        EvaluationMain.out.println("spawning child workers for entities of ontology " + file);
        Path tdir = outDir.resolve(file.getName());
        ontology = null;
        m = null;
        try {
            m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);

        }
        catch(Exception exc){
            EvaluationMain.out.println("Could not load ontology '" + file + "': " + exc.getMessage());
            return;
        }

        if(!tdir.toFile().exists()){
            try {
                Files.createDirectory(tdir);
            } catch (IOException e) {
                EvaluationMain.out.println("error: could not create directory for modules");
                return;
            }
        }

        List<PreparationWorker> spawned = new ArrayList<>();
        ELRuleBuilder el = new ELRuleBuilder(true);
        RuleSet rs = el.buildRules(ontology);
        List<OWLEntity> seeds = new LinkedList<>();
        for(OWLEntity e : ontology.getSignature()) {
            if (!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) {
                continue;
            }
            seeds.add(e);
        }
        if(count < 0){
            for(OWLEntity e : seeds) {
                try {
                    PreparationWorker child = new PreparationWorker(m, (el.unknownObjects().isEmpty()) ? rs : null, ontology, tdir, e);
                    spawned.add(child);
                    pool.submit(child);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
        else{
            List<String> files = new LinkedList<>();
            try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tdir)) {
                for(Path p : directoryStream){
                    if(p.toString().endsWith("_eq")) continue;
                    files.add(p.getFileName().toString());
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

            for(int i = 0; i < files.size(); i++){
                if(!(tdir.resolve(files.get(i) + "_eq").toFile().exists())){
                    OWLEntity search = null;
                    for(OWLEntity e : ontology.getSignature()){
                        if(ModuleSetup.sanitizeFilename(e.toString()).equals(files.get(i))){
                            search = e;
                            break;
                        }
                    }
                    spawned.add(new PreparationWorker(m, (el.unknownObjects().isEmpty()) ? rs : null, ontology, tdir, search));
                }
            }

            Random rand = new Random();
            for(int i = files.size(); i < Math.min(count, seeds.size()); i++){
                OWLEntity e = seeds.get(rand.nextInt(seeds.size()));
                String mName = ModuleSetup.sanitizeFilename(e.toString());
                String eqName = mName + "_eq";
                while(files.contains(mName)){
                    e = seeds.get(rand.nextInt(seeds.size()));
                }

                try {
                    PreparationWorker child = new PreparationWorker(m, (el.unknownObjects().isEmpty()) ? rs : null, ontology, tdir, e);
                    spawned.add(child);
                }
                catch(Exception exc){
                    exc.printStackTrace();
                }
            }

            for(PreparationWorker pw : spawned) pool.submit(pw);
        }
        PreparationWorker base = new PreparationWorker(m, (el.unknownObjects().isEmpty()) ? rs : null, ontology, tdir, null);
        spawned.add(base);
        pool.submit(base);

        int size = spawned.size();
        EvaluationMain.out.println("spawned " + spawned.size() + " workers for ontology " + file);
        long cTime = System.currentTimeMillis();
        while(!spawned.isEmpty()){
            for(int i = 0; i < spawned.size(); i++){
                if(spawned.get(i).done){
                    spawned.remove(i);
                    i--;
                    long nTime = System.currentTimeMillis();
                    if(nTime - cTime > 2000) {
                        EvaluationMain.out.println("ontology " + file + " " + (1 + size - spawned.size()) + "/" + size);
                        cTime = nTime;
                    }
                }
            }
            /*System.out.println("remaining: " + spawned.size());
            if(spawned.size() == 1){
                System.out.println(spawned.get(0).entity);
            }*/
        }
        EvaluationMain.out.println("ontology " + file + " finished");
    }

    private void runChildWorker(){
        Path mName, eqName;
        if(entity != null) {
            mName = outDir.resolve(ModuleSetup.sanitizeFilename(entity.toString()));
            eqName = outDir.resolve(ModuleSetup.sanitizeFilename(entity.toString() + "_eq"));
        }
        else{
            mName = outDir.resolve("baseModule");
            eqName = outDir.resolve("baseModule_eq");
        }

        Set<OWLEntity> signature;
        if(entity != null) signature = Collections.singleton(entity);
        else signature = Collections.emptySet();
        Set<OWLAxiom> module = null;

        if(!mName.toFile().exists()){
            SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
            module = extractor.extract(signature);
            ModuleIO.writeModule(mName.toFile(), module);
        }
        if(ruleSet != null && !eqName.toFile().exists()){
            if(entity == null){
                ModuleIO.writeModule(eqName.toFile(), ruleSet.getBaseModule());
            }
            else{
                RBMExtractor rbme = new RBMExtractor(true, false);
                RBMExtractorNoDef ndef = new RBMExtractorNoDef(false);

                module = rbme.extractModule(ruleSet, signature);
                if (EqCorrectnessChecker.isCorrectEqModule(module, rbme, ontology, ndef.extractModule(ruleSet, signature)) != null) {
                    EvaluationMain.out.println("produced non-eq-local module for ontology " + ontology + " with entity " + entity);
                } else {
                    ModuleIO.writeModule(eqName.toFile(), module);
                }
            }
        }
        else{
            EvaluationMain.out.println("not doing definitions because rs " + ruleSet + " " + eqName.toFile().exists());
        }
    }

    public boolean isDone(){
        return done;
    }

    @Override
    public void run() {
        if(ontology == null)
            makeChildWorkers();
        else
            runChildWorker();
        done = true;
    }
}
