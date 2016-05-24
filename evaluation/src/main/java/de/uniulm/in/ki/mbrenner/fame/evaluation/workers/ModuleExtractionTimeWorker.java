package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.evaluation.TestModuleExtraction;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Spellmaker on 02.04.2016.
 */
public class ModuleExtractionTimeWorker implements Callable<Long[]> {
    public File f;
    private int id;
    private List<OWLEntity> allEntities;
    private Random rand;

    public ModuleExtractionTimeWorker(File f, int id){
        this.f = f;
        this.id = id;
        this.rand = new Random();
    }

    @Override
    public Long[] call() throws Exception {
        Long[] res = new Long[8];
        OWLOntologyManager m; OWLOntology ontology;
        try{
            m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
        }
        catch(Throwable e){
            message("Could not load ontology " + f);
            return null;
        }

        if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size || ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
            message("Skipping ontology " + f + ": " + ontology.getLogicalAxiomCount() + " axioms vs " + EvaluationMain.max_size);
            return null;
        }

        res[0] = (long) ontology.getAxiomCount();
        res[1] = (long) ontology.getLogicalAxiomCount();
        try{
            res[2] = (long) new RuleBuilder().buildRules(ontology).getBaseModule().size();
        }
        catch(Throwable e){
            message("Error determining base module for ontology " + f);
            return null;
        }

        allEntities = new ArrayList<>(ontology.getSignature().size());
        allEntities.addAll(ontology.getClassesInSignature());
        allEntities.addAll(ontology.getObjectPropertiesInSignature());
        //for safety reasons
        for(int i = 0; i < allEntities.size(); i++){
            if(allEntities.get(i).isTopEntity()){
                allEntities.remove(i);
                i--;
            }
        }
        RuleSet rs = null;
        RuleBuilder bmrb = new RuleBuilder();
        try {
            rs = bmrb.buildRules(ontology);
        }
        catch(Throwable t){
            message("Could not generate ruleset");
        }

        IncrementalExtractor ie = null;
        try{
            ie = new IncrementalExtractor(ontology);
        }
        catch(Throwable e){
            message("Could not create incremental extractor");
        }

        Translator trans = null;
        Set<ComplexIntegerAxiom> transOntology;
        Set<NormalizedIntegerAxiom> normOntology = null;
        ModuleExtractor jcel = null;
        if(!TestModuleExtraction.skip_jcel) {
            try {
                trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
                trans.getTranslationRepository().addAxiomEntities(ontology);
                transOntology = trans.translateSA(ontology.getAxioms());
                normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
                jcel = new ModuleExtractor();
            } catch (Throwable t) {
                trans = null;
                normOntology = null;
                message("Could not create jcel");
            }
        }
        else{
            message("Skipped jcel");
        }


        //prepare Signatures
        List<Set<OWLEntity>> signatures = new LinkedList<>();
        List<Set<Integer>> jcelSignatureProps = new LinkedList<>();
        List<Set<Integer>> jcelSignatureClass = new LinkedList<>();

        for(int i = 0; i < TestModuleExtraction.element_count; i++){
            Set<OWLEntity> c = getRandSignature();
            signatures.add(c);
            if(jcel != null){
                Set<Integer> iClasses = new HashSet<>();
                Set<Integer> iProperties = new HashSet<>();
                jcelSignatureProps.add(iProperties);
                jcelSignatureClass.add(iClasses);

                for (OWLEntity e : c) {
                    if (e instanceof OWLClass) iClasses.add(trans.translateC((OWLClass) e).getId());
                    else iProperties.add(((IntegerObjectProperty) trans.translateOPE((OWLObjectProperty) e)).getId());
                }
            }
        }
        message("initialization complete");

        long start; long end;

        //Bmrb
        try{
            RBMExtractorNoDef nodef = new RBMExtractorNoDef(false);
            start = System.currentTimeMillis();
            for(Set<OWLEntity> sig : signatures){
                nodef.extractModule(rs, sig);
            }
            end = System.currentTimeMillis();
            res[3] = end - start;
            message("BMRB Finished");
        }
        catch(Throwable t){
            res[3] = -1L;
            message("BMRB had errors: " + t);
        }
        //BMRB+Def
        try{
            RBMExtractor fame = new RBMExtractor(true, false);
            start = System.currentTimeMillis();
            for(Set<OWLEntity> sig : signatures){
                fame.extractModule(rs, sig);
            }
            end = System.currentTimeMillis();
            res[4] = end - start;
            message("BMRB+Def Finished");
        }
        catch(Throwable t){
            res[4] = -1L;
            message("BMRB+Def had errors");
        }
        //OWLAPI
        try {
            SyntacticLocalityModuleExtractor synt = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
            start = System.currentTimeMillis();
            for(Set<OWLEntity> sig : signatures){
                synt.extract(sig);
            }
            end = System.currentTimeMillis();
            res[5] = end - start;
            message("OWLAPI finished");
        }
        catch(Throwable e){
            res[5] = -1L;
            message("OWLAPI had errors");
        }
        //JCEL
        try{
            Iterator<Set<Integer>> classIterator = jcelSignatureClass.iterator();
            start = System.currentTimeMillis();
            for(Set<Integer> props : jcelSignatureProps){
                jcel.extractModule(normOntology, classIterator.next(), props);
            }
            end = System.currentTimeMillis();
            res[6] = end - start;
            message("JCEL finished");
        }
        catch(Throwable e){
            res[6] = -1L;
            message("JCEL had errors");
        }

        //Incr
        try{
            start = System.currentTimeMillis();
            for(Set<OWLEntity> sign : signatures){
                ie.extractModuleStatic(sign);
            }
            end = System.currentTimeMillis();
            res[7] = end - start;
        }
        catch(Throwable e){
            res[7] = -1L;
            message("INCR had errors");
        }
        return res;
    }

    private void message(String s){
        EvaluationMain.out.println("[Task " + id + "] " + s);
    }

    private Set<OWLEntity> getRandSignature(){
        Set<OWLEntity> sign = new HashSet<>();
        while(sign.size() < TestModuleExtraction.sig_size)
            sign.add(allEntities.get(rand.nextInt(allEntities.size())));
        return sign;
    }
}
