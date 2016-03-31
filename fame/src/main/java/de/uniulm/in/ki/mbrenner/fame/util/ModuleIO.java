package de.uniulm.in.ki.mbrenner.fame.util;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 08.03.2016.
 */
public class ModuleIO {
    public static void writeModule(String s, Set<OWLAxiom> module){
        writeModule(new File(s), module);
    }

    public static void writeModule(File f, Set<OWLAxiom> module){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            for(OWLAxiom a : module){
                bw.write(a.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> readModule(File f){
        Set<String> module = new HashSet<>();
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String s = null;
            while((s = br.readLine()) != null){
                module.add(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return module;
    }


}
