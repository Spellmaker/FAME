package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.util.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.debug.annotationtest.PrintOnShutdown;
import de.uniulm.in.ki.mbrenner.fame.definitions.SimpleDefinitionLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.debug.incremental.customextractor.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBAxiom;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.ClassCounter;
import de.uniulm.in.ki.mbrenner.fame.util.Misc;
import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 03.03.2016.
 */
public class MainTest{
    @PrintOnShutdown(text="hallo")
    int testInt;

    public static void main(String[] args) throws Exception{
        /*OWLDataFactory df = new OWLDataFactoryImpl();
        OWLClass a = df.getOWLClass(IRI.create("A"));
        OWLClass b = df.getOWLClass(IRI.create("B"));
        OWLClass c = df.getOWLClass(IRI.create("C"));
        OWLClass d = df.getOWLClass(IRI.create("D"));
        OWLClass top = df.getOWLThing();

        OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(a, b);
        OWLSubClassOfAxiom ax2 = df.getOWLSubClassOfAxiom(b, c);
        OWLSubClassOfAxiom ax3 = df.getOWLSubClassOfAxiom(c, df.getOWLObjectIntersectionOf(a, d));
        OWLSubClassOfAxiom ax4 = df.getOWLSubClassOfAxiom(df.getOWLThing(), b);

        List<OWLAxiom> axs = new LinkedList<>();
        axs.add(ax1); axs.add(ax2); axs.add(ax3); axs.add(ax4);

        SimpleDefinitionLocalityExtractor defextr = new SimpleDefinitionLocalityExtractor();
        defextr.debug = true;
        Set<OWLAxiom> tmod = defextr.getDefinitionLocalityModule(axs, Collections.singleton(a));
        System.out.println(tmod);
        System.exit(0);*/



        /*OWLClass a = new OWLClassImpl(IRI.create("A"));
        OWLClass b = new OWLClassImpl(IRI.create("B"));
        OWLClass c = new OWLClassImpl(IRI.create("C"));
        Set<OWLClass> s11 = new HashSet<>();
        s11.add(a); s11.add(b);
        Set<OWLClass> s21 = new HashSet<>();
        s21.add(b); s21.add(c);
        OWLEquivalentClassesAxiom eq1 = new OWLEquivalentClassesAxiomImpl(s11, Collections.emptySet());
        OWLEquivalentClassesAxiom eq2 = new OWLEquivalentClassesAxiomImpl(s21, Collections.emptySet());

        Set<OWLEntity> sig1 = new HashSet<>();
        sig1.add(a); sig1.add(b); sig1.add(c);
        Set<OWLAxiom> o1 = new HashSet<>();
        o1.add(eq1);
        o1.add(eq2);

        RuleSet rs1 = new RuleSet();
        new BottomModeRuleBuilder().buildRules(o1, sig1, false, rs1, rs1);

        new RBMExtractor(true, false).extractModule(rs1, Collections.singleton(a)).forEach(x -> System.out.println(x));
        System.exit(0);*/




        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.medical));


        Set<OWLEntity> s = new HashSet<>();
        s.add(Misc.getEntity(o, "<http://chen.moe/onto/med/Genetic_Disorder>"));
        s.add(Misc.getEntity(o, "<http://chen.moe/onto/med/Cystic_Fibrosis>"));

        long strt, nd;
        RBMExtractorNoDef rbme = new RBMExtractorNoDef(false);
        RuleSet nrs = new BottomModeRuleBuilder().buildRules(o);
        DRBRuleSet drs = new DRBAxiom().buildRules(o);
        DRBExtractor drb = new DRBExtractor();
        SimpleDefinitionLocalityExtractor dloc = new SimpleDefinitionLocalityExtractor();
        Set<OWLAxiom> tm = new HashSet<>();
        int times = 10000;
        strt = System.currentTimeMillis();
        for(int i = 0; i < times; i++) tm = rbme.extractModule(nrs, s);
        nd = System.currentTimeMillis();
        System.out.println("rbme: " + (nd - strt));
        strt = System.currentTimeMillis();
        for(int i = 0; i < times; i++) tm = drb.extractModule(drs, s);
        nd = System.currentTimeMillis();
        System.out.println("drb: " + (nd - strt));
        strt = System.currentTimeMillis();
        for(int i = 0; i < times; i++) tm = dloc.extract(o.getAxioms(), s);
        nd = System.currentTimeMillis();
        System.out.println("dloc: " + (nd - strt));

        System.exit(0);


        Set<OWLEntity> sig = new HashSet<>();
        RuleSet rs = new BottomModeRuleBuilder().buildRules(o);
        SyntacticLocalityModuleExtractor syntExtr = new SyntacticLocalityModuleExtractor(m, o, ModuleType.STAR);

        DRBRuleSet drb2 = new DRBAxiom().buildRules(o);
        System.out.println(drb2.size());
        System.out.println(rs.ruleCount());

        int tmp = 0;
        for(DRBRule r : drb2){
            if(tmp++ > 10) break;
            System.out.println(r.toString());
        }



        System.out.println("starting loop");




        int skip = 0;
        int tries = 10;
        for(OWLEntity e : o.getSignature()){
            if(skip++ < 1) continue;
            //System.out.println(e);
            Set<OWLAxiom> botLocMod = new RBMExtractorNoDef(false).extractModule(rs, Collections.singleton(e)).stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());
            //Set<OWLAxiom> defLocMod = new SimpleDefinitionLocalityExtractor().getDefinitionLocalityModule(o.getAxioms(), Collections.singleton(e));
            if(botLocMod.size() > 100) continue;

            //System.out.println(botLocMod.size() + " vs " + defLocMod.size());
            Set<OWLEntity> redSig = new HashSet<>();
            botLocMod.stream().forEach(x -> redSig.addAll(x.getSignature()));
            int i = 0;
            for(OWLEntity f : redSig){
                if(i++ > tries) break;
                if(e.equals(f)) continue;
                Set<OWLEntity> classSig = new HashSet<>();
                classSig.add(e);
                classSig.add(f);

                //System.out.println("signature is " + OWLPrinter.getString(classSig));
                //System.out.println("botloc:");
                //botLocMod.forEach(x -> System.out.println(OWLPrinter.getString(x)));
                //System.out.println("defloc:");
                SimpleDefinitionLocalityExtractor extr1 = new SimpleDefinitionLocalityExtractor();
                SimpleDefinitionLocalityExtractor extr2 = new SimpleDefinitionLocalityExtractor();

                Set<OWLAxiom> abdefloc = extr1.extract(o.getAxioms(), classSig);
                //abdefloc.forEach(x -> System.out.println(OWLPrinter.getString(x)));

                Set<OWLAxiom> abdefbotloc = new SimpleDefinitionLocalityExtractor().extract(botLocMod, classSig);
                Set<OWLAxiom> abdeflocdefloc = Collections.emptySet();
                if(abdefloc.size() > 0){
                    abdeflocdefloc = extr2.extract(abdefloc, classSig);
                }
                if(abdefloc.size() != abdefbotloc.size() || abdefloc.size() != abdeflocdefloc.size())
                   System.out.println(OWLPrinter.getString(classSig) + " def(ab,bot) " + abdefbotloc.size() + " def(ab,O)" + abdefloc.size() + " def(ab,def(ab,O)) " + abdeflocdefloc.size());
            }
        }


        System.exit(0);



        int smallest = 1000000000;
        OWLEntity smallestEntity = null;

        for(OWLEntity e : o.getSignature()){
            //if(!e.toString().equals("<http://www.co-ode.org/ontologies/galen#PositiveFamilyHistory>")) continue;
            SimpleDefinitionLocalityExtractor defExt = new SimpleDefinitionLocalityExtractor();
            //defExt.debug = true;

            Set<OWLAxiom> defMod = defExt.extract(o.getAxioms(), Collections.singleton(e));
            Set<OWLAxiom> botMod = new RBMExtractorNoDef(false).extractModule(rs, Collections.singleton(e));
            Set<OWLAxiom> starMod = syntExtr.extract(Collections.singleton(e));
            Set<OWLAxiom> starDefMod = new SimpleDefinitionLocalityExtractor().extract(starMod, Collections.singleton(e));
            Set<OWLAxiom> rbmeDefMod = new RBMExtractor(true, false).extractModule(rs, Collections.singleton(e));

            /*System.out.println("additional axiom in defmod:");
            defMod.stream().filter(x -> !rbmeDefMod.contains(x)).forEach(x -> System.out.println(OWLPrinter.getString(x)));
            System.out.println("defmod:");
            defMod.forEach(x -> System.out.println(OWLPrinter.getString(x)));
            System.out.println("defs:");
            defExt.finalDefinitions.entrySet().forEach(x -> System.out.println(OWLPrinter.getString(x.getKey()) +" -> " + OWLPrinter.getString(x.getValue())));
            System.out.println("check:");
            new DirectLocalityExtractor(false).extractModule(rs, defExt.finalExtSignature).stream().filter(x -> x instanceof OWLLogicalAxiom).filter(x -> !defMod.contains(x)).forEach(x -> System.out.println(OWLPrinter.getString(x)));
            System.out.println("sig:");
            defExt.finalSignature.forEach(x -> System.out.println(OWLPrinter.getString(x));*/

            if(defMod.size() > rbmeDefMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count()) {
                System.out.println("(++) " + e +
                        ": bot " + botMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " star " + starMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " def " + defMod.size() +
                        " rbmedef " + rbmeDefMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " *def " + starDefMod.size());
            //    if(defMod.size() < smallest){
            //        smallest = defMod.size();
            //        smallestEntity = e;
            //        System.out.println("new smallest entity: " + smallestEntity + " at " + smallest);
            //    }
            }
            else{
                System.out.println(e +
                        ": bot " + botMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " star " + starMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " def " + defMod.size() +
                        " rbmedef " + rbmeDefMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                        " *def " + starDefMod.size());
            }
        }

        System.exit(0);


        ClassCounter cc = new ClassCounter();
        for(OWLAxiom ax : o.getAxioms()){
            cc.add(ax);
        }

        for(String str : cc){
            System.out.println(str);
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
                SimpleDefinitionLocalityExtractor defex = new SimpleDefinitionLocalityExtractor();
                Set<OWLAxiom> defloc = defex.extract(o.getAxioms(), signature);
                long s2 = defloc.size();
                Set<OWLAxiom> starloc = synt.extract(signature);
                long s3 = starloc.stream().filter(x -> x instanceof OWLLogicalAxiom).count();
                SimpleDefinitionLocalityExtractor defex2 = new SimpleDefinitionLocalityExtractor();
                Set<OWLAxiom> stardefloc = defex2.extract(starloc, signature);
                long s4 = stardefloc.size();
                /*
                SimpleDefinitionLocalityExtractor de = new SimpleDefinitionLocalityExtractor();
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
