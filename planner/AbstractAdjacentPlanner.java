package planner;

import java.util.*;
import static java.lang.Math.abs;

import graph.Edge;
import gridpanel.GridPanelCell;
import planner.AbstractPlanner;
import planner.Stage;

public abstract class AbstractAdjacentPlanner extends AbstractPlanner
// An abstract planner where a robot only moves to adjacent nodes
{
    public abstract int getNext(int u);

    public abstract GridPanelCell getGridPanelCell(int u);

    public List<Integer> getPath(int u)
    {
        List<Integer> l = new ArrayList<Integer>();
        if(getNext(u) == -1)
            return l;
        while(u != goal) {
            l.add(u);
            u = getNext(u);
        }
        l.add(goal);
        return l;
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

    public void examineUpdates(List<EdgeStatus> edgeUpdates) {}

    public long move(int radius) {
        if(curr == goal)
            throw new RuntimeException("Already at goal");

        List<EdgeStatus> updatedEdges = getUpdatedEdges(radius);
        boolean changed = (updatedEdges.size() > 0);
        examineUpdates(updatedEdges);

        long time_taken = 0;
        if(changed)
            time_taken = replan();
        if(getNext(curr) == -1)
            throw new RuntimeException("No path to destination");

        int prev = curr;
        curr = getNext(curr);
        distance += graphLocal.getWeight(prev, curr);
        doCallback(prev);
        doCallback(curr);
        return time_taken;
    }
    public long move() {return this.move(0);}

    public Set<Integer> getNearbyNodes(int center, int radius) {
        Queue<Integer> queue = new ArrayDeque<Integer>();
        queue.add(center);
        Map<Integer, Integer> distMap = new HashMap<Integer, Integer>();
        distMap.put(center, 0);
        Set<Integer> nodes = new HashSet<Integer>();

        while(!queue.isEmpty()) {
            int u = queue.remove();
            if(!nodes.contains(u)) {
                int du = distMap.get(u);
                nodes.add(u);
                Map<Integer, Double> nbrs = graphLocal.getNbrs(curr);
                for(Map.Entry<Integer, Double> entry: nbrs.entrySet()) {
                    int v = entry.getKey();
                    int dv = u + 1;
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

    public List<EdgeStatus> getUpdatedEdges(int radius)
    {
        synchronized(graphRemote)
        {
            Iterable<Integer> nearbyNodes = getNearbyNodes(curr, radius);
            //System.err.println("nearbyNodes: " + nearbyNodes);
            TreeSet<Edge2> ledgeSet = new TreeSet<Edge2>();
            TreeSet<Edge2> redgeSet = new TreeSet<Edge2>();
            for(int u: nearbyNodes) {
                Set<Integer> lnbrs = graphLocal.getNbrs(u).keySet();
                for(int v: lnbrs) {
                    ledgeSet.add(new Edge2(u, v));
                    ledgeSet.add(new Edge2(v, u));
                }
                Set<Integer> rnbrs = graphRemote.getNbrs(u).keySet();
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
                    graphLocal.update(new Edge(e2.u, e2.v, wr), false);
                    j++;
                }
                else {
                    double wl = graphLocal.getWeight(e1.u, e1.v);
                    double wr = graphRemote.getWeight(e2.u, e2.v);
                    if(abs(wl - wr) >= Edge.EPS) {
                        output.add(new EdgeStatus(e1.u, e1.v, wl, wr));
                        graphLocal.update(new Edge(e2.u, e2.v, wr), false);
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
                graphLocal.update(new Edge(e2.u, e2.v, wr), false);
                j++;
            }
            //System.err.println(output);
            return output;
        }
    }
}
