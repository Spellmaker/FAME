package de.uniulm.in.ki.mbrenner.fame.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Checks ontologies for correctness by loading them via the owlapi.
 * Ontologies without errors are copied from a source directory to a target directory
 * Created by spellmaker on 03.03.2016.
 */
public class OntologyCheck {
    public static void main(String[] args){
        //load ontologies from a specified directory
        String dir = "C:\\Users\\spellmaker\\SemanticWeb\\ontologies\\";
        String tdir = "C:\\Users\\spellmaker\\SemanticWeb\\errorless\\";

        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dir))){
            for(Path path : directoryStream){
                try {
                    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
                    OWLOntology ont = m.loadOntologyFromOntologyDocument(path.toFile());

                    Files.copy(path, Paths.get(tdir, path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                }
                catch(OWLOntologyCreationException exc){
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                } catch(UnloadableImportException exc){
                    System.out.println("Could not load ontology '" + path + "': " + exc.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Inaccessible directory");
            e.printStackTrace();
        } catch(Exception e){
            System.out.println("Unexpected error");
            e.printStackTrace();
        }
    }
}
