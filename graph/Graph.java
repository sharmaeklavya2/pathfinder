package graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import static java.util.Collections.unmodifiableMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import graph.Edge;
import graph.AbstractGraph;

public class Graph extends AbstractGraph
{
    List<Map<Integer, Double>> adj;

    public int size() {
        return adj.size();
    }
    public Map<Integer, Double> getNbrs(int node) {
        return unmodifiableMap(adj.get(node));
    }
    public boolean adjacent(int src, int dst) {
        return (src < adj.size() && src >= 0 && adj.get(src).containsKey(dst));
    }
    public double getWeight(int src, int dst) {
        return adj.get(src).get(dst);
    }

    public Graph(int n, Edge[] edges, boolean symmetric) {
        this(n);

        for(Edge e: edges) {
            update(e);
            if(symmetric)
                update(e.reverse());
        }
    }
    public Graph(int n, Edge[] edges) {
        this(n, edges, true);
    }
    public Graph(int n) {
        adj = new ArrayList<Map<Integer, Double>>(n);
        for(int i=0; i<n; ++i)
            adj.add(new HashMap<Integer, Double>());
    }
    public String toString() {
        return "Graph(" + size() + ", " + adj + ")";
    }

    public void update(Edge e) {
        adj.get(e.getSrc()).put(e.getDst(), e.getW());
    }

    public static void main(String[] args) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());
        String s;
        String[] words;
        Graph graph = new Graph(n);
        while((s = br.readLine()) != null)
        {
            words = s.split(" ");
            int src = Integer.parseInt(words[0]);
            int dst = Integer.parseInt(words[1]);
            double w = Double.parseDouble(words[2]);
            Edge e = new Edge(src, dst, w);
            graph.update(e);
            graph.update(e.reverse());
        }
        System.out.println(graph.toString());
    }
}
