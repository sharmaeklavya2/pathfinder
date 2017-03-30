package graph;

import java.util.Map;
import java.util.Set;

import graph.Graph;

public abstract class AbstractGraph
{
    public abstract int size();
    public abstract Map<Integer, Double> getPreds(int node);
    public abstract Map<Integer, Double> getSuccs(int node);
    public abstract Map<Integer, Double> getPredsCopy(int node);
    public abstract Map<Integer, Double> getSuccsCopy(int node);
    public abstract Set<Integer> getNbrs(int node);
    public abstract boolean hasEdge(int src, int dst);
    public abstract double getWeight(int src, int dst);
    public abstract String toString();

    synchronized public Graph toGraph() {
        return new Graph(this);
    }
}
