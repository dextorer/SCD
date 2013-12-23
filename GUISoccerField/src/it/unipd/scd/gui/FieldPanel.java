package it.unipd.scd.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unipd.scd.model.Coordinate;
import it.unipd.scd.model.Event;
import it.unipd.scd.model.Team;
import it.unipd.scd.model.TeamColor;
import it.unipd.scd.model.game.MatchEvent;
import it.unipd.scd.model.motion.CatchEvent;
import it.unipd.scd.model.motion.MotionEvent;

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
import java.util.Random;

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
    private final String EVENT_TYPE = "type_of_event";
    private final String EVENT_ID = "event_id";
    private final String PLAYER_ID = "player_id";
    private final String PLAYER_NUMBER = "player_number";
    private final String PLAYER_TEAM = "player_team";
    private final String FROM_X = "from_x";
    private final String FROM_Y = "from_y";
    private final String TO_X = "to_x";
    private final String TO_Y = "to_y";

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

            players[i] = new Player(player.get("number").getAsInt(), team, cells[new Random().nextInt(cells.length)], false);
        }

        for (Player p : players) {
            p.position.hasPlayer = true;
            p.position.player = p;
        }

        repaint();
        revalidate();
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

//        AffineTransform original = g2d.getTransform();

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

    public void Deserialize(String payload) {
        JsonParser parser = new JsonParser();
        JsonArray buf = parser.parse(payload).getAsJsonArray();

        JsonObject action;
        String event_type;
        int player_id, player_number;
        Team player_team;
        Coordinate from, to;
        ArrayList<Event> event = new ArrayList<Event>();

        for (int i = 0; i < buf.size(); i++) {
            event.clear();

            action = buf.get(i).getAsJsonObject();
            event_type = action.get(EVENT_TYPE).toString();

            // id number team from to
            if (event_type.equals("match")) {  // match event
                Event game = new MatchEvent(action.get(EVENT_ID).toString(), action.get(PLAYER_ID).getAsInt());
                event.add(game);
            }
            else if (event_type.equals("catch")) { // catch event
                MotionEvent motion = new CatchEvent();
                player_id = action.get(PLAYER_ID).getAsInt();

              //  motion.initialize();
            }
        }
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
