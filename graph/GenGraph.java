package graph;

import java.util.*;
import static java.util.Collections.unmodifiableMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

import graph.Edge;
import graph.AbstractGraph;
import util.CmdUtil;

public class GenGraph extends AbstractGraph
{
    private List<HashMap<Integer, Double>> preds;
    private List<HashMap<Integer, Double>> succs;
    private int _size;

    public int size() {
        return _size;
    }
    synchronized public Map<Integer, Double> getPreds(int node) {
        return unmodifiableMap(preds.get(node));
    }
    synchronized public Map<Integer, Double> getSuccs(int node) {
        return unmodifiableMap(succs.get(node));
    }
    synchronized public Map<Integer, Double> getPredsCopy(int node) {
        return new HashMap<Integer, Double>(preds.get(node));
    }
    synchronized public Map<Integer, Double> getSuccsCopy(int node) {
        return new HashMap<Integer, Double>(succs.get(node));
    }
    synchronized public Set<Integer> getNbrs(int node) {
        Set<Integer> nbrs = new HashSet<Integer>(succs.get(node).keySet());
        nbrs.addAll(preds.get(node).keySet());
        return nbrs;
    }

    synchronized public boolean hasEdge(int src, int dst) {
        return (src < _size && src >= 0 && succs.get(src).containsKey(dst));
    }

    public static class NonAdjacentException extends RuntimeException {
        public NonAdjacentException(String s) {
            super(s);
        }
    }

    synchronized public double getWeight(int src, int dst) {
        try {
            return succs.get(src).get(dst);
        }
        catch(NullPointerException e) {
            throw new NonAdjacentException("getWeight(" + src + ", " + dst + ") failed");
        }
    }

    public GenGraph(int n, Edge[] edges, boolean symmetric) {
        this(n);

        for(Edge e: edges)
            update(e, symmetric);
    }
    public GenGraph(int n) {
        _size = n;
        preds = new ArrayList<HashMap<Integer, Double>>(n);
        succs = new ArrayList<HashMap<Integer, Double>>(n);
        for(int i=0; i<n; ++i) {
            preds.add(new HashMap<Integer, Double>());
            succs.add(new HashMap<Integer, Double>());
        }
    }
    public GenGraph(AbstractGraph graph2) {
        this(graph2.size());
        for(int u=0; u<_size; ++u) {
            for(Map.Entry<Integer, Double> entry: graph2.getSuccs(u).entrySet()) {
                int v = entry.getKey();
                double w = entry.getValue();
                succs.get(u).put(v, w);
                preds.get(v).put(u, w);
            }
        }
    }

    synchronized public String toString() {
        return "GenGraph(" + _size + ", " + succs + ")";
    }

    synchronized public void update(int u, int v, double w) {
        succs.get(u).put(v, w);
        preds.get(v).put(u, w);
    }
    synchronized public void update(int u, int v, double w, boolean symmetric) {
        update(u, v, w);
        if(symmetric)
            update(v, u, w);
    }
    synchronized public void update(Edge e) {
        update(e.getSrc(), e.getDst(), e.getW());
    }
    synchronized public void update(Edge e, boolean symmetric) {
        update(e.getSrc(), e.getDst(), e.getW(), symmetric);
    }
    synchronized public void breakEdge(int u, int v) {
        try {
            succs.get(u).remove(v);
            preds.get(v).remove(u);
        }
        catch(NullPointerException e) {}
    }

    public GenGraph(BufferedReader br, boolean symmetric) throws IOException
    {
        int n = Integer.parseInt(br.readLine());
        _size = n;
        preds = new ArrayList<HashMap<Integer, Double>>(n);
        succs = new ArrayList<HashMap<Integer, Double>>(n);
        for(int i=0; i<n; ++i) {
            preds.add(new HashMap<Integer, Double>());
            succs.add(new HashMap<Integer, Double>());
        }

        String s;
        String[] words;
        while((s = br.readLine()) != null) {
            if(!s.equals("")) {
                words = s.split(" ");
                int src = Integer.parseInt(words[0]);
                int dst = Integer.parseInt(words[1]);
                double w = Double.parseDouble(words[2]);
                update(src, dst, w, symmetric);
            }
        }
    }
    public GenGraph(BufferedReader br) throws IOException {
        this(br, true);
    }

    public static void main(String[] args) throws IOException
    {
        String usage = "usage: java graphs.GenGraph [file]";
        boolean symmetric = true;
        GenGraph graph = new GenGraph(CmdUtil.getBrFromArgs(args, usage, true));
        System.out.println(graph.toString());
    }
}
