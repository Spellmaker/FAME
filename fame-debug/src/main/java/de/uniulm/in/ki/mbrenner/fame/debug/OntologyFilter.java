package de.uniulm.in.ki.mbrenner.fame.debug;

import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.util.Misc;
import de.uniulm.in.ki.mbrenner.owlapiaddons.misc.ClassCounter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Set;

/**
 * Created by spellmaker on 25.03.2016.
 */
public class OntologyFilter {
    public static void main(String[] args) throws Exception{
        filterOntologies("C:\\Users\\spellmaker\\Downloads\\oboFoundry", "C:\\Users\\spellmaker\\Downloads\\oboFoundry\\filtered");
    }

    public static void filterOntologies(String sourceDir, String targetDir) throws Exception{
        filterOntologies(Misc.fileList(sourceDir), targetDir);
    }

    public static void filterOntologies(List<File> ontologies, String targetDir) throws Exception{
        Path tDir = Paths.get(targetDir);

        Path notLoading = tDir.resolve("NotLoading");
        Files.createDirectories(notLoading);
        Path notEL = tDir.resolve("NotEL");
        Files.createDirectories(notEL);
        Path finalStage = tDir.resolve("Final");
        Files.createDirectories(finalStage);
        Path notJCEL = tDir.resolve("JCEL");
        Files.createDirectories(notJCEL);
        Path notHYS = tDir.resolve("HYS");
        Files.createDirectories(notHYS);

        //try loading ontologies
        int cnt = 0;
        for(File f : ontologies){
            System.out.println("processing ontology " + ++cnt + " of " + ontologies.size());
            OWLOntology o = null;
            OWLOntologyManager m = null;
            try {
                m = OWLManager.createOWLOntologyManager();
                OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
            }
            catch(Throwable t){
                System.out.println("failed for ontology " + f + ": " + t + " stage=loading");
                if(!Files.exists(notLoading.resolve(f.getName())))
                Files.copy(f.toPath(), notLoading.resolve(f.getName()));
                continue;
            }
            try{
                ELRuleBuilder el = new ELRuleBuilder();
                el.buildRules(o);
                if(!el.unknownObjects().isEmpty()){
                    System.out.println("Non-EL stuff in ontology:");
                    ClassCounter cc = new ClassCounter();
                    cc.addAll(el.unknownObjects());
                    cc.forEach(x -> System.out.println(x));
                    throw new Exception("non-el ontology");
                }
            }
            catch(Throwable t){
                System.out.println("failed for ontology " + f + ": " + t + " stage=el");
                if(!Files.exists(notEL.resolve(f.getName())))
                Files.copy(f.toPath(), notEL.resolve(f.getName()));
                continue;
            }
            try{
                Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
                Set<ComplexIntegerAxiom> transOntology = trans.translateSA(o.getAxioms());
                Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
            }
            catch(Throwable t){
                System.out.println("failed for ontology " + f + ": " + t + " stage=jcel");
                if(!Files.exists(notJCEL.resolve(f.getName())))
                Files.copy(f.toPath(), notJCEL.resolve(f.getName()));
                continue;
            }

            try{
                HyS hys = new HyS(o, ModuleType.BOT);
                hys.condense(SCCAlgorithm.TARJAN);
                hys.condense(SCCAlgorithm.MREACHABILITY);
            }
            catch(Throwable t){
                System.out.println("failed for ontology " + f + ": " + t + " stage=hys");
                if(!Files.exists(notHYS.resolve(f.getName())))
                Files.copy(f.toPath(), notHYS.resolve(f.getName()));
                continue;
            }

            System.out.println("ontology " + f + " passed all tests so far");
            if(!Files.exists(finalStage.resolve(f.getName())))
            Files.copy(f.toPath(), finalStage.resolve(f.getName()));
        }
    }
}
