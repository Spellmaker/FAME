package de.uniulm.in.ki.mbrenner.fame.debug.incremental.providers;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 26.04.2016.
 */
public class RandomModifier implements IncrementalOntologyManager {
    private int change;
    private int iterations;
    private OWLOntology full;
    private OWLOntologyManager m;

    private OWLOntology current;
    private int current_iteration;
    private List<OWLAxiom> currentOntology;
    private List<OWLAxiom> removed;
    private List<OWLAxiom> added;
    private Random rand;
    private Set<OWLClass> previousSignature;

    private OWLReasonerFactory reasonerFactory;

    public RandomModifier(OWLReasonerFactory factory, File file, int change, int iterations) throws OWLOntologyCreationException{
        this.change = change;
        this.m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        full = m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);
        current_iteration = 0;
        this.iterations = iterations;

        currentOntology = new LinkedList<>(full.getLogicalAxioms());
        current = m.createOntology(new HashSet<>(currentOntology));
        removed = new LinkedList<>();
        added = new LinkedList<>();
        rand = new Random();

        this.reasonerFactory = factory;

        choseNew();
    }

    @Override
    public OWLOntology getCurrent() {
        return current;
    }

    @Override
    public OWLOntology next() {
        if(current_iteration < iterations) {
            try {
                choseNew();
            } catch (OWLOntologyCreationException exc) {
                //this shouldn't happen
            }
            current_iteration++;
        }
        return current;
    }

    @Override
    public Collection<OWLAxiom> getRemoved() {
        return removed;
    }

    @Override
    public Collection<OWLAxiom> getAdded() {
        return added;
    }

    @Override
    public boolean hasMore() {
        return current_iteration < iterations;
    }

    @Override
    public OWLReasoner getReasoner() {
        return reasonerFactory.createNonBufferingReasoner(current);
    }

    @Override
    public Set<OWLClass> previousClassesInSignature() {
        return previousSignature;
    }

    @Override
    public Set<OWLClass> newClasses(){
        return current.getClassesInSignature().stream().filter(x -> !previousSignature.contains(x)).collect(Collectors.toSet());
    }

    private void choseNew() throws OWLOntologyCreationException{
        List<Integer> positions = new ArrayList<>(change);
        for(int i = 0; i < change; i++) positions.add(rand.nextInt());
        Collections.sort(positions);

        added.clear();
        added = removed;
        removed = new LinkedList<>();
        Iterator<Integer> iter = positions.iterator();
        int i = 0;
        int nPos = iter.next();
        for(OWLAxiom ax : currentOntology){
            if(i == nPos){
                removed.add(ax);
                if(!iter.hasNext()) break;
                nPos = iter.next();
            }
            i++;
        }
        currentOntology.addAll(added);
        currentOntology.removeAll(removed);

        previousSignature = current.getClassesInSignature();
        m.removeOntology(current);
        current = m.createOntology(new HashSet<>(currentOntology));
    }
}
