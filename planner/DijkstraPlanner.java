package planner;

import java.util.*;
import java.awt.Color;
import static java.lang.Math.abs;

import gridpanel.GridPanelCell;
import gridpanel.MutableGridPanelCell;
import graph.AbstractGraph;
import graph.GenGraph;
import graph.GridGraph;
import graph.Edge;
import planner.AbstractPlanner;
import robot.Robot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class PQElem implements Comparable<PQElem>
{
    int value;
    double priority;
    public static final double EPS = 0.00001;

    public int getValue() {return value;}
    public double getPriority() {return priority;}

    public PQElem(int value, double priority) {
        this.value = value;
        this.priority = priority;
    }
    public int compareTo(PQElem pq) {
        double diff = priority - pq.priority;
        if(diff >= EPS)
            return 1;
        else if(diff <= -EPS)
            return -1;
        else
            return 0;
    }
}

abstract class DijkstraPlannerHelper extends AbstractPlanner
{
    public static enum Stage
    {NEW, OPEN, CLOSED}

    private Stage stage[];
    private double dist[];
    private int next[];

    public double getDist(int u) {return dist[u];}
    public int getNext(int u) {return next[u];}
    public Stage getStage(int u) {return stage[u];}

    protected void setDist(int u, double dist) {
        this.dist[u] = dist;
        callback.nodeUpdate(u);
    }
    protected void setNext(int u, int next) {
        this.next[u] = next;
        callback.nodeUpdate(u);
    }
    protected void setStage(int u, Stage stage) {
        this.stage[u] = stage;
        callback.nodeUpdate(u);
    }
    protected void setDistNextStage(int u, double dist, int next, Stage stage) {
        this.dist[u] = dist;
        this.next[u] = next;
        this.stage[u] = stage;
        callback.nodeUpdate(u);
    }

    protected DijkstraPlannerHelper(int n)
    {
        super();
        stage = new Stage[n];
        dist = new double[n];
        next = new int[n];
    }
}

public class DijkstraPlanner extends DijkstraPlannerHelper
{
    private PriorityQueue<PQElem> pq;
    private Robot robot;
    private AbstractGraph graph;

    public DijkstraPlanner(int goal, Robot robot, AbstractPlanner.Callback callback) {
        super(robot.getGraph().size());
        this.goal = goal;
        this.callback = callback;
        resetRobot(robot);
    }
    public DijkstraPlanner(int goal, Robot robot) {
        this(goal, robot, new AbstractPlanner.Callback());
    }

    public Robot getRobot() {
        return robot;
    }
    public void resetRobot(Robot robot) {
    /* Reset Planner */
        this.robot = robot;
        this.graph = robot.getGraph();
        replan();
    }

    protected void examineUpdates(Set<Integer> l) {}

    public void reset() {
        replan();
    }

    public GridPanelCell getGridPanelCell(int u, boolean onPath) {
        int v = getNext(u);
        int type = 0;
        Stage stage = getStage(u);
        int arrowX = 0, arrowY = 0;

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

        //List<int> path = getPath(getCurr());
        Color color;
        if(type > 0) {
            if(onPath) {
                if(stage == Stage.OPEN)
                    color = Color.MAGENTA.darker();
                else
                    color = Color.BLUE.darker();
            }
            else {
                if(stage == Stage.OPEN)
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
                if(stage == Stage.NEW) {
                    color = Color.CYAN.darker();
                }
                else if(stage == Stage.OPEN) {
                    color = Color.MAGENTA;
                }
                else {
                    color = Color.CYAN;
                }
            }
            else {
                if(stage == Stage.NEW) {
                    color = Color.LIGHT_GRAY;
                }
                else if(stage == Stage.OPEN) {
                    color = new Color(255, 63, 63);
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
        return gpc;
    }

    public long replan()
    {
        System.err.println("Replanning");
        for(int i=0; i < graph.size(); ++i) {
            setDistNextStage(i, Double.POSITIVE_INFINITY, -1, Stage.NEW);
        }
        setDistNextStage(goal, 0, goal, Stage.OPEN);
        pq = new PriorityQueue<PQElem>();
        pq.add(new PQElem(goal, getDist(goal)));

        long pops;
        for(pops = 0; !pq.isEmpty(); ++pops)
        {
            PQElem head = pq.poll();
            int u = head.getValue();
            double prio = head.getPriority();
            if(getStage(u) == Stage.CLOSED)
                continue;
            else if(getStage(u) != Stage.OPEN)
                throw new RuntimeException("Stage " + getStage(u) + " node found in priority queue");
            else if(abs(prio - getDist(u)) >= PQElem.EPS)
                throw new RuntimeException("Mismatch between priority queue ("
                    + prio + ") and dist[" + u + "] (" + getDist(u) + ")");
            setStage(u, Stage.CLOSED);
            if(u == robot.getPosition())
                break;

            for(Map.Entry<Integer, Double> entry: graph.getPreds(u).entrySet()) {
                int v = entry.getKey();
                double w = entry.getValue();
                if(getStage(v) == Stage.NEW) {
                    setDistNextStage(v, getDist(u) + w, u, Stage.OPEN);
                    pq.add(new PQElem(v, getDist(v)));
                }
                else if(getStage(v) == Stage.OPEN) {
                    double dist2 = getDist(u) + w;
                    if(dist2 < getDist(v)) {
                        setDistNextStage(v, dist2, u, Stage.OPEN);
                        pq.add(new PQElem(v, dist2));
                    }
                }
            }
        }
        /*
        for(int i=0; i<graph.size(); ++i)
            System.out.print(" " + getDist(i));
        */
        callback.pathUpdate();
        return pops;
    }
}
