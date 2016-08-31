package de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator;

import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.PropertyVisitorAdapterEx;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class PropertyGenerator extends PropertyVisitorAdapterEx<Set<Rule>>{
    private Generator parent;

    public PropertyGenerator(Generator parent){
        this.parent = parent;
    }

    @Override
    public @Nonnull Set<Rule> visit(@Nonnull OWLObjectProperty property){
        if(property.isTopEntity())          parent.botMode = false;
        else if(property.isBottomEntity())  parent.botMode = true;
        else                                parent.botMode = parent.interpretedAsBot.get(property);
        return Collections.emptySet();
    }
}
