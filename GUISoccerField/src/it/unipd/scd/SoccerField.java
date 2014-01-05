package it.unipd.scd;

import de.javasoft.plaf.synthetica.SyntheticaBlueIceLookAndFeel;
import it.unipd.scd.gui.SoccerFrame;

import javax.swing.*;
import java.text.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 9/22/13
 * Time: 9:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class SoccerField {

    private static SoccerFrame frame;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new SyntheticaBlueIceLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        frame = new SoccerFrame();
    }

}
