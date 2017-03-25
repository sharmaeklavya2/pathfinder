package graph;

import java.util.Map;

import graph.Graph;

public abstract class AbstractGraph
{
    public abstract int size();
    public abstract Map<Integer, Double> getNbrs(int node);
    public abstract Map<Integer, Double> getNbrsCopy(int node);
    public abstract boolean adjacent(int src, int dst);
    public abstract double getWeight(int src, int dst);
    public abstract String toString();

    synchronized public Graph toGraph() {
        return new Graph(this);
    }
}
