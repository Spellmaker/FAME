package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class SwitchPanel extends JPanel{
    private List<Component> currentComponents;
    private List<Component> otherComponents;

    private Color cColor;
    private Color oColor;

    public SwitchPanel(){
        this.currentComponents = new LinkedList<>();
        this.otherComponents = new LinkedList<>();
        oColor = this.getBackground();
        cColor = this.getBackground();
    }

    public void switchContent(){
        for (Component c : currentComponents) {
            c.setVisible(false);
        }
        for (Component c : otherComponents) {
            c.setVisible(true);
        }
        List<Component> l = currentComponents;
        currentComponents = otherComponents;
        otherComponents = l;
        Color t = cColor;
        cColor = oColor;
        oColor = t;
        this.setBackground(cColor);
    }


    public void toggle(){
        if(!otherComponents.isEmpty()) {
            switchContent();
            this.revalidate();
        }
    }

    @Override
    public Component add(Component comp){
        Component ret = super.add(comp);
        currentComponents.add(comp);
        return ret;
    }

    @Override
    public void setBackground(Color color){
        cColor = color;
        super.setBackground(color);
    }
}
