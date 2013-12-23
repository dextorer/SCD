package it.unipd.scd.model.game;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEvent extends GameEvent {

    String event;
    int player_id;

    public MatchEvent(String event, int player_id) {
        this.event = event;
        this.player_id = player_id;
        System.out.println("event: " + event);
        System.out.println("player id: " + player_id);
    }

    @Override
    public void draw() {

    }
}