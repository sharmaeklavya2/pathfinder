package planner;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.lang.IllegalArgumentException;
import java.util.PriorityQueue;
import static java.lang.Math.abs;

import graph.AbstractGraph;
import graph.Graph;
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

public class DijkstraPlanner extends AbstractAdjacentPlanner
{
    private Stage stage[];
    private double dist[];
    private int next[];

    private PriorityQueue<PQElem> pq;

    public double getDist(int u) {return dist[u];}
    public int getNext(int u) {return next[u];}
    public Stage getStage(int u) {return stage[u];}

    public DijkstraPlanner(int curr, int goal, AbstractGraph graph)
    {
        this.goal = goal;
        this.graphRemote = graph;
        stage = new Stage[graph.size()];
        dist = new double[graph.size()];
        next = new int[graph.size()];
        reset(curr);
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
        for(int i=0; i < graphLocal.size(); ++i)
        {
            stage[i] = Stage.NEW;
            dist[i] = Double.POSITIVE_INFINITY;
            next[i] = -1;
        }
        dist[goal] = 0;
        stage[goal] = Stage.OPEN;
        next[goal] = goal;
        pq = new PriorityQueue<PQElem>();
        pq.add(new PQElem(goal, dist[goal]));

        long pops;
        for(pops = 0; !pq.isEmpty(); ++pops)
        {
            PQElem head = pq.poll();
            int u = head.getValue();
            double prio = head.getPriority();
            if(stage[u] == Stage.CLOSED)
                continue;
            else if(stage[u] != Stage.OPEN)
                throw new RuntimeException("Stage " + stage[u] + " node found in priority queue");
            else if(abs(prio - dist[u]) >= PQElem.EPS)
                throw new RuntimeException("Mismatch between priority queue ("
                    + prio + ") and dist[" + u + "] (" + dist[u] + ")");
            stage[u] = Stage.CLOSED;
            if(u == curr)
                break;

            for(Map.Entry<Integer, Double> entry: graphLocal.getNbrs(u).entrySet()) {
                int v = entry.getKey();
                double w = entry.getValue();
                if(stage[v] == Stage.NEW) {
                    stage[v] = Stage.OPEN;
                    dist[v] = dist[u] + w;
                    next[v] = u;
                    pq.add(new PQElem(v, dist[v]));
                }
                else if(stage[v] == Stage.OPEN) {
                    double dist2 = dist[u] + w;
                    if(dist2 < dist[v])
                    {
                        dist[v] = dist2;
                        next[v] = u;
                        pq.add(new PQElem(v, dist[v]));
                    }
                }
            }
        }
        /*
        for(int i=0; i<graphLocal.size(); ++i)
            System.out.print(" " + dist[i]);
        */
        return pops;
    }
}
