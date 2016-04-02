package de.uniulm.in.ki.mbrenner.fame.util;

import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.ELRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
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
 * Created by spellmaker on 08.03.2016.
 */
public class ModuleSetup {
    public static final String ontologyDirectory = "C:\\Users\\spellmaker\\SemanticWeb\\unitTest\\ontologies\\";
    public static final String moduleDirectory = "C:\\Users\\spellmaker\\SemanticWeb\\unitTest\\modules\\";

    public static void main(String[] args) {
        prepare();
    }

    public static void prepare(){
        Path modulePath = Paths.get(moduleDirectory);
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(ontologyDirectory))){
            for(Path path : directoryStream){
                Path tdir = modulePath.resolve(path.getFileName());

                OWLOntology o = null;
                OWLOntologyManager m = null;
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
                    BottomModeRuleBuilder el = new BottomModeRuleBuilder();
                    RuleSet rs = el.buildRules(o);
                    if (el.unknownObjects().isEmpty()) {
                        RBMExtractor rbme = new RBMExtractor(true, false);
                        Set<OWLAxiom> module = rbme.extractModule(rs, signature);
                        if (EqCorrectnessChecker.isCorrectEqModule(module, rbme, o) != null) {
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

    public static String sanitizeFilename(String input){
        return input.replace("<", "_").replace(">", "_").replace("#", "_").replace("/", "_").replace(":", "_");
    }

    public static double printPercent(int max, int curr){
        return ((double) curr * 100) / (double) max;
    }

    //public static String sanitizeFilename(String inputName) {
    //    return inputName.replaceAll("^[a-zA-Z0-9-_\\.]", "_");
    //}
}
