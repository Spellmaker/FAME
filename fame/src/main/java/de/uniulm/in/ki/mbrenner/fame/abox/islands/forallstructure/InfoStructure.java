package de.uniulm.in.ki.mbrenner.fame.abox.islands.forallstructure;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 16.06.2016.
 */
public class InfoStructure extends HashMap<OWLPropertyExpression, Set<OWLClassExpression>> {
    @Override
    public String toString(){
        String s = "{";
        Iterator<Entry<OWLPropertyExpression, Set<OWLClassExpression>>> iter = entrySet().iterator();
        s += oneEntry(iter.next());
        while(iter.hasNext())
            s += ", " + oneEntry(iter.next());
        return s + "}";
    }

    private String oneEntry(Map.Entry<OWLPropertyExpression, Set<OWLClassExpression>> entry){
        String s = "";
        s = OWLPrinter.getString(entry.getKey()) + " = [";
        if(entry.getValue() == InfoStructureBuilder.STAR)
            s += "*";
        else{
            if(entry.getValue() == null){
                System.out.println(entry);
            }
            Iterator<OWLClassExpression> iter = entry.getValue().iterator();
            s += OWLPrinter.getString(iter.next());
            while(iter.hasNext())
                s += ", " + OWLPrinter.getString(iter.next());
        }

        return s + "]";
    }
}
