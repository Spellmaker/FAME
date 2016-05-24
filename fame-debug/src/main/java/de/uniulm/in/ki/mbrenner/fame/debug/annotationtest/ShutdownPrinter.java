package de.uniulm.in.ki.mbrenner.fame.debug.annotationtest;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 18.05.2016.
 */
public class ShutdownPrinter extends Thread{
    private List<Object> watchedObjects;
    private String header;


    public ShutdownPrinter(){
        watchedObjects = new LinkedList<>();
    }

    public void add(Object...objects){
        for(Object o : objects) watchedObjects.add(o);
    }

    @Override
    public void run(){
        for(Object o : watchedObjects){
            Class<?> c = o.getClass();
            System.out.println(c);
            for(Field f : c.getDeclaredFields()){
                if(f.isAnnotationPresent(PrintOnShutdown.class)){
                    PrintOnShutdown ann = f.getAnnotation(PrintOnShutdown.class);
                    String txt = ann.text();
                    if(txt.equals("")) txt = f.getName();
                    try {
                        System.out.println(txt + ": " + f.get(o));
                    }
                    catch(IllegalAccessException exc){
                        System.out.println(txt + ": not accessible");
                    }
                }
            }
        }
    }
}
