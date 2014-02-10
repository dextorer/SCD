package it.unipd.scd.manager.model.motion;

import it.unipd.scd.manager.GUI;
/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class TackleEvent extends MotionEvent {

    private int otherPlayerId;
    private int otherPlayerNumber;

    public TackleEvent(int otherPlayerId, int otherPlayerNumber) {
        this.otherPlayerId = otherPlayerId;
        this.otherPlayerNumber = otherPlayerNumber;
    }

    @Override
    public void draw() {
     //   FieldPanel.setHasBall(otherPlayerId, false);
     //   FieldPanel.setHasBall(id, true);

        GUI.forwardLog("Player " + number + " has tackled Player " + otherPlayerNumber);
    }
}
