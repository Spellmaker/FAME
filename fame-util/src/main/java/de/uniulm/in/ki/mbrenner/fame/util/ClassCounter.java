package de.uniulm.in.ki.mbrenner.fame.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 18.03.2016.
 */
public class ClassCounter implements Iterable<String>{
    private Map<Class<?>, Integer> count;

    public ClassCounter(){
        count = new HashMap<>();
    }

    public void add(Object o){
        Integer i = count.get(o.getClass());
        if(i == null) i = 0;
        count.put(o.getClass(), i + 1);
    }

    public void addAll(Collection<?> c){
        c.forEach(x -> add(x));
    }

    public void addOnce(ClassCounter other){
        for(Map.Entry<Class<?>, Integer> e : other.count.entrySet()){
            Integer i = count.get(e.getKey());
            if(i == null) i = 0;
            count.put(e.getKey(), i + 1);
        }
    }

    public void merge(ClassCounter other){
        for(Map.Entry<Class<?>, Integer> e : other.count.entrySet()){
            Integer i = count.get(e.getKey());
            if(i == null){
                count.put(e.getKey(), e.getValue());
            }
            else{
                count.put(e.getKey(), e.getValue() + i);
            }
        }
    }

    public String toString(){
        String s = "[";
        for(Map.Entry<Class<?>, Integer> e : count.entrySet()){
            s += e.getKey() + ":" + e.getValue() + ", ";
        }
        if(count.size() > 0) s = s.substring(0, s.length() - 2);
        s += "]";
        return s;
    }


    @Override
    public Iterator<String> iterator() {
        return count.entrySet().stream().map(x -> x.getKey() + ": " + x.getValue()).collect(Collectors.toList()).iterator();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        count.entrySet().stream().map(x -> x.getKey() + ": " + x.getValue()).forEach(action);
    }
}
