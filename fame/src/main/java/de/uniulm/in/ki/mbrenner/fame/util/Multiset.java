package de.uniulm.in.ki.mbrenner.fame.util;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class Multiset <T> implements Set<T> {
    private Map<T, Integer> content;

    public Multiset(){
        content = new HashMap<>();
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return content.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return content.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return content.keySet().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return content.keySet().toArray(a);
    }

    @Override
    public boolean add(T t) {
        Integer count = content.get(t);
        if(count == null){
            content.put(t, 1);
            return true;
        }
        else{
            content.put(t, count + 1);
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        Integer count = content.get(o);
        Map content = this.content;
        if(count == null) return false;
        count -= 1;
        if (count == 0) {
            content.remove(o);
            return true;
        }
        else{
            content.put(o, count);
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().filter(x -> content.containsKey(x)).count() == c.size();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean res = false;
        for(T x : c){
            res = add(x) || res;
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = false;
        for(Object x : c){
            res = remove(x) || res;
        }
        return res;
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Multiset)){
            return false;
        }
        return ((Multiset)o).content.equals(this.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }
}
