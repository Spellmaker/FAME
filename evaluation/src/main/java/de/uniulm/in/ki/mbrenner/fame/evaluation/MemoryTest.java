package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectExplorer;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by spellmaker on 01.04.2016.
 */
public class MemoryTest implements EvaluationCase {
    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        Path outPath = null;
        if(options.size() > 0) {
            outPath = Paths.get(options.get(0));
        }

        EvaluationMain.out.println("File;Filesize;RuleSet;Incremental;HyS;JCEL");
        int cnt = 0;
        for(File f : ontologies){
            EvaluationMain.out.println("Handling ontology " + ++cnt + " of " + ontologies.size());
            OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f));//"C:\\Users\\spellmaker\\Downloads\\oboFoundry\\taxrank.owl")), loaderConfig);

            if(ontology.getLogicalAxiomCount() > EvaluationMain.max_size){
                EvaluationMain.out.println("Skipping ontology " + f + " as it is too large");
                continue;
            }
            if(ontology.getLogicalAxiomCount() < EvaluationMain.min_size){
                EvaluationMain.out.println("Skipping ontology " + f + " as it is too small");
                continue;
            }


            ObjectExplorer.examineStatic = true;
            String line = "" + f +";";

            line += Files.size(f.toPath()) + ";";

            RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology);
            line += MemoryMeasurer.measureBytes(rs) + ";";
            IncrementalExtractor ie = new IncrementalExtractor(ontology);
            line += MemoryMeasurer.measureBytes(ontology) + ";";
            try{
                HyS h = new HyS(ontology, ModuleType.BOT);
                h.condense(SCCAlgorithm.TARJAN);
                h.condense(SCCAlgorithm.MREACHABILITY);
                line += MemoryMeasurer.measureBytes(h) +";";
            }
            catch(Throwable t){
                line += "-1;";
            }

            try{
                Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
                trans.getTranslationRepository().addAxiomEntities(ontology);
                Set<ComplexIntegerAxiom> transOntology = trans.translateSA(ontology.getAxioms());
                Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
                ModuleExtractor extr = new ModuleExtractor();

                long b = MemoryMeasurer.measureBytes(trans);
                b += MemoryMeasurer.measureBytes(normOntology);
                b += MemoryMeasurer.measureBytes(extr);
                line += b;
            }
            catch(Throwable t){
                line += "-1";
            }
            EvaluationMain.out.println(line);
            if(outPath != null){
                Files.write(outPath.resolve(f.getName()), Collections.singleton(line));
            }
        }
    }
}
