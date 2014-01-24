package it.unipd.scd.manager;
/**
 * Created with IntelliJ IDEA.
 * User: alessandro
 * Date: 10/1/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */

import it.unipd.scd.scdcommunication.CommInterface;
import it.unipd.scd.scdcommunication.SCDComm;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.io.IOUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import javax.swing.JFrame;

public class GUI implements ActionListener {
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

    public GUI() {
        mFrameWidth = 570;
        mFrameHeight = 520;
    };

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
    }

    public void openConnection() {
        if (mField.equals("managerHome")) {
            mField = SCDComm.MANAGER_HOME_ENDPOINT;
            mTeam = "TEAM_ONE";
        }
        else {
            mField = SCDComm.MANAGER_VISITORS_ENDPOINT;
            mTeam = "TEAM_TWO";
        }

        SCDComm comm = new SCDComm(mHost, mPort, mField, new CommInterface() {
            @Override
            public void onMessage(String payload) {
                System.out.println("Message \n" + payload);
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
            System.out.println(content);
            mStatistics = content;
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAndShowGUI() {
        //Create and set up the window for team 1.
        mTeamFrame = new JFrame("Manage " + mTeam.toLowerCase());
        mTeamFrame.setLayout(new FlowLayout());

        mManagedTeam = new Team(mTeam.toLowerCase(), mStatistics);
        mManagedTeam.drawGUI();
        mTeamFrame.getContentPane().add(mManagedTeam.getSplitPane());
        mTeamFrame.getContentPane().add(mManagedTeam.getApplyButton());

        mManagedTeam.getApplyButton().addActionListener(this);
        mManagedTeam.getApplyButton().setActionCommand("apply");

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
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        GUI manager = new GUI();
        manager.config();
        manager.openConnection();
        manager.getStatistics();
        manager.createAndShowGUI();
    }
}
