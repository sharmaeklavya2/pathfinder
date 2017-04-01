package planner;

import java.util.*;
import java.awt.Color;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import util.PQ;

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

abstract class DStarLitePlannerHelper extends AbstractPlanner
{
    private double g[];
    private double rhs[];

    public double getG(int u) {return g[u];}
    public double getRhs(int u) {return rhs[u];}
    public double getMinGRhs(int u) {
        return min(g[u], rhs[u]);
    }

    protected void setG(int u, double g) {
        this.g[u] = g;
        callback.nodeUpdate(u);
    }
    protected void setRhs(int u, double rhs) {
        this.rhs[u] = rhs;
        callback.nodeUpdate(u);
    }
    protected void setGRhs(int u, double g, double rhs) {
        this.g[u] = g;
        this.rhs[u] = rhs;
        callback.nodeUpdate(u);
    }

    protected DStarLitePlannerHelper(int n)
    {
        super();
        g = new double[n];
        rhs = new double[n];
    }
}

public class DStarLitePlanner extends DStarLitePlannerHelper
{
    protected static class NodeCostPair {
        public int node;
        public double cost;

        public NodeCostPair(int node, double cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    private PQ pq;
    private Robot robot;
    private AbstractGraph graph;

    public Robot getRobot() {
        return robot;
    }

    public DStarLitePlanner(int goal, Robot robot, AbstractPlanner.Callback callback) {
        super(robot.getGraph().size());
        this.goal = goal;
        this.callback = callback;
        resetRobot(robot);
    }
    public DStarLitePlanner(int goal, Robot robot) {
        this(goal, robot, new AbstractPlanner.Callback());
    }

    public void resetRobot(Robot robot) {
    /* Reset Planner */
        this.robot = robot;
        this.graph = robot.getGraph();
        reset();
    }

    public void reset() {
        pq = new PQ();
        for(int i=0; i < graph.size(); ++i) {
            setGRhs(i, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        setRhs(goal, 0);
        pq.push(goal, getMinGRhs(goal));
        replan();
    }

    public NodeCostPair getBestSucc(int u) {
        int best = -1;
        double minCost = Double.POSITIVE_INFINITY;
        for(Map.Entry<Integer, Double> entry: graph.getSuccs(u).entrySet()) {
            int v = entry.getKey();
            double cost = entry.getValue() + getG(v);
            if(cost < minCost) {
                minCost = cost;
                best = v;
            }
        }
        return new NodeCostPair(best, minCost);
    }

    public int getNext(int u) {
        if(Double.isInfinite(getG(u))) {
            return -1;
        }
        return getBestSucc(u).node;
    }

    protected void updateNode(int u) {
        if(u != goal) {
            NodeCostPair ncp = getBestSucc(u);
            setRhs(u, ncp.cost);
            //System.err.println("updateNode(" + u + "): (" + ncp.node + ", " + ncp.cost + ")");
            pq.push(u, getMinGRhs(u));
        }
    }

    protected void examineUpdates(Set<Integer> l) {
        for(int u: l) {
            updateNode(u);
        }
    }

    public long replan()
    {
        System.err.println("Replanning");
        long pops;
        for(pops=0; !pq.isEmpty(); ++pops) {
            PQ.PQElem entry = pq.top();
            int u = entry.getValue();
            double prio = entry.getPriority();
            double gu = getG(u), rhsu = getRhs(u);
            //System.err.println("u = " + u + ", prio = " + prio + ", g(u) = " + gu + ", rhs(u) = " + rhsu);
            if(gu == rhsu) {
                pq.pop();
                continue;
            }
            int curr = robot.getPosition();
            if(prio >= getMinGRhs(curr) && !(getRhs(curr) != getG(curr))) {
                // !(u != v) is different from u == v when both u and v are infinity
                //System.err.println("breaking out: u = " + u + ", prio = " + prio + ", g(u) = " + gu + ", rhs(u) = " + rhsu);
                break;
            }
            pq.pop();
            if(gu > rhsu) {
                setG(u, rhsu);
            }
            else {
                setG(u, Double.POSITIVE_INFINITY);
                updateNode(u);
            }
            for(Map.Entry<Integer, Double> entry2: graph.getPreds(u).entrySet()) {
                int v = entry2.getKey();
                updateNode(v);
            }
        }
        /*
        for(int i=0; i<graph.size(); ++i) {
            System.err.println("" + i + " " + getG(i) + " " + getRhs(i));
        }
        */
        callback.pathUpdate();
        return pops;
    }

    public GridPanelCell getGridPanelCell(int u, boolean onPath) {
        int v = getNext(u);
        int type = 0;
        int arrowX = 0, arrowY = 0;
        double diff = getG(u) - getRhs(u);
        boolean isOpen = !(Double.isInfinite(getG(u)) && Double.isInfinite(getRhs(u))) && abs(diff) >= PQ.PQElem.EPS;

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
        gpc.setToolTip("g: " + getG(u) + "\nrhs: " + getRhs(u));
        return gpc;
    }
}
