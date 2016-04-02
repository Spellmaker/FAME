package de.uniulm.in.ki.mbrenner.fame.util;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import de.tu_dresden.inf.lat.hys.graph_tools.SCCAlgorithm;
import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.datatype.IntegerObjectProperty;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.ReverseAxiomTranslator;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDefCopy;
import de.uniulm.in.ki.mbrenner.fame.incremental.v2.OWLDictionary;
import de.uniulm.in.ki.mbrenner.fame.incremental.v2.RuleStorage;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalModule;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.TreeBuilder;
import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NormalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.locality.EquivalenceLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.locality.SyntacticLocalityEvaluator;
import de.uniulm.in.ki.mbrenner.fame.related.HyS.HyS;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
import de.uniulm.in.ki.mbrenner.oremanager.OREManager;
import de.uniulm.in.ki.mbrenner.oremanager.filters.ORENoFilter;
import objectexplorer.MemoryMeasurer;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 03.03.2016.
 */
public class MainTest {
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
    private static boolean checkSkip(Collection<OWLObject> unknown){
        for(OWLObject o : unknown){
            if(o instanceof OWLAnnotationAxiom) continue;

            return true;
        }
        return false;
    }
    static List<String> repl = new LinkedList<>();
    {
        repl.add("http://chen.moe/onto/testObjectOneof#");
    }
    public static void main(String[] args) throws Exception{
        String f = "C:\\Users\\spellmaker\\Downloads\\oboFoundry\\ma.owl";

        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(f)));//"C:\\Users\\spellmaker\\Downloads\\oboFoundry\\taxrank.owl")), loaderConfig);

        System.out.println("file size: " + Files.size(Paths.get(f)));

        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(ontology);
        System.out.println("rule set size: " + MemoryMeasurer.measureBytes(rs));
        IncrementalExtractor ie = new IncrementalExtractor(ontology);
        System.out.println("incremental extractor size: " + MemoryMeasurer.measureBytes(ontology));
        try{
            HyS h = new HyS(ontology, ModuleType.BOT);
            h.condense(SCCAlgorithm.TARJAN);
            h.condense(SCCAlgorithm.MREACHABILITY);
            System.out.println("hys size: " + MemoryMeasurer.measureBytes(h));
        }
        catch(Throwable t){
            System.out.println("hyserror");
        }

        try{
            Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
            trans.getTranslationRepository().addAxiomEntities(ontology);
            Set<ComplexIntegerAxiom> transOntology = trans.translateSA(ontology.getAxioms());
            Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
            ModuleExtractor extr = new ModuleExtractor();

            long b = MemoryMeasurer.measureBytes(trans);
            b += MemoryMeasurer.measureBytes(normOntology);
            b += MemoryMeasurer.measureBytes(extr);

            System.out.println("jcel size: " + b);
        }
        catch(Throwable t){
            System.out.println("JCEL error");
        }

        /*int iter = 1;
        int extriter = 10000;
        long start, end;

        BottomModeRuleBuilder bmrb = new BottomModeRuleBuilder();
        RuleSet rs = null;
        start = System.currentTimeMillis();
        for(int i = 0; i < iter; i++){
            rs = bmrb.buildRules(ontology);
        }
        end = System.currentTimeMillis();
        System.out.println("rbme rulegen: " + (end - start));

        IncrementalExtractor ie = null;
        start = System.currentTimeMillis();
        for(int i = 0; i < iter; i++){
            ie = new IncrementalExtractor(ontology);
        }
        end = System.currentTimeMillis();
        System.out.println("incr rulegen: " + (end - start));

        OWLEntity e = ontology.getClassesInSignature().iterator().next();
        Set<OWLEntity> s = Collections.singleton(e);

        Set<OWLAxiom> m1;
        IncrementalModule m2;
        RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
        start = System.currentTimeMillis();
        for(int i = 0; i < extriter; i++){
            m1 = rbme.extractModule(rs, s);
        }
        end = System.currentTimeMillis();
        long rbmetime = end - start;
        start = System.currentTimeMillis();
        for(int i = 0; i < extriter; i++){
            m2 = ie.extractModule(e);
        }
        end = System.currentTimeMillis();
        System.out.println("rbme modextr: " + rbmetime);
        System.out.println("incr modextr: " + (end - start));

        /*SyntacticLocalityModuleExtractor synt = new SyntacticLocalityModuleExtractor(m, ontology, ModuleType.BOT);
        synt.extract(new HashSet<>()).forEach(x -> System.out.println(x));

        RuleSet rs = (new BottomModeRuleBuilder().buildRules(ontology));

        (new BottomModeRuleBuilder().buildRules(ontology)).getBaseModule().forEach(x -> System.out.println(x));

        IncrementalExtractor ie = new IncrementalExtractor(ontology);*/


        /*Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
        Set<ComplexIntegerAxiom> transOntology = trans.translateSA(ontology.getAxioms());
        Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
        ReverseAxiomTranslator reverse = new ReverseAxiomTranslator(trans, ontology);
        ModuleExtractor extractor = new ModuleExtractor();
        for(OWLClass c : ontology.getClassesInSignature()){
            Set<NormalizedIntegerAxiom> mod = extractor.extractModule(normOntology, Collections.singleton(trans.translateC(c).getId()), Collections.emptySet());
            for (NormalizedIntegerAxiom n : mod) {
                System.out.println(n);
            }

        }

        /*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        //OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.medical));
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new File("C:\\Users\\spellmaker\\SemanticWeb\\testDeletionDoubleRule.owl"));
        System.out.println("loaded");
        OWLAxiom a1 = null;
        OWLAxiom a2 = null;
        for(OWLAxiom a : ontology.getAxioms()){
            if(a instanceof OWLDeclarationAxiom) continue;
            if(a.toString().contains("#C")) a1 = a;
            if(a.toString().contains("#D")) a2 = a;


            System.out.println(a);
        }
        OWLEntity entity = null;
        for(OWLEntity e : ontology.getSignature()){
            if(e.toString().contains("#E")) entity = e;
        }
        System.out.println("found:");
        System.out.println(a1);
        System.out.println(a2);
        System.out.println(entity);
        System.out.println("> complete module");
        IncrementalExtractor ie = new IncrementalExtractor(ontology);
        IncrementalModule im = ie.extractModule(entity);
        for(OWLAxiom a : im.getOWLModule()){
            if(a instanceof OWLDeclarationAxiom) continue;
            System.out.println(a);
        }
        System.out.println("> ontology modified");
        ie.modifyOntology(Collections.emptySet(), Collections.singleton(a1));
        for(OWLAxiom a : ie.getModule(entity).getOWLModule()){
            if(a instanceof OWLDeclarationAxiom) continue;
            System.out.println(a);
        }


        System.exit(0);



        HyS hys = new HyS(ontology, ModuleType.BOT);
        hys.condense(SCCAlgorithm.TARJAN);
        hys.condense(SCCAlgorithm.MREACHABILITY);
        for(OWLEntity e : ontology.getClassesInSignature()) {
            System.out.println("for entity " + e);
            Set<de.tu_dresden.inf.lat.hys.graph_tools.Node> mod = hys.getConnectedComponent(Collections.singleton(e));
            for(de.tu_dresden.inf.lat.hys.graph_tools.Node n : mod){
                System.out.print("*");
                hys.getAxioms(n).forEach(x -> System.out.println(x));
            }
        }*/


        /*List<OWLAxiom> allAxioms = new ArrayList<>(ontology.getLogicalAxioms());
        Random r = new Random();

        int rsize = 2;
        List<OWLAxiom> removed = new ArrayList<>(rsize);


        for(OWLAxiom a : allAxioms){
            if(a.toString().equals("SubClassOf(<http://chen.moe/onto/med/Genetic_Fibrosis> <http://chen.moe/onto/med/Genetic_Disorder>)")) removed.add(a);
            if(a.toString().equals("EquivalentClasses(<http://chen.moe/onto/med/Cystic_Fibrosis> ObjectIntersectionOf(<http://chen.moe/onto/med/Fibrosis> ObjectSomeValuesFrom(<http://chen.moe/onto/med/has_Origin> <http://chen.moe/onto/med/Genetic_Origin>) ObjectSomeValuesFrom(<http://chen.moe/onto/med/located_In> <http://chen.moe/onto/med/Pancreas>)) )")) removed.add(a);
        }
        allAxioms.removeAll(removed);

        //for(int i = 0; i < rsize; i++){
        //    int pos = r.nextInt(allAxioms.size());
        //    removed.add(allAxioms.get(pos));
        //    allAxioms.remove(pos);
        //}

        OWLOntology workingOntology = m.createOntology(new HashSet<>(allAxioms));
        RuleSet rs = (new BottomModeRuleBuilder()).buildRules(workingOntology);

        System.out.println("partition is: ");
        System.out.println("remaining:");
        for(OWLAxiom a : allAxioms) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);
        System.out.println("removed:");
        for(OWLAxiom a : removed) if(!(a instanceof OWLDeclarationAxiom)) System.out.println(a);


        IncrementalExtractor ie = new IncrementalExtractor(new HashSet<>(allAxioms));
        for(OWLEntity e : workingOntology.getClassesInSignature()){
            IncrementalModule im = ie.extractModule(e);
            Set<OWLAxiom> mod = (new RBMExtractorNoDef(false)).extractModule(rs, Collections.singleton(e));
            if(!mod.equals(im.getOWLModule())){
                System.out.println("failed for entity " + e + ": expected module size " + mod.size() + " got size " + im.size());
            }
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
                if(!mod.equals(im.getOWLModule())){
                    System.out.println("failed: expected size " + mod.size() + " got " + im.size() + " for entity " + e);
                    System.out.println("extracted module was ");
                    im.getOWLModule().forEach(x -> System.out.println(x));
                    System.out.println("should have been ");
                    mod.forEach(x -> System.out.println(x));
                }
            }
        }


        /*Reasoner.ReasonerFactory rf = new Reasoner.ReasonerFactory();
        OWLReasoner r = rf.createNonBufferingReasoner(ontology);
        OWLClass geneticOrigin = null;
        OWLClass geneticDisorder = null;
        OWLClass cysticFibrosis = null;
        for(OWLClass c : ontology.getClassesInSignature()){
            if(c.toString().contains("Genetic_Origin")) geneticOrigin = c;
            if(c.toString().contains("Genetic_Disorder")) geneticDisorder = c;
            if(c.toString().contains("Cystic_Fibrosis")) cysticFibrosis = c;

            NodeSet<OWLClass> sup = r.getSuperClasses(c, false);
            for(Node<OWLClass> n : sup){
                System.out.println(c + " C " + n);
            }
        }

        OWLSubClassOfAxiom s1 = new OWLSubClassOfAxiomImpl(geneticOrigin, geneticDisorder, Collections.emptySet());
        OWLSubClassOfAxiom s2 = new OWLSubClassOfAxiomImpl(cysticFibrosis, geneticDisorder, Collections.emptySet());

        System.out.println(s1 + " is entailed: " + r.isEntailed(s1));
        System.out.println(s2 + " is entailed: " + r.isEntailed(s2));*/
    }

    private static void jceltest() throws Exception{
        OREManager ore = new OREManager();
        ore.load(Paths.get("C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\"), "el\\classification", "el\\consistency", "el\\instantiation");
        List<File> ontologies = ore.filterOntologies(new ORENoFilter());
        System.out.println("el ontologies: " + ontologies.size());

        int failed_early = 0;
        int failed_late = 0;
        int ocount = 1;
        for(File f : ontologies) {
            try {
                OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
                loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
                OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(f), loaderConfig);

                Translator trans = new Translator(m.getOWLDataFactory(), new IntegerOntologyObjectFactoryImpl());
                Set<ComplexIntegerAxiom> transOntology = trans.translateSA(ontology.getAxioms());
                Set<NormalizedIntegerAxiom> normOntology = (new OntologyNormalizer()).normalize(transOntology, trans.getOntologyObjectFactory());
                ModuleExtractor extractor = new ModuleExtractor();
                Set<NormalizedIntegerAxiom> module = null;
                int cnt = 0;
                try {
                    /*for (OWLEntity e : ontology.getSignature()) {
                        if (!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)) continue;
                        if (++cnt > 100) break;

                        Set<Integer> intSet = new HashSet<>();
                        intSet.add((e instanceof OWLClass) ? trans.translateC((OWLClass) e).getId() : ((IntegerObjectProperty) trans.translateOPE((OWLObjectProperty) e)).getId());
                        if (e instanceof OWLClass)
                            extractor.extractModule(normOntology, intSet, Collections.emptySet());
                        else
                            extractor.extractModule(normOntology, Collections.emptySet(), intSet);
                    }*/
                } catch (Exception e) {
                    failed_late++;
                    System.out.println("failed late for ontology " + f + ": " + e);
                }
            }
            catch(Exception e){
                failed_early++;
                System.out.println("failed early for ontology " + f + ": " + e);
            }
            System.out.println("finished ontology " + ocount++ + " of " + ontologies.size());
        }
        System.out.println("total ontologies: " + ontologies.size());
        System.out.println("failed early: " + failed_early);
        System.out.println("failed late: " + failed_late);
        System.out.println("failed total: " + (failed_early+failed_late));
    }

    private static void galentest() throws Exception{

        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));

        BottomModeRuleBuilder rb = new BottomModeRuleBuilder();
        RuleSet rs = rb.buildRules(o);

        int cnt = 0;
        for(OWLEntity e : o.getSignature()){
            if(!(e instanceof OWLClass) && !(e instanceof OWLObjectProperty)){
                continue;
            }
            if(!e.toString().contains("PromyelocyticLeukaemia")) continue;

            if(++cnt > 100) break;

            Set<OWLAxiom> mdef = (new RBMExtractor(true, false)).extractModule(rs, Collections.singleton(e));
            Set<OWLAxiom> mndef = (new RBMExtractor(false, false)).extractModule(rs, Collections.singleton(e));

            if(!mdef.equals(mndef)){
                System.out.println("size difference: " + mdef.size() + " vs " + mndef.size());
                for(OWLAxiom a : mndef){
                    if(a instanceof OWLDeclarationAxiom) continue;
                    if(!mdef.contains(a)){
                        System.out.println(a);
                    }
                }
                break;
            }
        }
        System.out.println("axioms in which it occurs:");
        for(OWLAxiom a : o.getAxioms()){
            if(a.toString().contains("#PromyelocyticLeukaemia")){
                System.out.println(a);
            }
        }
    }
}
