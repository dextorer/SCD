package it.unipd.scd.model.motion;

import it.unipd.scd.gui.FieldPanel;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShotEvent extends MotionEvent {

    @Override
    public void draw() {
        FieldPanel.setPlayerHasBall(id, false);
    }
}
