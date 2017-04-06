package graph;

import java.util.Map;
import java.util.Set;

import graph.GenGraph;

/** Abstract class for representing graphs.

    Nodes are numbered from 0 to n-1 where n is the size of the graph.
*/
public abstract class AbstractGraph
{
    /** Number of nodes in the graph. */
    public abstract int size();

    /** Get a map which contains predecessors of {@code node} along with weights of
        edges joining them to {@code node}. */
    public abstract Map<Integer, Double> getPreds(int node);

    /** Get a map which contains successors of {@code node} along with weights of
        edges joining them to {@code node}. */
    public abstract Map<Integer, Double> getSuccs(int node);

    /** Get a map which contains predecessors of {@code node} along with weights of
        edges joining them to {@code node}. This map is a copy and can be modified. */
    public abstract Map<Integer, Double> getPredsCopy(int node);

    /** Get a map which contains successors of {@code node} along with weights of
        edges joining them to {@code node}. This map is a copy and can be modified. */
    public abstract Map<Integer, Double> getSuccsCopy(int node);

    /** Get neighbors (both predecessors and successors) of {@code node}. */
    public abstract Set<Integer> getNbrs(int node);

    /** Return whether there is an edge from {@code src} to {@code dst}. */
    public abstract boolean hasEdge(int src, int dst);

    /** Return weight of edge from {@code src} to {@code dst}.

        If there is no edge between {@code src} and {@code dst}, either throw
        a {@link RuntimeException} or return {@linkplain Double#POSITIVE_INFINITY infinity}. */
    public abstract double getWeight(int src, int dst);

    public abstract String toString();

    /** Return a {@link GenGraph} instance. It is possible to convert every {@link AbstractGraph}
        instance to a {@link GenGraph} instance. */
    synchronized public GenGraph toGenGraph() {
        return new GenGraph(this);
    }
}
