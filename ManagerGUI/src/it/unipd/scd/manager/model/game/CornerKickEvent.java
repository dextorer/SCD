package it.unipd.scd.manager.model.game;

import it.unipd.scd.manager.GUI;
import it.unipd.scd.manager.model.Coordinate;
import it.unipd.scd.manager.model.Team;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class CornerKickEvent extends GameEvent {

    private int id;
    private int number;
    private Team team;
    private Coordinate coordinate;
    private double start_time, end_time;

    public CornerKickEvent(int id, int number, Team team, Coordinate coord, double start_time, double end_time) {
        this.id = id;
        this.number = number;
        this.team = team;
        this.coordinate = coord;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    @Override
    public void draw() {
        GUI.forwardLog("Player " + number + " will now throw a corner kick. Sit tight!");

        //FieldPanel.showCornerImage();
    }
}
