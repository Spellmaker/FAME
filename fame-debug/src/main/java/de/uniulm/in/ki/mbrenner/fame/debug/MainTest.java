package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.abox.islands.IslandBuilder;
import de.uniulm.in.ki.mbrenner.fame.abox.islands.snf.ShallowNormalForm;
import de.uniulm.in.ki.mbrenner.fame.debug.graphstream.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.debug.graphstream.TopRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator.Generator;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.OntologiePaths;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 03.03.2016.
 */
class PEdge{
    final OWLNamedIndividual start;
    final OWLObjectProperty prop;
    final OWLNamedIndividual end;
    public PEdge(OWLNamedIndividual start, OWLNamedIndividual end, OWLObjectProperty prop){
        this.end = end;
        this.prop = prop;
        this.start = start;
    }

    public PEdge inv(){
        return new PEdge(end, start, prop);
    }
}
public class MainTest{


    public static void main(String[] args) throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(new File("C:\\Users\\spellmaker\\IdeaProjects\\jena-lubm\\data\\all.owl.xml")), loaderConfig);
        OWLOntology tbox = m.createOntology(ontology.getTBoxAxioms(Imports.INCLUDED));
        System.out.println(tbox.getSignature());

        Set<OWLEntity> signature = tbox.getSignature().stream().filter(x -> x.toString().equals("<http://swat.cse.lehigh.edu/onto/univ-bench.owl#AssistantProfessor>")).collect(Collectors.toSet());
        System.out.println(signature);
        System.out.println(tbox.getLogicalAxiomCount());
        Set<OWLAxiom> module = new de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef(false).extractModule(new RuleBuilder().buildRules(tbox), signature);
        Set<OWLEntity> moduleSignature = module.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet());
        System.out.println(module.stream().filter(x -> x instanceof OWLLogicalAxiom).count());

        //materialize
        Map<OWLNamedIndividual, Set<OWLClass>> classMap = new HashMap<>();
        Map<OWLNamedIndividual, Set<PEdge>> roleMap = new HashMap<>();
        Map<OWLNamedIndividual, Set<PEdge>> invRoleMap = new HashMap<>();
        ontology.getIndividualsInSignature().forEach(x -> {
            classMap.put(x, new HashSet<>());
            roleMap.put(x, new HashSet<>());
            invRoleMap.put(x, new HashSet<>());
        });

        OWLReasoner reasoner = new Reasoner.ReasonerFactory().createNonBufferingReasoner(ontology);
        System.out.println(reasoner.getPrecomputableInferenceTypes());
        reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS, InferenceType.CLASS_ASSERTIONS);


        System.out.println("modSig: " + moduleSignature);

        for(OWLClass c : ontology.getClassesInSignature()){
            if(!moduleSignature.contains(c)) continue;
            reasoner.getInstances(c, false).getFlattened().stream().forEach(x -> classMap.get(x).add(c));
        }


        for(OWLObjectProperty p : ontology.getObjectPropertiesInSignature()){
            if(!moduleSignature.contains(p)) continue;

            for(OWLNamedIndividual i : ontology.getIndividualsInSignature()){
                reasoner.getObjectPropertyValues(i, p).getFlattened().forEach(x -> {
                    PEdge e = new PEdge(i, x, p);
                    roleMap.get(i).add(e);
                    invRoleMap.get(x).add(e.inv());
                });
            }
        }

        System.out.println("precomputations completed");

        //rebuild ontology
        Set<OWLAxiom> ontoModule = new HashSet<>();
        ontoModule.addAll(module);

        //bottom not checked!
        for(OWLNamedIndividual i : ontology.getIndividualsInSignature()){
            for(OWLClass c : classMap.get(i)){
                ontoModule.add(m.getOWLDataFactory().getOWLClassAssertionAxiom(c, i));
                //System.out.println("adding class assertion");
            }
            for(PEdge e : roleMap.get(i)){
                ontoModule.add(m.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(e.prop, e.start, e.end));
                //System.out.println("adding property assertion");
            }
        }

        System.out.println(ontoModule.size());



        /*System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        RBMExtractorNoDef rbme = new RBMExtractorNoDef(true);

        Generator g = new Generator();
        for(OWLEntity e : ontology.getSignature()){
            g.get(e);
        }
        g.get(m.getOWLDataFactory().getOWLThing());

        Map<OWLObject, Boolean> interpret = new HashMap<>();
        for(OWLEntity e : ontology.getSignature()){
            interpret.put(e, false);
        }
        g.buildRules(ontology.getAxioms(), interpret);
        RuleSet rs = new RuleSet(false);
        for(int i = 0; i < g.dictionarySize(); i++){
            if(rs.getId(g.get(i)) != i){
                System.out.println("you should fix this: " + i + " " + g.get(i));
                System.exit(0);
            }
        }
        for(Rule r : g.getRules()){
            rs.addRule(-1, r);
        }
        rs.finalizeSet();

        //RuleSet rs = new TopRuleBuilder().buildRules(ontology);
        for(Rule r : rs){
            if(!(rs.getObject(r.getHeadOrAxiom()) instanceof OWLDeclarationAxiom)){
                System.out.println(r.toDebugString(rs));
            }
        }
        int i = 0;
        System.setOut(new WaitStream(System.out));
        for(OWLEntity e : ontology.getSignature()){
            //if(!e.toString().equals("<http://www.co-ode.org/ontologies/galen#PalatineTonsil>")) continue;
            System.out.println(e);
            rbme.extractModule(rs, Collections.singleton(e));
            if(i++ > 10) break;
        }*/



        /*OWLDataFactory fact = m.getOWLDataFactory();

        System.out.println("Axioms:");
        ontology.getLogicalAxioms().forEach(OWLPrinter::println);
        System.out.println("Individuals:");
        ontology.getIndividualsInSignature().forEach(OWLPrinter::println);
        OWLNamedIndividual a = (OWLNamedIndividual) ontology.getSignature().stream().filter(x -> OWLPrinter.getString(x).equals("a")).findFirst().get();

        ShallowNormalForm snf = new ShallowNormalForm(fact);
        ontology.getTBoxAxioms(Imports.INCLUDED).forEach(x -> OWLPrinter.println(snf.getSNF(x)));
        System.out.println("island for a:");
        IslandBuilder island = new IslandBuilder(m, ontology);
        island.getIsland(a, new HashSet<>()).forEach(OWLPrinter::println);



        /*OWLClass chair = fact.getOWLClass(IRI.create("Chair"));
        OWLClass department = fact.getOWLClass(IRI.create("Department"));
        OWLClass person = fact.getOWLClass(IRI.create("Person"));
        OWLObjectProperty headOf = fact.getOWLObjectProperty(IRI.create("headOf"));

        OWLAxiom axiom = fact.getOWLSubClassOfAxiom(chair, fact.getOWLObjectIntersectionOf(fact.getOWLObjectSomeValuesFrom(headOf, department), person));

        OWLPrinter.println(axiom);
        ShallowNormalForm snf = new ShallowNormalForm(fact);
        snf.getSNF(axiom).forEach(OWLPrinter::println);*/
        /*ShallowNormalForm snf = new ShallowNormalForm(fact);
        ontology.getAxioms().stream().filter(x -> x instanceof OWLLogicalAxiom).map(snf::getSNF).forEach(x -> x.forEach(OWLPrinter::println));

        System.out.println("info structure:");
        System.out.println(InfoStructureBuilder.build(ontology, m));

        Set<OWLIndividual> s = ontology.getIndividualsInSignature().stream().filter(x -> x.toString().contains("p1")).collect(Collectors.toSet());

        IslandBuilder ib = new IslandBuilder(m, ontology);
        for(OWLIndividual i : s){
            System.out.println("ind: " + i);
            ib.getIsland(i, new HashSet<>()).forEach(x -> System.out.println("\t" + OWLPrinter.getString(x)));
        }

        /*OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(OntologiePaths.galen)), loaderConfig);//"C:\\Users\\spellmaker\\Desktop\\snomedStated_INT_20140731.owl")), loaderConfig);

        RuleSet rs = new RuleBuilder().buildRules(ontology);

        List<String> inter = Files.readAllLines(Paths.get("C:\\Users\\spellmaker\\Desktop\\interpretation"));
        Set<String> wrongmod = new HashSet<>(Files.readAllLines(Paths.get("C:\\Users\\spellmaker\\Desktop\\wrong_module")));
        Set<String> parentmod = new HashSet<>(Files.readAllLines(Paths.get("C:\\Users\\spellmaker\\Desktop\\parent_module")));

        Set<OWLEntity> sig = ontology.getSignature().stream().filter(x -> x.toString().equals("<http://www.co-ode.org/ontologies/galen#Posture>")).collect(Collectors.toSet());

        Set<OWLAxiom> wm = ontology.getAxioms().stream().filter(x -> wrongmod.contains(x.toString())).collect(Collectors.toSet());
        Set<OWLAxiom> pm = ontology.getAxioms().stream().filter(x -> parentmod.contains(x.toString())).collect(Collectors.toSet());

        System.out.println("Restored a module with " + wm.size() + " elements");
        System.out.println("Restored a parent module with " + pm.size() + " elements");

        Map<OWLObject, Boolean> map = new HashMap<>();
        Iterator<String> iter = inter.iterator();
        while(iter.hasNext()){
            String obj = iter.next();
            boolean isBot = Boolean.parseBoolean(iter.next());

            for(OWLEntity e : ontology.getSignature()){
                if(e.toString().equals(obj)){
                    map.put(e, isBot);
                    break;
                }
            }
        }
        System.out.println("Restored an interpretation with " + map.size() + " elements (file had " + (inter.size()/2) + "*2 lines)");

        System.out.println(Verifier.verifyModule(pm, wm, map));
        Set<OWLAxiom> botMod = new RBMExtractorNoDef().extractModule(new RuleBuilder().buildRules(ontology), sig);
        System.out.println("Botloc size: " + botMod.size());
        Generator gen = new Generator();
        gen.buildRules(botMod, map);
        Set<OWLAxiom> genMod = gen.getResult(sig);
        System.out.println("Size of module extracted using the provided interpretation: " + genMod.size());
        System.out.println("Stored and generated are equal: " + genMod.equals(wm));

        System.exit(0);*/


        /*System.out.println("loaded, starting");
        for(OWLEntity e : ontology.getSignature()){
            if(!e.toString().equals("<http://purl.obolibrary.org/obo/MRO_0001069>")) continue;
            Set<OWLEntity> signature = Collections.singleton(e);
            Set<OWLAxiom> botMod = new RBMExtractorNoDef().extractModule(rs, signature);//.stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());



            Map<OWLObject, Boolean> map = new HashMap<>();
            ontology.getSignature().forEach(x -> map.put(x, true));

            Generator gen = new Generator();
            gen.buildRules(ontology.getAxioms(), map);

            Set<OWLAxiom> oMod = gen.getResult(signature);

            System.out.println(e + " botmod: " + botMod.size() + " omod: " + oMod.size());

            System.out.println(ModuleDiff.diff(botMod, oMod));

            GeneticExtractor gExtr = new GeneticExtractor(ontology, signature, 10);
            Set<OWLAxiom> genMod = gExtr.extract();

            System.out.println(ModuleDiff.diff(Misc.stripNonLogical(botMod), Misc.stripNonLogical(genMod)));
        }*/
    }
}
