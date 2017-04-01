package gridpanel;

import java.awt.Color;

import gridpanel.GridPanelCell;

public class MutableGridPanelCell extends GridPanelCell
{
    public MutableGridPanelCell(GridPanelCell c) {
        super(c);
    }
    public MutableGridPanelCell(Color color, int arrowX, int arrowY, double circleRadius, String toolTip) {
        super(color, arrowX, arrowY, circleRadius, toolTip);
    }
    public MutableGridPanelCell() {
        super();
    }

    public void setColor(Color color) {
        this.color = color;
    }
    public void setArrowX(int arrowX) {
        this.arrowX = arrowX;
    }
    public void setArrowY(int arrowY) {
        this.arrowY = arrowY;
    }
    public void setCircleRadius(double circleRadius) {
        this.circleRadius = circleRadius;
    }
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }
    public GridPanelCell getCopy() {
        return new GridPanelCell(this);
    }
}
