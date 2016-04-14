package de.uniulm.in.ki.mbrenner.fame.util;

import de.tudresden.inf.lat.jcel.core.algorithm.module.ModuleExtractor;
import de.tudresden.inf.lat.jcel.coreontology.axiom.NormalizedIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.complex.ComplexIntegerAxiom;
import de.tudresden.inf.lat.jcel.ontology.axiom.extension.IntegerOntologyObjectFactoryImpl;
import de.tudresden.inf.lat.jcel.ontology.normalization.OntologyNormalizer;
import de.tudresden.inf.lat.jcel.owlapi.translator.Translator;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.IncrIncrementalAddWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.IncrIncrementalWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results.IncrTimeResult;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.DummyRuleContainer;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.ModificationResult;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.TreeBuilder;
import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.IncrementalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.folder.NormalRuleFolder;
import de.uniulm.in.ki.mbrenner.fame.incremental.v3.treebuilder.nodes.Node;
import de.uniulm.in.ki.mbrenner.fame.rule.*;
import de.uniulm.in.ki.mbrenner.oremanager.OREManager;
import de.uniulm.in.ki.mbrenner.oremanager.filters.ORENoFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

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


    private static void choseNew(int change, Set<OWLAxiom> currentSet, List<OWLAxiom> currentList, Set<OWLAxiom> removedAxioms, Random r){
        Set<OWLAxiom> nrem = new HashSet<>();
        for(int i = 0; i < change; i++){
            OWLAxiom c = currentList.get(r.nextInt(currentList.size()));
            currentSet.remove(c);
            currentList.remove(c);
            nrem.add(c);
        }
        currentSet.addAll(removedAxioms);
        currentList.addAll(removedAxioms);
        //EvaluationMain.out.println("adding " + removedAxioms);
        removedAxioms.clear();
        removedAxioms.addAll(nrem);
        //EvaluationMain.out.println("removing " + removedAxioms);
    }

    private static long test(boolean naive) throws Exception{
        String file = "C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_d5d7a77f-d9fe-4eac-96e9-579f6957b33f_OBI.owl_functional.owl";
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(file));
        List<OWLAxiom> currentList = new ArrayList<>(o.getLogicalAxioms());
        Set<OWLAxiom> currentSet = new HashSet<>(currentList);
        Set<OWLAxiom> removedAxioms = new HashSet<>();
        int change = 1;
        Random r = new Random();

        choseNew(change, currentSet, currentList, removedAxioms, r);

        IncrementalExtractor ie = new IncrementalExtractor(currentSet);
        for(OWLClass c : o.getClassesInSignature()){
            ie.extractModule(c);
        }

        long start, end;
        start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            Set<OWLAxiom> add = new HashSet<>(removedAxioms);
            choseNew(change, currentSet, currentList, removedAxioms, r);

            if(naive)
                ie.modifyOntologyNaive(add, removedAxioms);
            else
                ie.modifyOntology(add, removedAxioms);
        }
        end = System.currentTimeMillis();
        return end - start;
    }

    private static void test2() throws Exception{
        String file = "C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_d5d7a77f-d9fe-4eac-96e9-579f6957b33f_OBI.owl_functional.owl";
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(file));
        List<OWLAxiom> currentList = new ArrayList<>(o.getLogicalAxioms());
        Set<OWLAxiom> currentSet = new HashSet<>(currentList);
        Set<OWLAxiom> removedAxioms = new HashSet<>();
        int change = 1;
        Random r = new Random();

        choseNew(change, currentSet, currentList, removedAxioms, r);

        IncrementalExtractor ie1 = new IncrementalExtractor(currentSet);
        IncrementalExtractor ie2 = new IncrementalExtractor(currentSet);
        for(OWLClass c : o.getClassesInSignature()){
            ie1.extractModule(c);
            ie2.extractModule(c);
        }

        for(int i = 0; i < 1; i++){
            Set<OWLAxiom> add = new HashSet<>(removedAxioms);
            choseNew(change, currentSet, currentList, removedAxioms, r);

            ModificationResult modRes1 = ie1.modifyOntologyNaive(add, removedAxioms);
            ModificationResult modRes2 = ie2.modifyOntology(add, removedAxioms);
            if(modRes1.additionAffected.size() != modRes2.additionAffected.size()){
                System.out.println("different addition affected: " + modRes1.additionAffected.size() + " vs " + modRes2.additionAffected.size());
            }
            if(modRes1.deletionAffected.size() != modRes2.deletionAffected.size()){
                System.out.println("different deletion affected: " + modRes1.deletionAffected.size() + " vs " + modRes2.deletionAffected.size());
            }
        }
    }

    public static void main(String[] args) throws Exception{

        /*System.out.println("incr: " + test(false));
        System.out.println("naiv: " + test(true));
        test2();
        System.exit(0);*/

        String file = OntologiePaths.galen;//"C:\\Users\\spellmaker\\Downloads\\ore2014_dataset\\dataset\\files\\approximated_d5d7a77f-d9fe-4eac-96e9-579f6957b33f_OBI.owl_functional.owl";
        /*IncrIncrementalWorker w = new IncrIncrementalWorker(new File(file), 1, 1000, 0, false, false);
        IncrTimeResult r1;
        r1 = w.call();
        w = new IncrIncrementalWorker(new File(file), 1, 1000, 0, true, false);
        IncrTimeResult r2 = w.call();
        w = new IncrIncrementalWorker(new File(file), 1, 1000, 0, true, true);
        IncrTimeResult r3 = w.call();

        System.out.println("time incr:" + r1.time);
        System.out.println("time naiv:" + r2.time);
        System.out.println("time half:" + r3.time);
        System.out.println("basemod aff: " + IncrIncrementalWorker.basemodaffected);*/

        IncrIncrementalAddWorker w2 = new IncrIncrementalAddWorker(new File(file), 1, 10, 0, false, false);
        IncrTimeResult r1_2 = w2.call();
        w2 = new IncrIncrementalAddWorker(new File(file), 1, 10, 0, true, false);
        IncrTimeResult r2_2 = w2.call();
        w2 = new IncrIncrementalAddWorker(new File(file), 1, 10, 0, false, true);
        IncrTimeResult r3_2 = w2.call();
        System.out.println("time incr:" + r1_2.time);
        System.out.println("time naiv:" + r2_2.time);
        System.out.println("time half:" + r3_2.time);
        System.out.println("basemod aff: " + IncrIncrementalAddWorker.basemodaffected);



        /*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.contest1));
        long start, end;

        TreeBuilder tb = new TreeBuilder();
        List<Node> forest = tb.buildTree(o.getAxioms());
        start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            DummyRuleContainer dummy = new DummyRuleContainer();
            NormalRuleFolder nrf = new NormalRuleFolder(dummy, dummy);
            nrf.buildRules(forest);
        }
        end = System.currentTimeMillis();

        System.out.println("nrf time: " + (end - start));

        start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            DummyRuleContainer dummy = new DummyRuleContainer();
            IncrementalRuleFolder incr = new IncrementalRuleFolder(dummy, dummy, Collections.emptyList());
            incr.buildRules(forest);
        }
        end = System.currentTimeMillis();

        System.out.println("incr time: " + (end - start));*/
        /*System.out.println("rule set size: " + MemoryMeasurer.measureBytes(rs));
        IncrementalExtractor ie = new IncrementalExtractor(ontology);
        System.out.println("incremental extractor size: " + MemoryMeasurer.measureBytes(ontology));
        ObjectExplorer.examineStatic = true;
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
        }*/

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
