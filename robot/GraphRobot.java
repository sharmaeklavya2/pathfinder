package robot;

import java.util.*;

import graph.AbstractGraph;
import graph.GenGraph;
import graph.Edge;
import static java.lang.Math.abs;

/** A class which implements {@link Robot} using {@link GenGraph} to store the local copy of the graph. */
public class GraphRobot implements Robot{
    protected GenGraph graphLocal;
    protected AbstractGraph graphRemote;
    protected int position;

    public int getPosition() {
        return position;
    }

    public int moveTo(int position) {
        this.position = position;
        return this.position;
    }

    public AbstractGraph getGraph() {
        return graphLocal;
    }

    public GraphRobot(AbstractGraph graph, int position) {
        this.graphRemote = graph;
        this.position = position;
        this.graphLocal = graph.toGenGraph();
    }

    public static class EdgeStatus {
        public int u, v;
        public Double wLocal, wRemote;

        public EdgeStatus(int u, int v, Double wLocal, Double wRemote) {
            this.u = u; this.v = v;
            this.wLocal = wLocal; this.wRemote = wRemote;
        }
        public String toString() {
            return "(" + u + ", " + v + ", " + wLocal + ", " + wRemote + ")";
        }
    }

    protected Set<Integer> getNearbyNodes(int radius) {
        Queue<Integer> queue = new ArrayDeque<Integer>();
        queue.add(position);
        Map<Integer, Integer> distMap = new HashMap<Integer, Integer>();
        distMap.put(position, 0);
        Set<Integer> nodes = new HashSet<Integer>();

        while(!queue.isEmpty()) {
            int u = queue.remove();
            if(!nodes.contains(u)) {
                int du = distMap.get(u);
                nodes.add(u);
                Set<Integer> nbrs = graphRemote.getNbrs(u);
                for(int v: nbrs) {
                    int dv = du + 1;
                    if(distMap.containsKey(v)) {
                        int dv2 = distMap.get(v);
                        if(dv < dv2) {
                            distMap.put(v, dv);
                        }
                        else {
                            dv = dv2;
                        }
                    }
                    else {
                        distMap.put(v, dv);
                    }
                    if(dv <= radius) {
                        queue.add(v);
                    }
                }
                // Also add nodes which would have been included if they weren't blocked
                // but their neighbors shouldn't be blocked.
                // This enables us to block paths from blocked nodes
                Set<Integer> lnbrs = graphLocal.getNbrs(u);
                for(int v: lnbrs) {
                    if(!nbrs.contains(v)) {
                        nodes.add(v);
                    }
                }
            }
        }
        return nodes;
    }

    protected static class Edge2 implements Comparable<Edge2> {
        public int u, v;

        public Edge2(int u, int v) {
            this.u = u; this.v = v;
        }
        @Override
        public int compareTo(Edge2 e) {
            if(u < e.u) {return -1;}
            else if(u > e.u) {return 1;}
            else if(v < e.v) {return -1;}
            else if(v > e.v) {return 1;}
            else return 0;
        }
        @Override
        public String toString() {
            return "(" + u + ", " + v + ")";
        }
    }

    public List<EdgeStatus> getUpdatedEdges(int radius) {
        synchronized(graphRemote) {
            Iterable<Integer> nearbyNodes = getNearbyNodes(radius);
            //System.err.println("nearbyNodes: " + nearbyNodes);
            TreeSet<Edge2> ledgeSet = new TreeSet<Edge2>();
            TreeSet<Edge2> redgeSet = new TreeSet<Edge2>();
            for(int u: nearbyNodes) {
                Set<Integer> lnbrs = graphLocal.getNbrs(u);
                for(int v: lnbrs) {
                    ledgeSet.add(new Edge2(u, v));
                    ledgeSet.add(new Edge2(v, u));
                }
                Set<Integer> rnbrs = graphRemote.getNbrs(u);
                for(int v: rnbrs) {
                    redgeSet.add(new Edge2(u, v));
                    redgeSet.add(new Edge2(v, u));
                }
            }
            ArrayList<Edge2> ledges = new ArrayList<Edge2>(ledgeSet);
            ArrayList<Edge2> redges = new ArrayList<Edge2>(redgeSet);
            //System.err.println(ledges);
            //System.err.println(redges);

            int i=0, j=0;

            List<EdgeStatus> output = new ArrayList<EdgeStatus>();
            while(i < ledges.size() && j < redges.size()) {
                Edge2 e1 = ledges.get(i);
                Edge2 e2 = redges.get(j);
                int compResult = e1.compareTo(e2);
                if(compResult < 0) {
                    // edge e1 has been removed from remote graph
                    output.add(new EdgeStatus(e1.u, e1.v, graphLocal.getWeight(e1.u, e1.v), null));
                    graphLocal.breakEdge(e1.u, e1.v);
                    i++;
                }
                else if(compResult > 0) {
                    // edge e2 has been added in remote graph
                    double wr = graphRemote.getWeight(e2.u, e2.v);
                    output.add(new EdgeStatus(e2.u, e2.v, null, wr));
                    graphLocal.update(e2.u, e2.v, wr, false);
                    j++;
                }
                else {
                    double wl = graphLocal.getWeight(e1.u, e1.v);
                    double wr = graphRemote.getWeight(e2.u, e2.v);
                    if(abs(wl - wr) >= Edge.EPS) {
                        output.add(new EdgeStatus(e1.u, e1.v, wl, wr));
                        graphLocal.update(e2.u, e2.v, wr, false);
                    }
                    i++; j++;
                }
            }
            while(i < ledges.size()) {
                Edge2 e1 = ledges.get(i);
                output.add(new EdgeStatus(e1.u, e1.v, graphLocal.getWeight(e1.u, e1.v), null));
                graphLocal.breakEdge(e1.u, e1.v);
                i++;
            }
            while(j < redges.size()) {
                Edge2 e2 = redges.get(j);
                double wr = graphRemote.getWeight(e2.u, e2.v);
                output.add(new EdgeStatus(e2.u, e2.v, null, wr));
                graphLocal.update(e2.u, e2.v, wr, false);
                j++;
            }
            //System.err.println(output);
            return output;
        }
    }

    public Set<Integer> getUpdatedNodes(int radius) {
        List<EdgeStatus> esl = getUpdatedEdges(radius);
        Set<Integer> output = new HashSet<Integer>();
        for(EdgeStatus es: esl) {
            output.add(es.u);
            output.add(es.v);
        }
        return output;
    }
}
