package de.uniulm.in.ki.mbrenner.fame.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by spellmaker on 15.03.2016.
 */
public class DevNull extends PrintStream {
    public DevNull(){
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //ignore
            }
        });
    }
}
