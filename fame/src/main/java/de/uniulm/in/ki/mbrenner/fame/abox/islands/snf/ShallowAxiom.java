package de.uniulm.in.ki.mbrenner.fame.abox.islands.snf;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 16.06.2016.
 */
public class ShallowAxiom extends AxiomVisitorAdapterEx<Set<OWLClassExpression>> {
    private ShallowNormalForm parent;
    public ShallowAxiom(ShallowNormalForm parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLDeclarationAxiom axiom){
        return Collections.emptySet();
    }
    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom){
        return Collections.emptySet();
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom){
        return Collections.emptySet();
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLSubClassOfAxiom axiom){
        OWLClassExpression  expr = parent.fact.getOWLObjectUnionOf(axiom.getSuperClass(), parent.fact.getOWLObjectComplementOf(axiom.getSubClass()));
        expr = expr.getNNF();
        return expr.accept(parent.shallowClass);
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLEquivalentClassesAxiom axiom){
        return axiom.asOWLSubClassOfAxioms().stream().
                map(x -> x.accept(this)).
                flatMap(Collection::stream).
                collect(Collectors.toSet());
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLClassAssertionAxiom axiom){
        return Collections.emptySet();
    }

    @Override
    public @Nonnull Set<OWLClassExpression> visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom){
        return Collections.emptySet();
    }
}
