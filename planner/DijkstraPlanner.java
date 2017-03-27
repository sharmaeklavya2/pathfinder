package planner;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.lang.IllegalArgumentException;
import java.util.PriorityQueue;
import static java.lang.Math.abs;
import java.awt.Color;

import gridpanel.GridPanelCell;
import graph.AbstractGraph;
import graph.Graph;
import graph.GridGraph;
import graph.Edge;
import planner.AbstractAdjacentPlanner;
import planner.Stage;

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

abstract class DijkstraPlannerHelper extends AbstractAdjacentPlanner
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
        doCallback(u);
    }
    protected void setNext(int u, int next) {
        this.next[u] = next;
        doCallback(u);
    }
    protected void setStage(int u, Stage stage) {
        this.stage[u] = stage;
        doCallback(u);
    }
    protected void setDistNextStage(int u, double dist, int next, Stage stage) {
        this.dist[u] = dist;
        this.next[u] = next;
        this.stage[u] = stage;
        doCallback(u);
    }

    protected DijkstraPlannerHelper(int curr, int goal, AbstractGraph graph, NodeUpdateCallback callback)
    {
        this.goal = goal;
        this.graphRemote = graph;
        stage = new Stage[graph.size()];
        dist = new double[graph.size()];
        next = new int[graph.size()];
        this.callback = callback;
        reset(curr);
    }
}

public class DijkstraPlanner extends DijkstraPlannerHelper
{
    private PriorityQueue<PQElem> pq;

    public DijkstraPlanner(int curr, int goal, AbstractGraph graph, NodeUpdateCallback callback) {
        super(curr, goal, graph, callback);
    }
    public DijkstraPlanner(int curr, int goal, AbstractGraph graph) {
        super(curr, goal, graph, null);
    }

    public void reset(int curr)
    /* Reset Planner and set current position */
    {
        if(curr < 0 || curr >= graphRemote.size())
            throw new IllegalArgumentException("Parameter curr has invalid value " + curr);
        this.distance = 0;
        this.graphLocal = graphRemote.toGraph();
        this.curr = curr;
        replan();
    }

    public GridPanelCell getGridPanelCell(int u) {
        int v = getNext(u);
        int type = 0;
        Stage stage = getStage(u);
        int arrowX = 0, arrowY = 0;

        if(graphRemote instanceof GridGraph) {
            GridGraph graph = (GridGraph)graphRemote;
            type = graph.getNode(u).getType();
            if(v != -1) {
                arrowX = graph.diffJ(u, v);
                arrowY = graph.diffI(u, v);
            }
            else {
                arrowX = arrowY = 0;
            }
        }

        //List<int> path = getPath(getCurr());
        Color color;
        if(type == 1) {
            if(stage == Stage.OPEN)
                color = Color.RED.darker();
            else
                color = Color.BLACK;
        }
        else {
            if(u == curr || u == goal) {
                if(u == curr && u == goal) {
                    color = Color.GREEN.darker();
                }
                else {
                    color = Color.GREEN;
                }
            }
            else if(stage == Stage.NEW) {
                color = Color.LIGHT_GRAY;
            }
            else if(stage == Stage.OPEN) {
                color = new Color(255, 63, 63);
            }
            else {
                color = Color.WHITE;
            }
        }
        return new GridPanelCell(color, arrowX, arrowY);
    }

    public boolean checkAndUpdate()
    // return true if graph was updated
    // this can only handle edge weight change and edge removal
    // it cannot handle edge addition
    {
        boolean changed = false;
        boolean debug = false;
        Map<Integer, Double> nbrs = graphLocal.getNbrsCopy(curr);
        //System.out.println("nbrs of " + curr + ": " + nbrs);
        for(Map.Entry<Integer, Double> entry: nbrs.entrySet()) {
            int v = entry.getKey();
            double w = entry.getValue();
            if(!graphRemote.adjacent(curr, v)) {
                graphLocal.breakEdge(curr, v);
                changed = true;
                if(debug)
                    System.err.println("1: Edge (" + curr + ", " + v + ") was removed");
            }
            else {
                double wreal = graphRemote.getWeight(curr, v);
                if(abs(wreal - w) >= PQElem.EPS) {
                    graphLocal.update(new Edge(curr, v, wreal));
                    changed = true;
                    if(debug)
                        System.err.println("1: Weight of (" + curr + ", " + v + ")" +
                            " changed from "+ w + " to " + wreal);
                }
            }

            if(graphLocal.adjacent(v, curr)) {
                if(!graphRemote.adjacent(v, curr)) {
                    graphLocal.breakEdge(v, curr);
                    changed = true;
                    if(debug)
                        System.err.println("2: Edge (" + v + ", " + curr + ") was removed");
                }
                else {
                    double wreal = graphRemote.getWeight(v, curr);
                    if(abs(wreal - w) >= PQElem.EPS) {
                        graphLocal.update(new Edge(v, curr, wreal));
                        changed = true;
                        if(debug)
                            System.err.println("2: Weight of (" + v + ", " + curr + ")" +
                                " changed from "+ w + " to " + wreal);
                    }
                }
            }
        }
        return changed;
    }

    public long replan()
    {
        System.err.println("Replanning");
        for(int i=0; i < graphLocal.size(); ++i) {
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
            if(u == curr)
                break;

            for(Map.Entry<Integer, Double> entry: graphLocal.getNbrs(u).entrySet()) {
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
        for(int i=0; i<graphLocal.size(); ++i)
            System.out.print(" " + getDist(i));
        */
        return pops;
    }
}
