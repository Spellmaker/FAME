package de.uniulm.in.ki.mbrenner.fame.debug.axiomviewer;

import de.uniulm.in.ki.mbrenner.owlprinter.OWLChars;
import de.uniulm.in.ki.mbrenner.owlprinter.OWLPrinter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by spellmaker on 12.05.2016.
 */
public class AxiomRenderer extends OWLAxiomVisitorAdapter {
    ClassRenderer classRenderer;
    IRIRenderer iriRenderer;
    PropertyRenderer propertyRenderer;
    JPanel currentPanel;
    JFrame frame;

    Map<OWLObject, OWLObject> interpretation;


    public AxiomRenderer(JFrame frame){
        this.classRenderer = new ClassRenderer(this);
        this.iriRenderer = new IRIRenderer(this);
        this.propertyRenderer = new PropertyRenderer(this);
        this.frame = frame;
    }

    boolean isInSignature(OWLObject o){
        OWLObject def = interpretation.get(o);
        if(def == null) return false;
        return def instanceof Unknown;
    }

    Component getAlt(OWLObject object){
        OWLObject inter = interpretation.get(object);
        if(inter == null){
            return null;
        }
        else if(inter instanceof OWLClass){
            return iriRenderer.render(((OWLClass)inter).getIRI());
        }
        else if(inter instanceof OWLObjectProperty){
            return iriRenderer.render(((OWLObjectProperty)inter).getIRI());
        }
        else if(inter instanceof OWLClassExpression){
            ((OWLClassExpression) inter).accept(classRenderer);
            return currentPanel;
        }
        else if(inter instanceof OWLPropertyExpression){
            ((OWLPropertyExpression) inter).accept(propertyRenderer);
            return currentPanel;
        }
        return new JLabel("ERROR");
    }

    void build(OWLClass concept){
        buildPanel(concept, concept.getIRI());
    }

    void build(OWLObjectProperty property){
        buildPanel(property, property.getIRI());
    }

    private void buildPanel(OWLObject object, IRI iri){
        OWLObject def = interpretation.get(object);

        SwitchPanel panel = getEmptyPanel();
        panel.add(iriRenderer.render(iri));

        if(def != null){
            if(def instanceof Unknown){
                panel.setBackground(Color.RED);
            }
            else{
                panel.switchContent();
                panel.add(getAlt(object));
                OWLObject tmp = interpretation.get(def);
                if(tmp != null && tmp instanceof Unknown){
                    panel.setBackground(Color.RED);
                }
                panel.switchContent();
            }
        }

        currentPanel = panel;
    }

    SwitchPanel getEmptyPanel(){
        SwitchPanel panel = new SwitchPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setFont(new java.awt.Font("Verdana", Font.BOLD, 24));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.addMouseListener(new ColorClickHandler(panel, frame));
        return panel;
    }

    JLabel getLabel(String text){
        JLabel label = new JLabel();
        label.setText(text);
        label.setFont(new java.awt.Font("Lucida Sans Unicode", Font.BOLD, 24));
        return label;
    }

    public JPanel render(OWLAxiom axiom, Map<OWLObject, OWLObject> interpretation){
        this.interpretation = interpretation;
        axiom.accept(this);
        return currentPanel;
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom){
        JPanel panel = getEmptyPanel();
        Iterator<OWLClassExpression> iter = axiom.getClassExpressions().iterator();
        iter.next().accept(classRenderer);
        panel.add(currentPanel);
        while(iter.hasNext()){
            panel.add(getLabel("" + OWLChars.equiv));
            iter.next().accept(classRenderer);
            panel.add(currentPanel);
        }
        currentPanel = panel;
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom){
        JPanel panel = getEmptyPanel();
        axiom.getSubClass().accept(classRenderer);
        panel.add(currentPanel);
        panel.add(getLabel("" + OWLChars.subset));
        axiom.getSuperClass().accept(classRenderer);
        panel.add(currentPanel);
        currentPanel = panel;
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom){
        JPanel panel = getEmptyPanel();
        axiom.getProperty().accept(propertyRenderer);
        panel.add(getLabel("Trans("));
        panel.add(currentPanel);
        panel.add(getLabel(")"));
        currentPanel = panel;
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        JPanel panel = getEmptyPanel();
        axiom.getSubProperty().accept(propertyRenderer);
        panel.add(currentPanel);
        panel.add(getLabel("" + OWLChars.subset));
        axiom.getSuperProperty().accept(propertyRenderer);
        panel.add(currentPanel);
        currentPanel = panel;
    }
}
