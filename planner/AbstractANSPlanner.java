package planner;

import java.util.*;
import static java.lang.Math.abs;

import graph.Edge;
import gridpanel.GridPanelCell;
import planner.AbstractPlanner;

public abstract class AbstractANSPlanner extends AbstractPlanner
// An abstract planner where planned path is an Adjacent Node Sequence (ANS),
{
    public abstract int getNext(int u);

    public GridPanelCell getGridPanelCell(int u) {
        return this.getGridPanelCell(u, false);
    }
    public abstract GridPanelCell getGridPanelCell(int u, boolean onPath);

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

    public List<EdgeStatus> getUpdatedEdges(int radius)
    {
        synchronized(graphRemote)
        {
            Iterable<Integer> nearbyNodes = getNearbyNodes(curr, radius);
            //System.err.println("nearbyNodes: " + nearbyNodes);
            TreeSet<Edge2> ledgeSet = new TreeSet<Edge2>();
            TreeSet<Edge2> redgeSet = new TreeSet<Edge2>();
            for(int u: nearbyNodes) {
                Set<Integer> lsuccs = graphLocal.getSuccs(u).keySet();
                for(int v: lsuccs) {
                    ledgeSet.add(new Edge2(u, v));
                    ledgeSet.add(new Edge2(v, u));
                }
                Set<Integer> rsuccs = graphRemote.getSuccs(u).keySet();
                for(int v: rsuccs) {
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