package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class ButtonListener implements MouseListener {
    private MainFrame frame;
    private JButton button;
    private JLabel label;
    private int count;

    public ButtonListener(MainFrame frame, JButton button, JLabel label, int count){
        this.frame = frame;
        this.button = button;
        this.count = count;
        this.label = label;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(button.isEnabled()){
            frame.next();
            label.setText("" + (--count));
            if(!frame.hasNext()){
                button.setEnabled(false);
            }
            frame.revalidate();
            frame.pack();
        }
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
