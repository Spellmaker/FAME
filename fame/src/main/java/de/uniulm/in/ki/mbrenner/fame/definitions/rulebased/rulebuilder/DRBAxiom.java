package de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBDefinition;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapter;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDisjointClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Axiom visitor for the DRBRuleBuilder
 *
 * Created by spellmaker on 25.05.2016.
 */
class DRBAxiom extends AxiomVisitorAdapterEx<Set<DRBRule>> {
    private final DRBRuleBuilder parent;

    public DRBAxiom(DRBRuleBuilder parent){
        this.parent = parent;
    }

    @Override
    public Set<DRBRule> visit(OWLSubClassOfAxiom axiom){
        Set<DRBRule> result = new HashSet<>();
        Set<DRBRule> sub = axiom.getSubClass().accept(parent.classVisitor);
        boolean subMode = parent.botMode;
        Set<DRBRule> sup = axiom.getSuperClass().accept(parent.classVisitor);
        if(subMode){
            result.addAll(sub);
            if(parent.botMode){
                result.add(DRBRuleFactory.getExternalRule(
                        axiom,
                        DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSubClass()
                ));
            }
            else{
                result.addAll(sup);
                result.add(DRBRuleFactory.getExternalRule(
                        axiom,
                        DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSubClass(), axiom.getSuperClass()
                ));
            }
        }
        else{
            if(parent.botMode){
                return Collections.singleton(DRBRuleFactory.getExternalRule(axiom));
            }
            else{
                result.addAll(sup);
                result.add(DRBRuleFactory.getExternalRule(
                        axiom,
                        DefFinder.getDefinitions(axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSuperClass()));
            }
        }

        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLSubObjectPropertyOfAxiom axiom){
        if(axiom.getSuperProperty().isTopEntity() || axiom.getSubProperty().isBottomEntity()) return Collections.emptySet();

        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom,
                DefFinder.getDefinitions(axiom.getSubProperty(), axiom.getSuperProperty()), axiom.getSubProperty()));
    }

    @Override
    public Set<DRBRule> visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        Set<DRBRule> result = new HashSet<>(left.accept(parent.classVisitor));
        boolean state = parent.botMode;
        result.addAll(right.accept(parent.classVisitor));

        if(state != parent.botMode){
            if(!state){
                return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left)));
            }
            else{
                return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right)));
            }
        }
        else{
            result.add(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(left, right), left));
            result.add(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(right, left), right));
        }
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLTransitiveObjectPropertyAxiom axiom){
        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
    }

    @Override
    public Set<DRBRule> visit(OWLReflexiveObjectPropertyAxiom axiom){
        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
    }

    @Override
    public Set<DRBRule> visit(OWLObjectPropertyDomainAxiom axiom){
        Set<DRBRule> result = new HashSet<>(axiom.getDomain().accept(parent.classVisitor));
        if(parent.botMode){
            result.clear();
            result.add(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
        }
        else{
            result.add(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty(), axiom.getDomain()));
        }
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLObjectPropertyRangeAxiom axiom){
        Set<DRBRule> result = new HashSet<>(axiom.getRange().accept(parent.classVisitor));
        if(parent.botMode){
            result.clear();
            result.add(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty()));
        }
        else{
            result.add(DRBRuleFactory.getExternalRule(axiom, axiom.getProperty(), axiom.getRange()));
        }
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLClassAssertionAxiom axiom){
        //return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(new OWLDataFactoryImpl().getOWLThing(), axiom.getClassExpression())));
        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom));
    }

    @Override
    public Set<DRBRule> visit(OWLObjectPropertyAssertionAxiom axiom){
        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom));
        //return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(new OWLDataFactoryImpl().getOWLTopObjectProperty(), axiom.getProperty())));
    }

    @Override
    public Set<DRBRule> visit(OWLDisjointClassesAxiom axiom){
        //classes all need to be pairwise disjoint
        //this is the case if at most one class is in topmode and only one class is not bot
        List<Set<DRBRule>> rules = new LinkedList<>();
        boolean found = false;
        for(OWLClassExpression oce : axiom.getClassExpressions()){
            Set<DRBRule> tmp = oce.accept(parent.classVisitor);
            if(!parent.botMode){
                if(found){
                    return Collections.singleton(DRBRuleFactory.getExternalRule(axiom));
                }
                found = true;
            }
            else{
                rules.add(tmp);
            }
        }

        Set<DRBRule> result = new HashSet<>(rules.stream().flatMap(Collection::stream).collect(Collectors.toSet()));
        if(found){
            for(OWLClassExpression oce : axiom.getClassExpressions()){
                result.add(DRBRuleFactory.getExternalRule(axiom, oce));
            }
        }
        else{
            List<OWLClassExpression> list = axiom.getClassExpressionsAsList();
            for(int i = 0; i < list.size() - 1; i++){
                for(int j = i + 1; j < list.size(); j++){
                    result.add(DRBRuleFactory.getExternalRule(axiom, list.get(i), list.get(j)));
                }
            }
        }
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLSubPropertyChainOfAxiom axiom){
        if(axiom.getSuperProperty().isTopEntity() || axiom.getPropertyChain().stream().filter(x -> x.isBottomEntity()).count() > 0) return Collections.emptySet();
        Set<OWLObjectPropertyExpression> chain = new HashSet<>(axiom.getPropertyChain());
        return Collections.singleton(DRBRuleFactory.getExternalRule(axiom, chain.toArray(new OWLObject[chain.size()])));
    }

    @Override
    public Set<DRBRule> visit(OWLDeclarationAxiom axiom){
        return Collections.emptySet();
    }

    @Override
    public Set<DRBRule> visit(OWLSameIndividualAxiom axiom){
        return axiom.getIndividuals().stream().map(x -> DRBRuleFactory.getExternalRule(axiom, x)).collect(Collectors.toSet());
    }

    @Override
    public Set<DRBRule> visit(OWLEquivalentObjectPropertiesAxiom axiom){
        Iterator<OWLObjectPropertyExpression> iter = axiom.getProperties().iterator();
        OWLObjectPropertyExpression first = iter.next();
        OWLObjectPropertyExpression second = iter.next();

        Set<DRBRule> result = new HashSet<>();
        result.add(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(second, first), second));
        result.add(DRBRuleFactory.getExternalRule(axiom, DefFinder.getDefinitions(first, second), first));
        return result;
    }

    @Override
    public Set<DRBRule> visit(OWLDifferentIndividualsAxiom axiom){
        return axiom.getIndividuals().stream().map(x -> DRBRuleFactory.getExternalRule(axiom, x)).collect(Collectors.toSet());
    }
}
