package driver;

import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.SwingUtilities;

import util.GUIUtil;
import graph.*;
import planner.*;
import gridpanel.GridPanel;
import gridpanel.GridPanelCell;
import robot.*;

import java.lang.Thread;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public abstract class GuiDriver
{
    public static class MyGridPanel extends GridPanel
    {
        AbstractPlanner planner;
        GridGraph graph;

        public MyGridPanel(AbstractPlanner planner, GridGraph graph) {
            super(graph.getRows(), graph.getCols());
            this.planner = planner;
            this.graph = graph;

            int grows = graph.getRows(), gcols = graph.getCols();
            for(int i=0; i<grows; ++i) {
                for(int j=0; j<gcols; ++j) {
                    setCell(i, j, planner.getGridPanelCell(i * gcols + j));
                }
            }
        }

        @Override
        public void handleClick(int row, int col) {
            GridGraph.Node node = graph.getNode(row, col);
            int type = node.getType();
            int type2 = type == 0 ? 1 : 0;
            graph.update(row, col, new GridGraph.Node(type2, node.getOcc()));
        }
    }

    private static AbstractPlanner getPlanner(String name, int start, int goal, GridGraph graph) {
        Robot robot = new GridRobot(graph, start);
        if(name.equals("DijkstraPlanner")) {
            return new DijkstraPlanner(goal, robot);
        }
        else {
            throw new RuntimeException("Invalid Planner type");
        }
    }

    private static GridGraph graph;
    private static GridPanel gridPanel;
    private static AbstractPlanner planner;
    private static int start=-1, goal=-1;
    private static long total_replan_time = 0;

    public static void run(String fpath, String plannerType, final int callbackSleep,
        int sensorRadius) throws IOException, GridGraph.CreateException
    {
        GUIUtil.setlf();

        // get graph
        BufferedReader fbr = new BufferedReader(new FileReader(fpath));
        String s;
        ArrayList<String> lines = new ArrayList<String>();
        while((s = fbr.readLine()) != null)
            lines.add(s);
        fbr.close();
        //System.err.println(lines);
        graph = new GridGraph(lines);
        int grows = graph.getRows(), gcols = graph.getCols();
        for(int i=0; i < grows; ++i) {
            for(int j=0; j < gcols; ++j) {
                char ch = lines.get(i).charAt(j);
                int k = i * gcols + j;
                if(ch == 'S' || ch == 's')
                    start = k;
                else if(ch == 'G' || ch == 'g')
                    goal = k;
            }
        }
        if(start == -1)
            throw new GridGraph.CreateException("Start node not specified");
        if(goal == -1)
            throw new GridGraph.CreateException("Goal node not specified");

        // get planner
        planner = getPlanner(plannerType, start, goal, graph);

        // get GridPanel
        gridPanel = new MyGridPanel(planner, graph);

        // add callbacks
        GridGraph.UpdateCallback gguc = new GridGraph.UpdateCallback() {
            @Override
            public void run(int i, int j) {
                try {
                    if(callbackSleep > 0)
                        Thread.sleep(callbackSleep);
                }
                catch(InterruptedException e) {}
                System.err.println("gguc(" + i + ", " + j + ")");
                int u = i * graph.getCols() + j;
                gridPanel.setCell(i, j, planner.getGridPanelCell(u));
            }
        };
        graph.setCallback(gguc);

        AbstractPlanner.Callback nuc = new AbstractPlanner.Callback() {
            @Override
            public void nodeUpdate(int u) {
                try {
                    if(callbackSleep > 0)
                        Thread.sleep(callbackSleep);
                }
                catch(InterruptedException e) {}
                //System.err.println("nuc(" + u + ")");
                gridPanel.setCell(u, planner.getGridPanelCell(u));
            }
            @Override
            public void move(int u, int v) {
                nodeUpdate(u);
                nodeUpdate(v);
            }
            @Override
            public void pathUpdate() {
                (new Thread("pathUpdate_thread") {
                    @Override
                    public void run() {
                        System.err.println("pathUpdate()");
                        int curr = planner.getRobot().getPosition();
                        if(curr != -1) {
                            curr = planner.getNext(curr);
                        }
                        if(curr != -1) {
                            int goal = planner.getGoal();
                            while(curr != goal) {
                                gridPanel.setCell(curr, planner.getGridPanelCell(curr, true));
                                curr = planner.getNext(curr);
                            }
                        }
                    }
                }).start();
            }
        };
        planner.setCallback(nuc);
        nuc.pathUpdate();

        // draw status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 3));
        JLabel distanceLabel = new JLabel("Distance covered: 0");
        JLabel replanLabel = new JLabel("Replanning time: 0");
        int pos = planner.getRobot().getPosition();
        JLabel posLabel = new JLabel("Position: " + pos + "(" + (pos / gcols) + ", " + (pos % gcols) + ")");
        statusPanel.add(distanceLabel);
        statusPanel.add(replanLabel);
        statusPanel.add(posLabel);

        // draw buttons panel
        JButton moveButton = new JButton("Move");
        class MoveActionListener implements ActionListener {
            int sensorRadius;
            public MoveActionListener(int sensorRadius) {
                this.sensorRadius = sensorRadius;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                (new Thread("move_thread") {
                    @Override
                    public void run() {
                        if(planner.getRobot().getPosition() != planner.getGoal())
                            total_replan_time += planner.move(sensorRadius);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                distanceLabel.setText("Distance covered: " + planner.getDistance());
                                replanLabel.setText("Replanning time: " + total_replan_time);
                                int pos = planner.getRobot().getPosition();
                                posLabel.setText("Position: " + pos + "(" + (pos / gcols) + ", " + (pos % gcols) + ")");
                                if(planner.getRobot().getPosition() == planner.getGoal())
                                    moveButton.setEnabled(false);
                            }
                        });
                    }
                }).start();
            }
        }
        moveButton.addActionListener(new MoveActionListener(sensorRadius));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                (new Thread("refresh_thread") {
                    @Override
                    public void run() {
                        for(int i=0; i<grows; ++i) {
                            for(int j=0; j<gcols; ++j) {
                                gridPanel.setCell(i, j, planner.getGridPanelCell(i * graph.getCols() + j));
                            }
                        }
                    }
                }).start();
            }
        });

        JButton resetButton = new JButton("Reset");
        class ResetActionListener implements ActionListener {
            int start;
            public ResetActionListener(int start) {
                this.start = start;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                (new Thread("reset_thread") {
                    @Override
                    public void run() {
                        int goal = planner.getGoal();
                        planner = getPlanner(plannerType, start, goal, graph);
                        planner.setCallback(nuc);
                        planner.reset();
                        total_replan_time = 0;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                distanceLabel.setText("Distance covered: " + planner.getDistance());
                                replanLabel.setText("Replanning time: " + total_replan_time);
                                int pos = planner.getRobot().getPosition();
                                posLabel.setText("Position: " + pos + "(" + (pos / gcols) + ", " + (pos % gcols) + ")");
                                moveButton.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        }
        resetButton.addActionListener(new ResetActionListener(start));

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.err.println("Export called");
                (new Thread("export_thread") {
                    @Override
                    public void run() {
                        String fname = "output.txt";
                        String s = graph.serialize();
                        try {
                            PrintWriter writer = new PrintWriter(fname, "UTF-8");
                            writer.print(s);
                            writer.close();
                        }
                        catch(IOException e) {
                            System.err.println("Couldn't write to " + fname);
                        }
                    }
                }).start();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(moveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(exportButton);

        // draw grid
        GUIUtil.makeGUI(plannerType, gridPanel, buttonPanel, statusPanel);

        BufferedReader inbr = new BufferedReader(new InputStreamReader(System.in));
        String[] words;
        while((s = inbr.readLine()) != null) {
            words = s.split(" ");
            if(words.length > 0) {
                if(words[0].equals("replace")) {
                    fbr = new BufferedReader(new FileReader(words[1]));
                    graph.copyFrom(new GridGraph(fbr));
                    fbr.close();
                }
            }
        }
        inbr.close();
    }

    public static void main(String[] args) throws IOException, GridGraph.CreateException {
        String usage = "usage: java driver.GuiDriver <fpath> <plannerType> <callbackSleep> <sensorRadius>";
        if(args.length == 4) {
            String fpath = args[0];
            String plannerType = args[1];
            int callbackSleep = Integer.parseInt(args[2]);
            int sensorRadius = Integer.parseInt(args[3]);
            run(fpath, plannerType, callbackSleep, sensorRadius);
        }
        else {
            System.err.println(usage);
            System.exit(1);
        }
    }
}
