package planner;

import java.util.List;
import java.util.ArrayList;

import planner.AbstractPlanner;
import planner.Stage;

public abstract class AbstractAdjacentPlanner extends AbstractPlanner
// An abstract planner where a robot only moves to adjacent nodes
{
    public abstract int getNext(int u);

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

    public long move()
    {
        if(curr == goal)
            throw new RuntimeException("Already at goal");
        boolean changed = checkAndUpdate();
        long time_taken = 0;
        if(changed)
            time_taken = replan();
        if(getNext(curr) == -1)
            throw new RuntimeException("No path to destination");
        distance += graphLocal.getWeight(curr, getNext(curr));
        int old_curr = curr;
        curr = getNext(curr);
        doCallback(old_curr);
        doCallback(curr);
        return time_taken;
    }
}
