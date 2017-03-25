package graph;

public class GridGraphNode
{
    private int type;
    private int occ;    // occupancy

    public int getType() {return type;}
    public int getOcc() {return occ;}

    public GridGraphNode(int type, int occ) {
        this.type = type;
        this.occ = occ;
    }
    public GridGraphNode() {
        this(0, 1);
    }

    public String toString() {
        return "GridGraphNode(" + type + ", " + occ + ")";
    }
}
