package it.unipd.scd.manager.model;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public enum TeamColor {
    RED(Color.RED),
    BLUE(Color.BLUE);

    public Color color;

    TeamColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
