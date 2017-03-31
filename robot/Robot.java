package robot;

import java.util.*;

import graph.AbstractGraph;

public interface Robot {
    public abstract int getPosition();
    public int moveTo(int position);
    public AbstractGraph getGraph();
    public Set<Integer> getUpdatedNodes(int radius);
}
