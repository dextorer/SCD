package it.unipd.scd.model.game;

import it.unipd.scd.gui.FieldPanel;
import it.unipd.scd.model.Coordinate;
import it.unipd.scd.model.Player;
import it.unipd.scd.model.Team;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class FoulEvent extends GameEvent {

    private int idOne;
    private int numberOne;
    private int idTwo;
    private int numberTwo;
    private Team teamOne;
    private Team teamTwo;
    private Coordinate coordinate;
    private double start_time, end_time;

    public FoulEvent(int idOne,
                     int numberOne,
                     Team teamOne,
                     int idTwo,
                     int numberTwo,
                     Team teamTwo,
                     Coordinate coord,
                     double start_time,
                     double end_time) {
        this.idOne = idOne;
        this.idTwo = idTwo;
        this.numberOne = numberOne;
        this.numberTwo = numberTwo;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.coordinate = coord;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    @Override
    public void draw() {
        FieldPanel.log("Player " + numberOne + " has committed a foul on Player " + numberTwo + ". Not fair, dude!");

        FieldPanel.setHasBall(idOne, false);
        FieldPanel.setHasBall(idTwo, false);

        FieldPanel.showFoulImage();
    }
}
