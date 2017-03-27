package driver;

import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import util.GUIUtil;
import graph.*;
import planner.*;
import gridpanel.GridPanel;
import gridpanel.GridPanelCell;

import java.lang.Thread;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public abstract class GUIAdjDriver
{
    public static class MyGridPanel extends GridPanel
    {
        AbstractAdjacentPlanner planner;
        GridGraph graph;

        public MyGridPanel(AbstractAdjacentPlanner planner, GridGraph graph) {
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
            GridGraphNode node = graph.getNode(row, col);
            int type = node.getType();
            int type2 = type == 0 ? 1 : 0;
            graph.update(row, col, new GridGraphNode(type2, node.getOcc()));
        }
    }

    public static void run(String fpath, String plannerType, final int callbackSleep) throws IOException, GridGraphCreateException
    {
        GUIUtil.setlf();
        GridGraph graph;
        GridPanel gridPanel;
        AbstractAdjacentPlanner planner;

        // get graph
        BufferedReader br = new BufferedReader(new FileReader(fpath));
        String s;
        ArrayList<String> lines = new ArrayList<String>();
        while((s = br.readLine()) != null)
            lines.add(s);
        //System.err.println(lines);
        graph = new GridGraph(lines);
        int grows = graph.getRows(), gcols = graph.getCols();
        int start=-1, goal=-1;
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
            throw new GridGraphCreateException("Start node not specified");
        if(goal == -1)
            throw new GridGraphCreateException("Goal node not specified");

        // get planner
        if(plannerType.equals("DijkstraPlanner")) {
            planner = new DijkstraPlanner(start, goal, graph);
        }
        else {
            throw new RuntimeException("Invalid Planner type");
        }

        // get GridPanel
        gridPanel = new MyGridPanel(planner, graph);

        // add callbacks
        GridGraphUpdateCallback gguc = new GridGraphUpdateCallback() {
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

        NodeUpdateCallback nuc = new NodeUpdateCallback() {
            @Override
            public void run(int u) {
                try {
                    if(callbackSleep > 0)
                        Thread.sleep(callbackSleep);
                }
                catch(InterruptedException e) {}
                System.err.println("nuc(" + u + ")");
                gridPanel.setCell(u / gcols, u % gcols, planner.getGridPanelCell(u));
            }
        };
        planner.setCallback(nuc);

        // draw buttons panel
        JButton moveButton = new JButton("Move");
        moveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                (new Thread("move_thread") {
                    @Override
                    public void run() {
                        if(planner.getCurr() != planner.getGoal())
                            planner.move();
                        if(planner.getCurr() == planner.getGoal())
                            moveButton.setEnabled(false);
                    }
                }).start();
            }
        });

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
                        planner.reset(start);
                    }
                }).start();
            }
        }
        resetButton.addActionListener(new ResetActionListener(start));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(moveButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(resetButton);

        // draw grid
        GUIUtil.makeGUI("Dijkstra", gridPanel, buttonPanel, null);
    }

    public static void main(String[] args) throws IOException, GridGraphCreateException {
        String usage = "usage: java driver.GUIAdjDriver <fpath> <plannerType> <callbackSleep>";
        if(args.length == 3) {
            String fpath = args[0];
            String plannerType = args[1];
            int callbackSleep = Integer.parseInt(args[2]);
            run(fpath, plannerType, callbackSleep);
        }
        else {
            System.err.println(usage);
            System.exit(1);
        }
    }
}
