package it.unipd.scd.gui;

import it.unipd.scd.scdcommunication.CommInterface;
import it.unipd.scd.scdcommunication.SCDComm;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private static int msgCount = 1;

    public SoccerFrame() {

        fieldPanel = new FieldPanel();

        JPanel buttonPanel = new JPanel();

        JButton connect = new JButton("Connect");
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SCDComm comm = new SCDComm("localhost", "28000", SCDComm.FIELD_ENDPOINT, new CommInterface() {
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

        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CloseableHttpClient httpclient = HttpClients.createDefault();

                HttpGet get = new HttpGet("http://localhost:28000/field/getParams");
                try {
                    CloseableHttpResponse response = httpclient.execute(get);
                    String content = IOUtils.toString(response.getEntity().getContent());
                    fieldPanel.initialize(content);
                    response.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                get = new HttpGet("http://localhost:28000/field/newGame");
                try {
                    CloseableHttpResponse response = httpclient.execute(get);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                fieldPanel.startDrawCycle();

            }
        });

        JButton toggleGrid = new JButton("Toggle grid");
        toggleGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fieldPanel.toggleGrid();
            }
        });

        buttonPanel.add(connect);
        buttonPanel.add(start);
        buttonPanel.add(toggleGrid);

        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);

//        connect.doClick();
//        drawTest();
    }

    public void drawTest() {
        fieldPanel.drawADot();
    }

}
