package de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure;

import de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure.RoleNameFinder.RoleNameFinder;
import de.uniulm.in.ki.mbrenner.fame.abox.islands.snf.ShallowNormalForm;
import de.uniulm.in.ki.mbrenner.owlapiaddons.visitor.ClassVisitorAdapter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 16.06.2016.
 */
public class InfoStructureBuilder extends ClassVisitorAdapter {
    public static final Set<OWLClassExpression> STAR = Collections.emptySet();
    private InfoStructure infoStructure;

    private Map<OWLPropertyExpression, Set<OWLPropertyExpression>> getRoleHierarchy(OWLOntology ontology){
        Set<OWLAxiom> roleAxioms = ontology.getRBoxAxioms(Imports.INCLUDED);
        System.out.println(roleAxioms);
        return null;
    }

    public void build(OWLClassExpression oce, InfoStructure infoStructure){
        this.infoStructure = infoStructure;
        oce.accept(this);
    }

    public static InfoStructure build(OWLOntology ontology, OWLOntologyManager m){
        //wrong
        InfoStructureBuilder struct = new InfoStructureBuilder();
        InfoStructure result = new InfoStructure();
        ShallowNormalForm snf = new ShallowNormalForm(m.getOWLDataFactory());
        Set<OWLClassExpression> normalized = ontology.getAxioms().stream().map(snf::getSNF).flatMap(Collection::stream).collect(Collectors.toSet());
        Set<OWLObjectPropertyExpression> roleNames = RoleNameFinder.getProperties(normalized);
        Map<OWLPropertyExpression, Set<OWLPropertyExpression>> hierarchy = RoleHierarchyBuilder.getHierarchy(m, ontology, roleNames);
        for(OWLClassExpression oce : normalized){
            struct.build(oce, result);
        }
        for(OWLObjectPropertyExpression p : roleNames){
            Set<OWLClassExpression> unionSet = result.get(p);
            if(unionSet == STAR) continue;
            Set<OWLPropertyExpression> s = hierarchy.get(p);
            if(s == null) continue;

            for(OWLPropertyExpression supProp : s){
                Set<OWLClassExpression> other = result.get(supProp);
                if(other == null) continue;
                if(other == STAR){
                    unionSet = STAR;
                    break;
                }
                unionSet.addAll(other);
            }
            if(unionSet != null) result.put(p, unionSet);
        }
        return result;
    }

    @Override
    public @Nonnull void visit(@Nonnull OWLObjectIntersectionOf oce){
        oce.getOperands().forEach(x -> x.accept(this));
    }

    @Override
    public @Nonnull void visit(@Nonnull OWLObjectUnionOf oce){
        oce.getOperands().forEach(x -> x.accept(this));
    }

    @Override
    public @Nonnull void visit(@Nonnull OWLObjectSomeValuesFrom oce){
        oce.getFiller().accept(this);
    }

    @Override
    public @Nonnull void visit(@Nonnull OWLClass oce){

    }

    @Override
    public @Nonnull void visit(@Nonnull OWLObjectComplementOf oce){

    }

    @Override
    public @Nonnull void visit(@Nonnull OWLObjectAllValuesFrom oce){
        if(oce.getFiller() instanceof OWLClass || oce.getFiller() instanceof OWLObjectComplementOf){
            Set<OWLClassExpression> s = infoStructure.get(oce.getProperty());
            if(s != STAR){
                if(s == null){
                    s = new HashSet<>();
                    infoStructure.put(oce.getProperty(), s);
                }
                s.add(oce.getFiller());
            }
        }
        else{
            infoStructure.put(oce.getProperty(), STAR);
            oce.getFiller().accept(this);
        }
    }
}
