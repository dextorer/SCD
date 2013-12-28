package it.unipd.scd.model.motion;

import it.unipd.scd.model.Coordinate;
import it.unipd.scd.model.Event;
import it.unipd.scd.model.Team;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MotionEvent extends Event {

    protected int id;
    protected int number;

    protected Team team;
    protected Coordinate from;
    protected Coordinate to;

    protected double startTime;
    protected double endTime;

    public void initialize(int id, int number, Team team, Coordinate from, Coordinate to, double start, double end) {
        this.id = id;
        this.number = number;
        this.team = team;
        this.from = from;
        this.to = to;

        this.startTime = start;
        this.endTime = end;
    }

    public double getStartTime() {
        return startTime;
    }

}
