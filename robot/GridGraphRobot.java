package robot;

import java.util.*;

import graph.AbstractGraph;
import graph.GridGraph;
import graph.Edge;
import static java.lang.Math.abs;

/** A class which implements {@link GridRobot} using {@link GridGraph} to store the local copy of the graph. */
public class GridGraphRobot implements GridRobot {
    protected GridGraph graphLocal, graphRemote;
    protected int position;

    public int getPosition() {
        return position;
    }

    public int moveTo(int position) {
        this.position = position;
        return this.position;
    }

    public GridGraph getGraph() {
        return graphLocal;
    }

    public GridGraphRobot(GridGraph graph, int position) {
        this.position = position;
        this.graphRemote = graph;
        this.graphLocal = new GridGraph(graph);
    }

    public List<Integer> getNearbyNodes(int u, int radius) {
        int rows = graphLocal.getRows();
        int cols = graphLocal.getCols();
        int ui = u / cols, uj = u % cols;
        List<Integer> output = new ArrayList<Integer>();
        for(int vi=ui-radius; vi <= ui+radius; ++vi) {
            if(vi >= 0 && vi < rows) {
                for(int vj=uj-radius; vj <= uj+radius; ++vj) {
                    if(vj >= 0 && vj < cols) {
                        int v = vi * cols + vj;
                        output.add(v);
                    }
                }
            }
        }
        return output;
    }

    public Set<Integer> getUpdatedNodes(int radius) {
        Set<Integer> output = new HashSet<Integer>();
        int u = getPosition();
        for(int v: getNearbyNodes(u, radius)) {
            GridGraph.Node lnode = graphLocal.getNode(v);
            GridGraph.Node rnode = graphRemote.getNode(v);
            if(!lnode.isSame(rnode)) {
                output.addAll(getNearbyNodes(v, 1));
                graphLocal.update(v, rnode);
            }
        }
        return output;
    }
}
