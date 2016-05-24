package de.uniulm.in.ki.mbrenner.fame.definitions.builder;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;

/**
 * Property visitor for the DefinitionBuilder
 *
 * Created by spellmaker on 28.04.2016.
 */
public class DBPropertyVisitor implements OWLPropertyExpressionVisitor {
    //OWLPropertyExpression currentProperty;
    DefinitionBuilder parent;

    public DBPropertyVisitor(DefinitionBuilder parent){
        this.parent = parent;
    }

    @Override
    public void visit(@Nonnull OWLObjectProperty owlObjectProperty) {
        if(parent.signature.contains(owlObjectProperty)){
            if(parent.currentTarget.equals(owlObjectProperty)){
                return;
            }
            parent.error = true;
            return;
        }
        OWLObjectProperty def = (OWLObjectProperty) parent.definitions.get(owlObjectProperty);
        if(def != null){
            if(def.equals(parent.currentTarget)){
                return;
            }
            parent.error = true;
            return;
        }
        parent.definitions.put(owlObjectProperty, parent.currentTarget);
        parent.dependent.add(owlObjectProperty);
    }

    //below is not allowed

    @Override
    public void visit(@Nonnull OWLObjectInverseOf owlObjectInverseOf) {

    }

    @Override
    public void visit(@Nonnull OWLDataProperty owlDataProperty) {

    }

    @Override
    public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {

    }
}
