package com.mcjty.gui.widgets;

import com.mcjty.gui.*;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.SelectionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WidgetList extends AbstractContainerWidget<WidgetList> implements Scrollable {
    private int rowheight = 16;
    private int first = 0;
    private int selected = -1;
    private long prevTime = -1;
    private List<SelectionEvent> selectionEvents = null;
    private Set<Integer> hilightedRows = new HashSet<Integer>();

    public WidgetList(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public int getRowheight() {
        return rowheight;
    }

    public WidgetList setRowheight(int rowheight) {
        this.rowheight = rowheight;
        return this;
    }

    public int getSelected() {
        return selected;
    }

    public WidgetList setSelected(int selected) {
        this.selected = selected;
        return this;
    }

    public void addHilightedRow(int index) {
        hilightedRows.add(index);
    }

    public void clearHilightedRows() {
        hilightedRows.clear();
    }

    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);
        int xx = x + bounds.x;
        int yy = y + bounds.y;
        int top = 0;        // Margin@@@?
//        drawBox(xx, yy, 0xffff0000);

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            child.setBounds(new Rectangle(0 /*@@@ margin?*/, top, bounds.width, rowheight));
            boolean hilighted = hilightedRows.contains(i);
            if (i == selected && hilighted) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width, yy + top + rowheight, 0xff888844, 0xff666622);
            } else if (i == selected) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width, yy + top + rowheight, 0xff666666, 0xff444444);
            } else if (hilighted) {
                RenderHelper.drawHorizontalGradientRect(xx, yy + top, xx + bounds.width, yy + top + rowheight, 0xffbbbb00, 0xff999900);
            }
            child.draw(window, xx, yy);
            top += rowheight;
        }
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        int newSelected = -1;
        int top = bounds.y;        // Margin@@@?

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Rectangle r = new Rectangle(bounds.x, top, bounds.width, rowheight);
            if (r.contains(x, y)) {
                newSelected = i;
                break;
            }
            top += rowheight;
        }
        if (newSelected != selected) {
            selected = newSelected;
            fireSelectionEvents(selected);
        }
        long t = mc.theWorld.getTotalWorldTime();
        if (prevTime != -1 && (t - prevTime) < 5) {
            fireDoubleClickEvent(selected);
        }
        prevTime = t;

        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
    }

    @Override
    public int getMaximum() {
        return children.size();
    }

    @Override
    public int getCountSelected() {
        return bounds.height / rowheight;
    }

    @Override
    public int getFirstSelected() {
        return first;
    }

    @Override
    public void setFirstSelected(int first) {
        this.first = first;
    }

    public WidgetList addSelectionEvent(SelectionEvent event) {
        if (selectionEvents == null) {
            selectionEvents = new ArrayList<SelectionEvent> ();
        }
        selectionEvents.add(event);
        return this;
    }

    public void removeSelectionEvent(SelectionEvent event) {
        if (selectionEvents != null) {
            selectionEvents.remove(event);
        }
    }

    private void fireSelectionEvents(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.select(this, index);
            }
        }
    }

    private void fireDoubleClickEvent(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.doubleClick(this, index);
            }
        }
    }
    @Override
    public WidgetList removeChild(Widget child) {
        int index = children.indexOf(child);
        if (index != -1) {
            Set<Integer> newHighlights = new HashSet<Integer>();
            for (Integer i : hilightedRows) {
                if (i < index) {
                    newHighlights.add(i);
                } else if (i > index) {
                    newHighlights.add(i-1);
                }
            }
            hilightedRows = newHighlights;
        }
        return super.removeChild(child);
    }

    @Override
    public void removeChildren() {
        super.removeChildren();
        hilightedRows.clear();
    }
}
