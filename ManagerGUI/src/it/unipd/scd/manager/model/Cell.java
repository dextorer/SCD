package it.unipd.scd.manager.model;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/24/13
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Cell {

    public int x;
    public int y;

    public boolean hasPlayer;
    public boolean hasBall;

    public Player player;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        hasPlayer = false;
        hasBall = false;
    }

    public Cell(int x, int y, Player player) {
        this.x = x;
        this.y = y;
        hasPlayer = true;
        this.player = player;
    }
}
