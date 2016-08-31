package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.SimpleDefinitionLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.owlapiaddons.misc.ClassCounter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by spellmaker on 02.05.2016.
 */
public class DefinitionEvaluation implements EvaluationCase{
    @Override
    public String getParameter() {
        return "definition";
    }

    @Override
    public String getHelpLine() {
        return null;
    }

    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        for(File f : ontologies){
            EvaluationMain.out.println("processing " + f);
            OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

            ClassCounter cc = new ClassCounter();
            for(OWLAxiom ax : ontology.getAxioms()){
                cc.add(ax);
            }

            for(String s : cc){
                EvaluationMain.out.println(s);
            }

            long owlapi = 0;
            long def = 0;
            IncrementalExtractor ie = new IncrementalExtractor(ontology);

            for(OWLEntity e : ontology.getSignature()) {
                long s1 = ie.extractModuleStatic(Collections.singleton(e)).getOWLModule().stream().filter(x -> x instanceof OWLLogicalAxiom).count();
                long s2 = new SimpleDefinitionLocalityExtractor().extract(ontology.getAxioms(), Collections.singleton(e)).size();

                owlapi += s1;
                def += s2;
                EvaluationMain.out.println(s1 + " vs " + s2);
            }

            EvaluationMain.out.println("bottom locality: " + owlapi);
            EvaluationMain.out.println("definition locality: " + def);
        }
    }
}
