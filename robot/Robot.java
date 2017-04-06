package robot;

import java.util.*;

import graph.AbstractGraph;

/** An agent which has a local copy of a map, an actuator to move about,
    and a sensor to detect changes in surroundings. */
public interface Robot {
    /** Return the node where the robot is located. */
    public abstract int getPosition();

    /** Command the robot to move to a certain location. It's not required for the robot to succeed. */
    public int moveTo(int position);

    /** Get the robot's local copy of the map. */
    public AbstractGraph getGraph();

    /** Get nodes in the robot's surroundings which have changed.
        The robot updates its local copy when this method is called. */
    public Set<Integer> getUpdatedNodes(int radius);
}
