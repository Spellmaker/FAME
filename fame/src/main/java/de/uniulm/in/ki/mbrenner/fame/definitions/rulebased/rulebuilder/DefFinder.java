package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.ClassDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.CombinedPropertyDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.definition.IdClassDefinition;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Spellmaker on 13.05.2016.
 */
public class DefFinder extends OWLClassExpressionVisitorAdapter {
    private Set<DRBDefinition> definitions;
    private Stack<IndicatorClass> currentObject;
    private OWLObject otherSide;

    private DefFinder(){
        definitions = new HashSet<>();
        currentObject = new Stack<>();
    }

    public static Set<DRBDefinition> getDefinitions(OWLClassExpression defineAs, OWLClassExpression expression){
        DefFinder finder = new DefFinder();
        finder.currentObject.push(null);
        finder.otherSide = defineAs;
        expression.accept(finder);
        return finder.definitions;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expression){
        IndicatorClass id = new IndicatorClass(expression.getFiller());
        if(currentObject.peek() == null)
            definitions.add(new DRBDefinition(expression.getProperty(), otherSide, new CombinedPropertyDefinition(id)));
        else
            definitions.add(new DRBDefinition(expression.getProperty(), currentObject.peek(), new CombinedPropertyDefinition(id)));
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
            definitions.add(new DRBDefinition(expression, otherSide, new ClassDefinition()));
        else
            definitions.add(new DRBDefinition(expression, currentObject.peek(), new IdClassDefinition(currentObject.peek())));
    }
}
