/**
 * Created by spellmaker on 08.03.2016.
 */
import de.uniulm.in.ki.mbrenner.fame.ModuleSetup;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class BaseModuleTest {
    private File file;

    public BaseModuleTest(File f){
        this.file = f;
    }

    private File baseModulePath(){
        Path tdir = Paths.get(ModuleSetup.moduleDirectory).resolve(file.getName());
        File f = tdir.resolve("baseModule").toFile();
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
    public void testBaseModuleRS() throws OWLOntologyCreationException{
        RuleSet rs = (new RuleBuilder()).buildRules(getOntology(), false);
        ModuleDiff diff = ModuleDiff.diff(baseModulePath(), rs.getBaseModule());
        assertTrue(makeMessage("Rule Set", diff), diff.modulesTempEqual());
    }

    @Test
    public void testBaseModuleRSNoDef() throws OWLOntologyCreationException{
        RuleSet rs = (new RuleBuilder()).buildRules(getOntology(), true);
        ModuleDiff diff = ModuleDiff.diff(baseModulePath(), rs.getBaseModule());
        assertTrue(makeMessage("Rule Set with NoDef", diff), diff.modulesTempEqual());
    }

    @Test
    public void testBaseModuleCRS() throws OWLOntologyCreationException{
        CompressedRuleSet crs = (new CompressedRuleBuilder()).buildRules(getOntology());
        ModuleDiff diff = ModuleDiff.diff(baseModulePath(), crs.getBase());
        assertTrue(makeMessage("Compressed Rule Set", diff), diff.modulesTempEqual());
    }

    @Test
    public void testBaseModuleEL() throws OWLOntologyCreationException{
        ELRuleBuilder el = new ELRuleBuilder();
        RuleSet rs = el.buildRules(getOntology());
        if(el.unknownObjects().isEmpty()){
            ModuleDiff diff = ModuleDiff.diff(baseModulePath(), rs.getBaseModule());
            assertTrue(makeMessage("EL Rule Builder", diff), diff.modulesTempEqual());
        }
    }

    @Test
    public void testBaseModuleIncremental() throws OWLOntologyCreationException{
        IncrementalExtractor ie = new IncrementalExtractor(getOntology());
        Set<OWLAxiom> mod = ie.getBaseModule().stream().map(x -> (OWLAxiom) ie.getObject(x)).collect(Collectors.toSet());
        ModuleDiff diff = ModuleDiff.diff(baseModulePath(), mod);
        assertTrue(makeMessage("Incremental", diff), diff.modulesTempEqual());
    }

    private String makeMessage(String toolName, ModuleDiff diff){
        String s = "Wrong Base Module for " + toolName + " and Ontology " + file.getName() + ":\nadditional axioms: ";
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

    //@BeforeClass
    //public static void prepareModules(){
    //    ModuleSetup.prepare();
    //}

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
