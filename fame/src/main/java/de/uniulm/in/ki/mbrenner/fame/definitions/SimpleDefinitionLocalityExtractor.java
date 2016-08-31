package de.uniulm.in.ki.mbrenner.fame.definitions;

import de.uniulm.in.ki.mbrenner.fame.definitions.builder.DefinitionBuilder;
import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.DirectLocalityExtractor;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts definition based modules from an ontology.
 *
 * The algorithm works similar to the owlapi locality extraction algorithm,
 * in that it evaluates the locality of all axioms of the ontology, which are not yet
 * part of the module under the current settings (in this case: signature of the module
 * and chosen definitions) until it finds a non-local one.
 * Instead of immediately extending the module with this non-local axiom the algorithm
 * tries to find a definition under which this axiom becomes local and extends its definition
 * set.
 * If no such definition can be found, the axiom is added to the module.
 *
 * Created by spellmaker on 28.04.2016.
 */
@Deprecated
public class SimpleDefinitionLocalityExtractor{
    /**
     * If set to true produces additional debug messages
     */
    public static boolean debug = false;
    /**
     * If set to true verifies that the algorithm will terminate
     */
    public static boolean verify = false;

    //data structures to track definitions
    private final Map<OWLAxiom, Set<OWLObject>> axiomToDefinitions = new HashMap<>(); //maps axioms to definitions for later deletion
    private final Map<OWLEntity, Set<OWLAxiom>> entityToAxioms = new HashMap<>();    //maps entities to dependent axioms
    /**
     * The final definitions used to keep axioms out of the module
     */
    public Map<OWLObject, OWLObject> finalDefinitions;
    /**
     * The final signature of the module
     */
    public Set<OWLEntity> finalSignature;
    /**
     * The final extended signature of the module.
     *
     * The extended signature contains the
     */
    public Set<OWLEntity> finalExtSignature;

    /**
     * Extracts a definition locality based module from the provided collection of axioms
     * @param axiomCollection A collection of axioms from which the module is to be extracted
     * @param sign The signature for which the module is to be extracted
     * @return A definition local module for the provided signature
     */
    public Set<OWLAxiom> extract(Collection<OWLAxiom> axiomCollection, Collection<OWLEntity> sign){
        List<OWLAxiom> axioms = new LinkedList<>(axiomCollection);
        Set<OWLEntity> signature = new HashSet<>(sign);
        Map<OWLObject, OWLObject> definitions = new HashMap<>();
        Set<OWLAxiom> module = new HashSet<>();
        List<OWLAxiom> remaining = new LinkedList<>(axioms);

        //get a rule set for the axioms, this will be used to restrict the range of axioms, which need to be checked
        //for locality
        RuleSet rs = new RuleSet();
        RuleBuilder bmrb = new RuleBuilder();
        bmrb.buildRules(new HashSet<>(axioms), axioms.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet()), true, rs, rs);
        DirectLocalityExtractor direct = new DirectLocalityExtractor(false);

        //the extended signature is the normal signature extended by the elements, for which there are definitions
        Set<OWLEntity> extSignature = new HashSet<>();
        extSignature.addAll(signature);

        DefinitionBuilder db = new DefinitionBuilder();
        DefinitionEvaluator de = new DefinitionEvaluator();

        int prev_size = remaining.size();
        int step = 0;

        if(debug) System.out.println("Entering main loop");

        while(true){
            if(debug) System.out.println("Iteration " + step);
            OWLAxiom nonLocal = null;

            Set<OWLAxiom> directNonLocal = direct.extractModule(rs, extSignature);
            for(OWLAxiom ax : directNonLocal){
                if(!remaining.contains(ax)) continue;
                if(!de.isDefinitionLocal(ax, signature, definitions)){
                    nonLocal = ax;
                    break;
                }
            }
            if(nonLocal == null) break;
            if(debug) System.out.println("non-local: " + OWLPrinter.getString(nonLocal));

            extSignature.addAll(nonLocal.getSignature());
            //try to fix the non-local axiom
            //TODO: Can a higher order definition hide other definitions?
            Map<OWLObject, OWLObject> ndef = db.tryDefine(nonLocal, definitions, signature);
            if(ndef == null){
                if(debug) System.out.println("definition attempt failed, consequences:");
                Set<OWLAxiom> change = resolveConflict(nonLocal, signature, definitions);
                if(debug) change.forEach(x -> System.out.println("+" + OWLPrinter.getString(x)));
                module.addAll(change);
                remaining.removeAll(change);
                //System.out.println(remaining.size());
                //System.out.println(prev_size);
                if(debug){
                    System.out.println("remaining definitions:");
                    definitions.entrySet().stream().filter(e -> e.getKey() instanceof OWLClass || e.getKey() instanceof OWLObjectProperty).forEach(e ->
                        System.out.println(OWLPrinter.getString(e.getKey()) + " -> " + OWLPrinter.getString(e.getValue()))
                    );
                }
            }
            else{
                if(debug) System.out.println("definition attempt succeeded, consequences:");
                //System.out.println("axiom is local now");
                Set<OWLObject> axiomDefinitions = new HashSet<>();
                for(Map.Entry<OWLObject, OWLObject> entry : ndef.entrySet()){
                    if(definitions.get(entry.getKey()) != null) continue;

                    //System.out.println("new definition: " + entry);
                    if(debug) System.out.println(OWLPrinter.getString(entry.getKey()) + " -> " + OWLPrinter.getString(entry.getValue()));

                    //for each new definition
                    //1. add it to the set of definitions
                    definitions.put(entry.getKey(), entry.getValue());
                    //2. add them to the set of things defined by the axiom
                    axiomDefinitions.add(entry.getKey());
                    //3. add the defined symbols to the extended Signature
                    //extSignature.addAll(entry.getKey().getSignature());
                }
                axiomToDefinitions.put(nonLocal, axiomDefinitions);
                for(OWLEntity e : db.getDependent()){
                    Set<OWLAxiom> depAxs = entityToAxioms.get(e);
                    if(depAxs == null){
                        depAxs = new HashSet<>();
                        entityToAxioms.put(e, depAxs);
                    }
                    depAxs.add(nonLocal);
                }
            }
            //debug check to determine correctness: Either the found axiom is local after the change or the
            //size has decreased
            //System.out.println("prev: " + prev_size + " rem: " + remaining.size());
            if(verify) {
                if (!de.isDefinitionLocal(nonLocal, signature, definitions) && prev_size <= remaining.size()) {
                    //if(remaining.contains(nonLocal)) System.out.println("nonlocal is in remaining");

                    System.out.println("axiom is " + OWLPrinter.getString(nonLocal));
                    System.out.println("definitions are:");
                    for (Map.Entry<OWLObject, OWLObject> e : definitions.entrySet()) {
                        System.out.println(OWLPrinter.getString(e.getKey()) + " -> " + OWLPrinter.getString(e.getValue()));
                    }
                    de.isDefinitionLocal(nonLocal, signature, definitions);

                    throw new IllegalArgumentException("error in implementation (step is " + step + ")");
                }
            }
            prev_size = remaining.size();
            step++;
            if(debug) System.out.println();
        }

        if(debug) System.out.println("No non-local axiom found, terminating");

        finalDefinitions = Collections.unmodifiableMap(definitions);
        finalSignature = Collections.unmodifiableSet(signature);
        finalExtSignature = Collections.unmodifiableSet(extSignature);

        return module;
    }

    private Set<OWLAxiom> resolveConflict(OWLAxiom root, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        Set<OWLAxiom> modAdd = new HashSet<>();
        modAdd.add(root);
        /*if(root.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#DomainCategory> <http://www.co-ode.org/ontologies/galen#TopCategory>)")){
            System.out.println("adding axiom in question");
        }*/
        signature.addAll(root.getSignature());
        for(OWLEntity e : root.getSignature()){
            Set<OWLAxiom> s = entityToAxioms.get(e);
            entityToAxioms.remove(e);
            if(s == null) continue;
            for(OWLAxiom ax : s){
                if(axiomToDefinitions.get(ax) == null){
                    continue;
                }
                axiomToDefinitions.get(ax).forEach(definitions::remove);
                axiomToDefinitions.remove(ax);
                //modAdd.add(ax);
                //signature.addAll(ax.getSignature());
                modAdd.addAll(resolveConflict(ax, signature, definitions));
            }
        }
        return modAdd;
    }
}
