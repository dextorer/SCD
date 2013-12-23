package it.unipd.scd.gui;

import it.unipd.scd.scdcommunication.CommInterface;
import it.unipd.scd.scdcommunication.SCDComm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        JButton button = new JButton("Connect");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SCDComm comm = new SCDComm("localhost", "28000", SCDComm.FIELD_ENDPOINT, new CommInterface() {
                    @Override
                    public void onMessage(String payload) {
                        System.out.println("Message " + (msgCount++) + "\n" + payload);
                    }

                    @Override
                    public void onCommMessage(String message) {
                        System.out.println("System message: " + message);
                    }
                });

                comm.initConnection();
            }
        });

        buttonPanel.add(button);

        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);

//        button.doClick();
        drawTest();
    }

    public void drawTest() {
        fieldPanel.drawADot();
    }

}
