package planner;

import java.util.*;
import gridpanel.GridPanelCell;
import graph.AbstractGraph;
import graph.GenGraph;
import robot.Robot;

public abstract class AbstractPlanner
{
    public static class Callback {
        public void nodeUpdate(int u){}
        public void move(int u, int v){}
        public void pathUpdate(){}
    }

    protected int goal;
    protected int distance;
    protected Callback callback;

    protected AbstractPlanner() {
        this.callback = new Callback();
        this.distance = 0;
    }

    public int getGoal() {return goal;}
    public double getDistance() {
        return this.distance;
    }

    public Callback getCallback() {return callback;}
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public abstract Robot getRobot();
    public abstract int getNext(int u);

    public abstract void reset();
    public abstract long replan();

    protected abstract void examineUpdates(Set<Integer> updatedNodes);

    public abstract GridPanelCell getGridPanelCell(int u, boolean onPath);
    public GridPanelCell getGridPanelCell(int u) {
        return getGridPanelCell(u, false);
    }

    public List<Integer> getPath(int u) {
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

    public long move(int radius) {
        int u = getRobot().getPosition();
        if(u == goal)
            throw new RuntimeException("Already at goal");

        Set<Integer> updatedNodes = getRobot().getUpdatedNodes(radius);
        boolean changed = (updatedNodes.size() > 0);
        examineUpdates(updatedNodes);

        long time_taken = 0;
        if(changed)
            time_taken = replan();
        int v = getNext(u);
        if(v == -1)
            throw new RuntimeException("No path to destination");

        getRobot().moveTo(v);
        // moveTo might not actually move to v
        int v2 = getRobot().getPosition();
        distance += getRobot().getGraph().getWeight(u, v2);
        callback.move(u, v);
        return time_taken;
    }
    public long move() {return this.move(0);}
}
