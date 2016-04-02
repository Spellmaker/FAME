package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import objectexplorer.MemoryMeasurer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Created by spellmaker on 01.04.2016.
 */
public class MemoryTest implements EvaluationCase {
    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        EvaluationMain.out.println("File;Filesize;RuleSet;Incremental;HyS;JCEL");
        for(File f : ontologies){
            OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
            loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
            OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f));//"C:\\Users\\spellmaker\\Downloads\\oboFoundry\\taxrank.owl")), loaderConfig);

            String s = "" + f +";";

            s += Files.size(f.toPath()) + ";";

            RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology);
            s += MemoryMeasurer.measureBytes(rs) + ";";
            IncrementalExtractor ie = new IncrementalExtractor(ontology);
            s += MemoryMeasurer.measureBytes(ontology) + ";";
            try{
                HyS h = new HyS(ontology, ModuleType.BOT);
                h.condense(SCCAlgorithm.TARJAN);
                h.condense(SCCAlgorithm.MREACHABILITY);
                s += MemoryMeasurer.measureBytes(h) +";";
            }
            catch(Throwable t){
                s += "-1;";
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
                s += b;
            }
            catch(Throwable t){
                s += "-1";
            }
            EvaluationMain.out.println(s);
        }
    }
}
