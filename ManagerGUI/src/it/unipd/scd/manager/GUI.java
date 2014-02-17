package it.unipd.scd.manager;
/**
 * Created with IntelliJ IDEA.
 * User: alessandro
 * Date: 10/1/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unipd.scd.manager.model.Coordinate;
import it.unipd.scd.manager.model.Event;
import it.unipd.scd.manager.model.TeamColor;
import it.unipd.scd.manager.model.game.*;
import it.unipd.scd.manager.model.motion.*;
import it.unipd.scd.scdcommunication.CommInterface;
import it.unipd.scd.scdcommunication.SCDComm;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class GUI implements ActionListener {
    private static GUI ref;

    private int mFrameWidth;
    private int mFrameHeight;
    private JFrame mTeamFrame;
    private Team mManagedTeam;

    private String mHost;
    private String mPort;
    private String mField;
    private String mTeam;
    private String mData;
    private String mStatistics;
    private String mTitleString;

    private JLabel mWaitForStart;
    private JButton mConnect;
    private JTextArea mLogArea;
    private JScrollPane mLogScrollPane;

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

    private it.unipd.scd.manager.model.Team teamOne;
    private it.unipd.scd.manager.model.Team teamTwo;

    public GUI() {
        //mFrameWidth = 570;
        //mFrameHeight = 600;
        mFrameWidth = 300;
        mFrameHeight = 100;
        ref = this;

        // initializing teams
        teamOne = new it.unipd.scd.manager.model.Team("Team One", TeamColor.RED);
        teamTwo = new it.unipd.scd.manager.model.Team("Team Two", TeamColor.BLUE);
    }

    public void firstScreen() {
        mTeamFrame = new JFrame("Manager - " + mTitleString);
        mTeamFrame.setLayout(new FlowLayout());

        mConnect = new JButton("Connect");
        mConnect.addActionListener(this);
        mConnect.setActionCommand("conn");

        mWaitForStart = new JLabel("Wait for the match to start...",SwingConstants.CENTER);
        mTeamFrame.getContentPane().add(mConnect);

        //Display the window.
        mTeamFrame.pack();
        mTeamFrame.setVisible(true);
        mTeamFrame.setSize(mFrameWidth, mFrameHeight);
        mTeamFrame.setResizable(false);
        mTeamFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Puts the window in the center of the screen
        Dimension dim1 = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim1.width-mFrameWidth)/2;
        int y = (dim1.height-mFrameHeight)/2;
        mTeamFrame.setLocation(x,y);
    }

    public void config() {
        BufferedReader br = null;
        try {
            String line;

            br = new BufferedReader(new FileReader("manager.conf"));

            String params = "";
            while ((line = br.readLine()) != null) {
                params += line + " ";
            }

            String[] par_array = params.split(" ");
            mHost = par_array[0];
            mPort = par_array[1];
            mField = par_array[2];

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (mField.equals("managerHome")) {
            mField = SCDComm.MANAGER_HOME_ENDPOINT;
            mTeam = "TEAM_ONE";
            mTitleString = "Team One";
        }
        else {
            mField = SCDComm.MANAGER_VISITORS_ENDPOINT;
            mTeam = "TEAM_TWO";
            mTitleString = "Team Two";
        }
    }

    public void openConnection() {
        SCDComm comm = new SCDComm(mHost, mPort, mField, new CommInterface() {
            @Override
            public void onMessage(String payload) {
             //   System.out.println("Message \n" + payload);
                JsonObject obj = new JsonParser().parse(payload).getAsJsonObject();
                if (obj.has("stats")) {
                    mStatistics = payload;
                    ShowGUI();
                }
                else
                    if (obj.has("command")) {
                        System.exit(0);
                    }
                else {
                    deserialize(payload);
                }
            }

            @Override
            public void onCommMessage(String message) {
                System.out.println("System message: " + message);
            }
        });

        comm.initConnection();
    }

    public void getStatistics() {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet get = new HttpGet("http://"+ mHost + ":" + mPort + "/manager/getStats?team=" + mTeam);
        try {
            CloseableHttpResponse response = httpClient.execute(get);
            String content = IOUtils.toString(response.getEntity().getContent());
            if (!content.equals("null")) {
                //System.out.println(content);
                mStatistics = content;
                ShowGUI();
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ShowGUI() {
        mTeamFrame.dispose();
        mFrameWidth = 570;
        mFrameHeight = 600;
        //Create and set up the window for team 1.
        mTeamFrame = new JFrame("Manage " + mTeam.toLowerCase());
        mTeamFrame.setLayout(new FlowLayout());

        mManagedTeam = new Team(mTeam.toLowerCase(), mStatistics);
        mManagedTeam.drawGUI();
        mTeamFrame.getContentPane().add(mManagedTeam.getSplitPane());
        mTeamFrame.getContentPane().add(mManagedTeam.getApplyButton());

        mManagedTeam.getApplyButton().addActionListener(this);
        mManagedTeam.getApplyButton().setActionCommand("apply");

        mLogArea = new JTextArea("Start a new game and check the logs right here", 5, 45);
        mLogArea.setEditable(false);
        mLogArea.setLineWrap(true);
        mLogArea.setWrapStyleWord(true);
        mLogScrollPane = new JScrollPane(mLogArea);
        mLogScrollPane.setWheelScrollingEnabled(true);
        mLogScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mLogScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        mTeamFrame.getContentPane().add(mLogScrollPane);

        //Display the window.
        mTeamFrame.pack();
        mTeamFrame.setVisible(true);
        mTeamFrame.setSize(mFrameWidth, mFrameHeight);
        mTeamFrame.setResizable(false);
        mTeamFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Puts the window in the center of the screen
        Dimension dim1 = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim1.width-mFrameWidth)/2;
        int y = (dim1.height-mFrameHeight)/2;
        mTeamFrame.setLocation(x,y);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("apply")) {
            mData = mManagedTeam.getUpdates();
            System.out.println(mData);

            if (!mData.equals("")) {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet get = null;
                try {
                    URI uri = new URIBuilder()
                            .setScheme("http")
                            .setHost(mHost)
                            .setPort(Integer.parseInt(mPort))
                            .setPath("/manager/updates")
                            .setParameter("data", URLEncoder.encode(mData, "UTF-8"))
                            .build();
                    get = new HttpGet(uri);
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }

                try {
                    CloseableHttpResponse response = httpclient.execute(get);
                    response.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        else
            if (e.getActionCommand().equals("conn")) {
                openConnection();
                getStatistics();
                mTeamFrame.getContentPane().remove(mConnect);
                mTeamFrame.setLayout(new BorderLayout());
                mTeamFrame.getContentPane().add(mWaitForStart,BorderLayout.CENTER);
                mTeamFrame.revalidate();
                mTeamFrame.repaint();
            }
    }

    public static void forwardLog(String message) {
        System.out.println("FORWARD LOG: " + message);
        ref.log(message);
    }

    public void log(String message) {
        try {
            mLogArea.getDocument().insertString(0, message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void deserialize(String payload) {
        JsonObject obj = new JsonParser().parse(payload).getAsJsonObject();
        JsonArray buf = obj.get(EVENTS_OBJECT).getAsJsonArray();
       // System.out.println("BUFFER SIZE: " + buf.size());

        JsonObject action;
        String event_type;
        int player_id, player_number;
        it.unipd.scd.manager.model.Team player_team;
        Coordinate from, to;
        String startTime, endTime;
        ArrayList<Event> event_array = new ArrayList<Event>();

        for (int i = 0; i < buf.size(); i++) {
            action = buf.get(i).getAsJsonObject();
            event_type = action.get(EVENT_TYPE).getAsString();
            //System.out.println("EVENTT TYPE: " + event_type);
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

                   /* if (player_team.equals(teamOne)) {
                        teamOneGoals++;
                    }
                    else {
                        teamTwoGoals++;
                    }*/
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
                it.unipd.scd.manager.model.Team opponent;
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

                m.initialize(player_id,
                        player_number,
                        player_team,
                        from,
                        to,
                        Double.parseDouble(startTime),
                        Double.parseDouble(endTime));
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

        for (Event e : event_array)
            e.draw();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        GUI manager = new GUI();
        manager.config();
   //     manager.openConnection();
        manager.firstScreen();
    //    manager.getStatistics();
    //    manager.ShowGUI();
    }
}
