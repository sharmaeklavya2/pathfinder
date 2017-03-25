package graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import static java.util.Collections.unmodifiableMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

import graph.Edge;
import graph.AbstractGraph;

public class Graph extends AbstractGraph
{
    private List<HashMap<Integer, Double>> adj;
    private int _size;

    public int size() {
        return _size;
    }
    synchronized public Map<Integer, Double> getNbrs(int node) {
        return unmodifiableMap(adj.get(node));
    }
    synchronized public HashMap<Integer, Double> getNbrsCopy(int node) {
        return new HashMap<Integer, Double>(adj.get(node));
    }
    synchronized public boolean adjacent(int src, int dst) {
        return (src < _size && src >= 0 && adj.get(src).containsKey(dst));
    }
    synchronized public double getWeight(int src, int dst) {
        return adj.get(src).get(dst);
    }

    public Graph(int n, Edge[] edges, boolean symmetric) {
        this(n);

        for(Edge e: edges)
            update(e, symmetric);
    }
    public Graph(int n) {
        _size = n;
        adj = new ArrayList<HashMap<Integer, Double>>(n);
        for(int i=0; i<n; ++i)
            adj.add(new HashMap<Integer, Double>());
    }
    public Graph(AbstractGraph graph2) {
        this(graph2.size());
        for(int u=0; u<_size; ++u) {
            for(Map.Entry<Integer, Double> entry: graph2.getNbrs(u).entrySet()) {
                int v = entry.getKey();
                double w = entry.getValue();
                adj.get(u).put(v, w);
            }
        }
    }

    synchronized public String toString() {
        return "Graph(" + _size + ", " + adj + ")";
    }

    synchronized public void update(Edge e) {
        adj.get(e.getSrc()).put(e.getDst(), e.getW());
    }
    synchronized public void update(Edge e, boolean symmetric) {
        update(e);
        if(symmetric)
            update(e.reverse());
    }
    synchronized public void breakEdge(int u, int v) {
        adj.get(u).remove(v);
    }

    public static Graph fromBufferedReader(BufferedReader br, boolean symmetric) throws IOException
    {
        int n = Integer.parseInt(br.readLine());
        String s;
        String[] words;
        Graph graph = new Graph(n);
        while((s = br.readLine()) != null) {
            if(!s.equals("")) {
                words = s.split(" ");
                int src = Integer.parseInt(words[0]);
                int dst = Integer.parseInt(words[1]);
                double w = Double.parseDouble(words[2]);
                graph.update(new Edge(src, dst, w), symmetric);
            }
        }
        return graph;
    }

    public static Graph fromFile(String path, boolean symmetric) throws IOException
    {return fromBufferedReader(new BufferedReader(new FileReader(path)), symmetric);}

    public static Graph fromStdin(boolean symmetric) throws IOException
    {return fromBufferedReader(new BufferedReader(new InputStreamReader(System.in)), symmetric);}

    public static void main(String[] args) throws IOException
    {
        String usage = "usage: java graphs.Graph [file]";
        boolean symmetric = true;
        Graph graph;
        if(args.length == 0) {
            graph = fromStdin(symmetric);
            System.out.println(graph.toString());
        }
        else if(args.length == 1) {
            if(args[0].equals("-h") || args[0].equals("--help"))
                System.out.println(usage);
            else {
                graph = fromFile(args[0], symmetric);
                System.out.println(graph.toString());
            }
        }
        else {
            System.err.println(usage);
            System.exit(1);
        }
    }
}
