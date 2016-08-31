package de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class AxiomGenerator extends AxiomVisitorAdapterEx<Set<Rule>>{
    private Generator parent;

    public AxiomGenerator(Generator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLSubClassOfAxiom axiom){
        Set<Rule> result = new HashSet<>();
        Set<Rule> subRules = axiom.getSubClass().accept(parent.classGenerator);
        boolean subMode = parent.botMode;
        Set<Rule> supRules = axiom.getSuperClass().accept(parent.classGenerator);
        boolean supMode = parent.botMode;

        int id = parent.get(axiom);

        if(subMode){
            if(supMode){
                result.addAll(subRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSubClass())));
            }
            else{
                result.addAll(subRules);
                result.addAll(supRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSubClass()), parent.get(axiom.getSuperClass())));
            }
        }
        else{
            if(supMode){
                parent.base.add(axiom);
            }
            else{
                result.addAll(supRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSuperClass())));
            }
        }
        return result;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom){
        Set<Rule> result = new HashSet<>();
        Set<Rule> subRules = axiom.getSubProperty().accept(parent.propertyGenerator);
        boolean subMode = parent.botMode;
        Set<Rule> supRules = axiom.getSuperProperty().accept(parent.propertyGenerator);
        boolean supMode = parent.botMode;

        int id = parent.get(axiom);

        if(subMode){
            if(supMode){
                result.addAll(subRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSubProperty())));
            }
            else{
                result.addAll(subRules);
                result.addAll(supRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSubProperty()), parent.get(axiom.getSuperProperty())));
            }
        }
        else{
            if(supMode){
                parent.base.add(axiom);
                return Collections.emptySet();
            }
            else{
                result.addAll(supRules);
                result.add(new Rule(null, id, null, parent.get(axiom.getSuperProperty())));
            }
        }
        return result;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLEquivalentClassesAxiom axiom){
        Iterator<OWLClassExpression> iter = axiom.getClassExpressions().iterator();

        Set<Rule> result = new HashSet<>();
        int id = parent.get(axiom);
        OWLClassExpression current = iter.next();
        result.addAll(current.accept(parent.classGenerator));
        result.add(new Rule(null, id, null, parent.get(current)));
        boolean firstMode = parent.botMode;
        while(iter.hasNext()){
            current = iter.next();
            result.addAll(current.accept(parent.classGenerator));
            if(firstMode != parent.botMode){
                parent.base.add(axiom);
                return Collections.emptySet();
            }
            result.add(new Rule(null, id, null, parent.get(current)));
        }
        return result;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom){
        Set<Rule> rules = new HashSet<>();
        rules.addAll(axiom.getProperty().accept(parent.propertyGenerator));
        rules.add(new Rule(null, parent.get(axiom), null, parent.get(axiom.getProperty())));
        return rules;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLDeclarationAxiom axiom){
        Set<Rule> rules = new HashSet<>();
        if(axiom.getEntity() instanceof OWLClass){
            rules.addAll(((OWLClass) axiom.getEntity()).accept(parent.classGenerator));
        }
        else if(axiom.getEntity() instanceof OWLObjectProperty){
            rules.addAll(((OWLObjectProperty) axiom.getEntity()).accept(parent.propertyGenerator));
        }

        rules.add(new Rule(null, parent.get(axiom), null, parent.get(axiom.getEntity())));
        return rules;
    }
}
