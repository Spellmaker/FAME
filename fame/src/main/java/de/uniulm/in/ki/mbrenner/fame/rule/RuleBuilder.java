package de.uniulm.in.ki.mbrenner.fame.rule;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Interface for classes which compile OWL ontologies of different expressivities into rules.
 * These rules can be used to extract modules out of the source ontologie.
 * @author spellmaker
 *
 */
public interface RuleBuilder {
	/**
	 * Compiles the given OWL axioms into a set of rules
	 * @param axioms The axioms of the source ontologie
	 * @return A rule set managing the created rules
	 * @throws UnknownOWLObjectException 
	 */
	public RuleSet buildRules(Set<OWLAxiom> axioms);
	
	public Collection<OWLObject> unknownObjects();
}
