package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import de.uniulm.in.ki.mbrenner.fame.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.definitions.DefinitionLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.DirectLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.rule.BottomModeRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class MainFrame extends JFrame{
    private Set<OWLEntity> signature;
    private Map<OWLObject, OWLObject> definitions;
    private Iterator<OWLAxiom> axioms;

    private JPanel renderedAxiom;

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
            if(ent.toString().equals("<http://www.co-ode.org/ontologies/galen#PalatineTonsil>")) signature.add(ent);
        }
        RuleSet rs = new BottomModeRuleBuilder().buildRules(o);

        DefinitionLocalityExtractor def = new DefinitionLocalityExtractor();

        SyntacticLocalityModuleExtractor syntExtr = new SyntacticLocalityModuleExtractor(m, o, ModuleType.STAR);

        Set<OWLAxiom> defMod = def.getDefinitionLocalityModule(o.getAxioms(), signature);
        Set<OWLAxiom> botMod = new RBMExtractorNoDef(false).extractModule(rs, signature);
        Set<OWLAxiom> starMod = syntExtr.extract(signature);
        Set<OWLAxiom> starDefMod = new DefinitionLocalityExtractor().getDefinitionLocalityModule(starMod, signature);
        System.out.println(signature.iterator().next() +
                ": bot " + botMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                " star " + starMod.stream().filter(x -> x instanceof OWLLogicalAxiom).count() +
                " def " + defMod.size() +
                " *def " + starDefMod.size());


        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);
        Set<OWLAxiom> culprits = direct.extractModule(rs, def.finalExtSignature).stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());
        culprits = culprits.stream().filter(x -> !defMod.contains(x)).collect(Collectors.toSet());

        MainFrame frame = new MainFrame(culprits, def.finalDefinitions, def.finalSignature);
        frame.setVisible(true);
    }

    public MainFrame(Collection<OWLAxiom> axioms, Map<OWLObject, OWLObject> definitions, Set<OWLEntity> signature) throws Exception{
        this.axioms = axioms.iterator();
        this.definitions = definitions;
        this.signature = signature;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.getContentPane().setLayout(new BorderLayout());
        JPanel navigation = new JPanel();
        JLabel lblCount = new JLabel("" + (axioms.size() - 1));
        navigation.add(lblCount);
        JButton btnNext = new JButton("Next");
        btnNext.addMouseListener(new ButtonListener(this, btnNext, lblCount, axioms.size() - 1));
        navigation.add(btnNext);
        this.getContentPane().add(navigation, BorderLayout.SOUTH);

        next();

        /*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new File(OntologiePaths.galen));

        OWLAxiom test = null;
        Iterator<OWLLogicalAxiom> iter = o.getLogicalAxioms().iterator();
        do{
            test = iter.next();
        }while(!test.toString().equals("EquivalentClasses(<http://www.co-ode.org/ontologies/galen#RiskOfCerebrovascularPathology> ObjectIntersectionOf(<http://www.co-ode.org/ontologies/galen#Risking> ObjectSomeValuesFrom(<http://www.co-ode.org/ontologies/galen#hasSpecificConsequence> <http://www.co-ode.org/ontologies/galen#CerebrovascularPathology>)) )"));//test instanceof OWLTransitiveObjectPropertyAxiom);

        Map<OWLObject, OWLObject> tmp = new HashMap<>();
        for(OWLEntity e : test.getSignature()){
            tmp.put(e, new OWLDataFactoryImpl().getOWLNothing());
        }

        System.out.println(test);


        this.pack();*/
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
            if(!(entry.getKey() instanceof OWLObjectProperty) && !(entry.getKey() instanceof OWLClass)) continue;

            interpretation.put(entry.getKey(), entry.getValue());
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
