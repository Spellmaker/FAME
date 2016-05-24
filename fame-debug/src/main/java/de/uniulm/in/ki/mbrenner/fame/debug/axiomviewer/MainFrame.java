package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import de.uniulm.in.ki.mbrenner.fame.util.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.definitions.SimpleDefinitionLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBAxiom;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.DirectLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class MainFrame extends JFrame{
    private Set<OWLEntity> signature;
    private Map<OWLObject, OWLObject> definitions;
    private Iterator<OWLAxiom> axioms;

    private JPanel renderedAxiom;

    private RuleSet rs;
    private DRBRuleSet drs;
    private OWLOntology ontology;

    public boolean hasNext(){
        return axioms.hasNext();
    }

    public void next(){
        OWLAxiom n = axioms.next();
        switchTo(n);
    }


    public static void main(String[] args) throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));

        Set<OWLEntity> signature = new HashSet<>();
        //somehow obtain an entity
        for(OWLEntity ent : o.getSignature()){
            if(ent.toString().equals("<http://www.co-ode.org/ontologies/galen#PositiveFamilyHistory>")) signature.add(ent);
        }
        RuleSet rs = new BottomModeRuleBuilder().buildRules(o);
        DRBRuleSet drs = new DRBAxiom().buildRules(o);

        /*SimpleDefinitionLocalityExtractor def = new SimpleDefinitionLocalityExtractor();

        SyntacticLocalityModuleExtractor syntExtr = new SyntacticLocalityModuleExtractor(m, o, ModuleType.STAR);

        Set<OWLAxiom> defMod = def.getDefinitionLocalityModule(o.getAxioms(), signature);
        Set<OWLAxiom> botMod = new RBMExtractorNoDef(false).extractModule(rs, signature);
        Set<OWLAxiom> starMod = syntExtr.extract(signature);
        Set<OWLAxiom> starDefMod = new SimpleDefinitionLocalityExtractor().getDefinitionLocalityModule(starMod, signature);
        System.out.println(signature.iterator().next() +
                ": bot " + botMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                " star " + starMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                " def " + defMod.size() +
                " *def " + starDefMod.size());


        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);
        Set<OWLAxiom> culprits = direct.extractModule(rs, def.finalExtSignature).stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());
        culprits = culprits.stream().filter(x -> !defMod.contains(x)).collect(Collectors.toSet());*/

        MainFrame frame = new MainFrame(rs, drs, o); //culprits, def.finalDefinitions, def.finalSignature);
        frame.setVisible(true);
        frame.loadEntity(signature);
    }

    private void loadEntitySimple(Set<OWLEntity> signature) throws Exception{
        this.signature = signature;

        SimpleDefinitionLocalityExtractor def = new SimpleDefinitionLocalityExtractor();
        Set<OWLAxiom> defMod = def.extract(ontology.getAxioms(), signature);

        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);
        Set<OWLAxiom> culprits = direct.extractModule(rs, def.finalExtSignature).stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());

        culprits = culprits.stream().filter(x -> !defMod.contains(x)).collect(Collectors.toSet());
        System.out.println("loading signature " + signature + " resulted in " + culprits.size() + " axioms to examine");

        generateGUI(culprits, def.finalDefinitions, def.finalSignature);
    }

    private void loadEntityRule(Set<OWLEntity> signature) throws Exception{
        this.signature = signature;

        DRBExtractor extractor = new DRBExtractor(true);
        Set<OWLAxiom> defMod = extractor.extractModule(drs, signature);
        Set<OWLEntity> finalSignature = defMod.stream().map(x -> x.getSignature()).flatMap(x -> x.stream()).collect(Collectors.toSet());
        Set<OWLEntity> finalExtSignature = new HashSet<>(finalSignature);
        finalExtSignature.addAll(extractor.getDefinitions().entrySet().stream().map(x -> (OWLEntity) x.getKey()).collect(Collectors.toSet()));

        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);
        Set<OWLAxiom> culprits = direct.extractModule(rs, finalExtSignature).stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());

        culprits = culprits.stream().filter(x -> !defMod.contains(x)).collect(Collectors.toSet());
        System.out.println("loading signature " + signature + " resulted in " + culprits.size() + " axioms to examine");
        System.out.println("full module size is " + new RBMExtractorNoDef(false).extractModule(rs, signature).size() + " vs definition based size " + defMod.size());
        System.out.println("definitions are:");
        extractor.getDefinitions().entrySet().forEach(x -> System.out.println(OWLPrinter.getString(x.getKey()) + " -> " + OWLPrinter.getString(x.getValue())));
        generateGUI(culprits, extractor.getDefinitions(), finalSignature);
    }


    public void loadEntity(Set<OWLEntity> signature) throws Exception{
        loadEntityRule(signature);
        //loadEntitySimple(signature);
    }

    private void generateGUI(Collection<OWLAxiom> axioms, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature) throws Exception{
        this.getContentPane().removeAll();
        this.axioms = axioms.iterator();
        this.definitions = definitions;
        this.signature = signature;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.getContentPane().setLayout(new BorderLayout());
        JPanel navigation = new JPanel();
        JButton btnFilter = new JButton("View");
        JTextField txtFilter = new JTextField(80);
        btnFilter.addMouseListener(new ChangeSignatureListener(this, txtFilter, ontology.getSignature()));
        navigation.add(btnFilter);
        navigation.add(txtFilter);


        JLabel lblCount = new JLabel("" + (axioms.size() - 1));
        navigation.add(lblCount);
        JButton btnNext = new JButton("Next");
        btnNext.addMouseListener(new ButtonListener(this, btnNext, lblCount, axioms.size() - 1));
        navigation.add(btnNext);
        this.getContentPane().add(navigation, BorderLayout.SOUTH);

        next();
    }

    public MainFrame(RuleSet rs, DRBRuleSet drs, OWLOntology ontology) throws Exception{
        this.rs = rs;
        this.drs = drs;
        this.ontology = ontology;
    }

    private void switchTo(OWLAxiom axiom){
        System.out.println("new: " + axiom);
        if(renderedAxiom != null) this.getContentPane().remove(renderedAxiom);
        renderedAxiom = new AxiomRenderer(this).render(axiom, makeInterpretation(axiom));
        this.getContentPane().add(renderedAxiom, BorderLayout.CENTER);
        this.pack();
    }

    private Map<OWLObject, OWLObject> makeInterpretation(OWLAxiom axiom){
        OWLDataFactory data = new OWLDataFactoryImpl();
        Map<OWLObject, OWLObject> interpretation = new HashMap<>();
        for(Map.Entry<OWLObject, OWLObject> entry : definitions.entrySet()){
            if(entry.getKey() instanceof OWLClass){
                DefinitionEvaluator de = new DefinitionEvaluator();
                interpretation.put(entry.getKey(), de.getDefined((OWLClassExpression)entry.getValue(), signature, definitions));
            }
            else if(entry.getKey() instanceof OWLObjectProperty){
                DefinitionEvaluator de = new DefinitionEvaluator();
                interpretation.put(entry.getKey(), de.getDefined((OWLObjectProperty)entry.getValue(), signature, definitions));
            }
            //if(!(entry.getKey() instanceof OWLObjectProperty) && !(entry.getKey() instanceof OWLClass)) continue;
            //interpretation.put(entry.getKey(), entry.getValue());
        }

        for(OWLEntity e : signature){
            interpretation.put(e, new Unknown());
        }

        for(OWLEntity e : axiom.getSignature()){
            if(interpretation.get(e) == null){
                interpretation.put(e, data.getOWLNothing());
            }
        }
        return interpretation;
    }
}
