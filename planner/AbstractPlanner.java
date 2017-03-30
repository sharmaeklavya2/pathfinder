package planner;

import graph.AbstractGraph;
import graph.GenGraph;
import planner.NodeUpdateCallback;

abstract class AbstractPlanner
{
    protected int goal, curr;
    protected AbstractGraph graphRemote;
    protected GenGraph graphLocal;
    protected int distance;
    protected NodeUpdateCallback callback;
    // graphRemote can be (in fact, we sometimes want it to be) modified from outside.

    public int getGoal() {return goal;}
    public int getCurr() {return curr;}
    public String getLocalGraphStr() {return graphLocal.toString();}
    public double getDistance() {return distance;}

    public NodeUpdateCallback getCallback() {return callback;}
    public void setCallback(NodeUpdateCallback callback) {
        this.callback = callback;
    }
    public void doCallback(int u) {
        if(callback != null)
            callback.run(u);
    }

    public abstract void reset(int curr);
    public abstract long replan();

    public abstract long move();
}
