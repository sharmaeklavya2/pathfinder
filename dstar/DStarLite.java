package dstar;

import java.util.*;
import static java.lang.Math.min;

import graph.AbstractGraph;
import util.PQ;

class DStarLiteHelper {
    /** Callback object which is called when a node's state changes. */
    public static class Callback {
        /** Callback method called to signal that a node {@code u}'s state has changed. */
        public void nodeUpdate(int u) {}
        /** Callback method called to signal that the graph's state has changed.
            This method is generally called when bulk updates happen to a graph. */
        public void fullUpdate() {}
    }

    private double g[];
    private double rhs[];
    private int size;
    private Callback callback;

    public Callback getCallback() {
        return callback;
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /** Construct a DStarLiteHelper instance which works on a graph of size {@code n}. */
    protected DStarLiteHelper(int n, Callback callback) {
        size = n;
        g = new double[n];
        rhs = new double[n];
        this.callback = callback;
    }

    /** Get {@code g(u)} for a node {@code u}. */
    public double getG(int u) {return g[u];}
    /** Get {@code rhs(u)} for a node {@code u}. */
    public double getRhs(int u) {return rhs[u];}
    /** Get {@code min(g(u), rhs(u))} for a node {@code u}. */
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
    /** Set {@code g(u)} and {@code rhs(u)} for a node {@code u}. */
    protected void setGRhs(int u, double g, double rhs) {
        this.g[u] = g;
        this.rhs[u] = rhs;
        callback.nodeUpdate(u);
    }
    /** Set {@code g(u)} and {@code rhs(u)} for every node {@code u}. */
    protected void setAllGRhs(double g, double rhs) {
        for(int i=0; i<size; ++i) {
            this.g[i] = g;
            this.rhs[i] = rhs;
        }
        callback.fullUpdate();
    }
}

/** Calculates and stores info about each node in a graph on which D*-lite algorithm is to be applied. */
public class DStarLite extends DStarLiteHelper {
    public static class NodeCostPair {
        public int node;
        public double cost;

        public NodeCostPair(int node, double cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    /** Goal node for path planning. */
    protected int goal;
    /** Local copy of graph used for path planning. */
    protected AbstractGraph graph;
    /** Priority queue of nodes. */
    protected PQ pq;

    /**
        @param goal {@link #goal}
        @param graph {@link #graph}
        @param callback {@link DStarLiteHelper.Callback} instance which will be called whenever a node's state changes.
    */
    public DStarLite(int goal, AbstractGraph graph, Callback callback) {
        super(graph.size(), callback);
        this.goal = goal;
        this.graph = graph;
        reset();
    }
    /** Same as {@linkplain #DStarLite the other constructor} but without a callback. */
    public DStarLite(int goal, AbstractGraph graph) {
        this(goal, graph, new Callback());
    }

    public int getGoal(int u) {return goal;}

    /** Reset things to the way they were right after the constructor was called. */
    public void reset() {
        pq = new PQ();
        setAllGRhs(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        setRhs(goal, 0);
        pq.push(goal, getMinGRhs(goal));
    }

    /** Get the best successor node and its associated cost.

        The best successor of a node u is a {@linkplain graph.AbstractGraph#getSuccs successor} v which has
        the least value of weight(u, v) + g(v). This least value is the associated cost.

        If no successor exists, return (-1, {@linkplain Double#POSITIVE_INFINITY infinity}). */
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

    /** The best successor of a node (-1 if no successor exists). */
    public int getNext(int u) {
        if(Double.isInfinite(getG(u))) {
            return -1;
        }
        return getBestSucc(u).node;
    }

    /** Update {@code rhs} of a node and push it on the priority queue. */
    public void updateNode(int u) {
        if(u != goal) {
            NodeCostPair ncp = getBestSucc(u);
            setRhs(u, ncp.cost);
            //System.err.println("updateNode(" + u + "): (" + ncp.node + ", " + ncp.cost + ")");
            pq.push(u, getMinGRhs(u));
        }
    }

    /** Update all nodes in the set. */
    public void examineUpdates(Set<Integer> l) {
        for(int u: l) {
            updateNode(u);
        }
    }

    /** Do one step of replanning assuming robot's current position is {@code curr}. */
    public boolean replanIter(int curr)
    {
        if(!pq.isEmpty()) {
            PQ.PQElem entry = pq.top();
            int u = entry.getValue();
            double prio = entry.getPriority();
            double gu = getG(u), rhsu = getRhs(u);
            //System.err.println("u = " + u + ", prio = " + prio + ", g(u) = " + gu + ", rhs(u) = " + rhsu);
            if(gu == rhsu) {
                pq.pop();
                return true;
            }
            if(prio >= getMinGRhs(curr) && !(getRhs(curr) != getG(curr))) {
                // !(u != v) is different from u == v when both u and v are infinity
                //System.err.println("breaking out: u = " + u + ", prio = " + prio + ", g(u) = " + gu + ", rhs(u) = " + rhsu);
                return false;
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
            return true;
        }
        else {
            return false;
        }
    }
}
