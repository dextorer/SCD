package it.unipd.scd.gui;

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

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 9/22/13
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class FieldPanel extends JPanel {

    public enum MatchState {
        FIRST_HALF,
        SECOND_HALF;
    }

    private final String SOCCER_FIELD_IMAGE_NAME = "soccer-field.png";
    private final InputStream SOCCER_FIELD_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + SOCCER_FIELD_IMAGE_NAME);

    private final String BALL_IMAGE_NAME = "ball.png";
    private final InputStream BALL_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + BALL_IMAGE_NAME);

    private final String GRID_IMAGE_NAME = "transparent_grid.png";
    private final InputStream GRID_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + GRID_IMAGE_NAME);

    private final String GOAL_IMAGE_NAME = "goal-text.png";
    private final InputStream GOAL_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + GOAL_IMAGE_NAME);

    private final String FOUL_IMAGE_NAME = "foul-text.png";
    private final InputStream FOUL_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + FOUL_IMAGE_NAME);

    private final String CORNER_IMAGE_NAME = "corner-text.png";
    private final InputStream CORNER_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + CORNER_IMAGE_NAME);

    private final String THROW_IN_IMAGE_NAME = "throw-in-text.png";
    private final InputStream THROW_IN_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + THROW_IN_IMAGE_NAME);

    private final String BEGIN_MATCH_IMAGE_NAME = "begin-match-text.png";
    private final InputStream BEGIN_MATCH_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + BEGIN_MATCH_IMAGE_NAME);

    private final String FIRST_HALF_IMAGE_NAME = "first-half-text.png";
    private final InputStream FIRST_HALF_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + FIRST_HALF_IMAGE_NAME);

    private final String SECOND_HALF_IMAGE_NAME = "second-half-text.png";
    private final InputStream SECOND_HALF_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + SECOND_HALF_IMAGE_NAME);

    private final String END_MATCH_IMAGE_NAME = "end-match-text.png";
    private final InputStream END_MATCH_IMAGE_PATH = this.getClass().getClassLoader().getResourceAsStream("res/" + END_MATCH_IMAGE_NAME);

    private final int PLAYER_PADDING = 3;

    private BufferedImage soccerFieldImage;
    private BufferedImage ballImage;
    private BufferedImage gridImage;
    private BufferedImage goalImage;
    private BufferedImage foulImage;
    private BufferedImage cornerImage;
    private BufferedImage throwInImage;
    private BufferedImage beginMatchImage;
    private BufferedImage firstHalfImage;
    private BufferedImage secondHalfImage;
    private BufferedImage endMatchImage;

    private final int CELL_PIXEL_SIZE = 14;
    private final int COLUMNS_HORIZONTAL_CELLS_NUMBER = 53;
    private final int ROWS_VERTICAL_CELLS_NUMBER = 35;

    private Cell[] cells;
    private Player[] players;
    private Cell ballCell;

    private Team teamOne;
    private Team teamTwo;

    private int teamOneGoals;
    private int teamTwoGoals;

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
    private final String OTHER_PLAYER_ID = "other_player_id";
    private final String OTHER_PLAYER_NUMBER = "other_player_number";
    private final String EVENT_X = "event_coord_x";
    private final String EVENT_Y = "event_coord_y";
    private final String FOUL_ID1 = "player_1_id";
    private final String FOUL_N1 = "player_1_number";
    private final String FOUL_T1 = "player_1_team";
    private final String FOUL_ID2 = "player_2_id";
    private final String FOUL_N2 = "player_2_number";
    private final String FOUL_T2 = "player_2_team";

    private boolean drawGrid;

    private SoccerFrame container;

    private boolean showImage;
    private BufferedImage currentImage;

    private MatchState state;

    private int totalMinutes;
    private int totalSeconds;
    private boolean shouldTrackTime;

    private double referenceTimeFirstHalf;
    private double referenceTimeSecondHalf;

    public FieldPanel(SoccerFrame parent) {

        this.container = parent;

        // load image
        try {
            soccerFieldImage = ImageIO.read(SOCCER_FIELD_IMAGE_PATH);
            ballImage = ImageIO.read(BALL_IMAGE_PATH);
            gridImage = ImageIO.read(GRID_IMAGE_PATH);
            goalImage = ImageIO.read(GOAL_IMAGE_PATH);
            foulImage = ImageIO.read(FOUL_IMAGE_PATH);
            cornerImage = ImageIO.read(CORNER_IMAGE_PATH);
            throwInImage = ImageIO.read(THROW_IN_IMAGE_PATH);
            beginMatchImage = ImageIO.read(BEGIN_MATCH_IMAGE_PATH);
            firstHalfImage = ImageIO.read(FIRST_HALF_IMAGE_PATH);
            secondHalfImage = ImageIO.read(SECOND_HALF_IMAGE_PATH);
            endMatchImage = ImageIO.read(END_MATCH_IMAGE_PATH);

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

            drawGrid = false;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize (String content) {
        JsonArray jsonedPlayers = new JsonParser().parse(content).getAsJsonObject().get("players").getAsJsonArray();
        players = new Player[jsonedPlayers.size()];

        for (int i=0; i<jsonedPlayers.size(); i++) {
            // initializing bench warmers

            JsonObject player = jsonedPlayers.get(i).getAsJsonObject();
            Team team = (player.get("team").getAsString().compareTo("TEAM_ONE") == 0) ? teamOne : teamTwo;

            players[i] = new Player(player.get("number").getAsInt(), team, getCell(player.get("id").getAsInt(), 0), false, player.get("on_the_field").getAsBoolean());
        }

        for (Player p : players) {
            if (p.position != null) {
                p.position.hasPlayer = true;
                p.position.player = p;
            }
        }

        repaint();
        revalidate();
    }

    public void startDrawCycle() {
        Thread t = new Thread() {
            @Override
            public void run() {

                while (true) {
                    synchronized (lock) {
                        while (draw_this.isEmpty()) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        ArrayList<Event> current = draw_this.get(0);
                        for (Event e : current) {

                            if (shouldTrackTime) {
                                if (e instanceof MotionEvent) {
                                    MotionEvent event = (MotionEvent) e;
                                    double diff = event.getStartTime() - (referenceTimeFirstHalf + referenceTimeSecondHalf);
                                    totalMinutes = (int) (diff / 60);
                                    totalSeconds = (int) (diff % 60);

                                    StatsPanel.setTime(totalMinutes, totalSeconds);
                                }
                            }

                            e.draw();

                            repaint();
                            revalidate();
                        }

                        if (players != null) {
                            for (int i=0; i<players.length; i++) {
                                Player p = players[i];
                                if (p.onTheField) {
                                    if (p.position.y <= ROWS_VERTICAL_CELLS_NUMBER * CELL_PIXEL_SIZE - CELL_PIXEL_SIZE) {
                                        break;
                                    }

                                    if (i == players.length-1) {
                                        resetBallPosition();
                                        container.setStartButtonEnabled(true);
                                    }
                                }
                            }
                        }

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

        // draw other components
        for (Cell c : cells) {
            if (c.hasPlayer) {

                // draw current player
                Player p = c.player;

                if (p.onTheField && p.position != null) {
                    g2d.setColor(p.team.color.getColor());
                    Ellipse2D.Double circle = new Ellipse2D.Double(p.position.x - PLAYER_PADDING, p.position.y - CELL_PIXEL_SIZE - PLAYER_PADDING, 20, 20);
                    g2d.fill(circle);

                    g2d.setColor(Color.WHITE);
                    String number = String.valueOf(p.number);
                    if (number.length() == 2) {
                        g2d.drawString(number, p.position.x - 1, p.position.y - 2);
                    }
                    else {
                        g2d.drawString(number, p.position.x + 3, p.position.y - 2);
                    }

                    if (p.hasBall) {
                        // draw ball
                        if (p.team.equals(teamOne)) {
                            g2d.drawImage(ballImage, p.position.x + CELL_PIXEL_SIZE / 2, p.position.y - CELL_PIXEL_SIZE, null);
                        }
                        else {
                            g2d.drawImage(ballImage, p.position.x - CELL_PIXEL_SIZE / 2, p.position.y - CELL_PIXEL_SIZE, null);
                        }
                    }
                }
            }
            else {
                if (c.hasBall) {
                    g2d.drawImage(ballImage, c.x, c.y - CELL_PIXEL_SIZE, null);
                }
            }
        }

        if (showImage && goalImage != null) {
            int coordX = soccerFieldImage.getWidth() / 2 - goalImage.getWidth() / 2;
            int coordY = soccerFieldImage.getHeight() / 2 - goalImage.getHeight() / 2;
            g2d.drawImage(currentImage, coordX, coordY, null);
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
            if (event_type.toLowerCase().equals("match"))
                e = new MatchEvent(
                        action.get(EVENT_ID).getAsString(),
                        action.get(PLAYER_ID).getAsInt(),
                        Double.parseDouble(action.get(START_TIME).getAsString()),
                        Double.parseDouble(action.get(END_TIME).getAsString()));
            else
                if (event_type.toLowerCase().equals("catch"))  // catch event
                    m = new CatchEvent();
            else
                if (event_type.toLowerCase().equals("shot"))  // shot event
                    m = new ShotEvent();
            else
                if (event_type.toLowerCase().equals("tackle")) // tackle event
                    m = new TackleEvent(action.get(OTHER_PLAYER_ID).getAsInt(), action.get(OTHER_PLAYER_NUMBER).getAsInt());
            else
                if (event_type.toLowerCase().equals("catch")) // catch event
                    m = new CatchEvent();
            else
                if (event_type.toLowerCase().equals("move")) // move event
                    m = new MoveEvent();
            else
                if (event_type.toLowerCase().equals("unary")) { // unary event

                    if (action.get(PLAYER_TEAM).getAsString().toLowerCase().equals("team_one"))
                        player_team = teamOne;
                    else
                        player_team = teamTwo;

                    if (action.get(EVENT_ID).getAsString().toLowerCase().equals("goal")) {
                        e = new GoalEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));

                        if (player_team.equals(teamOne)) {
                            teamOneGoals++;
                        }
                        else {
                            teamTwoGoals++;
                        }
                    }
                    else if (action.get(EVENT_ID).getAsString().toLowerCase().equals("goal_kick")) {
                        e = new GoalKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                    }
                    else if (action.get(EVENT_ID).getAsString().toLowerCase().equals("corner_kick")) {
                        e = new CornerKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                    }
                    else if (action.get(EVENT_ID).getAsString().toLowerCase().equals("penalty_kick")) {
                        e = new PenaltyKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                    }
                    else if (action.get(EVENT_ID).getAsString().toLowerCase().equals("throw_in")) {
                        e = new ThrowInEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                    }
                    else if (action.get(EVENT_ID).getAsString().toLowerCase().equals("free_kick")) {
                        e = new FreeKickEvent(
                                action.get(PLAYER_ID).getAsInt(),
                                action.get(PLAYER_NUMBER).getAsInt(),
                                player_team,
                                new Coordinate(action.get(EVENT_X).getAsInt(), action.get(EVENT_Y).getAsInt()),
                                Double.parseDouble(action.get(START_TIME).getAsString()),
                                Double.parseDouble(action.get(END_TIME).getAsString()));
                    }
                }
            else
                if (event_type.toLowerCase().equals("binary")) {  // binary event
                    Team opponent;
                    if (action.get(FOUL_T1).getAsString().toLowerCase().equals("team_one")) {
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

                if (action.get(PLAYER_TEAM).getAsString().toLowerCase().equals("team_one"))
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

    public static void log(String message) {
        ref.container.log(message);
    }

    public void toggleGrid() {
        drawGrid = !drawGrid;
        repaint();
        revalidate();
    }

    private void displayGoalImage() {

        currentImage = goalImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayFoulImage() {

        currentImage = foulImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayCornerImage() {

        currentImage = cornerImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayThrowInImage() {

        currentImage = throwInImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayBeginMatchImage() {

        currentImage = beginMatchImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayFirstHalfImage() {

        currentImage = firstHalfImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displaySecondHalfImage() {

        currentImage = secondHalfImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void displayEndMatchImage() {

        currentImage = endMatchImage;
        showImage = true;

        repaint();
        revalidate();

        Timer t = new Timer (3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage = false;
                currentImage = null;

                repaint();
                revalidate();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    public static void showGoalImage() {
        ref.displayGoalImage();
        StatsPanel.setScore(ref.teamOneGoals, ref.teamTwoGoals);
    }

    public static void showFoulImage() {
        ref.displayFoulImage();
    }

    public static void showCornerImage() {
        ref.displayCornerImage();
    }

    public static void showThrowInImage() {
        ref.displayThrowInImage();
    }

    public static void showBeginMatchImage() {
        ref.displayBeginMatchImage();
    }

    public static void showFirstHalfImage() {
        ref.displayFirstHalfImage();
    }

    public static void showSecondHalfImage() {
        ref.displaySecondHalfImage();
    }

    public static void showEndMatchImage() {
        ref.displayEndMatchImage();
    }

    public static void setStartButtonText(String text) {
        ref.container.setStartButtonText(text);
    }

    public void setStartButtonEnabled(Boolean enabled) {
        ref.container.setStartButtonEnabled(enabled);
    }

    public static void setMatchState(MatchState state) {
        ref.state = state;

        if (ref.state == MatchState.SECOND_HALF) {
            ref.container.setStartButtonText("Begin second half");
        }
        else {
            ref.container.setStartButtonText("New game");
        }
    }

    public static MatchState getMatchState() {
        return ref.state;
    }

    public static void setTrackTime(boolean track, double baseTime, boolean firstHalf) {
        ref.shouldTrackTime = track;
        if (firstHalf && track) {
            ref.referenceTimeFirstHalf = baseTime;
        }
        else {
            ref.referenceTimeSecondHalf = baseTime;
        }
    }

    public static void resetBallPosition() {
        if (ref.ballCell != null) {
            ref.ballCell.hasBall = false;
        }
        ref.ballCell = ref.getCell(26, 17);
        ref.ballCell.hasBall = true;
    }

}
