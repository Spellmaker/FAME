package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Spellmaker on 02.04.2016.
 */
public class HySRuleGenerationTest implements EvaluationCase{
    public static int iterations;

    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        iterations = Integer.parseInt(options.get(0));
        Path outDir = null;
        if(options.size() >= 2){
            outDir = Paths.get(options.get(1));
        }
        int task = 0;
        List<String> lines = new LinkedList<>();
        int sizecount = 0;
        int errors = 0;
        String header = "file;axioms;logical axioms;base module;hys";
        lines.add(header);
        for(File f : ontologies){
            EvaluationMain.out.println("Working on task " + task++ + " of " + ontologies.size());
            try{
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
                EvaluationMain.out.println("Ontology loaded");

                if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size || ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
                    EvaluationMain.out.println("Skipping ontology " + f + ": " + ontology.getLogicalAxiomCount() + " axioms vs " + EvaluationMain.max_size);
                    continue;
                }

                String line = ontology.getAxiomCount() + ";" + ontology.getLogicalAxiomCount() + ";";
                try{
                    line += new RuleBuilder().buildRules(ontology).getBaseModule().size() + ";";
                }
                catch(Throwable t){
                    EvaluationMain.out.println("couldn't determine base module");
                    line += "-1;";
                }

                try {
                    long start = System.currentTimeMillis();
                    for(int i = 0; i < iterations; i++) {
                        HyS hys = new HyS(ontology, ModuleType.BOT);
                        hys.condense(SCCAlgorithm.TARJAN);
                        hys.condense(SCCAlgorithm.MREACHABILITY);
                    }
                    long end = System.currentTimeMillis();
                    line += (end - start);
                }
                catch(Throwable t){
                    EvaluationMain.out.println("HyS error: " + t);
                    errors++;
                    line += -1;
                }
                lines.add(line);
                if(outDir != null && !Files.exists(outDir.resolve(f.getName()))){
                    Files.write(outDir.resolve(f.getName()), Collections.singleton(line));
                }
                EvaluationMain.out.println(header);
                EvaluationMain.out.println(line);
            }
            catch(Throwable t){
                EvaluationMain.out.println("Error in task " + task + " for ontology " + f + ": " + t);
            }
        }
        EvaluationMain.out.println("finished " + ontologies.size() + " ontologies");
        EvaluationMain.out.println("skipped " + sizecount + " due to size reasons");
        EvaluationMain.out.println("error in " + errors + " ontologies");
        lines.forEach(x -> EvaluationMain.out.println(x));
    }
}
