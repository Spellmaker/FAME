package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by spellmaker on 02.06.2016.
 */
public abstract class WorkerResult {
    protected File f;
    protected long axioms;
    protected long entities;

    private Field[] fields;
    private Method[] methods;
    private String header;

    public WorkerResult(OntologyWorker<?> worker){
        this.f = worker.getFile();
        this.axioms = worker.getOntology().getLogicalAxiomCount();
        this.entities = worker.getOntology().getSignature().size();
    }

    private void determineOrder(){
        List<Field> fields = new LinkedList<>();
        List<Method> methods = new LinkedList<>();
        for(Field f : this.getClass().getFields()){
            if(f.isAnnotationPresent(PrintField.class)){
                fields.add(f);
            }
        }
        for(Method m : this.getClass().getMethods()){
            if(m.isAnnotationPresent(PrintField.class)){
                methods.add(m);
            }
        }

        this.fields = fields.toArray(new Field[fields.size()]);
        this.methods = methods.toArray(new Method[methods.size()]);
        this.header = "ontology;axiom count;entities";
        for(Field f : fields){
            this.header += ";" + f.getName();
        }
        for(Method m : methods) {
            this.header += ";" + m.getName();
        }
    }


    @Override
    public String toString(){
        if(fields == null) determineOrder();

        String res = f + ";" + axioms + ";" + entities;

        for(Field f : fields){
            try {
                if(!f.isAccessible()){
                    f.setAccessible(true);
                }
                res += ";" + f.get(this).toString();
            }
            catch(IllegalAccessException e){
                res += ";IllegalAccess";
            }
        }
        for(Method m : methods){
            try{
                if(!m.isAccessible()){
                    m.setAccessible(true);
                }
                res += ";" + m.invoke(this).toString();
            }
            catch(IllegalAccessException e){
                res += ";IllegalAccess";
            }
            catch(InvocationTargetException e){
                res += ";InvocationTarget";
            }
        }
        return res;
    }

    public String getHeader(){
        if(fields == null) determineOrder();
        return header;
    }
}