package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.genetic.GeneticExtractor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class GeneticEvaluation implements EvaluationCase{

    @Override
    public String getParameter() {
        return "genetic";
    }

    @Override
    public String getHelpLine() {
        return "genetic -o <runs> <generation_size> <mutation_multiplier> <mutation_changes>";
    }

    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        for(File f : ontologies){
            OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

            int runs = Integer.parseInt(options.get(0));
            for(OWLEntity e : ontology.getSignature()){
                EvaluationMain.out.println(e);
                GeneticExtractor gen = new GeneticExtractor(ontology, Collections.singleton(e), runs);
                gen.setParams(Integer.parseInt(options.get(1)), Integer.parseInt(options.get(2)), Integer.parseInt(options.get(3)), runs);
                gen.out = EvaluationMain.out;
                gen.extract();
            }
        }
    }
}
