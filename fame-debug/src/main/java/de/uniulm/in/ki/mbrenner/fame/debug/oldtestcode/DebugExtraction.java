package de.uniulm.in.ki.mbrenner.fame.debug.oldtestcode;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.CompressedExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.*;
import de.uniulm.in.ki.mbrenner.fame.util.locality.SyntacticLocalityEvaluator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by spellmaker on 23.03.2016.
 */
public class DebugExtraction {
    public static void printCRuleSet(CompressedRuleSet crs){
        for(CompressedRule cr : crs){
            String s = cr.toString();
            for(String r : repl){
                s = s.replace(r, "");
            }
            System.out.println(s);
        }
    }

    public static void printRuleSet(RuleSet rs){
        for(Rule r : rs){
            String s = r.toDebugString(rs);
            for(String re : repl){
                s = s.replace(re, "");
            }
            System.out.println(s);
        }
    }
    public static String replace(Object s){
        String tmp = s.toString();
        for(String t : repl){
            tmp = tmp.replaceAll(t, "");
        }
        return tmp;
    }

    public static void print(Object s){
        System.out.println(replace(s));
    }
    public static void printSig(Set<OWLEntity> sig){
        if(sig.isEmpty()){
            System.out.println("[]");
        }
        else {
            System.out.print("[");
            String str = "";
            for (OWLEntity e : sig) {
                str += replace(e) + ", ";
            }
            System.out.print(str.substring(0, str.length() - 2));
            System.out.println("]");
        }
    }
    public static void printLocality(String evaluator, String other, SyntacticLocalityEvaluator local, OWLAxiom ax, Set<OWLEntity> signature, boolean misses){
        if(misses)
            System.out.println(evaluator + " misses " + replace(ax));
        else
            System.out.println(evaluator + " wrongly includes " + replace(ax));
        if (local.isLocal(ax, signature)) {
            System.out.println("And the axiom is local w.r.t " + other + " signature!");
            if(!misses) {
                System.out.println("Relevant Elements in signature: ");
                for (OWLEntity e : ax.getSignature()) {
                    if (signature.contains(e)) {
                        System.out.println("+" + e);
                    }
                }
            }
        } else {
            System.out.println("But the axiom is not local to " + other + " signature.");
            if(misses) {
                System.out.println("Relevant Elements in signature: ");
                for (OWLEntity e : ax.getSignature()) {
                    if (signature.contains(e)) {
                        System.out.println("+" + e);
                    }
                }
            }
        }
    }

    public static boolean hasAxiom(CompressedRuleSet crs2){
        for(OWLAxiom a : crs2.getBase()){
            if(a.toString().contains("genid2")){
                return true;
            }
        }
        return false;
    }


    private static List<String> repl = new LinkedList<>();
    private static OWLOntology o;
    public void test() throws Exception{
        String path = "C:\\Users\\spellmaker\\SemanticWeb\\unitTest\\ontologies\\testObjectPropertyProperties.owl";
        boolean printRules = false;
        boolean compare = true;
        boolean owlapi_debug = false;
        boolean printOntology = false;
        boolean printModules = false;
        boolean printSignature = false;

        File f = new File(path);
        repl.add("http://www.isi.edu/~pan/damltime/time.owl#");//o.getOntologyID().getOntologyIRI().get().toString() + "#"
        repl.add("http://daml.umbc.edu/ontologies/ittalks/event#");
        repl.add("http://www.biopax.org/release/biopax-level2.owl#");
        String axiomFinder = null;
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);
        System.out.println("Loaded ontology: ");
        if(printOntology) {
            for (OWLAxiom a : o.getAxioms()) {
                if (a instanceof OWLDeclarationAxiom) continue;
                print(a);
            }
        }
        System.out.println("ontology signature:");
        if(printSignature) {
            for (OWLEntity e : o.getSignature()) {
                System.out.println(e + " " + e.getClass());
            }
            for (OWLIndividual i : o.getIndividualsInSignature()) {
                System.out.println(i + " " + i.getClass());
            }
        }

        SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(m, o, ModuleType.BOT);
        SyntacticLocalityEvaluator eval = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
        System.out.println("> build normal rule set");
        RuleSet rs = (new RuleBuilder()).buildRules(o);
        System.out.println("> build compressed rule set");
        CompressedRuleSet crs = (new CompressedRuleBuilder()).buildRules(o);
        System.out.println("> rule sets finished");
        if(printRules) {
            System.out.println("rule sets:");
            System.out.println("fame:");
            printRuleSet(rs);
            System.out.println("cfame:");
            printCRuleSet(crs);
        }

        System.out.println("base modules:");
        System.out.println("fame:");
        for(OWLAxiom a : rs.getBaseModule()){
            if(!crs.getBase().contains(a)){
                System.out.println("fame additional axiom " + a);
            }
        }

        System.out.println("cfame:");
        for(OWLAxiom a : crs.getBase()){
            if(!rs.getBaseModule().contains(a)){
                System.out.println("cfame additional axiom " + a);
            }
        }
        System.out.println("starting loop");
        for(OWLEntity e : o.getSignature()){
            if(axiomFinder != null) {
                if (!e.toString().contains(axiomFinder)) continue;
                if (!(e instanceof OWLClass) && !(e instanceof OWLProperty)) {
                    continue;
                }
            }
            //System.out.println("found element " + e);
            Set<OWLEntity> signature = new HashSet<>();
            signature.add(e);
            System.out.println("------------------------Signature is: ");
            printSig(signature);
            Set<OWLAxiom> module = (new RBMExtractorNoDef(false)).extractModule(rs, signature);
            Set<OWLAxiom> cmodule = (new CompressedExtractor()).extractModule(crs, signature);
            Set<OWLAxiom> owlapi = extractor.extract(signature, 0, 0, null, owlapi_debug);//.extract(signature);
            boolean allcorrect = true;
            if(printModules) {
                System.out.println("fame:");
                for (OWLAxiom a : module) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    print(a);
                }
                System.out.println("cfame:");
                for (OWLAxiom a : cmodule) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    print(a);
                }
                System.out.println("owlapi:");
                for (OWLAxiom a : owlapi) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    print(a);
                }
            }
            Set<OWLEntity> m_sig = new HashSet<>();
            for(OWLAxiom a : module) m_sig.addAll(a.getSignature());
            Set<OWLEntity> o_sig = new HashSet<>();
            for(OWLAxiom a : owlapi) o_sig.addAll(a.getSignature());
            Set<OWLEntity> c_sig = new HashSet<>();
            for(OWLAxiom a : cmodule) c_sig.addAll(a.getSignature());

            System.out.println("owlapi signature: ");
            printSig(o_sig);
            System.out.println("fame signature: ");
            printSig(m_sig);
            System.out.println("cfame signature: ");
            printSig(c_sig);

            if(compare) {
                for (OWLAxiom a : owlapi) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    if (!module.contains(a)) {
                        printLocality("FAME", "FAME", eval, a, m_sig, true);
                        allcorrect = false;
                    }
                    if (!cmodule.contains(a)) {
                        printLocality("CFAME", "CFAME", eval, a, c_sig, true);
                        allcorrect = false;
                    }
                }

                List<OWLAxiom> tmp = new LinkedList<>();
                for (OWLAxiom a : module) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    if (!owlapi.contains(a)) {
                        printLocality("FAME", "owlapi", eval, a, o_sig, false);
                        allcorrect = false;
                        tmp.add(a);
                    }
                }

                if(!tmp.isEmpty()){
                    System.out.println("debugging code:");
                    System.out.println("List<String> filter = new LinkedList<>();");
                    for(OWLAxiom a : tmp) System.out.println("filter.add(\"" + a.toString() + "\");");
                }

                tmp.clear();
                for (OWLAxiom a : cmodule) {
                    if (a instanceof OWLDeclarationAxiom) continue;
                    if (!owlapi.contains(a)) {
                        printLocality("CFAME", "owlapi", eval, a, o_sig, false);
                        allcorrect = false;
                        tmp.add(a);
                    }
                }

                if(!tmp.isEmpty()){
                    System.out.println("debugging code:");
                    System.out.println("List<String> filter = new LinkedList<>();");
                    for(OWLAxiom a : tmp) System.out.println("filter.add(\"" + a.toString() + "\");");
                }

                if(!allcorrect) break;
                /*System.out.println("locality evaluator test");
                for(OWLAxiom a : cmodule){
                    if(replace(a).equals("SubClassOf(<publicationXref> DataMaxCardinality(1 <TITLE> rdfs:Literal))")){
                        System.out.println("found test axiom");
                        for(OWLEntity ent : a.getSignature()){
                            if(ent.toString().contains("publicationXref")){
                                Set<OWLEntity> sig = new HashSet<>();
                                sig.add(ent);
                                System.out.println("found test entity");
                                System.out.println(eval.isLocal(a, sig));
                            }
                        }
                    }
                }*/
            }
        }
    }
}
