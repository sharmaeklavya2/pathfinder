package gridpanel;

import java.awt.Color;

import gridpanel.GridPanelCell;

public class MutableGridPanelCell extends GridPanelCell
{
    public void setColor(Color color) {
        this.color = color;
    }
    public void setArrowX(int arrowX) {
        this.arrowX = arrowX;
    }
    public void setArrowY(int arrowY) {
        this.arrowY = arrowY;
    }
    public GridPanelCell getCopy() {
        return new GridPanelCell(this);
    }
}
