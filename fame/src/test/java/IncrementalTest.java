import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by spellmaker on 30.03.2016.
 */
public class IncrementalTest {
    @Test
    public void testIncremental() throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.medical));

        List<OWLAxiom> allAxioms = new ArrayList<>(ontology.getLogicalAxioms());
        Random r = new Random();

        int rsize = 2;
        List<OWLAxiom> removed = new ArrayList<>(rsize);

        for(int i = 0; i < rsize; i++){
            int pos = r.nextInt(allAxioms.size());
            removed.add(allAxioms.get(pos));
            allAxioms.remove(pos);
        }

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);

        //System.out.println("partition is: ");
        //System.out.println("remaining:");
        //for(OWLAxiom a : allAxioms) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);
        //System.out.println("removed:");
        //for(OWLAxiom a : removed) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);


        IncrementalExtractor ie = new IncrementalExtractor(new HashSet<>(allAxioms));
        for(OWLEntity e : workingOntology.getClassesInSignature()){
            IncrementalModule im = ie.extractModule(e);
            Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));
            assertTrue("initialization failed for entity " + e + " expected size " + mod.size() + " got " + im.size(), mod.equals(im.getOWLModule()));
        }
        System.out.println("done with normal extractions");

        //pick a number of axioms to remove from the ontology, remove them and add new axioms
        for(int i = 0; i < 10; i++){
            System.out.println("iteration: " + i);

            List<OWLAxiom> nremoved = new ArrayList<>();
            int rem = r.nextInt(2);
            System.out.println("removing " + rem + " additional axioms");
            for(int j = 0; j < rem; j++){
                int pos = r.nextInt(allAxioms.size());
                nremoved.add(allAxioms.get(pos));
                System.out.println("chose " + allAxioms.get(pos));
                allAxioms.remove(pos);
            }
            allAxioms.addAll(removed);
            workingOntology = m.createOntology(new HashSet<>(allAxioms));
            ie.modifyOntology(new HashSet<>(removed), new HashSet<>(nremoved));
            removed = nremoved;

            rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);
            for(OWLEntity e : workingOntology.getClassesInSignature()){
                IncrementalModule im = ie.getModule(e);
                Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));

                assertTrue("failed: expected size " + mod.size() + " got " + im.size() + " for entity " + e, mod.equals(im.getOWLModule()));
            }
        }
    }
}
