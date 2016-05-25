
import de.uniulm.in.ki.mbrenner.fame.ModuleSetup;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.*;
import de.uniulm.in.ki.mbrenner.fame.util.ModuleDiff;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the correctness of different extractors for modules generated for singleton signatures
 */
@RunWith(Parameterized.class)
public class SingletonModuleTest {
    private final File file;

    /**
     * Creates a new test
     * @param f The test ontology
     */
    public SingletonModuleTest(File f){
        this.file = f;
    }

    private File modulePath(OWLEntity e){
        Path tdir = Paths.get(ModuleSetup.moduleDirectory).resolve(file.getName());
        return tdir.resolve(ModuleSetup.sanitizeFilename(e.toString())).toFile();
    }

    private File modulePathEq(OWLEntity e){
        Path tdir = Paths.get(ModuleSetup.moduleDirectory).resolve(file.getName());
        return tdir.resolve(ModuleSetup.sanitizeFilename(e.toString()) + "_eq").toFile();
    }

    private OWLOntology getOntology() throws OWLOntologyCreationException{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        return m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);
    }

    /**
     * Tests the normal rule builder with the normal extractor for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleBMRBNormal() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        RuleSet rs = (new RuleBuilder()).buildRules(o, false);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractor(false, false)).extractModule(rs, signature));
            assertTrue(makeMessage("BMRB+NormalExtractor", diff, e), diff.modulesTempEqual());
        }
    }

    /**
     * Tests the normal rule builder together with the nodef extractor for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleBMRBNoDef() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        RuleSet rs = (new RuleBuilder()).buildRules(o, true);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractorNoDef(false)).extractModule(rs, signature));
            assertTrue(makeMessage("BMRB+NoDef", diff, e), diff.modulesTempEqual());
        }
    }

    /**
     * Tests the incremental module extractor for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleIncremental() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        IncrementalExtractor ie = new IncrementalExtractor(o);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            //Set<OWLEntity> signature = new HashSet<>();
            //signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), ie.extractModule(e).getOWLModule());
            assertTrue(makeMessage("Incremental", diff, e), diff.modulesTempEqual());
        }
    }

    /**
     * Tests the compressed module extraction chain for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleCR() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        CompressedRuleSet rs = (new CompressedRuleBuilder()).buildRules(o);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new CompressedExtractor()).extractModule(rs, signature));
            assertTrue(makeMessage("Compressed Rules", diff, e), diff.modulesTempEqual());
        }
    }

    /**
     * Tests the el rule builder together with the normal extractor for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleELNormal() throws OWLOntologyCreationException{
        ELRuleBuilder el = new ELRuleBuilder();
        OWLOntology o = getOntology();
        RuleSet rs = el.buildRules(o);
        if(el.unknownObjects().isEmpty()){
            for(OWLEntity e : o.getSignature()){
                if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
                if(!modulePath(e).exists()) continue;

                Set<OWLEntity> signature = new HashSet<>();
                signature.add(e);
                ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractor(false, false)).extractModule(rs, signature));
                assertTrue(makeMessage("EL+Normal", diff, e), diff.modulesTempEqual());
            }
        }
    }

    /**
     * Tests the el rule builder together with the noDef extractor for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testModuleELNoDef() throws OWLOntologyCreationException{
        ELRuleBuilder el = new ELRuleBuilder();
        OWLOntology o = getOntology();
        RuleSet rs = el.buildRules(o);
        if(el.unknownObjects().isEmpty()){
            for(OWLEntity e : o.getSignature()){
                if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
                if(!modulePath(e).exists()) continue;

                Set<OWLEntity> signature = new HashSet<>();
                signature.add(e);
                ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractorNoDef(false)).extractModule(rs, signature));
                assertTrue(makeMessage("EL+NoDef", diff, e), diff.modulesTempEqual());
            }
        }
    }

    /**
     * Tests the correctness of el rulebuilder  eq locality modules for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testEqModuleEL() throws OWLOntologyCreationException{
        ELRuleBuilder el = new ELRuleBuilder();
        OWLOntology o = getOntology();
        RuleSet rs = el.buildRules(o);
        if(el.unknownObjects().isEmpty()){
            for(OWLEntity e : o.getSignature()){
                if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
                if(!modulePathEq(e).exists()) continue;

                Set<OWLEntity> signature = new HashSet<>();
                signature.add(e);
                assertTrue("EL+def already made a mistake when creating the template for ontology " + file + " and signature " + signature, modulePathEq(e).exists());
                ModuleDiff diff = ModuleDiff.diff(modulePathEq(e), (new RBMExtractor(true, false)).extractModule(rs, signature));
                assertTrue(makeMessage("EL+Def", diff, e), diff.modulesTempEqual());
            }
        }
    }

    /**
     * Tests the correctness of normal rule builder eq locality modules for correctness
     * @throws OWLOntologyCreationException If the ontology could not be loaded
     */
    @Test
    public void testEqModuleBMRB() throws OWLOntologyCreationException{
        RuleBuilder bmrb = new RuleBuilder();
        OWLOntology o = getOntology();
        RuleSet rs = bmrb.buildRules(o);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePathEq(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            assertTrue("BMRB+Def already made a mistake when creating the template for ontology " + file + " and signature " + signature, modulePathEq(e).exists());
            ModuleDiff diff = ModuleDiff.diff(modulePathEq(e), (new RBMExtractor(true, false)).extractModule(rs, signature));
            assertTrue(makeMessage("BMRB+Def", diff, e), diff.modulesTempEqual());
        }
    }

    private String makeMessage(String toolName, ModuleDiff diff, OWLEntity e){
        String s = "Wrong Module for " + toolName + " in Ontology " + file.getName() + " with entity " + e + ":\nadditional axioms: ";
        for(String a : diff.module2add) s += a + ", ";
        s = s.substring(0, s.length() - 2);
        s += "\nmissing axioms: ";
        for(String a : diff.module1add){
            s+= a + ", ";
        }
        s = s.substring(0, s.length() - 2);
        s += "\n(Do not get hung up on temp nodes, they will be disregarded if everything else is correct)";
        return s;
    }

    /*@BeforeClass
    public static void prepareModules(){
        ModuleSetup.prepare();
    }*/

    /**
     * Collects test ontologies
     * @return The ontologies for the test cases
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        Collection<Object[]> result = new LinkedList<>();
        Path modulePath = Paths.get(ModuleSetup.moduleDirectory);
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(ModuleSetup.ontologyDirectory))) {
            for (Path path : directoryStream) {
                //Path tdir = modulePath.resolve(path.getFileName());
                OWLOntology o;
                OWLOntologyManager m;
                try {
                    m = OWLManager.createOWLOntologyManager();
                    OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                    loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                    o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(path.toFile()), loaderConfig);
                    if(o.isEmpty()) throw new OWLOntologyCreationException("ontology is empty");
                    Object[] tmp = new Object[1];
                    tmp[0] = path.toFile();
                    result.add(tmp);

                } catch (OWLOntologyCreationException | UnloadableImportException exc) {
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                }
            }
        }
        catch(IOException e){
            fail("could not read all ontologies");
        }
        return result;
    }
}
