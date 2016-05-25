package de.uniulm.in.ki.mbrenner.fame;

import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.locality.EqCorrectnessChecker;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleIO;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Prepares test ontologies for the test cases
 * More specifically, it attempts to load ontologies and then extracts all modules for single elements and stores them
 * in a provided directory to avoid recomputation
 *
 * Created by spellmaker on 08.03.2016.
 */
public class ModuleSetup {
    /**
     * The directory of the ontologies
     */
    public static final String ontologyDirectory = "C:\\Users\\spellmaker\\SemanticWeb\\unitTest\\ontologies\\";
    /**
     * The module directory
     */
    public static final String moduleDirectory = "C:\\Users\\spellmaker\\SemanticWeb\\unitTest\\modules\\";

    /**
     * Program entry point
     * @param args Program parameters, ignored
     */
    public static void main(String[] args) {
        prepare();
    }

    /**
     * Prepares the ontologies for testing
     */
    public static void prepare(){
        Path modulePath = Paths.get(moduleDirectory);
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(ontologyDirectory))){
            for(Path path : directoryStream){
                Path tdir = modulePath.resolve(path.getFileName());

                OWLOntology o;
                OWLOntologyManager m;
                try {
                    m = OWLManager.createOWLOntologyManager();OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                    loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                    o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(path.toFile()), loaderConfig);

                }
                catch(OWLOntologyCreationException exc){
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                    continue;
                } catch(UnloadableImportException exc){
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                    continue;
                }

                if(!tdir.toFile().exists()) {
                    Files.createDirectory(tdir);
                    SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(m, o, ModuleType.BOT);
                    for (OWLEntity e : o.getSignature()) {
                        if (!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) {
                            continue;
                        }

                        Set<OWLEntity> signature = new HashSet<>();
                        signature.add(e);
                        Set<OWLAxiom> module = extractor.extract(signature);
                        System.out.println("filename: " + sanitizeFilename(e.toString()));
                        ModuleIO.writeModule(tdir.resolve(sanitizeFilename(e.toString())).toFile(), module);
                    }
                    Set<OWLAxiom> baseModule = extractor.extract(new HashSet<>());
                    ModuleIO.writeModule(tdir.resolve("baseModule").toFile(), baseModule);
                }
                int max = o.getSignature().size();
                int curr = 0;
                int last = -5;
                for(OWLEntity e : o.getSignature()){
                    double perc = printPercent(max, ++curr);
                    if(perc - 5 > last){
                        last += 5;
                        System.out.println(perc + "% done");
                    }
                    if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) {
                        continue;
                    }

                    Path eqFile = tdir.resolve(sanitizeFilename(e.toString()) + "_eq");
                    if(eqFile.toFile().exists()) continue;
                    System.out.println("eq file is " + eqFile);

                    Set<OWLEntity> signature = Collections.singleton(e);
                    RuleBuilder el = new RuleBuilder();
                    RuleSet rs = el.buildRules(o);
                    if (el.unknownObjects().isEmpty()) {
                        RBMExtractor rbme = new RBMExtractor(true, false);
                        Set<OWLAxiom> module = rbme.extractModule(rs, signature);
                        if (EqCorrectnessChecker.isCorrectEqModule(module, rbme.getActiveDefinitions(), o, new RBMExtractorNoDef(false).extractModule(rs, signature)) != null) {
                            System.out.println("produced non-eq-local module for ontology " + o);
                        } else {
                            ModuleIO.writeModule(eqFile.toFile(), module);
                        }
                    }
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Removes some illegal symbols from filenames
     * @param input The input name
     * @return A sanitized name
     */
    public static String sanitizeFilename(String input){
        return input.replace("<", "_").replace(">", "_").replace("#", "_").replace("/", "_").replace(":", "_");
    }

    /**
     * Converts the current progress to a percentage
     * @param max The maximum number
     * @param curr The current number
     * @return The percentage
     */
    public static double printPercent(int max, int curr){
        return ((double) curr * 100) / (double) max;
    }

    //public static String sanitizeFilename(String inputName) {
    //    return inputName.replaceAll("^[a-zA-Z0-9-_\\.]", "_");
    //}
}
