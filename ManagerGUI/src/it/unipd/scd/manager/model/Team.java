package it.unipd.scd.manager.model;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class Team {

    public TeamColor color;
    public String name;

    public Team(String name, TeamColor color) {
        this.name = name;
        this.color = color;
    }

}
