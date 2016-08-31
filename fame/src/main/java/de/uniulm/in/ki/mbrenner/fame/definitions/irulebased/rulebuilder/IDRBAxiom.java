package de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rulebuilder;

import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.irulebased.rule.IDRBRuleFactory;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRule;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleFactory;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.AxiomVisitorAdapterEx;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Axiom visitor for the IDRBRuleBuilder
 *
 * Created by spellmaker on 25.05.2016.
 */
class IDRBAxiom extends AxiomVisitorAdapterEx<Set<IDRBRule>> {
    private final IDRBRuleBuilder parent;

    public IDRBAxiom(IDRBRuleBuilder parent){
        this.parent = parent;
    }

    @Override
    public Set<IDRBRule> visit(OWLSubClassOfAxiom axiom){
        Set<IDRBRule> result;
        Set<IDRBRule> sub = axiom.getSubClass().accept(parent.classVisitor);
        boolean subMode = parent.botMode;
        Set<IDRBRule> sup = axiom.getSuperClass().accept(parent.classVisitor);
        if(subMode){
            result = new HashSet<>(sub);
            if(parent.botMode){
                result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet,
                        axiom,
                        IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSubClass()
                ));
            }
            else{
                result.addAll(sup);
                result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet,
                        axiom,
                        IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSubClass(), axiom.getSuperClass()
                ));
            }
        }
        else{
            if(parent.botMode){
                return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom));
            }
            else{
                result = new HashSet<>(sup);
                result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet,
                        axiom,
                        IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubClass(), axiom.getSuperClass()),
                        axiom.getSuperClass()));
            }
        }

        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLSubObjectPropertyOfAxiom axiom){
        if(axiom.getSuperProperty().isTopEntity() || axiom.getSubProperty().isBottomEntity()) return Collections.emptySet();

        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom,
                IDefFinder.getDefinitions(parent.ruleSet, axiom.getSubProperty(), axiom.getSuperProperty()), axiom.getSubProperty()));
    }

    @Override
    public Set<IDRBRule> visit(OWLEquivalentClassesAxiom axiom){
        OWLClassExpression left = axiom.getClassExpressionsAsList().get(0);
        OWLClassExpression right = axiom.getClassExpressionsAsList().get(1);

        Set<IDRBRule> result = new HashSet<>(left.accept(parent.classVisitor));
        boolean state = parent.botMode;
        result.addAll(right.accept(parent.classVisitor));

        if(state != parent.botMode){
            if(!state){
                return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, right, left)));
            }
            else{
                return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, left, right)));
            }
        }
        else{
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, left, right), left));
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, right, left), right));
        }
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLTransitiveObjectPropertyAxiom axiom){
        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty()));
    }

    @Override
    public Set<IDRBRule> visit(OWLReflexiveObjectPropertyAxiom axiom){
        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty()));
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectPropertyDomainAxiom axiom){
        Set<IDRBRule> result = new HashSet<>(axiom.getDomain().accept(parent.classVisitor));
        if(parent.botMode){
            result.clear();
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty()));
        }
        else{
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty(), axiom.getDomain()));
        }
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectPropertyRangeAxiom axiom){
        Set<IDRBRule> result = new HashSet<>(axiom.getRange().accept(parent.classVisitor));
        if(parent.botMode){
            result.clear();
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty()));
        }
        else{
            result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, axiom.getProperty(), axiom.getRange()));
        }
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLClassAssertionAxiom axiom){
        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom));
        //return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, new OWLDataFactoryImpl().getOWLThing(), axiom.getClassExpression())));
    }

    @Override
    public Set<IDRBRule> visit(OWLObjectPropertyAssertionAxiom axiom){
        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom));
       // return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, new OWLDataFactoryImpl().getOWLTopObjectProperty(), axiom.getProperty())));
    }

    @Override
    public Set<IDRBRule> visit(OWLDisjointClassesAxiom axiom){
        //classes all need to be pairwise disjoint
        //this is the case if at most one class is in topmode and only one class is not bot
        List<Set<IDRBRule>> rules = new LinkedList<>();
        boolean found = false;
        for(OWLClassExpression oce : axiom.getClassExpressions()){
            Set<IDRBRule> tmp = oce.accept(parent.classVisitor);
            if(!parent.botMode){
                if(found){
                    return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom));
                }
                found = true;
            }
            else{
                rules.add(tmp);
            }
        }

        Set<IDRBRule> result = new HashSet<>(rules.stream().flatMap(Collection::stream).collect(Collectors.toSet()));
        if(found){
            for(OWLClassExpression oce : axiom.getClassExpressions()){
                result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, oce));
            }
        }
        else{
            List<OWLClassExpression> list = axiom.getClassExpressionsAsList();
            for(int i = 0; i < list.size() - 1; i++){
                for(int j = i + 1; j < list.size(); j++){
                    result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, list.get(i), list.get(j)));
                }
            }
        }
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLSubPropertyChainOfAxiom axiom){
        if(axiom.getSuperProperty().isTopEntity() || axiom.getPropertyChain().stream().filter(x -> x.isBottomEntity()).count() > 0) return Collections.emptySet();
        Set<OWLObjectPropertyExpression> chain = new HashSet<>(axiom.getPropertyChain());
        return Collections.singleton(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, chain.toArray(new OWLObject[chain.size()])));
    }

    @Override
    public Set<IDRBRule> visit(OWLDeclarationAxiom axiom){
        return Collections.emptySet();
    }

    @Override
    public Set<IDRBRule> visit(OWLSameIndividualAxiom axiom){
        return axiom.getIndividuals().stream().map(x -> IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, x)).collect(Collectors.toSet());
    }

    @Override
    public Set<IDRBRule> visit(OWLEquivalentObjectPropertiesAxiom axiom){
        Iterator<OWLObjectPropertyExpression> iter = axiom.getProperties().iterator();
        OWLObjectPropertyExpression first = iter.next();
        OWLObjectPropertyExpression second = iter.next();

        Set<IDRBRule> result = new HashSet<>();
        result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, second, first), second));
        result.add(IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, IDefFinder.getDefinitions(parent.ruleSet, first, second), first));
        return result;
    }

    @Override
    public Set<IDRBRule> visit(OWLDifferentIndividualsAxiom axiom){
        return axiom.getIndividuals().stream().map(x -> IDRBRuleFactory.getExternalRule(parent.ruleSet, axiom, x)).collect(Collectors.toSet());
    }
}
