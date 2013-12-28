package it.unipd.scd.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/27/13
 * Time: 6:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatsPanel extends JPanel {

    private JPanel gameScoreContainer;
    private JLabel gameScoreLabel;
    private JLabel gameScore;

    private JPanel elapsedGameContainer;
    private JLabel elapsedGameTimeLabel;
    private JLabel elapsedGameTime;

    private static StatsPanel ref;

    public StatsPanel() {

        ref = this;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);

        gameScoreLabel = new JLabel("Home - Visitors");
        gameScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameScore = new JLabel("0 - 0");
        gameScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameScoreContainer = new JPanel();
        gameScoreContainer.setLayout(new BoxLayout(gameScoreContainer, BoxLayout.Y_AXIS));
        gameScoreContainer.add(gameScoreLabel);
        gameScoreContainer.add(gameScore);

        elapsedGameTimeLabel = new JLabel("Game time");
        elapsedGameTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        elapsedGameTime = new JLabel("00:00");
        elapsedGameTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        elapsedGameContainer = new JPanel();
        elapsedGameContainer.setLayout(new BoxLayout(elapsedGameContainer, BoxLayout.Y_AXIS));
        elapsedGameContainer.add(elapsedGameTimeLabel);
        elapsedGameContainer.add(elapsedGameTime);

        add(Box.createRigidArea(new Dimension(250, 0)));
        add(gameScoreContainer);
        add(Box.createRigidArea(new Dimension(50, 0)));
        add(elapsedGameContainer);

    }

    public static void setTime(int minutes, int seconds) {
        ref.elapsedGameTime.setText((minutes < 10 ? ("0" + minutes) : minutes) + ":" + (seconds < 10 ? ("0" + seconds) : seconds));
    }

    public static void setScore(int home, int visitors) {
        ref.gameScore.setText(home + " - " + visitors);
    }



}
