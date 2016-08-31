package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 01.06.2016.
 */
public abstract class OntologyWorker<T> implements Callable<T> {
    protected File file;
    protected OWLOntology ontology;

    public OntologyWorker(File file){
        this.file = file;
    }

    public T call() throws Exception{
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration loaderConfig = new OWLOntologyLoaderConfiguration();
        loaderConfig = loaderConfig.setLoadAnnotationAxioms(false);
        OWLOntology ontology = m.loadOntologyFromOntologyDocument(new FileDocumentSource(file), loaderConfig);

        if(ontology.getLogicalAxiomCount() < EvaluationMain.min_size || ontology.getLogicalAxiomCount() > EvaluationMain.max_size)
            throw new OntologySizeException(ontology.getLogicalAxiomCount(), file);

        this.ontology = ontology;
        return process(ontology);
    }

    abstract protected T process(OWLOntology ontology) throws Exception;

    public OWLOntology getOntology(){
        return ontology;
    }

    public File getFile(){
        return file;
    }
}
