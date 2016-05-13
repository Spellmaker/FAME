package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.definitions.builder.DefinitionBuilder;
import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.DefinitionLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.DefinitionUtility;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import de.uniulm.in.ki.mbrenner.fame.extractor.DirectLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ClassCounter;
import de.uniulm.in.ki.mbrenner.fame.util.ClassPrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.clarkparsia.owlapi.modularity.locality.LocalityClass.BOTTOM_BOTTOM;

/**
 * Created by spellmaker on 03.03.2016.
 */
public class MainTest{
    public static void main(String[] args) throws Exception{
        OWLClass a = new OWLClassImpl(IRI.create("A"));
        OWLClass b = new OWLClassImpl(IRI.create("B"));
        OWLClass c = new OWLClassImpl(IRI.create("C"));
        OWLClass d = new OWLClassImpl(IRI.create("D"));
        Set<OWLClass> set = new HashSet<>();
        set.add(b);
        set.add(c);
        OWLObjectIntersectionOf intersec = new OWLObjectIntersectionOfImpl(set);
        OWLSubClassOfAxiom ax1 = new OWLSubClassOfAxiomImpl(a, b, Collections.emptySet());
        OWLSubClassOfAxiom ax2 = new OWLSubClassOfAxiomImpl(a, intersec, Collections.emptySet());
        OWLSubClassOfAxiom ax3 = new OWLSubClassOfAxiomImpl(d, b, Collections.emptySet());

        Set<OWLEntity> testSig = new HashSet<>();
        testSig.add(a);
        testSig.add(d);
        List<OWLAxiom> axLis = new LinkedList<>();
        axLis.add(ax1);
        axLis.add(ax2);
        axLis.add(ax3);

        DefinitionLocalityExtractor defExtrTest = new DefinitionLocalityExtractor();
        defExtrTest.getDefinitionLocalityModule(axLis, testSig).forEach(x -> System.out.println(x));
        System.exit(0);




        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));

        Set<OWLEntity> sig = new HashSet<>();
        RuleSet rs = new BottomModeRuleBuilder().buildRules(o);
        SyntacticLocalityModuleExtractor syntExtr = new SyntacticLocalityModuleExtractor(m, o, ModuleType.STAR);

        System.out.println("starting loop");
        for(OWLEntity e : o.getSignature()){
            DefinitionLocalityExtractor defExt = new DefinitionLocalityExtractor();

            Set<OWLAxiom> defMod = defExt.getDefinitionLocalityModule(o.getAxioms(), Collections.singleton(e));
            Set<OWLAxiom> botMod = new RBMExtractorNoDef(false).extractModule(rs, Collections.singleton(e));
            Set<OWLAxiom> starMod = syntExtr.extract(Collections.singleton(e));
            Set<OWLAxiom> starDefMod = defExt.getDefinitionLocalityModule(starMod, Collections.singleton(e));
            System.out.println(e +
                    ": bot " + botMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                    " star " + starMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                    " def " + defMod.size() +
                    " *def " + starDefMod.size());

        }

        System.exit(0);


        ClassCounter cc = new ClassCounter();
        for(OWLAxiom ax : o.getAxioms()){
            cc.add(ax);
        }

        for(String s : cc){
            System.out.println(s);
        }

        long owlapi = 0;
        long def = 0;
        long star = 0;
        long stardef = 0;
        IncrementalExtractor ie = new IncrementalExtractor(o);

        SyntacticLocalityModuleExtractor synt = new SyntacticLocalityModuleExtractor(m, o, ModuleType.STAR);

        for(OWLEntity e1 : o.getSignature()){

            for(OWLEntity e2 : o.getSignature()) {
                if(e1.equals(e2)) continue;


                Set<OWLEntity> signature = new HashSet<>();
                signature.add(e1);
                signature.add(e2);

                System.out.println("signature is " + signature);
                long s1 = ie.extractModuleStatic(signature).getOWLModule().stream().filter(x -> x instanceof OWLLogicalAxiom).count();
                DefinitionLocalityExtractor defex = new DefinitionLocalityExtractor();
                Set<OWLAxiom> defloc = defex.getDefinitionLocalityModule(o.getAxioms(), signature);
                long s2 = defloc.size();
                Set<OWLAxiom> starloc = synt.extract(signature);
                long s3 = starloc.stream().filter(x -> x instanceof OWLLogicalAxiom).count();
                DefinitionLocalityExtractor defex2 = new DefinitionLocalityExtractor();
                Set<OWLAxiom> stardefloc = defex2.getDefinitionLocalityModule(starloc, signature);
                long s4 = stardefloc.size();
                /*
                DefinitionLocalityExtractor de = new DefinitionLocalityExtractor();
                de.getDefinitionLocalityModule(o.getAxioms(), signature);
                System.out.println(">definitions:");
                DefinitionUtility.printDefinitions(de.finalDefinitions);
                System.out.println(">owl module:");
                for(OWLAxiom a : DefinitionUtility.getCheckNeeded(o.getAxioms(), Collections.emptySet(), de.finalDefinitions, Collections.singleton(e))){
                    if(a instanceof OWLDeclarationAxiom) continue;
                    System.out.println(ClassPrinter.printAxiom(a));
                }*/
                owlapi += s1;
                def += s2;
                star += s3;
                stardef += s4;
                System.out.println("bottom: " + s1 + " def: " + s2 + " star: " + s3 + " star+def: " + s4);
                /*System.out.println("star module:");
                starloc.forEach(x -> System.out.println(x));
                System.out.println("star+def module:");
                stardefloc.forEach(x -> System.out.println(x));
                System.out.println("definitions:");
                System.out.println(defex2.finalDefinitions.size());
                System.out.println(defex2.finalDefinitions);*/
            }
        }

        System.out.println("bottom locality: " + owlapi);
        System.out.println("definition locality: " + def);
        System.out.println("star locality: " + star);
        System.out.println("star+def locality: " + stardef);

        System.exit(0);

        //System.setOut(new WaitStream(System.out));
        RuleSet unoptimized = new BottomModeRuleBuilder().buildRules(o);
        RuleSet optimized = RuleOptimizer.optimizeRules(unoptimized);
        System.out.println("rule sets finished");

        long start, end;
        start = System.currentTimeMillis();
        for(OWLEntity e : o.getClassesInSignature()){
            Set<OWLAxiom> mod = new RBMExtractorNoDef(false).extractModule(unoptimized, Collections.singleton(e));
        }
        end = System.currentTimeMillis();
        System.out.println("unoptimized: " + (end - start));


        start = System.currentTimeMillis();
        for(OWLEntity e : o.getClassesInSignature()){
            Set<OWLAxiom> mod = new RBMExtractorNoDef(false).extractModule(optimized, Collections.singleton(e));
        }
        end = System.currentTimeMillis();
        System.out.println("optimized: " + (end - start));

        List<Rule> r1 = new ArrayList<>();
        List<Rule> r2 = new ArrayList<>();
        unoptimized.forEach(x -> r1.add(x));
        optimized.forEach(x -> r2.add(x));

        System.exit(0);
    }
}
