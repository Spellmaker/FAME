package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 17.05.2016.
 */
public class ChangeSignatureListener implements MouseListener {
    private MainFrame frame;
    private JTextField text;
    private Set<OWLEntity> signature;

    public ChangeSignatureListener(MainFrame frame, JTextField text, Set<OWLEntity> signature){
        this.frame = frame;
        this.text = text;
        this.signature = signature;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Set<OWLEntity> sig = signature.stream().
                filter(x -> Arrays.asList(text.getText().split(",")).contains(x.toString())).
                collect(Collectors.toSet());
        if(!sig.isEmpty()) {
            try {
                frame.loadEntity(sig);
            }
            catch(Exception exc){
                System.out.println("couldn't load signature " + sig);
            }
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
