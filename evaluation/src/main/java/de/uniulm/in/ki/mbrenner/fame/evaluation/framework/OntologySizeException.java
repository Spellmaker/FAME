package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import java.io.File;

/**
 * Created by spellmaker on 01.06.2016.
 */
public class OntologySizeException extends Exception {
    private int size;
    private File file;
    private String detailMessage;

    public OntologySizeException(int size, File f){
        this.size = size;
        this.file = f;
        this.detailMessage = "Skipped Ontology " + file + ": size " + size;
    }

    @Override
    public String getMessage(){
        return this.detailMessage;
    }

    @Override
    public String toString(){
        return this.detailMessage;
    }
}
