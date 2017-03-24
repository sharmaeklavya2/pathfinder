package gridpanel;

import java.awt.Color;

import gridpanel.GridPanelCell;

public class MutableGridPanelCell extends GridPanelCell
{
    public void setColor(Color c) {
        color = c;
    }
    public GridPanelCell clone() {
        return new GridPanelCell(this);
    }
}
