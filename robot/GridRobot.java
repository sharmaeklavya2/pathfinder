package robot;

import java.util.*;

import graph.AbstractGraph;
import graph.GridGraph;
import graph.Edge;
import static java.lang.Math.abs;

public class GridRobot implements Robot{
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

    public GridRobot(GridGraph graph, int position) {
        this.position = position;
        this.graphRemote = graph;
        this.graphLocal = new GridGraph(graph);
    }

    public Set<Integer> getUpdatedNodes(int radius) {
        int u = getPosition();
        int rows = graphLocal.getRows();
        int cols = graphLocal.getCols();
        int ui = u / cols, uj = u % cols;
        Set<Integer> output = new HashSet<Integer>();
        for(int vi=ui-radius; vi <= ui+radius; ++vi) {
            if(vi >= 0 && vi < rows) {
                for(int vj=uj-radius; vj <= uj+radius; ++vj) {
                    if(vj >= 0 && vj < cols) {
                        int v = vi * cols + vj;
                        GridGraph.Node lnode = graphLocal.getNode(v);
                        GridGraph.Node rnode = graphRemote.getNode(v);
                        if(!lnode.isSame(rnode)) {
                            output.add(v);
                            graphLocal.update(v, rnode);
                        }
                    }
                }
            }
        }
        return output;
    }
}
