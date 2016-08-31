package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleSet;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Extracts a definition based locality module from a given ontology
 * The current implementation does not perform all possible definitions,
 * but instead relies on simple definitions, which only define complex expressions
 * via single concepts
 *
 * Created by spellmaker on 20.05.2016.
 */
public class IDRBExtractor {
    private Queue<Integer> procQueue;
    private boolean[] notBot;
    private boolean[] inSignature;

    private Integer[] isDefinedAs;
    private Set<Integer>[] isReasonFor;
    private Set<Integer>[] invariants;
    private Set<Integer>[] invariantAxioms;
    private Set<Integer>[] objectsToAxioms;
    /*private Map<OWLObject, OWLObject> isDefinedAs;
    private Map<OWLObject, Set<OWLObject>> isReasonFor;
    private Map<OWLObject, Set<OWLObject>> invariants;
    private Map<OWLObject, Set<OWLAxiom>> invariantAxioms;
    private Map<OWLEntity, Set<OWLAxiom>> objectsToAxioms;*/

    private IDRBRuleSet rules;
    
    private Set<Integer> module;

    private static boolean debug = false;

    /**
     * Default constructor
     */
    public IDRBExtractor(){
        debug = false;
    }

    /**
     * Constructs a new instance
     * Allows to choose the debugging mode
     * @param d If debug mode is to be enabled
     */
    public IDRBExtractor(boolean d){
        debug = d;
    }

    private void addQueue(Integer o){
        if(!notBot[o]){
            notBot[o] = true;
            procQueue.add(o);
        }
    }

    private void addSignature(Integer e){
        if(!inSignature[e]){
            inSignature[e] = true;
            //TODO: Implement
            // /module.add(new OWLDeclarationAxiomImpl(, Collections.emptySet()));
        }
    }

    private void addResponsible(Integer symbol, Integer cause){
        Set<Integer> set = isReasonFor[cause];
        if(set == null){
            set = new HashSet<>();
            isReasonFor[cause] = set;
        }
        set.add(symbol);
    }

    /**
     * Provides the definitions used by the last extraction process
     * @return The definitions used by the last extraction process
     */
    public Map<OWLObject, OWLObject> getDefinitions(){
        Map<OWLObject, OWLObject> defs = new HashMap<>();
        for(int i = 0; i < isDefinedAs.length; i++){
            if(isDefinedAs[i] != null && !inSignature[i]){
                defs.put(rules.getObject(i), rules.getObject(isDefinedAs[i]));
            }
        }
        return Collections.unmodifiableMap(defs);
    }

    /**
     * Extracts a definition local module for the provided signature
     * @param rules A set of extraction rules generated for an ontology
     * @param signature The signature for the module
     * @return A definition local module for the provided signature
     */
    public Set<Integer> extractModule(@Nonnull IDRBRuleSet rules, @Nonnull Set<OWLEntity> signature){
        this.rules = rules;
        IDebug.debugDictionary = rules;
        procQueue = new LinkedList<>();
        int[] ruleCounter = new int[rules.size()];
        notBot = new boolean[rules.dictionarySize()];
        inSignature = new boolean[rules.dictionarySize()];
        isDefinedAs = new Integer[rules.dictionarySize()];
        isReasonFor = new Set[rules.dictionarySize()];
        objectsToAxioms = new Set[rules.dictionarySize()];
        Set<Integer> extModule = new HashSet<>();
        invariants = new Set[rules.dictionarySize()];
        invariantAxioms = new Set[rules.dictionarySize()];

        module = new HashSet<>();

        for(OWLEntity e : signature){
            Integer id = rules.getId(e);
            procQueue.add(id);
            notBot[id] = true;
            addSignature(id);
        }

        //base module
        for(Integer ax : rules.getBaseAxioms()){
            rollback(ax);
        }

        for(Integer e : rules.getBaseEntities()){
            addQueue(e);
        }

        if(debug) System.out.println("starting extraction for sig " + OWLPrinter.getString(signature));

        while(!procQueue.isEmpty()){
            Integer head = procQueue.poll();
            if(debug) System.out.println("head: " + OWLPrinter.getString(rules.getObject(head)));
            for(Iterator<IDRBRule> ruleIterator = rules.rulesForObjects(head); ruleIterator.hasNext(); ){
                IDRBRule cRule = ruleIterator.next();
                //if(debug) System.out.println("\tActive rule: " + cRule + "(" + (ruleCounter[cRule.id] + 1) + "/" + cRule.size() + ")");
                if(++ruleCounter[cRule.id] >= cRule.size() && (cRule.axiom == null || !extModule.contains(cRule.axiom))){
                    if(debug) System.out.println("\tActive rule: " + cRule + "(" + (ruleCounter[cRule.id] + 1) + "/" + cRule.size() + ")");
                    if(cRule.head != null){
                        //intermediate symbol
                        addQueue(cRule.head);
                    }
                    else{
                        //OWLAxiom tmp = (OWLAxiom) rules.getObject(cRule.axiom);
                        //if(tmp.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")){
                        //    System.out.println("encountering axiom");
                        //    for(OWLEntity e : tmp.getSignature()){
                        //        int id = rules.getId(e);
                        //        System.out.println(OWLPrinter.getString(e) + " notBot: " + notBot[id] + " inSig: " + inSignature[id] + " def: " + rules.getObject(isDefinedAs[id]));
                        //    }
                        //}

                        //if(OWLPrinter.getString(tmp).equals("Trans(InverseLocativeAttribute)")){
                        //    System.out.println("encountered axiom Trans(InverseLocativeAttribute)");
                        //}


                        //actually handle definitions in here
                        Set<Integer> dependentOn = tryDefine(cRule);
                        extModule.add(cRule.axiom);
                        if(dependentOn == null){
                            //if(tmp.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")){
                            //    System.out.println("rollback");
                            //}
                            if(debug) System.out.println("cannot establish definition, rolling back");
                            //roll back and re-establish a stable state
                            rollback(cRule.axiom);
                        }
                        else{
                            //if(tmp.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")){
                            //    System.out.println("def success");
                            //}
                            if(debug) System.out.println("definition established");
                            //store definitions by adding a mapping of the defined symbol
                            //to this axiom
                            for(Integer o : dependentOn){
                                Set<Integer> s = objectsToAxioms[o];
                                if(s == null){
                                    s = new HashSet<>();
                                    objectsToAxioms[o] = s;
                                }
                                addQueue(o);
                                s.add(cRule.axiom);
                            }
                        }
                    }
                }
            }
        }
        if(debug) System.out.println("extraction completed");

        return module;
    }

    private Set<Integer> tryDefine(IDRBRule cRule){
        //attempt to use the definition implied by the provided rule
        Set<Integer> dependentOn = new HashSet<>();
        //only if there even is a definition
        if(!cRule.definitions.isEmpty()){
            //can only be done if all symbols can be defined
            for(IDRBDefinition def : cRule.definitions){
                Integer symbol = def.definedSymbol;
                //axiom relies on the definition of the symbol
                dependentOn.add(symbol);
                //find out what the symbol needs to be defined as
                Integer defAs = def.definingSymbol;//def.definition.action(def.definingSymbol);
                //resolve the definition to the definition set it belongs to
                Integer defAsResolved = inSignature[defAs] ? defAs : isDefinedAs[defAs];
                if(defAsResolved == null) defAsResolved = defAs;
                //find out if there is already a definition for the symbol or if it is in the signature
                Integer existingDef = inSignature[symbol] || symbol == 0 || symbol == 1 ? symbol : isDefinedAs[symbol];

                if(existingDef == null){
                    //if there is no definition, make one as needed
                    isDefinedAs[symbol] = defAsResolved;
                    addResponsible(symbol, defAs);
                }
                else{
                    //check if the values maybe coincide
                    if(defAsResolved.equals(existingDef)){
                        //as the definition depends on the defining symbol to keep its defined value,
                        //it is important to also notice if this value changes due to some other
                        //actions.
                        //in theory, these actions might change the value but still allow those two values
                        //to be the same
                        //we therefore add an invariant, that these two symbols always need to be defined
                        //with the same value (or one needs to have the value of the other, if one is part
                        //of the signature
                        addInvariant(symbol, defAs, cRule.axiom);
                    }
                    else{
                        //otherwise this won't work
                        return null;
                    }
                }
            }
            //all symbols could be defined, definition works
            return dependentOn;
        }
        //no definition available, doesn't work
        return null;
    }

    private void addInvariant(Integer o1, Integer o2, Integer axiom){
        //System.out.println("adding invariant " + rules.getObject(o1) + "==" + rules.getObject(o2));
        Set<Integer> s = invariants[o1];
        if(s == null){
            s = new HashSet<>();
            invariants[o1] = s;
        }
        s.add(o2);
        s = invariants[o2];
        if(s == null){
            s = new HashSet<>();
            invariants[o2] = s;
        }
        s.add(o1);

        Set<Integer> sa = invariantAxioms[o1];
        if(sa == null){
            sa = new HashSet<>();
            invariantAxioms[o1] = sa;
        }
        sa.add(axiom);
        sa = invariantAxioms[o2];
        if(sa == null){
            sa = new HashSet<>();
            invariantAxioms[o2] = sa;
        }
        sa.add(axiom);
    }

    private boolean invariantHolds(Integer o1){
        //checks if the invariants for this objects hold
        Set<Integer> s = invariants[o1];
        if(s == null) return true;

        boolean o1InSig = inSignature[o1];
        for(Integer o2 : s){
            //System.out.println("checking invariant " + rules.getObject(o1) + "==" + rules.getObject(o2));
            boolean o2InSig = inSignature[o2];
            if(o1InSig && o2InSig) return false;

            Integer o1Val = o1InSig ? o1 : isDefinedAs[o1]; o1Val = o1Val == null ? o1 : o1Val;
            Integer o2Val = o2InSig ? o2 : isDefinedAs[o2]; o2Val = o2Val == null ? o2 : o2Val;

            if(!o1Val.equals(o2Val)) return false;
        }
        return true;
    }

    private void removeInvariant(Integer o1){
        Set<Integer> s = invariants[o1];
        invariants[o1] = null;
        invariantAxioms[o1] = null;
        if(s != null){
            s.forEach(this::removeInvariant);
        }
    }

    private Set<Integer> updateResponsible(Integer o, Integer def){
        //updates the definitions by following subsumption chains
        //and updating the pointers to the definitions
        //also returns a list of symbols, for whom the final definition
        //changed so that the invariants can get checked later
        Set<Integer> changed = new HashSet<>();
        Set<Integer> s = isReasonFor[o];
        if(s != null){
            for(Integer other : s){
                if(!inSignature[other]) {
                    changed.add(other);
                    isDefinedAs[other] = def;
                    changed.addAll(updateResponsible(other, def));
                }
            }
        }
        return changed;
    }

    //public static int hurtInvariants = 0;

    private void rollback(Integer axiom){
        //boolean found = OWLPrinter.getString(rules.getObject(axiom)).equals("Trans(InverseLocativeAttribute)");


        //We assume that the provided axiom cannot be made local or stopped being local
        //due to other rollbacks.
        //Rollback removes all definitions for the symbols in the axiom and makes them part
        //of the module signature. In turn, it executes rollback for each axiom depending
        //on a symbol of this axiom
        if(module.contains(axiom)) return;

        Set<Integer> changed = new HashSet<>();
        module.add(axiom);
        if(debug) System.out.println("adding axiom " + OWLPrinter.getString(rules.getObject(axiom)));
        for(Integer e : rules.getSignature(axiom)){
            //if(found) System.out.println("rolling back for " + rules.getObject(e));
            //update status in the correct table
            addSignature(e);
            addQueue(e);
            isDefinedAs[e] = null;
            changed.add(e);
            changed.addAll(updateResponsible(e, e));
            Set<Integer> affectedAxioms = objectsToAxioms[e];
            objectsToAxioms[e] = null;
            if(affectedAxioms != null){
                affectedAxioms.forEach(x -> rollback(x));
            }
        }
        //verify all affected
        //if(found){
        //    changed.forEach(x -> System.out.println(rules.getObject(x) + ": " + invariantHolds(x)));
        //}
        changed.stream().filter(o -> !invariantHolds(o)).forEach(o -> {
            hurtInvariants++;
            Set<Integer> s = invariantAxioms[o];
            removeInvariant(o);
            if (s != null) {
                s.forEach(x -> rollback(x));
            }
        });
    }

    public int hurtInvariants = 0;
}
