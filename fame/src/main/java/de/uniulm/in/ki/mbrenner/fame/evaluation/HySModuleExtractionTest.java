package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.tu_dresden.inf.lat.hys.graph_tools.Node;
import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Spellmaker on 02.04.2016.
 */
public class HySModuleExtractionTest implements EvaluationCase{
    public static int sig_size;
    public static int element_count;
    private Random rand;
    private List<OWLEntity> allEntities;

    private Set<OWLEntity> getRandSignature(){
        Set<OWLEntity> sign = new HashSet<>();
        while(sign.size() < sig_size)
            sign.add(allEntities.get(rand.nextInt(allEntities.size())));
        return sign;
    }


    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {sig_size = Integer.parseInt(options.get(0));
        rand = new Random();
        sig_size = Integer.parseInt(options.get(0));
        element_count = Integer.parseInt(options.get(1));
        EvaluationMain.out.println("Extracting " + element_count + " signatures of size " + sig_size);
        Path outDir = null;
        if(options.size() >= 3){
            outDir = Paths.get(options.get(2));
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


                allEntities = new ArrayList<>(ontology.getSignature().size());
                allEntities.addAll(ontology.getClassesInSignature());
                allEntities.addAll(ontology.getObjectPropertiesInSignature());
                EvaluationMain.out.println("Signature primed");

                String line = f + ";" + ontology.getAxiomCount() + ";" + ontology.getLogicalAxiomCount() + ";";
                try{
                    line += new BottomModeRuleBuilder().buildRules(ontology).getBaseModule().size() + ";";
                }
                catch(Throwable t){
                    EvaluationMain.out.println("couldn't determine base module");
                    line += "-1;";
                }

                //for safety reasons
                for(int i = 0; i < allEntities.size(); i++){
                    if(allEntities.get(i).isTopEntity()){
                        allEntities.remove(i);
                        i--;
                    }
                }
                List<Set<OWLEntity>> signatures = new LinkedList<>();

                for(int i = 0; i < element_count; i++){
                    Set<OWLEntity> c = getRandSignature();
                    signatures.add(c);
                }
                EvaluationMain.out.println("Signatures determined");
                try {
                    HyS hys = new HyS(ontology, ModuleType.BOT);
                    hys.condense(SCCAlgorithm.TARJAN);
                    hys.condense(SCCAlgorithm.MREACHABILITY);
                    EvaluationMain.out.println("HyS built");
                    long start = System.currentTimeMillis();
                    for (Set<OWLEntity> sign : signatures) {
                        Set<Node> allNodes = hys.getConnectedComponent(sign);
                        hys.getAxioms(allNodes);
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
