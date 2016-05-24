package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;

import static de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter.getString;

/**
 * Extracts a definition based locality module from a given ontology
 * The current implementation does not perform all possible definitions,
 * but instead relies on simple definitions, which only define complex expressions
 * via single concepts
 * Created by spellmaker on 20.05.2016.
 */
public class DRBExtractor{
    private Queue<OWLObject> procQueue;
    private int[] ruleCounter;
    private Set<OWLObject> notBot;
    private Set<OWLObject> inSignature;

    private Map<OWLObject, OWLObject> isDefinedAs;
    private Map<OWLObject, Set<OWLObject>> isReasonFor;
    private Map<OWLObject, Set<OWLObject>> invariants;
    private Map<OWLObject, Set<OWLAxiom>> invariantAxioms;

    private Map<OWLEntity, Set<OWLAxiom>> objectsToAxioms;

    private Set<OWLAxiom> module;
    private Set<OWLAxiom> extModule;

    public static boolean debug = false;

    public DRBExtractor(){
        debug = false;
    }

    public DRBExtractor(boolean d){
        debug = d;
    }

    private void addQueue(OWLObject o){
        if(notBot.add(o)) procQueue.add(o);
    }

    private void addResponsible(OWLObject symbol, OWLObject cause){
        Set<OWLObject> set = isReasonFor.get(cause);
        if(set == null){
            set = new HashSet<>();
            isReasonFor.put(cause, set);
        }
        set.add(symbol);
    }

    public Map<OWLObject, OWLObject> getDefinitions(){
        Map<OWLObject, OWLObject> defs = new HashMap<>();
        for(Map.Entry<OWLObject, OWLObject> e : isDefinedAs.entrySet()){
            if(!inSignature.contains(e.getKey())) defs.put(e.getKey(), e.getValue());
        }
        return Collections.unmodifiableMap(defs);
    }

    public Set<OWLAxiom> extractModule(@Nonnull DRBRuleSet rules, @Nonnull Set<OWLEntity> signature){
        procQueue = new LinkedList<>();
        ruleCounter = new int[rules.size()];
        notBot = new HashSet<>();
        inSignature = new HashSet<>();
        isDefinedAs = new HashMap<>();
        isReasonFor = new HashMap<>();
        objectsToAxioms = new HashMap<>();
        extModule = new HashSet<>();
        invariants = new HashMap<>();
        invariantAxioms = new HashMap<>();

        module = new HashSet<>();

        for(OWLEntity e : signature){
            procQueue.add(e);
            notBot.add(e);
            inSignature.add(e);
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
                        //actually handle definitions in here
                        Set<OWLEntity> dependentOn = tryDefine(cRule);
                        extModule.add(cRule.axiom);
                        if(dependentOn == null){
                            if(debug) System.out.println("cannot establish definition, rolling back");
                            //roll back and re-establish a stable state
                            rollback(cRule.axiom);
                        }
                        else{
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
        //attempt to use the definition implied by the provided rule
        Set<OWLEntity> dependentOn = new HashSet<>();
        //only if there even is a definition
        if(!cRule.definitions.isEmpty()){
            //can only be done if all symbols can be defined
            for(DRBDefinition def : cRule.definitions){
                OWLEntity symbol = (OWLEntity) def.definedSymbol;
                //axiom relies on the definition of the symbol
                dependentOn.add(symbol);
                //find out what the symbol needs to be defined as
                OWLObject defAs = def.definition.action(def.definingSymbol);
                //resolve the definition to the definition set it belongs to
                OWLObject defAsResolved = inSignature.contains(defAs) ? defAs : isDefinedAs.get(defAs);
                if(defAsResolved == null) defAsResolved = defAs;
                //find out if there is already a definition for the symbol or if it is in the signature
                OWLObject existingDef = inSignature.contains(symbol) || symbol.isTopEntity() ? symbol : isDefinedAs.get(symbol);

                if(existingDef == null){
                    //if there is no definition, make one as needed
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

    private boolean invariantHolds(OWLObject o1){
        //checks if the invariants for this objects hold
        Set<OWLObject> s = invariants.get(o1);
        if(s == null) return true;

        boolean o1InSig = inSignature.contains(o1);
        for(OWLObject o2 : s){
            boolean o2InSig = inSignature.contains(o2);
            if(o1InSig && o2InSig) return false;

            OWLObject o1Val = o1InSig ? o1 : isDefinedAs.get(o1); o1Val = o1Val == null ? o1 : o1Val;
            OWLObject o2Val = o2InSig ? o2 : isDefinedAs.get(o2); o2Val = o2Val == null ? o2 : o2Val;

            if(!o1Val.equals(o2Val)) return false;
        }
        return true;
    }

    private void removeInvariant(OWLObject o1){
        Set<OWLObject> s = invariants.get(o1);
        invariants.remove(o1);
        invariantAxioms.remove(o1);
        if(s != null){
            for(OWLObject o2 : s){
                removeInvariant(o2);
            }
        }
    }

    private Set<OWLObject> updateResponsible(OWLObject o, OWLObject def){
        //updates the definitions by following subsumption chains
        //and updating the pointers to the definitions
        //also returns a list of symbols, for whom the final definition
        //changed so that the invariants can get checked later
        Set<OWLObject> changed = new HashSet<>();
        Set<OWLObject> s = isReasonFor.get(o);
        if(s != null){
            for(OWLObject other : s){
                s.add(other);
                isDefinedAs.put(other, def);
                s.addAll(updateResponsible(other, def));
            }
        }
        return changed;
    }

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
            inSignature.add(e);
            addQueue(e);
            isDefinedAs.remove(e);
            changed.addAll(updateResponsible(e, e));
            Set<OWLAxiom> affectedAxioms = objectsToAxioms.get(e);
            objectsToAxioms.remove(e);
            if(affectedAxioms != null){
                affectedAxioms.forEach(x -> rollback(x));
            }
        }
        //verify all affected
        for(OWLObject o : changed){
            if(!invariantHolds(o)){
                Set<OWLAxiom> s = invariantAxioms.get(o);
                removeInvariant(o);
                if(s != null){
                    s.forEach(x -> rollback(x));
                }
            }
        }
    }
}
