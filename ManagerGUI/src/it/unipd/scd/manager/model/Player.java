package it.unipd.scd.manager.model;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/24/13
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Player {

    public Cell position;
    public int number;
    public Team team;
    public boolean hasBall;
    public boolean onTheField;

    public Player(int number, Team team, Cell position, boolean hasBall, boolean onTheField) {
        this.position = position;
        this.number = number;
        this.hasBall = hasBall;
        this.team = team;
        this.onTheField = onTheField;
    }

    public Player(int number, Team team, int x, int y, boolean hasBall, boolean onTheField) {
        this.position = new Cell(x,y);
        this.number = number;
        this.hasBall = hasBall;
        this.team = team;
        this.onTheField = onTheField;
    }
}
