package de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapterEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class ClassGenerator extends ClassVisitorAdapterEx<Set<Rule>>{
    private Generator parent;

    public ClassGenerator(Generator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLClass expr){
        if(expr.isBottomEntity())   parent.botMode = true;
        else if(expr.isTopEntity()) parent.botMode = false;
        else                        parent.botMode = parent.interpretedAsBot.get(expr);
        return Collections.emptySet();
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLObjectIntersectionOf expr){
        boolean foundBot = false;
        Set<Rule> rules = new HashSet<>();
        for(OWLClassExpression oce : expr.getOperands()){
            Set<Rule> tmp = oce.accept(this);
            if(parent.botMode){
                if(!foundBot){
                    foundBot = true;
                    rules.clear();
                }
                rules.addAll(tmp);
            }
            else if(!foundBot){
                rules.addAll(tmp);
            }
        }

        parent.botMode = foundBot;
        int id = parent.get(expr);
        if(foundBot){
            Integer[] body = new Integer[expr.getOperands().size()];
            Iterator<OWLClassExpression> iter = expr.getOperands().iterator();
            for(int i = 0; i < body.length; i++){
                body[i] = parent.get(iter.next());
            }
            rules.add(new Rule(id, null, null, body));//        id, -1, body));
        }
        else{
            for(OWLClassExpression oce : expr.getOperands()){
                rules.add(new Rule(id, null, null, parent.get(oce)));
            }
        }
        return rules;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLObjectSomeValuesFrom expr){
        Set<Rule> prules = expr.getProperty().accept(parent.propertyGenerator);
        boolean propMode = parent.botMode;
        Set<Rule> frules = expr.getFiller().accept(parent.classGenerator);
        Set<Rule> result = new HashSet<>();
        if(propMode){
            if(parent.botMode){
                parent.botMode = true;
                result.addAll(prules);
                result.addAll(frules);
                result.add(new Rule(parent.get(expr), null, null, parent.get(expr.getFiller()), parent.get(expr.getProperty())));
            }
            else{
                parent.botMode = true;
                result.addAll(prules);
                result.add(new Rule(parent.get(expr), null, null, parent.get(expr.getProperty())));
            }
        }
        else{
            if(parent.botMode){
                parent.botMode = true;
                result.addAll(frules);
                result.add(new Rule(parent.get(expr), null, null, parent.get(expr.getFiller())));
            }
            else{
                parent.botMode = false;
                result.addAll(frules);
                result.addAll(prules);
                int id = parent.get(expr);
                result.add(new Rule(id,null, null,  parent.get(expr.getFiller())));
                result.add(new Rule(id,null, null,  parent.get(expr.getProperty())));
            }
        }
        return result;
    }
}
