package it.unipd.scd.manager;
/**
 * Created with IntelliJ IDEA.
 * User: alessandro
 * Date: 9/28/13
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.java.swing.plaf.motif.resources.motif;
import com.sun.org.apache.xpath.internal.SourceTree;
import net.miginfocom.swing.MigLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Team implements ListSelectionListener, ActionListener {
    private final String STATS_OBJECT = "stats";
    private final int TEAM_SIZE = 11;

    private String mTeamName;
    private String mTeamData;

    private ArrayList<Player> mPlayers = new ArrayList<Player>();
    private String mFormationScheme;

    MigLayout mVerticalLayout = new MigLayout("wrap, flowy",
                                       "[center]",                      // column constraints : 1 column
                                       "[][]20[][]10[]");               // row constraints    : 4 rows, middle rows with a gap of 20
    MigLayout mGrid = new MigLayout("wrap, fillx",
            "[center][center]20[center][center]",
            "[center]20[center]20[center]20[center]");

    // panel of the player's list and of the formation scheme combo box
    private JPanel mTeamPanel = new JPanel(mVerticalLayout);

    // available formation scheme
    JComboBox<String> mFormationBox = new JComboBox<String>(new String[]{ "3-5-2", "4-4-2", "5-3-2"});
    private boolean mFormationChanged = false;
  /*  private JList<String> mList = new JList<String>(new String[]{"Goal Keeper", "Back 1", "Back 2", "Back 3", "Midfielder 1", "Midfielder 2",
                                                            "Midfielder 3", "Midfielder 4", "Midfielder 5", "Forward 1", "Forward 2",
                                                            "Backup 1", "Backup 2", "Backup 3", "Backup 4", "Backup 5", "Backup 6",
                                                            "Backup 7" }); */
    private JList<String> mList;
    private DefaultListModel<String> mListModel = new DefaultListModel<String>();

    // panel of the scroll list
    private JScrollPane mPlayersListPanel;

    // panel containing the player's statistics
    private JPanel mPlayersStatsPanel = new JPanel(mGrid);
    private JLabel mKeeperLabel;                                        // goal keeper label
    private JLabel mPlayerNumber;									    // player's number text field
    private JLabel mAttack;							    			    // label 1 (attack)
    private JLabel mDefense;					    					// label 2 (defense)
    private JLabel mPower;					    					    // label 3 (power)
    private JLabel mPrecision;			    						    // label 4 (precision)
    private JLabel mSpeed;			    							    // label 5 (speed)
    private JLabel mTackle;										        // label 6 (tackle)
    private JLabel mGoalKeeping;    									// label 7 (goal_keeping)

    private JComboBox<String> mInPlayer;
    private JComboBox<String> mOutPlayer;
    private JButton mSubstitution = new JButton("Substitution");
    private int mInPlayerName;
    private int mOutPlayerName;
    private boolean mPendingSub = false;
    private int mPendingOutPlayer;
    private int mPendingInPlayer;

    private JButton mApply = new JButton("Apply");

    private JSplitPane mSplitPanel;
    private final int mSplitPanelWidth = 550;
    private final int mSplitPanelHeight = 410;

    public Team(String team, String statistics) {
        this.mTeamName = team;
        JsonObject obj = new JsonParser().parse(statistics).getAsJsonObject();
        JsonArray buf = obj.get(STATS_OBJECT).getAsJsonArray();

        JsonObject stat;
        int number, attack, defense, power, precision, speed, tackle, goal_keeping;
        String role;

        for (int i = 0; i < buf.size(); i++) {
            stat = buf.get(i).getAsJsonObject();
            number = stat.get("number").getAsInt();
            attack = stat.get("attack").getAsInt();
            defense = stat.get("defense").getAsInt();
            power = stat.get("power").getAsInt();
            precision = stat.get("precision").getAsInt();
            speed = stat.get("speed").getAsInt();
            tackle = stat.get("tackle").getAsInt();
            goal_keeping = stat.get("goal_keeping").getAsInt();
            role = stat.get("role").getAsString();

            if (goal_keeping > 0)
                mPlayers.add(0,new Player(number, attack, defense, power, precision, speed, tackle, goal_keeping, role));
            else
                mPlayers.add(new Player(number, attack, defense, power, precision, speed, tackle, goal_keeping, role));
        }

        mFormationScheme = obj.get("formation").getAsString();

       /* mPlayers.add(new Player(0,0,0,0,0,0,0,0,"Backup"));
        mPlayers.add(new Player(0,0,0,0,0,0,0,0,"Backup"));
        mPlayers.add(new Player(0,0,0,0,0,0,0,0,"Backup"));
        mPlayers.add(new Player(0,0,0,0,0,0,0,0,"Backup")); */
    }

    private void sortPlayersByRole() {
        boolean done = false;
        // put Goal Keeper at the head of the players list
        for (int i = 0; i < mPlayers.size() && !done; i++) {
            if (mPlayers.get(i).getRole().equals("Goal Keeper")) {
                Collections.swap(mPlayers, 0, i);
                done = true;
                mListModel.addElement("Goal Keeper");
            }
        }
        // put the Defenders between the Goal Keeper and the midfielders
        int def_offset = 1;
        for (int i = 1; i < mPlayers.size(); i++) {
            if (mPlayers.get(i).getRole().equals("Defender")) {
                Collections.swap(mPlayers, def_offset, i);
                mListModel.addElement("Defender " + computeRoleIndex(def_offset));
                def_offset++;
            }
        }
        // put the Midfielders between the Defenders and the forwards
        int mid_offset = def_offset;
        for (int i = mid_offset; i < mPlayers.size(); i++) {
            if (mPlayers.get(i).getRole().equals("Midfielder")) {
                Collections.swap(mPlayers, mid_offset, i);
                mListModel.addElement("Midfielder " + computeRoleIndex(mid_offset));
                mid_offset++;
            }
        }
        // add forwards to the list model
        int for_offset = mid_offset;
        for (int i = for_offset; i < mPlayers.size(); i++)
            if (mPlayers.get(i).getRole().equals("Forward")) {
                Collections.swap(mPlayers, for_offset, i);
                mListModel.addElement("Forward " + computeRoleIndex(for_offset));
                for_offset++;
            }

        // add backup players
        for (int i = for_offset; i < mPlayers.size(); i++)
            mListModel.addElement("Backup " + computeRoleIndex(i));
    }

    public void drawGUI() {
        /****************************************
         *			    FORMATION	    		*											 												    *
         ****************************************/
        if (mFormationScheme.equals("O_352"))
            mFormationBox.setSelectedIndex(0);
        else
            if (mFormationScheme.equals("B_442"))
                mFormationBox.setSelectedIndex(1);
        else
            if (mFormationScheme.equals("D_532"))
                mFormationBox.setSelectedIndex(2);

        mFormationBox.addActionListener(this);
        mFormationBox.setActionCommand("changeScheme");

        /****************************************
         *			   PLAYERS LIST    			*											 												    *
         ****************************************/
        // Crea la lista dei giocatori e la mette in uno scroll pane
        sortPlayersByRole();
        mList = new JList<String>(mListModel);
        mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mList.setSelectedIndex(0);
        mList.addListSelectionListener(this);

        /****************************************
         *		  SUBSTITUTIONS COMBO BOX	    *											 												    *
         ****************************************/
        String[] out_players = new String[TEAM_SIZE];

        out_players[0] = mPlayers.get(0).getRole();
        for (int i = 1; i < TEAM_SIZE; i++)
            out_players[i] = mPlayers.get(i).getRole() + " " + computeRoleIndex(i);

        String[] in_players;
        if (mPlayers.size() > TEAM_SIZE) {
            in_players = new String[mPlayers.size()-TEAM_SIZE];
            for (int i = 0; i < (mPlayers.size()-TEAM_SIZE); i++)
                in_players[i] = mPlayers.get(i+TEAM_SIZE).getRole() + " " + (computeRoleIndex(i)+1);
        }
        else {
            in_players = new String[1];
            in_players[0] = "No Backups Available";
        }

        DefaultComboBoxModel<String> in_model = new DefaultComboBoxModel<String>(in_players);
        DefaultComboBoxModel<String> out_model = new DefaultComboBoxModel<String>(out_players);
        mInPlayer = new JComboBox<String>(in_model);
        mOutPlayer = new JComboBox<String>(out_model);
        if (in_players[0].equals("No Backups Available")) {
            mInPlayer.setEnabled(false);
            mOutPlayer.setEnabled(false);
            mSubstitution.setEnabled(false);
        }
        mInPlayer.addActionListener(this);
        mOutPlayer.addActionListener(this);
        mInPlayer.setActionCommand("inPlayer");
        mOutPlayer.setActionCommand("outPlayer");
        mInPlayerName = TEAM_SIZE;
        mOutPlayerName = 0;

        /****************************************
         *		    PLAYER'S STAT PANEL      	*											 												    *
         ****************************************/
        // editor per modificare il nome
        mPlayerNumber = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getNumber()));
        mAttack = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getAttack()));
        mDefense = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getDefense()));
        mPower = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getPower()));
        mPrecision = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getPrecision()));
        mSpeed = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getSpeed()));
        mTackle = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getTackle()));
        mGoalKeeping = new JLabel(Integer.toString(mPlayers.get(mList.getSelectedIndex()).getGoalKeeping()));

        mKeeperLabel = new JLabel("Goal Keeping:");

        /************************************************************
         *		                  MAIN PANEL                  		*											 												    *
         ************************************************************/
        mTeamPanel.add(new JLabel("Formation Scheme:"));
        mTeamPanel.add(mFormationBox);
        mTeamPanel.add(new JLabel("Players:"));
        mPlayersListPanel = new JScrollPane(mList);
        mTeamPanel.add(mPlayersListPanel, "wmin 140, hmin 315");

        mPlayersStatsPanel.add(new JLabel("Number:"), "span 2, right");
        mPlayersStatsPanel.add(mPlayerNumber, "wrap, span 2, left, wmin 25");
        mPlayersStatsPanel.add(mKeeperLabel);
        mPlayersStatsPanel.add(mGoalKeeping, "wrap");
        mPlayersStatsPanel.add(new JLabel("Attack:"));
        mPlayersStatsPanel.add(mAttack);
        mPlayersStatsPanel.add(new JLabel("Defense:"));
        mPlayersStatsPanel.add(mDefense);
        mPlayersStatsPanel.add(new JLabel("Power:"));
        mPlayersStatsPanel.add(mPower);
        mPlayersStatsPanel.add(new JLabel("Precision:"));
        mPlayersStatsPanel.add(mPrecision);
        mPlayersStatsPanel.add(new JLabel("Speed:"));
        mPlayersStatsPanel.add(mSpeed);
        mPlayersStatsPanel.add(new JLabel("Tackle:"));
        mPlayersStatsPanel.add(mTackle, "wrap 100px");
        mPlayersStatsPanel.add(new JLabel("Out: "));
        mPlayersStatsPanel.add(mOutPlayer, "wrap");
        mPlayersStatsPanel.add(new JLabel("In: "));
        mPlayersStatsPanel.add(mInPlayer, "wrap");
        mPlayersStatsPanel.add(mSubstitution, "span 4, center");

        // Create a split pane with the two scroll panes in it.
        mSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mTeamPanel, mPlayersStatsPanel);
        mSplitPanel.setDividerLocation(150);

        // Provide a preferred size for the split pane.
        mSplitPanel.setPreferredSize(new Dimension(mSplitPanelWidth, mSplitPanelHeight));

        // "Substitution" button action listener
        mSubstitution.addActionListener(this);
        mSubstitution.setActionCommand("sub");

        // "Start" button action listener
        // mApply.addActionListener(this);
        // mApply.setActionCommand("apply");
    }

    public void valueChanged(ListSelectionEvent e) {
        // if the formation scheme has been changed through the combo box
        if (mFormationChanged) {
            mPlayerNumber.setText(Integer.toString(mPlayers.get(0).getNumber()));
            mAttack.setText(Integer.toString(mPlayers.get(0).getAttack()));
            mDefense.setText(Integer.toString(mPlayers.get(0).getDefense()));
            mPower.setText(Integer.toString(mPlayers.get(0).getPower()));
            mPrecision.setText(Integer.toString(mPlayers.get(0).getPrecision()));
            mSpeed.setText(Integer.toString(mPlayers.get(0).getSpeed()));
            mTackle.setText(Integer.toString(mPlayers.get(0).getTackle()));
            mGoalKeeping.setText(Integer.toString(mPlayers.get(0).getGoalKeeping()));
            mFormationChanged = false;
        }
        else {
            JList l = (JList)e.getSource();
            // System.out.println("List selected index: " + l.getSelectedIndex());

            // update label values
            mGoalKeeping.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getGoalKeeping()));
            mPlayerNumber.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getNumber()));
            mAttack.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getAttack()));
            mDefense.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getDefense()));
            mPower.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getPower()));
            mPrecision.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getPrecision()));
            mSpeed.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getSpeed()));
            mTackle.setText(Integer.toString(mPlayers.get(l.getSelectedIndex()).getTackle()));
        }
    }

    public void actionPerformed(ActionEvent e) {
        // fired whenever the formation scheme is changed in the combo box
        if(e.getActionCommand().equals("changeScheme")){
            mFormationChanged = true;
            JComboBox cb = (JComboBox)e.getSource();
            String scheme = (String)cb.getSelectedItem();
            updatePlayers(scheme);
        }
        else
            if (e.getActionCommand().equals("inPlayer")) {
                JComboBox cb = (JComboBox)e.getSource();
                mInPlayerName = cb.getSelectedIndex() + TEAM_SIZE;
             //   System.out.println(mInPlayerName);
            }
        else
            if (e.getActionCommand().equals("outPlayer")) {
                JComboBox cb = (JComboBox)e.getSource();
                mOutPlayerName = cb.getSelectedIndex();
          //      System.out.println(mOutPlayerName);
            }
        else
            if (e.getActionCommand().equals("sub")) {
                if (mPendingSub)
                    JOptionPane.showMessageDialog(
                            null,
                            "You can do only one substitution at a time. \n" +
                                    "Please press the \"Apply\" button to commit the previous one.",
                            "Info Message",
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    mPendingSub = true;
                    mPendingInPlayer = mPlayers.get(mInPlayerName).getNumber();
                    mPendingOutPlayer = mPlayers.get(mOutPlayerName).getNumber();
                  //  System.out.println(mPlayers.get(mOutPlayerName).getRole() + " " + mPlayers.get(mInPlayerName).getRole());
                    Collections.swap(mPlayers, mOutPlayerName, mInPlayerName);
                    String role = mPlayers.get(mOutPlayerName).getRole();
                    mPlayers.get(mOutPlayerName).setRole(mPlayers.get(mInPlayerName).getRole());
                    mPlayers.get(mInPlayerName).setRole(role);
                  //  System.out.println(mPlayers.get(mOutPlayerName).getRole() + " " + mPlayers.get(mInPlayerName).getRole());
                }
            }
    }

    private void updatePlayers(String scheme) {
        DefaultListModel<String> list_model = new DefaultListModel<String>();
        DefaultComboBoxModel<String> out_model = new DefaultComboBoxModel<String>();

        if (scheme.equals("3-5-2")) {
            list_model.addElement("Goal Keeper"); out_model.addElement("Goal Keeper");
            list_model.addElement("Defender " + computeRoleIndex(1));     out_model.addElement("Defender " + computeRoleIndex(1));
            list_model.addElement("Defender " + computeRoleIndex(2));     out_model.addElement("Defender " + computeRoleIndex(2));
            list_model.addElement("Defender " + computeRoleIndex(3));     out_model.addElement("Defender " + computeRoleIndex(3));
            mPlayers.get(4).setRole("Midfielder");
            list_model.addElement("Midfielder " + computeRoleIndex(4));   out_model.addElement("Midfielder " + computeRoleIndex(4));
            mPlayers.get(5).setRole("Midfielder");
            list_model.addElement("Midfielder " + computeRoleIndex(5));   out_model.addElement("Midfielder " + computeRoleIndex(5));
            list_model.addElement("Midfielder " + computeRoleIndex(6));   out_model.addElement("Midfielder " + computeRoleIndex(6));
            list_model.addElement("Midfielder " + computeRoleIndex(7));   out_model.addElement("Midfielder " + computeRoleIndex(7));
            list_model.addElement("Midfielder " + computeRoleIndex(8));   out_model.addElement("Midfielder " + computeRoleIndex(8));
            list_model.addElement("Forward " + computeRoleIndex(9));      out_model.addElement("Forward " + computeRoleIndex(9));
            list_model.addElement("Forward " + computeRoleIndex(10));     out_model.addElement("Forward " + computeRoleIndex(10));
        }
        else
            if (scheme.equals("4-4-2")) {
                list_model.addElement("Goal Keeper"); out_model.addElement("Goal Keeper");
                list_model.addElement("Defender " + computeRoleIndex(1));    out_model.addElement("Defender " + computeRoleIndex(1));
                list_model.addElement("Defender " + computeRoleIndex(2));    out_model.addElement("Defender " + computeRoleIndex(2));
                list_model.addElement("Defender " + computeRoleIndex(3));    out_model.addElement("Defender " + computeRoleIndex(3));
                mPlayers.get(4).setRole("Defender");
                list_model.addElement("Defender " + computeRoleIndex(4));    out_model.addElement("Defender " + computeRoleIndex(4));
                mPlayers.get(5).setRole("Midfielder");
                list_model.addElement("Midfielder " + computeRoleIndex(5));  out_model.addElement("Midfielder " + computeRoleIndex(5));
                list_model.addElement("Midfielder " + computeRoleIndex(6));  out_model.addElement("Midfielder " + computeRoleIndex(6));
                list_model.addElement("Midfielder " + computeRoleIndex(7));  out_model.addElement("Midfielder " + computeRoleIndex(7));
                list_model.addElement("Midfielder " + computeRoleIndex(8));  out_model.addElement("Midfielder " + computeRoleIndex(8));
                list_model.addElement("Forward " + computeRoleIndex(9));     out_model.addElement("Forward " + computeRoleIndex(9));
                list_model.addElement("Forward " + computeRoleIndex(10));    out_model.addElement("Forward " + computeRoleIndex(10));
            }
        else
            if (scheme.equals("5-3-2")) {
                list_model.addElement("Goal Keeper"); out_model.addElement("Goal Keeper");
                list_model.addElement("Defender " + computeRoleIndex(1));    out_model.addElement("Defender " + computeRoleIndex(1));
                list_model.addElement("Defender " + computeRoleIndex(2));    out_model.addElement("Defender " + computeRoleIndex(2));
                list_model.addElement("Defender " + computeRoleIndex(3));    out_model.addElement("Defender " + computeRoleIndex(3));
                mPlayers.get(4).setRole("Defender");
                list_model.addElement("Defender " + computeRoleIndex(4));    out_model.addElement("Defender " + computeRoleIndex(4));
                mPlayers.get(5).setRole("Defender");
                list_model.addElement("Defender " + computeRoleIndex(5));    out_model.addElement("Defender " + computeRoleIndex(5));
                list_model.addElement("Midfielder " + computeRoleIndex(6));  out_model.addElement("Midfielder " + computeRoleIndex(6));
                list_model.addElement("Midfielder " + computeRoleIndex(7));  out_model.addElement("Midfielder " + computeRoleIndex(7));
                list_model.addElement("Midfielder " + computeRoleIndex(8));  out_model.addElement("Midfielder " + computeRoleIndex(8));
                list_model.addElement("Forward " + computeRoleIndex(9));     out_model.addElement("Forward " + computeRoleIndex(9));
                list_model.addElement("Forward " + computeRoleIndex(10));    out_model.addElement("Forward " + computeRoleIndex(10));
            }
        // add backup players
        if (mPlayers.size() > TEAM_SIZE)
            for (int i = 0; i < (mPlayers.size()-TEAM_SIZE); i++)
                list_model.addElement("Backup " + (i+1));

        mList.setModel(list_model);
        mList.setSelectedIndex(0);
        mOutPlayer.setModel(out_model);
    }

    private int computeRoleIndex(int index) {
        if (index == 0)
            return 0;

        String role = mPlayers.get(index).getRole();
        if (!role.equals(mPlayers.get(index-1).getRole()))
            return 1;
        else
            return 1 + computeRoleIndex(index-1);
    }

    public String getUpdates() {
        String formation = (String)mFormationBox.getSelectedItem();
        if (formation.equals("3-5-2"))
            formation = "O_352";
        else
        if (formation.equals("4-4-2"))
            formation = "B_442";
        else
        if (formation.equals("5-3-2"))
            formation = "D_532";

        if (!formation.equals(mFormationScheme)) {
            mFormationScheme = formation;
            mTeamData = "{\"TEAM\": {\"name\":\"" + mTeamName.toUpperCase() + "\", \"formation\":\"" + formation + "\"";
            if (mPendingSub) {
                mTeamData = mTeamData + ", \"substitution\":[" + mPendingOutPlayer + "," + mPendingInPlayer + "]}}";
                mPendingSub = false;
            }
            else
                mTeamData = mTeamData + "}}";
        }
        else
            if (mPendingSub) {
                mTeamData = "{\"TEAM\": {\"name\":\"" + mTeamName.toUpperCase() + "\",";
                mTeamData = mTeamData + "\"substitution\":[" + mPendingOutPlayer + "," + mPendingInPlayer + "]}}";
                mPendingSub = false;
            }
        else {
            JOptionPane.showMessageDialog(
                    null,
                    "No updates found!",
                    "Info Message",
                    JOptionPane.INFORMATION_MESSAGE);
            mTeamData = "";
        }
       // System.out.println(mTeamData);

        return mTeamData;

        //  mTeamData  = mTeamData + "{\"team\":\"" + mTeamName.toUpperCase() + "\",\"formation\":\"" + formation + "\"}" + '\n';
    }

    public JSplitPane getSplitPane() { return mSplitPanel; }

    public JButton getApplyButton() { return mApply; }

}
