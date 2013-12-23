package it.unipd.scd.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unipd.scd.model.Coordinate;
import it.unipd.scd.model.Event;
import it.unipd.scd.model.Team;
import it.unipd.scd.model.TeamColor;
import it.unipd.scd.model.game.*;
import it.unipd.scd.model.motion.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 9/22/13
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class FieldPanel extends JPanel {

    public static final int REDRAW_INTERVAL = 60;

    private final String SOCCER_FIELD_IMAGE_NAME = "soccer-field.png";
    private final InputStream SOCCER_FIELD_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + SOCCER_FIELD_IMAGE_NAME);

    private final String BALL_IMAGE_NAME = "ball.png";
    private final InputStream BALL_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + BALL_IMAGE_NAME);

    private final String GRID_IMAGE_NAME = "transparent_grid.png";
    private final InputStream GRID_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + GRID_IMAGE_NAME);

    private final int PLAYER_PADDING = 3;

    private BufferedImage soccerFieldImage;
    private BufferedImage ballImage;
    private BufferedImage gridImage;

    private final int CELL_PIXEL_SIZE = 14;
    private final int COLUMNS_HORIZONTAL_CELLS_NUMBER = 53;
    private final int ROWS_VERTICAL_CELLS_NUMBER = 35;

    private Cell[] cells;
    private Player[] players;

    private Team teamOne;
    private Team teamTwo;

    private ArrayList<ArrayList<Event>> draw_this = new ArrayList<ArrayList<Event>>();
    private final String EVENTS_OBJECT = "events";
    private final String EVENT_TYPE = "type_of_event";
    private final String EVENT_ID = "event_id";
    private final String PLAYER_ID = "player_id";
    private final String PLAYER_NUMBER = "player_number";
    private final String PLAYER_TEAM = "player_team";
    private final String FROM_X = "from_x";
    private final String FROM_Y = "from_y";
    private final String TO_X = "to_x";
    private final String TO_Y = "to_y";
    private final String OTHER_PLAYER = "other_player_id";
    private final String EVENT_X = "event_coord_x";
    private final String EVENT_Y = "event_coord_y";
    private final String FOUL_ID1 = "player_1_id";
    private final String FOUL_N1 = "player_1_number";
    private final String FOUL_T1 = "player_1_team";
    private final String FOUL_ID2 = "player_2_id";
    private final String FOUL_N2 = "player_2_number";
    private final String FOUL_T2 = "player_2_team";

    public FieldPanel() {

        // load image
        try {
            soccerFieldImage = ImageIO.read(SOCCER_FIELD_IMAGE_PATH);
            ballImage = ImageIO.read(BALL_IMAGE_PATH);
            gridImage = ImageIO.read(GRID_IMAGE_PATH);

            cells = new Cell[COLUMNS_HORIZONTAL_CELLS_NUMBER * ROWS_VERTICAL_CELLS_NUMBER];

            // initializing cells
            for (int i=0; i<ROWS_VERTICAL_CELLS_NUMBER; i++) {
                for (int j=0; j<COLUMNS_HORIZONTAL_CELLS_NUMBER; j++) {
                    cells[i * COLUMNS_HORIZONTAL_CELLS_NUMBER + j] = new Cell(j * CELL_PIXEL_SIZE, i * CELL_PIXEL_SIZE);
                }
            }

            for (Cell c : cells) {
                // changing coordinate system
                setRealCoordinates(c);
            }

            // initializing teams
            teamOne = new Team("Team One", TeamColor.RED);
            teamTwo = new Team("Team Two", TeamColor.BLUE);

            // initializing bench warmers
            players = new Player[10];
            players[0] = new Player(12, teamOne, cells[355], false);
            players[1] = new Player(56, teamTwo, cells[750], true);

            for (Player p : players) {
                p.position.hasPlayer = true;
                p.position.player = p;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRealCoordinates(Cell input) {
        input.y = soccerFieldImage.getHeight() - input.y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(soccerFieldImage, 0, 0, null);

        // draw grid
        g2d.drawImage(gridImage, 0, 0, null);

        AffineTransform original = g2d.getTransform();

//        g2d.drawImage(ballImage, 168, 168, null);

        // draw other components

        for (Cell c : cells) {
            if (c.hasPlayer) {
                // draw player

                Player p = c.player;

                g2d.setColor(p.team.color.getColor());
                Ellipse2D.Double circle = new Ellipse2D.Double(p.position.x - PLAYER_PADDING, p.position.y - CELL_PIXEL_SIZE - PLAYER_PADDING, 20, 20);
                g2d.fill(circle);

                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(p.number), p.position.x - 1, p.position.y - 2);

                if (p.hasBall) {
                    // draw ball
                    g2d.drawImage(ballImage, p.position.x, p.position.y, null);
                }
            }
            else {
                if (c.hasBall) {
                    g2d.drawImage(ballImage, c.x, c.y, null);
                }
            }
        }

        g2d.setTransform(original);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(soccerFieldImage.getWidth(), soccerFieldImage.getHeight());
    }

    public void drawADot() {

        Timer t = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                players[0].position.x++;

                revalidate();
                repaint();
            }
        });
        t.setInitialDelay(1000);
        t.setRepeats(true);

        t.start();
    }

    public void deserialize(String payload) {
        System.out.println("deserialize");
        JsonParser parser = new JsonParser();
        JsonArray buf = parser.parse(payload).getAsJsonObject().get(EVENTS_OBJECT).getAsJsonArray();

        JsonObject action;
        String event_type;
        int player_id, player_number;
        Team player_team;
        Coordinate from, to;
        ArrayList<Event> event_array = new ArrayList<Event>();

        for (int i = 0; i < buf.size(); i++) {
            System.out.println("for: " + i);
            action = buf.get(i).getAsJsonObject();
            event_type = action.get(EVENT_TYPE).toString();

            MotionEvent m = null;
            Event e = null;

            // id number team from to
            if (event_type.equals("match"))
                e = new MatchEvent(action.get(EVENT_ID).toString(), action.get(PLAYER_ID).getAsInt());
            else
                if (event_type.equals("catch"))  // catch event
                    m = new CatchEvent();
            else
                if (event_type.equals("shot"))  // shot event
                    m = new ShotEvent();
            else
                if (event_type.equals("tackle")) // tackle event
                    m = new TackleEvent(action.get(OTHER_PLAYER).getAsInt());
            else
                if (event_type.equals("catch")) // catch event
                    m = new CatchEvent();
            else
                if (event_type.equals("move")) // move event
                    m = new MoveEvent();
            else
                if (event_type.equals("unary")) { // unary event

                    if (action.get(PLAYER_TEAM).toString().equals("TEAM_ONE"))
                        player_team = teamOne;
                    else
                        player_team = teamTwo;

                    if (action.get(EVENT_ID).toString().equals("Goal"))
                        e = new GoalEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));

                    if (action.get(EVENT_ID).toString().equals("Goal_Kick"))
                        e = new GoalKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));

                    if (action.get(EVENT_ID).toString().equals("Corner_Kick"))
                        e = new CornerKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));

                    if (action.get(EVENT_ID).toString().equals("Penalty_Kick"))
                        e = new PenaltyKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));

                    if (action.get(EVENT_ID).toString().equals("Throw_In"))
                        e = new ThrowInEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));

                    if (action.get(EVENT_ID).toString().equals("Free_Kick"))
                        e = new FreeKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));
                }
            else
                if (event_type.equals("binary")) {  // binary event
                    Team opponent;
                    if (action.get(FOUL_T1).toString().equals("TEAM_ONE")) {
                        player_team = teamOne;
                        opponent = teamTwo;
                    }
                    else {
                        player_team = teamTwo;
                        opponent = teamOne;
                    }
                    e = new FoulEvent(      // foul event
                            action.get(FOUL_ID1).getAsInt(),
                            action.get(FOUL_N1).getAsInt(),
                            player_team,
                            action.get(FOUL_ID2).getAsInt(),
                            action.get(FOUL_N2).getAsInt(),
                            opponent,
                            new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()));
                }

            if (m != null) {
                player_id = action.get(PLAYER_ID).getAsInt();
                player_number = action.get(PLAYER_NUMBER).getAsInt();

                if (action.get(PLAYER_TEAM).toString().equals("TEAM_ONE"))
                    player_team = teamOne;
                else
                    player_team = teamTwo;

                from = new Coordinate(action.get(FROM_X).getAsInt(), action.get(FROM_Y).getAsInt());
                to = new Coordinate(action.get(TO_X).getAsInt(), action.get(TO_Y).getAsInt());

                m.initialize(player_id, player_number, player_team, from, to);
                event_array.add(m);

                System.out.println("player id: " + player_id);
                System.out.println("player number: " + player_number);
                System.out.println("player team: " + player_team);
                System.out.println("from: " + from.x + " " + from.y);
                System.out.println("to: " + to.x + " " + to.y);
            }
            else
                event_array.add(e);
        }
        draw_this.add(event_array);
        System.out.println("end deserialize");
    }

    private static class Player {

        public Cell position;
        public int number;
        public Team team;
        public boolean hasBall;

        public Player(int number, Team team, Cell position, boolean hasBall) {
            this.position = position;
            this.number = number;
            this.hasBall = hasBall;
            this.team = team;
        }

        public Player(int number, Team team, int x, int y, boolean hasBall) {
            this.position = new Cell(x,y);
            this.number = number;
            this.hasBall = hasBall;
            this.team = team;
        }
    }

    private static class Cell {

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
}
