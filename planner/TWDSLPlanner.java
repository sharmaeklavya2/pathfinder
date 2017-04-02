package planner;

import java.util.*;
import java.awt.Color;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import util.PQ;
import dstar.DStarLite;

import gridpanel.GridPanelCell;
import gridpanel.MutableGridPanelCell;
import graph.GridGraph;
import planner.AbstractPlanner;
import robot.GridRobot;

public class TWDSLPlanner extends AbstractPlanner
{
    private GridRobot robot;
    private int start;
    private GridGraph graph;
    private DStarLite dstar, rdstar;
    private DStarLite.Callback dstarCallback;

    public GridRobot getRobot() {
        return robot;
    }
    public int getStart() {
        return start;
    }

    public void setCallback(Callback c) {
        callback = c;
        dstarCallback = new DStarLite.Callback() {
            @Override
            public void nodeUpdate(int u) {
                callback.nodeUpdate(u);
            }
        };
    }

    public TWDSLPlanner(int start, int goal, GridRobot robot, AbstractPlanner.Callback callback) {
        this.goal = goal;
        this.start = start;
        setCallback(callback);
        resetRobot(robot);
    }
    public TWDSLPlanner(int start, int goal, GridRobot robot) {
        this(start, goal, robot, new AbstractPlanner.Callback());
    }

    public void resetRobot(GridRobot robot) {
    /* Reset Planner */
        this.robot = robot;
        graph = robot.getGraph();
        dstar = new DStarLite(goal, graph, dstarCallback);
        rdstar = new DStarLite(start, graph, dstarCallback);
        reset();
    }

    public void reset() {
        dstar.reset();
        rdstar.reset();
        replan();
    }
    public int getNext(int u) {
        return dstar.getNext(u);
    }
    protected void updateNode(int u) {
        dstar.updateNode(u);
        rdstar.updateNode(u);
    }
    protected void examineUpdates(Set<Integer> l) {
        dstar.examineUpdates(l);
        rdstar.examineUpdates(l);
    }
    public long replan()
    {
        System.err.println("Replanning");
        long pops;
        for(pops=0; dstar.replanIter(robot.getPosition()); ++pops);
        for(; rdstar.replanIter(goal); ++pops);
        callback.pathUpdate();
        return pops;
    }

    public GridPanelCell getGridPanelCell(int u, boolean onPath) {
        int v = getNext(u);
        int type = 0;
        int arrowX = 0, arrowY = 0;
        double gu = dstar.getG(u), rhsu = dstar.getRhs(u);
        double rgu = rdstar.getG(u), rrhsu = rdstar.getRhs(u);
        double diff = gu - rhsu, rdiff = rgu - rrhsu;
        boolean isOpen = !(Double.isInfinite(gu) && Double.isInfinite(rhsu)) && abs(diff) >= PQ.PQElem.EPS;
        boolean risOpen = !(Double.isInfinite(rgu) && Double.isInfinite(rrhsu)) && abs(rdiff) >= PQ.PQElem.EPS;

        double dgoal = dstar.getG(goal) + rdstar.getG(goal);
        if(graph instanceof GridGraph) {
            GridGraph gridGraph = (GridGraph)graph;
            type = gridGraph.getNode(u).getType();
            if(v != -1) {
                arrowX = gridGraph.diffJ(u, v);
                arrowY = gridGraph.diffI(u, v);
            }
            else {
                arrowX = arrowY = 0;
            }
        }

        Color color;
        if(type > 0) {
            if(onPath) {
                if(onPath)
                    color = Color.MAGENTA.darker();
                else
                    color = Color.BLUE.darker();
            }
            else {
                if(isOpen)
                    color = Color.RED.darker();
                else
                    color = Color.BLACK;
            }
        }
        else {
            int curr = robot.getPosition();
            if(u == curr || u == goal) {
                if(u == curr && u == goal) {
                    color = Color.GREEN.darker();
                }
                else {
                    color = Color.GREEN;
                }
            }
            else if(onPath) {
                if(isOpen || risOpen) {
                    color = Color.MAGENTA;
                }
                else if(v == -1) {
                    color = Color.CYAN.darker();
                }
                else {
                    color = Color.CYAN;
                }
            }
            else {
                if(isOpen) {
                    color = new Color(255, 127, 127);
                    if(risOpen) {
                        color = new Color(255, 127, 255);
                    }
                }
                else if(risOpen) {
                    color = new Color(127, 127, 255);
                }
                else if(v == -1) {
                    color = Color.LIGHT_GRAY;
                }
                else {
                    double du = gu + rgu;
                    if(abs(du - dgoal) < PQElem.EPS) {
                        color = new Color(191, 255, 255);
                    }
                    else {
                        color = Color.WHITE;
                    }
                }
            }
        }
        MutableGridPanelCell gpc = new MutableGridPanelCell();
        gpc.setColor(color);
        gpc.setArrowX(arrowX);
        gpc.setArrowY(arrowY);
        gpc.setToolTip("g: " + gu + "\nrhs: " + rhsu + "\nrg: " + rgu + "\nrrhs: " + rrhsu
            + "\ntotal: " + (gu + rgu));
        return gpc;
    }
}
