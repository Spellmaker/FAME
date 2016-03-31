package de.uniulm.in.ki.mbrenner.fame.rule;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class InvalidRule extends Rule {
    public InvalidRule(){
        super(null, null, null);
    }

    @Override
    public boolean equals(Object o){
        return false;
    }
}
