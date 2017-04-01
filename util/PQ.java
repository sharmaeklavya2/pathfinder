package util;

import java.util.*;

public class PQ {
    public class PQElem implements Comparable<PQElem>
    {
        private int value;
        private double priority;
        public static final double EPS = 0.00001;

        public int getValue() {return value;}
        public double getPriority() {return priority;}

        public PQElem(int value, double priority) {
            this.value = value;
            this.priority = priority;
        }
        public int compareTo(PQElem pq) {
            double diff = priority - pq.priority;
            if(diff >= EPS)
                return 1;
            else if(diff <= -EPS)
                return -1;
            else
                return value - pq.value;
        }
    }

    TreeSet<PQElem> set;
    Map<Integer, Double> prioMap;

    public PQ() {
        set = new TreeSet<PQElem>();
        prioMap = new HashMap<Integer, Double>();
    }

    public void push(int value, double priority) {
        Double oldPrio = prioMap.get(value);
        if(oldPrio != null) {
            set.remove(new PQElem(value, oldPrio));
        }
        prioMap.put(value, priority);
        set.add(new PQElem(value, priority));
    }

    public PQElem top() {
        return set.first();
    }

    public PQElem pop() {
        PQElem elem = set.pollFirst();
        prioMap.remove(elem.getValue());
        return elem;
    }

    public void clear() {
        set.clear();
        prioMap.clear();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }
}
