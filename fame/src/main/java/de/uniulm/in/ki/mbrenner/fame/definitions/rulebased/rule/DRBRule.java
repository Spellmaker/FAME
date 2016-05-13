package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.DRBDefinition;
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

    public DRBRule(OWLObject head, OWLAxiom axiom, @Nonnull Set<DRBDefinition> definitions, @Nonnull OWLObject...body){
        this.body = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(body)));
        this.head = head;
        this.axiom = axiom;
        this.definitions = Collections.unmodifiableSet(definitions);
    }

    public @Nonnull OWLObject getHeadOrAxiom(){
        return (head == null) ? axiom : head;
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
            res += OWLRendererProvider.render(o) + ", ";
        }

        if(body.size() > 0){
            res = res.substring(0, res.length() - 2);
        }

        res += " -> " + OWLRendererProvider.render(getHeadOrAxiom());
        if(!definitions.isEmpty()){
            res += "def: ";
            for(DRBDefinition def : definitions){
                res += def.toString();
            }
        }

        return res;
    }

}
