package gridpanel;

import java.awt.Color;

public class GridPanelCell
{
    protected Color color = Color.white;
    public Color getColor() {
        return color;
    }

    public GridPanelCell(Color c) {
        color = c;
    }
    public GridPanelCell(GridPanelCell gpc) {
        this(gpc.color);
    }
    public GridPanelCell() {
        this(Color.WHITE);
    }

    public GridPanelCell clone() {
        return this;
    }
}
