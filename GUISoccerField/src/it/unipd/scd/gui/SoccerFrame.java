package it.unipd.scd.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unipd.scd.scdcommunication.CommInterface;
import it.unipd.scd.scdcommunication.SCDComm;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dextor
 * Date: 9/22/13
 * Time: 9:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class SoccerFrame extends JFrame {

    private FieldPanel fieldPanel;
    private StatsPanel statsPanel;
    private JPanel buttonPanel;

    private static int msgCount = 1;

    private JButton connect;
    private JButton start;
    private JButton pause;
    private JButton quit;
    private JButton newGame;
    private JButton toggleGrid;

    private JTextArea logArea;
    private JScrollPane logScrollPane;

    private String host;
    private String port;

    private SCDComm comm;

    public SoccerFrame() {

        readConfig();

        fieldPanel = new FieldPanel(this);
//        fieldPanel.setBorder(BorderFactory.createTitledBorder("Game area"));

        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        statsPanel = new StatsPanel();
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        connect = new JButton("Connect");
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                comm = new SCDComm(host, port, SCDComm.FIELD_ENDPOINT, new CommInterface() {
                    @Override
                    public void onMessage(String payload) {
                        System.out.println("Message " + (msgCount++) + "\n" + payload);
                        fieldPanel.deserialize(payload);
                    }

                    @Override
                    public void onCommMessage(String message) {
                        System.out.println("System message: " + message);
                    }
                });

                comm.initConnection();
            }
        });

        start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CloseableHttpClient httpclient = HttpClients.createDefault();

                HttpGet get = new HttpGet("http://" + host + ":" + port + "/field/getParams");
                try {
                    CloseableHttpResponse response = httpclient.execute(get);
                    String content = IOUtils.toString(response.getEntity().getContent());
                    fieldPanel.initialize(content);
                    response.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                get = new HttpGet("http://" + host + ":" + port + "/field/newGame");
                try {
                    CloseableHttpResponse response = httpclient.execute(get);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        httpclient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                fieldPanel.startDrawCycle();

            }
        });

        pause = new JButton("Pause");
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet get = new HttpGet("http://" + host + ":" + port + "/field/pauseGame");

                try {
                    CloseableHttpResponse response = client.execute(get);
                    String content = IOUtils.toString(response.getEntity().getContent());
                    String status = new JsonParser().parse(content).getAsJsonObject().get("status").getAsString();
                    if (status.toLowerCase().equals("paused")) {
                        pause.setText("Resume");
                    }
                    else {
                        pause.setText("Pause");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    get.releaseConnection();
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        quit = new JButton("Quit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet get = new HttpGet("http://" + host + ":" + port + "/field/quit");

                try {
                    CloseableHttpResponse response = client.execute(get);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    get.releaseConnection();
                    try {
                        client.close();
                        dispose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        newGame = new JButton("New game");
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet get = new HttpGet("http://" + host + ":" + port + "/field/newGame");

                try {
                    CloseableHttpResponse response = client.execute(get);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    get.releaseConnection();
                    try {
                        client.close();
                        dispose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        toggleGrid = new JButton("Toggle grid");
        toggleGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fieldPanel.toggleGrid();
            }
        });

        buttonPanel.add(connect);
        buttonPanel.add(start);
        buttonPanel.add(pause);
        buttonPanel.add(quit);
        buttonPanel.add(toggleGrid);

        logArea = new JTextArea("Start a new game and check the logs right here", 5, 40);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setWheelScrollingEnabled(true);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));

        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel testContainer = new JPanel();
        testContainer.setLayout(new BorderLayout());
        testContainer.add(fieldPanel, BorderLayout.CENTER);
        testContainer.add(statsPanel, BorderLayout.SOUTH);

        add(buttonPanel, BorderLayout.NORTH);
        add(testContainer, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        pack();
        setVisible(true);
        setTitle("Soccer - A concurrent and distributed game simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // closing connections to the server
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (comm != null && comm.isOpen()) {
                    comm.terminateConnection("field");
                }
            }
        }));
    }

    private void readConfig() {
        File f = new File("gui.conf");

        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "No configuration file detected! Sticking to default values.");
            host = "localhost";
            port = "28000";
        }
        else {
            try {
                String content = IOUtils.toString(new FileInputStream(f));
                JsonObject root = new JsonParser().parse(content).getAsJsonObject();
                host = root.get("host").getAsString();
                port = root.get("port").getAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void drawTest() {
        fieldPanel.drawADot();
    }

    public void log(String message) {
        try {
            logArea.getDocument().insertString(0, message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}
