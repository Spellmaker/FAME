package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBDefinition;
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
class DefFinder extends OWLClassExpressionVisitorAdapter {
    private final Set<DRBDefinition> definitions;
    private final Stack<IndicatorClass> currentObject;
    private OWLObject otherSide;

    private DefFinder(){
        definitions = new HashSet<>();
        currentObject = new Stack<>();
    }

    /**
     * Provides definitions which define one class expression as another
     * @param defineAs The target value
     * @param expression The source value
     * @return A set of definitions which define expression as defineAs
     */
    public static Set<DRBDefinition> getDefinitions(OWLClassExpression defineAs, OWLClassExpression expression){
        DefFinder finder = new DefFinder();
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
    public static Set<DRBDefinition> getDefinitions(OWLObjectPropertyExpression defineAs, OWLObjectPropertyExpression expression){
        return Collections.singleton(new DRBDefinition(expression, defineAs));
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        IndicatorClass id = new IndicatorClass(expression.getFiller());
        if(currentObject.peek() == null)
            definitions.add(new DRBDefinition(expression.getProperty(), new CombinedObjectProperty((OWLClassExpression) otherSide, id)));
        else
            definitions.add(new DRBDefinition(expression.getProperty(), new CombinedObjectProperty(currentObject.peek(), id)));
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
            definitions.add(new DRBDefinition(expression, otherSide));
        else
            definitions.add(new DRBDefinition(expression, currentObject.peek()));
    }
}
