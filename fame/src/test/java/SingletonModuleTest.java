/**
 * Created by spellmaker on 08.03.2016.
 */

import de.uniulm.in.ki.mbrenner.fame.ModuleSetup;
import de.uniulm.in.ki.mbrenner.fame.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
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

@RunWith(Parameterized.class)
public class SingletonModuleTest {
    private File file;

    public SingletonModuleTest(File f){
        this.file = f;
    }

    private File modulePath(OWLEntity e){
        Path tdir = Paths.get(ModuleSetup.moduleDirectory).resolve(file.getName());
        File f = tdir.resolve(ModuleSetup.sanitizeFilename(e.toString())).toFile();
        return f;
    }

    private File modulePathEq(OWLEntity e){
        Path tdir = Paths.get(ModuleSetup.moduleDirectory).resolve(file.getName());
        File f = tdir.resolve(ModuleSetup.sanitizeFilename(e.toString()) + "_eq").toFile();
        return f;
    }

    private OWLOntology getOntology() throws OWLOntologyCreationException{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);
        return o;
    }

    @Test
    public void testModuleBMRBNormal() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(o, false);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractor(false, false)).extractModule(rs, signature));
            assertTrue(makeMessage("BMRB+NormalExtractor", diff, e), diff.modulesTempEqual());
        }
    }

    @Test
    public void testModuleBMRBNoDef() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(o, true);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), (new RBMExtractorNoDef(false)).extractModule(rs, signature));
            assertTrue(makeMessage("BMRB+NoDef", diff, e), diff.modulesTempEqual());
        }
    }

    @Test
    public void testModuleIncremental() throws OWLOntologyCreationException{
        OWLOntology o = getOntology();
        IncrementalExtractor ie = new IncrementalExtractor(o);
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
            if(!modulePath(e).exists()) continue;

            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            ModuleDiff diff = ModuleDiff.diff(modulePath(e), ie.extractModule(e).getOWLModule());
            assertTrue(makeMessage("Incremental", diff, e), diff.modulesTempEqual());
        }
    }

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

    @Test
    public void testEqModuleBMRB() throws OWLOntologyCreationException{
        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
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

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        Collection<Object[]> result = new LinkedList<>();
        Path modulePath = Paths.get(ModuleSetup.moduleDirectory);
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(ModuleSetup.ontologyDirectory))) {
            for (Path path : directoryStream) {
                Path tdir = modulePath.resolve(path.getFileName());
                OWLOntology o = null;
                OWLOntologyManager m = null;
                try {
                    m = OWLManager.createOWLOntologyManager();
                    OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                    loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                    o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(path.toFile()), loaderConfig);

                } catch (OWLOntologyCreationException exc) {
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                    continue;
                } catch (UnloadableImportException exc) {
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                    continue;
                }
                if(o != null){
                    Object[] tmp = new Object[1];
                    tmp[0] = path.toFile();
                    result.add(tmp);
                }
            }
        }
        catch(IOException e){
            fail("could not read all ontologies");
        }
        return result;
    }
}
