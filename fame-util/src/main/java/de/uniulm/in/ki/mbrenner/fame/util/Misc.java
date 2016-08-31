package de.uniulm.in.ki.mbrenner.fame.util;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 23.03.2016.
 */
public class Misc {
    public static List<File> fileList(String directory){
        List<File> fileNames = new ArrayList<>();
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))){
            for(Path path : directoryStream){
                fileNames.add(path.toFile());
            }
        }catch(IOException e){
            System.out.println("unreadable directory");
            e.printStackTrace();
        }
        return fileNames;
    }

    public static Set<OWLAxiom> stripNonLogical(Set<OWLAxiom> module){
        return module.stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());
    }

    public static OWLEntity getEntity(OWLOntology o, String s){
        for(OWLEntity e : o.getSignature()){
            if(e.toString().equals(s)){
                return e;
            }
        }
        return null;
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
