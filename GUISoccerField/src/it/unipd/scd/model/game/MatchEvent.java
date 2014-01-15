package it.unipd.scd.model.game;

import it.unipd.scd.gui.FieldPanel;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 12/23/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEvent extends GameEvent {

    private String event;
    private int player_id;
    private double start_time, end_time;

    public MatchEvent(String event, int player_id, double start_time, double end_time) {
        this.event = event;
        this.player_id = player_id;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    @Override
    public void draw() {

        System.out.println("Match Event: " + event.toLowerCase());

        if (event.toLowerCase().equals("begin_of_match")) {
            FieldPanel.log("The game begins!");

            FieldPanel.showBeginMatchImage();
            FieldPanel.setTrackTime(true, start_time, true);

            FieldPanel.setMatchState(FieldPanel.MatchState.FIRST_HALF);
        }
        else if (event.toLowerCase().equals("end_of_first_half")) {
            FieldPanel.log("End of first half. Get some rest, warriors.");

            FieldPanel.showFirstHalfImage();
            FieldPanel.setTrackTime(false, 0, false);

            FieldPanel.setMatchState(FieldPanel.MatchState.SECOND_HALF);
        }
        else if (event.toLowerCase().equals("begin_of_second_half")) {
            FieldPanel.log("On with the second half now!");

            FieldPanel.showSecondHalfImage();
            FieldPanel.setTrackTime(true, start_time, false);
        }
        else {
            FieldPanel.log("That's all, folks. Thanks for watching!");

            FieldPanel.showEndMatchImage();
            FieldPanel.setTrackTime(false, 0, false);

            FieldPanel.setMatchState(FieldPanel.MatchState.FIRST_HALF);
        }
    }

    public double getStartTime() {
        return start_time;
    }
}