package de.uniulm.in.ki.mbrenner.fame.abox.islands.snf;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 16.06.2016.
 */
public class ShallowClass extends ClassVisitorAdapterEx<Set<OWLClassExpression>>{
    private ShallowNormalForm parent;
    public ShallowClass(ShallowNormalForm parent){
        this.parent = parent;
    }

    private Set<OWLClassExpression> combineOr(Set<OWLClassExpression> op1, Set<OWLClassExpression> op2){
        Set<OWLClassExpression> andSet = new HashSet<>();
        for(OWLClassExpression a : op1){
            for(OWLClassExpression b : op2){
                Set<OWLClassExpression> un = new HashSet<>();
                if(a instanceof OWLObjectUnionOf) un.addAll(((OWLObjectUnionOf) a).getOperands());
                else un.add(a);

                if(b instanceof OWLObjectUnionOf) un.addAll(((OWLObjectUnionOf) b).getOperands());
                else un.add(b);

                andSet.add(parent.fact.getOWLObjectUnionOf(un));
            }
        }
        return andSet;
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectSomeValuesFrom oce){
        return Collections.singleton(oce);
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectAllValuesFrom oce){
        return Collections.singleton(oce);
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectComplementOf oce){
        return Collections.singleton(oce);
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLClass oce){
        return Collections.singleton(oce);
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectIntersectionOf oce){
        return oce.getOperands().stream().
                map(x -> x.accept(this)).
                flatMap(Collection::stream).
                collect(Collectors.toSet());
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectUnionOf oce){
        Iterator<OWLClassExpression> iter = oce.getOperands().iterator();
        Set<OWLClassExpression> current = iter.next().accept(this);

        while(iter.hasNext()){
            current = combineOr(current, iter.next().accept(this));
        }
        return current;
    }
}
