package it.unipd.scd.model.motion;

import it.unipd.scd.model.Player;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class TackleEvent extends MotionEvent {

    private int otherPlayerId;

    public TackleEvent(int otherPlayerId) {
        this.otherPlayerId = otherPlayerId;
    }

    @Override
    public void draw() {

    }
}
