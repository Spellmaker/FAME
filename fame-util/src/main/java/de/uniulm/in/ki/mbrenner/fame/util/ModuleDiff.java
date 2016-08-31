package de.uniulm.in.ki.mbrenner.fame.util;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 08.03.2016.
 */
public class ModuleDiff {
    public List<String> module1add;
    public List<String> module2add;

    public ModuleDiff(){
        module1add = new LinkedList<>();
        module2add = new LinkedList<>();
    }

    public void addModule1(String s){
        module1add.add(s);
    }

    public void addModule2(String s){
        module2add.add(s);
    }

    public boolean modulesEqual(){
        return module1add.isEmpty() && module2add.isEmpty();
    }

    public boolean modulesTempEqual(){
        for(String s : module1add){
            if(!hasTempNodeEquivalent(s, module2add)){
                return false;
            }
        }
        for(String s : module1add){
            if(!hasTempNodeEquivalent(s, module1add)){
                return false;
            }
        }
        return true;
    }

    public static ModuleDiff diffString(Set<String> m1, Set<String> m2){
        ModuleDiff diff = new ModuleDiff();
        for(String s : m1)
            if(!m2.contains(s))
                diff.addModule1(s);

        for(String s : m2)
            if(!m1.contains(s))
                diff.addModule2(s);
        return diff;
    }

    public static ModuleDiff diff(Set<OWLAxiom> m1, Set<OWLAxiom> m2){
        return diffString(
                m1.stream().map(x -> x.toString()).collect(Collectors.toSet()),
                m2.stream().map(x -> x.toString()).collect(Collectors.toSet())
        );
    }

    public static ModuleDiff diff(File m1, File m2){
        return diffString(ModuleIO.readModule(m1), ModuleIO.readModule(m2));
    }

    public static ModuleDiff diff(File m1, Set<OWLAxiom> m2){
        return diffString(
                ModuleIO.readModule(m1),
                m2.stream().map(x -> x.toString()).collect(Collectors.toSet())
        );
    }

    private boolean hasTempNodeEquivalent(String s, List<String> other){
        for(String t : other){
            if(replTempNodes(s).equals(replTempNodes(t))){
                return true;
            }
        }
        return false;
    }

    private String replTempNodes(String s){
        return s.replaceAll("_:(.*)" + Pattern.quote(")"), "_:tmp)");
    }

    @Override
    public String toString(){
        String s = "additional in set1:\n";
        s += module1add.stream().reduce("", (x, y) -> x + y + "\n");
        s += "additional in set2:\n";
        s += module2add.stream().reduce("", (x, y) -> x + y + "\n");
        return s;
    }
}
