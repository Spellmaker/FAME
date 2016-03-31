package de.uniulm.in.ki.mbrenner.oremanager.filters;

import de.uniulm.in.ki.mbrenner.oremanager.OREFilter;

/**
 * Created by spellmaker on 14.03.2016.
 */
public class ORECountFilter implements OREFilter{
    private int i = 0;
    private int count;

    public ORECountFilter(int count){
        this.count = count;
    }

    @Override
    public boolean accept(String... s) {
        return (i++) < count;
    }
}
