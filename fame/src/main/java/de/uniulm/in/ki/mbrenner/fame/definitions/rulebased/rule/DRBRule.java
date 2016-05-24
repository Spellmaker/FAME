package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.util.printer.OWLPrinter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DRBRule implements Iterable<OWLObject>{
    public final List<OWLObject> body;
    public final OWLObject head;
    public final OWLAxiom axiom;
    public final Set<DRBDefinition> definitions;
    public int id;

    public DRBRule(OWLObject head, OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        this.body = Collections.unmodifiableList(Arrays.asList(body));
        this.head = head;
        this.axiom = axiom;
        this.definitions = Collections.unmodifiableSet(definitions);
    }

    public @Nonnull OWLObject getHeadOrAxiom(){
        return (head == null) ? axiom : head;
    }

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
