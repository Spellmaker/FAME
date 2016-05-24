package de.uniulm.in.ki.mbrenner.fame.evaluation.workers;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.CompressedRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.CompressedRuleSet;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 03.03.2016.
 */
public class ModuleCorrectnessWorker implements Callable<Object[]> {
    public File f;

    public ModuleCorrectnessWorker(File f){
        this.f = f;
    }

    private void addUnknown(Map<Class<?>, Integer> map, OWLObject o){
        Integer i = map.get(o.getClass());
        if(i == null){
            i = 0;
        }
        map.put(o.getClass(), i + 1);
    }

    @Override
    public Object[] call() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
        RuleBuilder bmrb = new RuleBuilder();
        CompressedRuleBuilder crb = new CompressedRuleBuilder();

        RuleSet rs = (bmrb).buildRules(o);
        CompressedRuleSet crs = (crb).buildRules(o);
        SyntacticLocalityModuleExtractor modextr = new SyntacticLocalityModuleExtractor(m, o, ModuleType.BOT);

        Object[] result = new Object[4];
        result[0] = true;
        result[1] = true;
        result[2] = true;
        result[3] = null;
        if(!bmrb.unknownObjects().isEmpty() || !crb.getUnknownObjects().isEmpty()){
            Map<Class<?>, Integer> unknown = new HashMap<>();
            bmrb.unknownObjects().forEach(x -> addUnknown(unknown, x));
            crb.getUnknownObjects().forEach(x -> addUnknown(unknown, x));

            result[3] = unknown;
            return result;
        }


        for(OWLEntity e : o.getSignature()){
            if(!(Boolean) result[0] && !(Boolean) result[1] && !(Boolean) result[2]) break;

            if(!(e instanceof OWLClass) && !(e instanceof OWLProperty)) continue;
            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);

            Set<OWLAxiom> owlapi = modextr.extract(signature);
            Set<OWLAxiom> fame = (new RBMExtractor(false, false)).extractModule(rs, signature);
            Set<OWLAxiom> famenodef = (new RBMExtractorNoDef(false)).extractModule(rs, signature);
            Set<OWLAxiom> famecompr = (new CompressedExtractor()).extractModule(crs, signature);

            boolean bfame = true; boolean bfamenodef = true; boolean bfamecompr = true;



            for(OWLAxiom a : owlapi){
                if(a instanceof OWLDeclarationAxiom) continue;

                if(!fame.contains(a)){
                    bfame = false;
                    //System.out.println("fame does not contain axiom " + a + " (" + f + ", " + e + ")");
                }
                if(!famenodef.contains(a)){
                    bfamenodef = false;
                    //System.out.println("famenodef does not contain axiom " + a + " (" + f + ", " + e + ")");
                }
                if(!famecompr.contains(a)){
                    bfamecompr = false;
                    //System.out.println("famecompr does not contain axiom " + a + " (" + f + ", " + e + ")");
                }
            }
            if(bfame){
                for(OWLAxiom a : fame){
                    if(a instanceof OWLDeclarationAxiom) continue;

                    if(!owlapi.contains(a)){
                        bfame = false;
                        //System.out.println("fame additionally contains contain axiom " + a + " (" + f + ", " + e + ")");
                        break;
                    }
                }
            }
            if(bfamenodef){
                for(OWLAxiom a : famenodef){
                    if(a instanceof OWLDeclarationAxiom) continue;
                    if(!owlapi.contains(a)){
                        bfamenodef = false;
                        //System.out.println("famenodef additionally contains contain axiom " + a + " (" + f + ", " + e + ")");
                        break;
                    }
                }
            }
            if(bfamecompr){
                for(OWLAxiom a : famecompr){
                    if(a instanceof OWLDeclarationAxiom) continue;
                    if(!owlapi.contains(a)){
                        bfamecompr = false;
                        //System.out.println("famecompr additionally contains contain axiom " + a + " (" + f + ", " + e + ")");
                        break;
                    }
                }
            }
            String print = "";
            if((Boolean) result[0] && !bfame){
                print += "Signature " + e + " is the first error for fame\n";
            }
            if((Boolean) result[1] && !bfamenodef){
                print += "Signature " + e + " is the first error for nodef\n";
            }
            if((Boolean) result[2] && !bfamecompr){
                print += "Signature " + e + " is the first error for compr\n";
            }
            if(!print.equals("")){
                print += "failure ontology: " + f;
                EvaluationMain.out.println(print);
            }

            result[0] = (Boolean) result[0] && bfame;
            result[1] = (Boolean) result[1] && bfamenodef;
            result[2] = (Boolean) result[2] && bfamecompr;

        }
        return result;
    }
}
