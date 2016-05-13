package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class ColorClickHandler implements MouseListener{
    private SwitchPanel panel;
    private JFrame frame;

    public ColorClickHandler(SwitchPanel panel, JFrame frame){
        this.panel = panel;
        this.frame = frame;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        panel.toggle();
        frame.pack();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
