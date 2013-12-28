package it.unipd.scd.model.motion;

import it.unipd.scd.gui.FieldPanel;
import it.unipd.scd.model.Player;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class MoveEvent extends MotionEvent {

    @Override
    public void draw() {
        FieldPanel.setPosition(id, to.x, to.y);
    }
}
