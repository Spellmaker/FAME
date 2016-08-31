package de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure.RoleNameFinder;

import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by spellmaker on 17.06.2016.
 */
public class RoleNameFinder {
    public static Set<OWLObjectPropertyExpression> getProperties(Set<OWLClassExpression> snf){
        ClassVisitor classVisitor = new ClassVisitor();
        snf.forEach(x -> x.accept(classVisitor));
        return classVisitor.result;
    }
}

class ClassVisitor extends ClassVisitorAdapter {
    Set<OWLObjectPropertyExpression> result;
    public ClassVisitor(){
        result = new HashSet<>();
    }

    @Override
    public void visit(@Nonnull OWLObjectIntersectionOf expr){
        expr.getOperands().forEach(x -> x.accept(this));
    }

    @Override
    public void visit(@Nonnull OWLObjectUnionOf expr){
        expr.getOperands().forEach(x -> x.accept(this));
    }

    @Override
    public void visit(@Nonnull OWLObjectSomeValuesFrom expr){
        result.add(expr.getProperty());
        expr.getFiller().accept(this);
    }

    @Override
    public void visit(@Nonnull OWLObjectAllValuesFrom expr){
        result.add(expr.getProperty());
        expr.getFiller().accept(this);
    }

    @Override
    public void visit(@Nonnull OWLObjectComplementOf expr){
        expr.getOperand().accept(this);
    }

    @Override
    public void visit(@Nonnull OWLClass expr){
        return;
    }
}