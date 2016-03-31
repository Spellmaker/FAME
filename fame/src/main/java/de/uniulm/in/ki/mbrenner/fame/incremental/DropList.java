package de.uniulm.in.ki.mbrenner.fame.incremental;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by spellmaker on 16.03.2016.
 */
public class DropList<T> implements Iterable<T>{
    class Container<S>{
        S element;
        int size;
        Container<S> prev;
        Container<S> next;

        public Container(S element, Container<S> prev, Container<S> next){
            this.element = element;
            this.prev = prev;
            if(prev == null) size = 1;
            else size = prev.size + 1;
            this.next = next;
        }

        public Container<S> append(S element){
            Container<S> nxt = new Container<>(element, this, null);
            this.next = nxt;
            return nxt;
        }
    }
    class DropIterator<U> implements Iterator<U> {
        Container<U> c;
        public DropIterator(Container<U> c){
            this.c = c;
        }
        @Override
        public boolean hasNext() {
            return c != null;
        }

        @Override
        public U next() {
            U r = c.element;
            c = c.next;
            return r;
        }
    }

    Container<T> root;
    Container<T> tail;

    public DropList(){
        root = null;
    }

    private DropList(Container<T> root){
        this.root = root;
    }

    public void add(T element){
        if(tail == null){
            root = new Container<>(element, null, null);
            tail = root;
        }
        else{
            tail = tail.append(element);
        }
    }

    public int size(){
        //not applicable for partial droplists!
        return tail.size;
    }

    public DropList<T> dropAfterPosition(int position){
        if(root == null) return null;
        Container<T> c = root;
        int count = 0;
        while(c != null && count < position){
            c = c.next;
            count++;
        }
        if(c == null || c.prev == null){
            root = null;
            tail = null;
        }
        else{
            c.prev.next = null;
            tail = c.prev;
        }
        return new DropList<>(c);
    }

    public DropList<T> dropAfter(T element){
        if(root == null) return null;
        Container<T> c = root;
        while(c != null){
            if(c.element.equals(element)){
                if(c.prev == null){
                    root = null;
                    tail = null;
                    break;
                }
                else{
                    c.prev.next = null;
                    tail = c.prev;
                    break;
                }
            }
            c = c.next;
        }
        return new DropList<>(c);
    }

    public T getLast(){
        if(tail == null) return null;
        return tail.element;
    }

    @Override
    public Iterator<T> iterator() {
        return new DropIterator<>(root);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for(T t : this){
            action.accept(t);
        }
    }
}