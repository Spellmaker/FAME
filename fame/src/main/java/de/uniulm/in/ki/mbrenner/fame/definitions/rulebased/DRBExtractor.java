package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

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
public class DRBExtractor{
    private Queue<OWLObject> procQueue;
    private Set<OWLObject> notBot;
    private Set<OWLObject> inSignature;

    private Map<OWLObject, OWLObject> isDefinedAs;
    private Map<OWLObject, Set<OWLObject>> isReasonFor;
    private Map<OWLObject, Set<OWLObject>> invariants;
    private Map<OWLObject, Set<OWLAxiom>> invariantAxioms;
    private Map<OWLEntity, Set<OWLAxiom>> objectsToAxioms;

    private Set<OWLAxiom> module;

    private static boolean debug = false;

    /**
     * Default constructor
     */
    public DRBExtractor(){
        debug = false;
    }

    /**
     * Constructs a new instance
     * Allows to choose the debugging mode
     * @param d If debug mode is to be enabled
     */
    public DRBExtractor(boolean d){
        debug = d;
    }

    private void addQueue(OWLObject o){
        if(notBot.add(o)) procQueue.add(o);
    }

    private void addSignature(OWLEntity e){
        if(inSignature.add(e)){
            module.add(new OWLDeclarationAxiomImpl(e, Collections.emptySet()));
        }
    }

    private void addResponsible(OWLObject symbol, OWLObject cause){
        if(!(cause instanceof OWLClassExpression) && symbol instanceof OWLClass){
            System.out.println("warning: adding " + cause + " (" + cause.getClass() + ") as reason for " + symbol + "(" + symbol.getClass() + ")");
        }


        Set<OWLObject> set = isReasonFor.get(cause);
        if(set == null){
            set = new HashSet<>();
            isReasonFor.put(cause, set);
        }
        set.add(symbol);
    }

    /**
     * Provides the definitions used by the last extraction process
     * @return The definitions used by the last extraction process
     */
    public Map<OWLObject, OWLObject> getDefinitions(){
        Map<OWLObject, OWLObject> defs = new HashMap<>();
        isDefinedAs.entrySet().stream().filter(x -> x.getKey() instanceof OWLEntity).filter(e -> !inSignature.contains(e.getKey())).forEach(e -> defs.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(defs);
    }

    static OWLEntity modConcept = null;
    static OWLEntity domCat = null;
    static OWLEntity phen = null;
    static OWLEntity topCat = null;


    /**
     * Extracts a definition local module for the provided signature
     * @param rules A set of extraction rules generated for an ontology
     * @param signature The signature for the module
     * @return A definition local module for the provided signature
     */
    public Set<OWLAxiom> extractModule(@Nonnull DRBRuleSet rules, @Nonnull Set<OWLEntity> signature){
        procQueue = new LinkedList<>();
        int[] ruleCounter = new int[rules.size()];
        notBot = new HashSet<>();
        inSignature = new HashSet<>();
        isDefinedAs = new HashMap<>();
        isReasonFor = new HashMap<>();
        objectsToAxioms = new HashMap<>();
        Set<OWLAxiom> extModule = new HashSet<>();
        invariants = new HashMap<>();
        invariantAxioms = new HashMap<>();

        module = new HashSet<>();

        for(OWLEntity e : signature){
            procQueue.add(e);
            notBot.add(e);
            addSignature(e);
        }

        //base module
        for(OWLAxiom ax : rules.getBaseAxioms()){
            rollback(ax);
        }

        for(OWLObject e : rules.getBaseEntities()){
            addQueue(e);
        }

        if(debug) System.out.println("starting extraction for sig " + OWLPrinter.getString(signature));

        while(!procQueue.isEmpty()){
            OWLObject head = procQueue.poll();
            if(debug) System.out.println("head: " + OWLPrinter.getString(head));
            for(Iterator<DRBRule> ruleIterator = rules.rulesForObjects(head); ruleIterator.hasNext(); ){
                DRBRule cRule = ruleIterator.next();
                //if(debug) System.out.println("\tActive rule: " + cRule + "(" + (ruleCounter[cRule.id] + 1) + "/" + cRule.size() + ")");
                if(++ruleCounter[cRule.id] >= cRule.size() && (cRule.axiom == null || !extModule.contains(cRule.axiom))){
                    if(debug) System.out.println("\tActive rule: " + cRule + "(" + (ruleCounter[cRule.id] + 1) + "/" + cRule.size() + ")");
                    if(cRule.head != null){
                        //intermediate symbol
                        addQueue(cRule.head);
                    }
                    else{
                        //if (cRule.axiom.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")) {
                        //    System.out.println("encountered axiom");
                        //}
                        //actually handle definitions in here
                        Set<OWLEntity> dependentOn = tryDefine(cRule);
                        extModule.add(cRule.axiom);

                        /*if(OWLPrinter.getString(cRule.axiom).equals("ModifierConcept⊑DomainCategory") ||
                                OWLPrinter.getString(cRule.axiom).equals("Phenomenon⊑DomainCategory") ||
                                OWLPrinter.getString(cRule.axiom).equals("DomainCategory⊑TopCategory")){
                            System.out.println("found special axiom " + OWLPrinter.getString(cRule.axiom));

                            for(OWLEntity e : DRBRuleBuilder.onto.getSignature()){
                                String s = OWLPrinter.getString(e);
                                if(s.equals("ModifierConcept")) modConcept = e;
                                else if(s.equals("DomainCategory")) domCat = e;
                                else if(s.equals("Phenomenon")) phen = e;
                                else if(s.equals("TopCategory")) topCat = e;
                            }
                            System.out.println("found all");
                        }*/

                        if(dependentOn == null){
                            //if (cRule.axiom.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")) {
                            //    System.out.println("rollback");
                            //}
                            if(debug) System.out.println("cannot establish definition, rolling back");
                            //roll back and re-establish a stable state
                            //int prev = hurtInvariants;
                            rollback(cRule.axiom);
                            //if(hurtInvariants > prev){
                            //    System.out.println("invariant breaks at " + OWLPrinter.getString(cRule.axiom));
                            //}
                        }
                        else{
                            //if (cRule.axiom.toString().equals("SubClassOf(<http://www.co-ode.org/ontologies/galen#ModifierConcept> <http://www.co-ode.org/ontologies/galen#DomainCategory>)")) {
                            //    System.out.println("def success");
                            //}
                            if(debug) System.out.println("definition established");
                            //store definitions by adding a mapping of the defined symbol
                            //to this axiom
                            for(OWLEntity o : dependentOn){
                                Set<OWLAxiom> s = objectsToAxioms.get(o);
                                if(s == null){
                                    s = new HashSet<>();
                                    objectsToAxioms.put(o, s);
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

    private Set<OWLEntity> tryDefine(DRBRule cRule){
        //attempt to use the definition provided by cRule
        Set<OWLEntity> dependentOn = new HashSet<>();
        //only if there even is a definition
        if(!cRule.definitions.isEmpty()){
            //can only be done if all symbols can be defined
            for(DRBDefinition def : cRule.definitions){
                OWLEntity symbol = (OWLEntity) def.definedSymbol;
                //axiom relies on the definition of the symbol
                dependentOn.add(symbol);
                //find out what the symbol needs to be defined as
                OWLObject defAs = def.definingSymbol;//def.definition.action(def.definingSymbol);
                //resolve the definition to the definition set it belongs to
                OWLObject defAsResolved = inSignature.contains(defAs) ? defAs : DRBEval.resolve(defAs, notBot, inSignature, isDefinedAs);//isDefinedAs.get(defAs);
                if(defAsResolved == null) defAsResolved = defAs;
                //find out if there is already a definition for the symbol or if it is in the signature
                OWLObject existingDef = inSignature.contains(symbol) || symbol.isTopEntity() ? symbol : isDefinedAs.get(symbol);

                if(defAs instanceof OWLClassExpression && !(defAs instanceof OWLClass)) defAs.getSignature().stream().forEach(x -> addResponsible(defAs, x));

                if(debug) System.out.println("\t\trequires definition of " + OWLPrinter.getString(symbol) + " as " + OWLPrinter.getString(defAsResolved));


                if(existingDef == null){
                    //if there is no definition, make one as needed
                    /*if(symbol instanceof OWLClassExpression && !(defAsResolved instanceof OWLClassExpression)){
                        System.out.println("found the culprit!");
                        System.out.println(symbol + " as " + defAsResolved);
                    }*/

                    //if(!(symbol instanceof OWLClassExpression) && defAs instanceof OWLClassExpression) System.out.println("happening: " + symbol + " " + symbol.getClass() + " " + defAs + " " + defAs.getClass());
                    isDefinedAs.put(symbol, defAsResolved);
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
                        if(debug) System.out.println("\t\tclashes with existing definitions");
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

    private void addInvariant(OWLObject o1, OWLObject o2, OWLAxiom axiom){
        //System.out.println("adding invariant " + o1 + "==" + o2);
        Set<OWLObject> s = invariants.get(o1);
        if(s == null){
            s = new HashSet<>();
            invariants.put(o1, s);
        }
        s.add(o2);
        s = invariants.get(o2);
        if(s == null){
            s = new HashSet<>();
            invariants.put(o2, s);
        }
        s.add(o1);

        Set<OWLAxiom> sa = invariantAxioms.get(o1);
        if(sa == null){
            sa = new HashSet<>();
            invariantAxioms.put(o1, sa);
        }
        sa.add(axiom);
        sa = invariantAxioms.get(o2);
        if(sa == null){
            sa = new HashSet<>();
            invariantAxioms.put(o2, sa);
        }
        sa.add(axiom);
    }

    public int hurtInvariants = 0;

    private boolean invariantHolds(OWLObject o1){
        //checks if the invariants for this objects hold
        Set<OWLObject> s = invariants.get(o1);
        if(s == null) return true;

        //boolean o1InSig = inSignature.contains(o1);
        for(OWLObject o2 : s){
            //boolean o2InSig = inSignature.contains(o2);
            /*if(o1InSig && o2InSig){
                //System.out.println("hurt invariant is " + o1 + " == " + o2);
                return false;
            }

            OWLObject o1Val = o1InSig ? o1 : isDefinedAs.get(o1);
            o1Val = (o1Val == null) ? o1 : o1Val;

            OWLObject o2Val = o2InSig ? o2 : isDefinedAs.get(o2);
            o2Val = (o2Val == null) ? o2 : o2Val;

            if(!o1Val.equals(o2Val)){
                //System.out.println("hurt invariant is " + o1 + " == " + o2);
                return false;
            }*/
            if(!DRBEval.resolve(o1, notBot, inSignature, isDefinedAs).
                    equals(DRBEval.resolve(o2, notBot, inSignature, isDefinedAs)))
                return false;
        }
        return true;
    }

    private void removeInvariant(OWLObject o1){
        Set<OWLObject> s = invariants.get(o1);
        invariants.remove(o1);
        invariantAxioms.remove(o1);
        if(s != null){
            s.forEach(this::removeInvariant);
        }
    }

    private Set<OWLObject> updateDefinitions(OWLObject o, OWLObject def){
        if(debug) System.out.println("\tUpdating definitions for " + OWLPrinter.getString(o));
        //updates the definitions by following subsumption chains
        //and updating the pointers to the definitions
        //also returns a list of symbols, for whom the final definition
        //changed so that the invariants can get checked later
        Set<OWLObject> changed = new HashSet<>();
        Set<OWLObject> s = isReasonFor.get(o);
        if(s != null){
            for(OWLObject other : s){
                if(!inSignature.contains(other)) {
                    changed.add(other);

                    if(other instanceof OWLClass || other instanceof OWLObjectProperty) {
                        /*if(other instanceof OWLClassExpression && !(def instanceof OWLClassExpression)){
                            System.out.println("found the culprit!");
                            System.out.println(other + " " + other.getClass() + " as " + def + " " + def.getClass() + " " + o + " " + o.getClass());
                        }*/
                        //if(!(other instanceof OWLClassExpression) && def instanceof OWLClassExpression) System.out.println("culprit: " + other + " " + other.getClass() + " " + def + " " + def.getClass());
                        isDefinedAs.put(other, def);
                        if(debug) System.out.println("\t\tnew definition for " + OWLPrinter.getString(other) + " is " + OWLPrinter.getString(def));
                        changed.addAll(updateDefinitions(other, def));
                    }
                    else {
                        OWLObject val = DRBEval.resolve(other, notBot, inSignature, isDefinedAs);
                        //if(!(other instanceof OWLClassExpression) && val instanceof OWLClassExpression) System.out.println("culprit: " + other + " " + other.getClass() + " " + val + " " + val.getClass());
                        isDefinedAs.put(other, val);
                        if(debug) System.out.println("\t\tnew definition for " + OWLPrinter.getString(other) + " is " + OWLPrinter.getString(val));
                        changed.addAll(updateDefinitions(other, val));
                    }

                }
            }
        }
        return changed;
    }

    //public static int hurtInvariants = 0;

    private void rollback(OWLAxiom axiom){
        //We assume that the provided axiom cannot be made local or stopped being local
        //due to other rollbacks.
        //Rollback removes all definitions for the symbols in the axiom and makes them part
        //of the module signature. In turn, it executes rollback for each axiom depending
        //on a symbol of this axiom
        if(module.contains(axiom)) return;

        Set<OWLObject> changed = new HashSet<>();
        module.add(axiom);
        if(debug) System.out.println("adding axiom " + OWLPrinter.getString(axiom));
        for(OWLEntity e : axiom.getSignature()){
            //update status in the correct table
            addSignature(e);
            addQueue(e);
            isDefinedAs.remove(e);
            changed.add(e);
            changed.addAll(updateDefinitions(e, e));
            Set<OWLAxiom> affectedAxioms = objectsToAxioms.get(e);
            objectsToAxioms.remove(e);
            if(affectedAxioms != null){
                affectedAxioms.forEach(this::rollback);
            }
        }
        if(debug) System.out.println("added axiom signature to signature");
        //verify all affected
        changed.stream().filter(o -> !invariantHolds(o)).forEach(o -> {
            hurtInvariants++;
            Set<OWLAxiom> s = invariantAxioms.get(o);
            removeInvariant(o);
            if (s != null) {
                s.forEach(this::rollback);
            }
        });
    }
}
