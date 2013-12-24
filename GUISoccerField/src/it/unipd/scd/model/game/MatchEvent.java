package it.unipd.scd.model.game;

import it.unipd.scd.model.Player;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEvent extends GameEvent {

    private String event;
    private int player_id;
    private double start_time, end_time;

    public MatchEvent(String event, int player_id, double start_time, double end_time) {
        this.event = event;
        this.player_id = player_id;
        this.start_time = start_time;
        this.end_time = end_time;
      /*  System.out.println("event: " + event);
        System.out.println("player id: " + player_id); */
    }

    @Override
    public void draw() {

    }
}