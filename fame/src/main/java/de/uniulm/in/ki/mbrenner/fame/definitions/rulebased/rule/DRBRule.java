package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a derivation rule for the module extraction, which also supports definitions
 *
 * The rule can be used as an iterable and will then provide access to the elements in its body
 *
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRule implements Iterable<OWLObject>{
    /**
     * The rules body
     * The rule triggers, if all elements in the body are interpreted with values different from bottom
     */
    public final List<OWLObject> body;
    /**
     * The head of the rule, if it is an intermediate rule
     * If the execution of the rule adds an axiom, the head is set to null
     */
    public final OWLObject head;
    /**
     * The axiom of the rule, if it is an external rule
     * If the execution of the rule adds an intermediate symbol, the axiom is set to null
     */
    public final OWLAxiom axiom;
    /**
     * The definitions associated with this rule
     * These definitions provide a way of avoiding the execution of the rule by assuming some values for other concepts or roles
     */
    public final Set<DRBDefinition> definitions;
    /**
     * The id of this rule relative to some rule set
     * This field is set when the rule is added to a rule set to be able to refer to rules via consecutive ids
     */
    public int id;

    /**
     * Constructs a new rule object
     * @param head The head of the rule; exactly one of head and axiom needs to be different from null
     * @param axiom The axiom of the rule; exactly one of head and axiom needs to be different from null
     * @param definitions The definitions associated with this rule
     * @param body The body of the rule
     */
    public DRBRule(OWLObject head, OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        if(head == null && axiom == null) throw new IllegalArgumentException("Cannot construct a rule without head and axiom");
        if(head != null && axiom != null) throw new IllegalArgumentException("Cannot construct a rule with both head and axiom");
        this.body = Collections.unmodifiableList(Arrays.asList(body));
        this.head = head;
        this.axiom = axiom;
        this.definitions = Collections.unmodifiableSet(definitions);
    }

    /**
     * Provides access to the head or axiom of the rule, returning whichever is different from null
     * @return The nonnull element of the two fields head and axiom
     */
    public @Nonnull OWLObject getHeadOrAxiom(){
        return (head == null) ? axiom : head;
    }

    /**
     * The size of the rule body
     * @return The number of elements in the rules body
     */
    public int size(){
        return body.size();
    }

    @Override
    public Iterator<OWLObject> iterator() {
        return body.iterator();
    }

    @Override
    public void forEach(Consumer<? super OWLObject> action) {
        body.forEach(action);
    }

    @Override
    public Spliterator<OWLObject> spliterator() {
        return body.spliterator();
    }

    @Override
    public String toString(){
        String res = "";
        for(OWLObject o : body){
            res += OWLPrinter.getString(o) + ", ";
        }

        if(body.size() > 0){
            res = res.substring(0, res.length() - 2);
        }

        res += " -> " + OWLPrinter.getString(getHeadOrAxiom());
        if(!definitions.isEmpty()){
            res += " def: ";
            for(DRBDefinition def : definitions){
                res += def.toString();
            }
        }

        return res;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof DRBRule){
            DRBRule other = (DRBRule) o;
            return getHeadOrAxiom().equals(other.getHeadOrAxiom()) && definitions.equals(other.definitions) && body.equals(other.body);
        }
        return false;
    }

    @Override
    public int hashCode(){
        int h = 0;
        if(head != null) h += head.hashCode();
        if(axiom != null) h += axiom.hashCode();
        h += definitions.hashCode();
        h += body.hashCode();
        return h;
    }
}
