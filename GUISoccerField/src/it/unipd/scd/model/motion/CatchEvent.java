package it.unipd.scd.model.motion;

import it.unipd.scd.gui.FieldPanel;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CatchEvent extends MotionEvent {

    @Override
    public void draw() {
        FieldPanel.setHasBall(id, true);

        FieldPanel.log("Player " + number + " has just catched the ball");
    }
}
