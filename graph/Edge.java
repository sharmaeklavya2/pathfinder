package graph;

/** Edge in a graph. It stores the source node, the destination node and edge weight. */
public class Edge
{
    private int src, dst;
    private double w;
    /** Minimum difference between weights for two edges to be different. */
    public static final double EPS = 0.00001;
    public static final double SQRT2 = 1.414213562373095048802;

    public Edge(int src, int dst, double w) {
        this.src = src;
        this.dst = dst;
        this.w = w;
    }
    public String toString() {
        return "(" + this.src + ", " + this.dst + ", " + this.w + ")";
    }
    public Edge reverse() {
        return new Edge(dst, src, w);
    }

    int getSrc() {return src;}
    int getDst() {return dst;}
    double getW() {return w;}
}
