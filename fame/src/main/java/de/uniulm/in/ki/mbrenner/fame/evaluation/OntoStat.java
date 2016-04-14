package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 14.04.2016.
 */
public class OntoStat implements EvaluationCase {
    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        Path outDir = Paths.get(options.get(0));
        List<String> lines = new LinkedList<>();
        lines.add("file;axioms;logical axioms;base module");
        int i = 0;
        for(File f : ontologies) {
            EvaluationMain.out.println("processing ontology " + ++i + " of " + ontologies.size());
            try {
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

                String s = f + ";" + ontology.getAxiomCount() + ";" + ontology.getLogicalAxiomCount() + ";" + new BottomModeRuleBuilder().buildRules(ontology).getBaseModule().size();
                lines.add(s);
            }
            catch(Exception e){
                EvaluationMain.out.println("an error occurred when evaluating ontology " + f);
            }
        }
        Files.write(outDir, lines);
    }
}
