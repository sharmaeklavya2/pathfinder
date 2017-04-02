package planner;

import java.util.*;
import java.awt.Color;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import util.PQ;
import dstar.DStarLite;

import gridpanel.GridPanelCell;
import gridpanel.MutableGridPanelCell;
import graph.AbstractGraph;
import graph.GridGraph;
import planner.AbstractPlanner;
import robot.Robot;

public class DStarLitePlanner extends AbstractPlanner
{
    private Robot robot;
    private AbstractGraph graph;
    private DStarLite dstar;
    private DStarLite.Callback dstarCallback;

    public Robot getRobot() {
        return robot;
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

    public DStarLitePlanner(int goal, Robot robot, AbstractPlanner.Callback callback) {
        this.goal = goal;
        setCallback(callback);
        resetRobot(robot);
    }
    public DStarLitePlanner(int goal, Robot robot) {
        this(goal, robot, new AbstractPlanner.Callback());
    }

    public void resetRobot(Robot robot) {
    /* Reset Planner */
        this.robot = robot;
        graph = robot.getGraph();
        dstar = new DStarLite(goal, graph, dstarCallback);
        reset();
    }

    public void reset() {
        dstar.reset();
        replan();
    }
    public int getNext(int u) {
        return dstar.getNext(u);
    }
    protected void updateNode(int u) {
        dstar.updateNode(u);
    }
    protected void examineUpdates(Set<Integer> l) {
        dstar.examineUpdates(l);
    }
    public long replan()
    {
        System.err.println("Replanning");
        long pops;
        for(pops=0; dstar.replanIter(robot.getPosition()); ++pops);
        callback.pathUpdate();
        return pops;
    }

    public GridPanelCell getGridPanelCell(int u, boolean onPath) {
        int v = getNext(u);
        int type = 0;
        int arrowX = 0, arrowY = 0;
        double gu = dstar.getG(u), rhsu = dstar.getRhs(u);
        double diff = gu - rhsu;
        boolean isOpen = !(Double.isInfinite(gu) && Double.isInfinite(rhsu)) && abs(diff) >= PQ.PQElem.EPS;

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
                if(isOpen) {
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
                    color = new Color(255, 63, 63);
                }
                else if(v == -1) {
                    color = Color.LIGHT_GRAY;
                }
                else {
                    color = Color.WHITE;
                }
            }
        }
        MutableGridPanelCell gpc = new MutableGridPanelCell();
        gpc.setColor(color);
        gpc.setArrowX(arrowX);
        gpc.setArrowY(arrowY);
        gpc.setToolTip("g: " + gu + "\nrhs: " + rhsu);
        return gpc;
    }
}
