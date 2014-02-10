package it.unipd.scd.manager.model.motion;

import it.unipd.scd.manager.GUI;

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
     //   FieldPanel.setHasBall(id, true);

        GUI.forwardLog("Player " + number + " has just catched the ball");
    }
}
