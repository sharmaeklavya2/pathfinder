package dstar;

import java.util.*;
import static java.lang.Math.min;

import graph.AbstractGraph;
import util.PQ;

class DStarLiteHelper {
    public static class Callback {
        public void nodeUpdate(int u) {}
    }

    private double g[];
    private double rhs[];
    private Callback callback;

    public Callback getCallback() {
        return callback;
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    protected DStarLiteHelper(int n, Callback callback) {
        g = new double[n];
        rhs = new double[n];
        this.callback = callback;
    }

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
}

public class DStarLite extends DStarLiteHelper {
    public static class NodeCostPair {
        public int node;
        public double cost;

        public NodeCostPair(int node, double cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    private int goal;
    private AbstractGraph graph;
    private PQ pq;

    public DStarLite(int goal, AbstractGraph graph, Callback callback) {
        super(graph.size(), callback);
        this.goal = goal;
        this.graph = graph;
        reset();
    }
    public DStarLite(int goal, AbstractGraph graph) {
        this(goal, graph, new Callback());
    }

    public double getGoal(int u) {return goal;}

    public void reset() {
        pq = new PQ();
        for(int i=0; i < graph.size(); ++i) {
            setGRhs(i, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        setRhs(goal, 0);
        pq.push(goal, getMinGRhs(goal));
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

    public void updateNode(int u) {
        if(u != goal) {
            NodeCostPair ncp = getBestSucc(u);
            setRhs(u, ncp.cost);
            //System.err.println("updateNode(" + u + "): (" + ncp.node + ", " + ncp.cost + ")");
            pq.push(u, getMinGRhs(u));
        }
    }

    public void examineUpdates(Set<Integer> l) {
        for(int u: l) {
            updateNode(u);
        }
    }

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
