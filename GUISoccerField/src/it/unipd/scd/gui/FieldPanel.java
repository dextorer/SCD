package it.unipd.scd.gui;

import animationx.core.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unipd.scd.model.*;
import it.unipd.scd.model.Event;
import it.unipd.scd.model.game.*;
import it.unipd.scd.model.motion.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 9/22/13
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class FieldPanel extends JPanel {

    //public static final int REDRAW_INTERVAL = 60;

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
    private Cell ballCell;

    private Team teamOne;
    private Team teamTwo;

    private ArrayList<ArrayList<Event>> draw_this = new ArrayList<ArrayList<Event>>();

    private Object lock;
    private static FieldPanel ref;

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
    private final String START_TIME = "start_time";
    private final String END_TIME = "end_time";
    private final String OTHER_PLAYER = "other_player_id";
    private final String EVENT_X = "event_coord_x";
    private final String EVENT_Y = "event_coord_y";
    private final String FOUL_ID1 = "player_1_id";
    private final String FOUL_N1 = "player_1_number";
    private final String FOUL_T1 = "player_1_team";
    private final String FOUL_ID2 = "player_2_id";
    private final String FOUL_N2 = "player_2_number";
    private final String FOUL_T2 = "player_2_team";

    private boolean drawGrid;

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

            lock = new Object();
            ref = this;

            ballCell = getCell(26, 17);
            ballCell.hasBall = true;

            drawGrid = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize (String content) {
        JsonArray jsonedPlayers = new JsonParser().parse(content).getAsJsonObject().get("players").getAsJsonArray();
        players = new Player[jsonedPlayers.size()];

        System.out.println(jsonedPlayers);

        for (int i=0; i<jsonedPlayers.size(); i++) {
            // initializing bench warmers

            JsonObject player = jsonedPlayers.get(i).getAsJsonObject();
            Team team = (player.get("team").getAsString().compareTo("TEAM_ONE") == 0) ? teamOne : teamTwo;

            players[i] = new Player(player.get("number").getAsInt(), team, cells[new Random().nextInt(cells.length)], false, player.get("on_the_field").getAsBoolean());
//            players[i] = new Player(player.get("number").getAsInt(), team, cells[26], false);
        }

        for (Player p : players) {
            p.position.hasPlayer = true;
            p.position.player = p;
        }

//        repaint();
//        revalidate();
    }

    public void startDrawCycle() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        while (draw_this.isEmpty()) {
                            try {
                                System.out.println("Going to sleep..");
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("The dragon has been awaken!");

                        ArrayList<Event> current = draw_this.get(0);
                        System.out.println("Drawing " + current.size() + " events..");
                        for (Event e : current) {
//                            System.out.println("Drawing cycle in progress..");
                            e.draw();
                        }

                        repaint();
                        revalidate();

                        System.out.println("Drawing cycle done.");

                        draw_this.remove(0);
                    }
                }
            }
        };
        t.start();
    }

    private void setRealCoordinates(Cell input) {
        input.y = soccerFieldImage.getHeight() - input.y;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(soccerFieldImage, 0, 0, null);

        // draw grid
        if (drawGrid) {
            g2d.drawImage(gridImage, 0, 0, null);
        }

//        AffineTransform original = g2d.getTransform();

//        g2d.drawImage(ballImage, 168, 168, null);

        // draw other components

        for (Cell c : cells) {
            if (c.hasPlayer) {
                // draw player

                Player p = c.player;

                if (p.onTheField) {
                    g2d.setColor(p.team.color.getColor());
                    Ellipse2D.Double circle = new Ellipse2D.Double(p.position.x - PLAYER_PADDING, p.position.y - CELL_PIXEL_SIZE - PLAYER_PADDING, 20, 20);
                    g2d.fill(circle);

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(String.valueOf(p.number), p.position.x - 1, p.position.y - 2);

                    if (p.hasBall) {
                        // draw ball
                        g2d.drawImage(ballImage, p.position.x, p.position.y - CELL_PIXEL_SIZE, null);
                    }
                }
            }
            else {
                if (c.hasBall) {
                    g2d.drawImage(ballImage, c.x, c.y - CELL_PIXEL_SIZE, null);
                }
            }
        }

//        g2d.setTransform(original);
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
        JsonObject obj = new JsonParser().parse(payload).getAsJsonObject();
        JsonArray buf = obj.get(EVENTS_OBJECT).getAsJsonArray();

        JsonObject action;
        String event_type;
        int player_id, player_number;
        Team player_team;
        Coordinate from, to;
        String startTime, endTime;
        ArrayList<Event> event_array = new ArrayList<Event>();

        for (int i = 0; i < buf.size(); i++) {
            action = buf.get(i).getAsJsonObject();
            event_type = action.get(EVENT_TYPE).getAsString();

            MotionEvent m = null;
            Event e = null;

            // id number team from to
            if (event_type.equals("match"))
                e = new MatchEvent(
                        action.get(EVENT_ID).getAsString(),
                        action.get(PLAYER_ID).getAsInt(),
                        Double.parseDouble(action.get(START_TIME).getAsString()),
                        Double.parseDouble(action.get(END_TIME).getAsString()));
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

                    if (action.get(PLAYER_TEAM).getAsString().equals("TEAM_ONE"))
                        player_team = teamOne;
                    else
                        player_team = teamTwo;

                    if (action.get(EVENT_ID).getAsString().equals("Goal"))
                        e = new GoalEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                    if (action.get(EVENT_ID).getAsString().equals("Goal_Kick"))
                        e = new GoalKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                    if (action.get(EVENT_ID).getAsString().equals("Corner_Kick"))
                        e = new CornerKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                    if (action.get(EVENT_ID).getAsString().equals("Penalty_Kick"))
                        e = new PenaltyKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                    if (action.get(EVENT_ID).getAsString().equals("Throw_In"))
                        e = new ThrowInEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                    if (action.get(EVENT_ID).getAsString().equals("Free_Kick"))
                        e = new FreeKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                }
            else
                if (event_type.equals("binary")) {  // binary event
                    Team opponent;
                    if (action.get(FOUL_T1).getAsString().equals("TEAM_ONE")) {
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
                            new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                            Double.parseDouble(action.get(START_TIME).getAsString()),
                            Double.parseDouble(action.get(END_TIME).getAsString()));
                }

            if (m != null) {
                player_id = action.get(PLAYER_ID).getAsInt();
                player_number = action.get(PLAYER_NUMBER).getAsInt();

                if (action.get(PLAYER_TEAM).getAsString().equals("TEAM_ONE"))
                    player_team = teamOne;
                else
                    player_team = teamTwo;

                from = new Coordinate(action.get(FROM_X).getAsInt(), action.get(FROM_Y).getAsInt());
                to = new Coordinate(action.get(TO_X).getAsInt(), action.get(TO_Y).getAsInt());

                startTime = action.get(START_TIME).getAsString();
                endTime = action.get(END_TIME).getAsString();

                m.initialize(player_id, player_number, player_team, from, to, Double.parseDouble(startTime), Double.parseDouble(endTime));
                event_array.add(m);

            /*    System.out.println("player id: " + player_id);
                System.out.println("player number: " + player_number);
                System.out.println("player team: " + player_team.name);
                System.out.println("from: " + from.x + " " + from.y);
                System.out.println("to: " + to.x + " " + to.y);
                System.out.println("start time: " + startTime);
                System.out.println("end time:" + endTime); */
            }
            else
                event_array.add(e);
        }

        synchronized (lock) {
            draw_this.add(event_array);
            lock.notify();
        }
    }

    private Cell getCell (int x, int y) {
        return cells[y * COLUMNS_HORIZONTAL_CELLS_NUMBER + x];
    }

    private Cell getCell (int id) {
        return players[id-1].position;
    }

    public static void setPosition (final int id, final int x, final int y) {

//        final AnimationUpdateThread thread = new AnimationUpdateThread(Animations.DEFAULT_FRAME_RATE);
//        final Animation a = new GeneralAnimation(thread, 0.2, AnimationConstants.EASE_IN_OUT);
//        a.

//        if (id == 0) {
//            // ball
//
//            Cell current = ref.ballCell;
//
//            if (ref.ballCell != null) {
//                ref.ballCell.hasBall = false;
//            }
//
//            ref.ballCell = ref.getCell(x, y);
//            ref.ballCell.hasBall = true;
//
//        }
//        else {
//            // player
//
//            Cell current = ref.getCell(id);
//            Cell target = ref.getCell(x,y);
//
//
//
//        }

        if (id == 0) {
            // that's the ball!
            if (ref.ballCell != null) {
                ref.ballCell.hasBall = false;
            }

            ref.ballCell = ref.getCell(x, y);
            ref.ballCell.hasBall = true;
        }
        else {
            ref.players[id-1].position = ref.getCell(x, y);
        }
    }

    private static double lerp(int x1, int x2, double t) {
        return x1 + (x2 - x1) * t;
    }

    public static void setHasBall (int id, boolean hasBall) {
        ref.players[id-1].hasBall = hasBall;
        ref.ballCell.hasBall = false;
    }

    public void toggleGrid() {
        drawGrid = !drawGrid;
        repaint();
        revalidate();
    }
}
