package it.unipd.scd.model.game;

import it.unipd.scd.model.Coordinate;
import it.unipd.scd.model.Team;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class PenaltyKickEvent extends GameEvent {

    private int id;
    private int number;

    private Team team;
    private Coordinate coordinate;

    public PenaltyKickEvent(int id, int number, Team team, Coordinate coord) {
        this.id = id;
        this.number = number;
        this.team = team;
        this.coordinate = coord;
    }

    @Override
    public void draw() {

    }
}
