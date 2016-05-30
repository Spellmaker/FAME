package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.incremental.OWLDictionary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Constructs definitions which define one class or property expression as another
 *
 * Created by Spellmaker on 13.05.2016.
 */
class IDefFinder extends OWLClassExpressionVisitorAdapter {
    private final Set<IDRBDefinition> definitions;
    private final Stack<IndicatorClass> currentObject;
    private OWLObject otherSide;
    private OWLDictionary provider;

    private IDefFinder(OWLDictionary provider){
        definitions = new HashSet<>();
        currentObject = new Stack<>();
        this.provider = provider;
    }

    /**
     * Provides definitions which define one class expression as another
     * @param defineAs The target value
     * @param expression The source value
     * @return A set of definitions which define expression as defineAs
     */
    public static Set<IDRBDefinition> getDefinitions(OWLDictionary provider, OWLClassExpression defineAs, OWLClassExpression expression){
        IDefFinder finder = new IDefFinder(provider);
        finder.currentObject.push(null);
        finder.otherSide = defineAs;
        expression.accept(finder);
        //Note: For a simplified version, we first only allow definitions, if the
        //left hand side is a simple class
        if(!(defineAs instanceof OWLClass)) return Collections.emptySet();
        return finder.definitions;
    }
    /**
     * Provides definitions which define one property expression as another
     *
     * Currently only works for ObjectProperties, as EL does not support any complex property expressions
     * @param defineAs The target value
     * @param expression The source value
     * @return A set of definitions which define expression as defineAs
     */
    public static Set<IDRBDefinition> getDefinitions(OWLDictionary provider, OWLObjectPropertyExpression defineAs, OWLObjectPropertyExpression expression){
        return Collections.singleton(new IDRBDefinition(provider.getId(expression), provider.getId(defineAs)));
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        IndicatorClass id = new IndicatorClass(expression.getFiller());
        if(currentObject.peek() == null)
            definitions.add(new IDRBDefinition(provider.getId(expression.getProperty()), provider.getId(new CombinedObjectProperty((OWLClassExpression) otherSide, id))));
        else
            definitions.add(new IDRBDefinition(provider.getId(expression.getProperty()), provider.getId(new CombinedObjectProperty(currentObject.peek(), id))));
        currentObject.push(id);
        expression.getFiller().accept(this);
        currentObject.pop();
    }

    @Override
    public void visit(OWLObjectIntersectionOf expression){
        for(OWLClassExpression oce : expression.getOperands()){
            oce.accept(this);
        }
    }

    @Override
    public void visit(OWLClass expression){
        if(currentObject.peek() == null)
            definitions.add(new IDRBDefinition(provider.getId(expression), provider.getId(otherSide)));
        else
            definitions.add(new IDRBDefinition(provider.getId(expression), provider.getId(currentObject.peek())));
    }
}
