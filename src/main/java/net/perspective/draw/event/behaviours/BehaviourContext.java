/*
 * BehaviourContext.java
 * 
 * Created on Oct 27, 2014 2:21:27 PM
 * 
 */

/**
 * Copyright (c) 2022 Christopher Tipper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw.event.behaviours;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.inject.Singleton;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.util.CanvasPoint;

/**
 * A context for the orchestration of mouse and touch behaviours
 * 
 * @author ctipper
 */

@Singleton
public class BehaviourContext {

    private ItemBehaviours strategy;
    private ContainsType containment, contains;
    private boolean edgeDetected;
    private int quad;
    private double sgnd_area;
    private CanvasPoint omega;

    /** Creates a new instance of <code>BehaviourContext</code> */
    public BehaviourContext() {
        this.quad = -1;
        this.sgnd_area = -1d;
        this.omega = new CanvasPoint(0, 0);
        this.containment = ContainsType.NONE;
        this.contains = ContainsType.NONE;
        this.edgeDetected = false;
    }

    /**
     * Set the strategy for this context
     * 
     * @param strategy  a behaviour strategy
     */
    public void setBehaviour(ItemBehaviours strategy) {
        this.strategy = strategy;
    }

    /**
     * Activate the strategy's select behaviour
     * 
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param index  the index of the item in list of drawings
     * @return item is selected
     */
    public boolean select(DrawItem item, int index) {
        return strategy.selectItem(this, item, index);
    }

    /**
     * Activate the strategy's edit behaviour
     * 
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param index  the index of the item in list of drawings
     */
    public void edit(DrawItem item, int index) {
        strategy.editItem(this, item, index);
    }

    /**
     * Activate the strategy's hover behaviour
     * 
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     */
    public void hover(DrawItem item) {
        strategy.hoverItem(this, item);
    }

    /**
     * Activate the strategy's alter behaviour
     * 
     * @param item  a {@link net.perspective.draw.geom.DrawItem}
     * @param xinc  an x increment
     * @param yinc  a y increment
     */
    public void alter(DrawItem item, double xinc, double yinc) {
        strategy.alterItem(this, item, xinc, yinc);
    }

    /**
     * Set a property that specifies the type of selection
     * 
     * @param containment  the containment type
     */
    public void setContainment(ContainsType containment) {
        this.containment = containment;
    }

    /**
     * Return a property that specifies the type of containment
     * 
     * @return containment
     */
    public ContainsType getContainment() {
        return containment;
    }

    /**
     * Set a property that specifies the type of selection
     * 
     * <p>This property is usually calculated on the basis of the containment property
     * 
     * @param contains  the contain type
     */
    public void setContains(ContainsType contains) {
        this.contains = contains;
    }

    /**
     * Return a property that specifies the type of selection
     * 
     * @return contains
     */
    public ContainsType getContains() {
        return contains;
    }

    /**
     * Reset this context
     */
    public void resetContainment() {
        containment = ContainsType.NONE;
        contains = ContainsType.NONE;
        quad = -1;
        sgnd_area = -1d;
        this.setEdgeDetected(false);
        this.setOmega(0, 0);
    }

    /**
     * Set true if an edge has been selected
     * 
     * Used by containment to select transformation
     * 
     * @param edgeDetected the edgeDetected to set
     */
    protected void setEdgeDetected(boolean edgeDetected) {
        this.edgeDetected = edgeDetected;
    }

    /**
     * Has an edge been selected
     * 
     * @return the edgeDetected
     */
    protected boolean isEdgeDetected() {
        return edgeDetected;
    }

    /**
     * Set the quadrant for purposes of shape selection
     * 
     * @param quad a quadrant id
     */
    public void setQuad(int quad) {
        this.quad = quad;
    }

    /**
     * Return the quadrant id of the selection
     * 
     * @return quad
     */
    public int getQuad() {
        return quad;
    }

    /**
     * Set the signed area of the selected figure
     * 
     * @param sgnd_area  a signed area
     * @see net.perspective.draw.geom.Figure#sgnd_area() 
     */
    public void setSgndArea(double sgnd_area) {
        this.sgnd_area = sgnd_area;
    }

    /**
     * Return the signed area of the selected figure
     * 
     * @return sgnd_area
     */
    public double getSgndArea() {
        return sgnd_area;
    }

    /**
     * Get an area centred on the specified point
     * 
     * @param p  a {@link net.perspective.draw.util.CanvasPoint}
     * @return area
     */
    public Area getRegion(CanvasPoint p) {
        Rectangle2D rect;
        rect = new Rectangle2D.Double(p.x - 10.0, p.y - 10.0, 20.0, 20.0);
        return new Area(rect);
    }

    /**
     * Set the mouse drag origin
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setOmega(double x, double y) {
        this.omega = new CanvasPoint(x, y);
    }

    /**
     * Get the mouse drag origin
     * 
     * @return a {@link net.perspective.draw.util.CanvasPoint}
     */
    public CanvasPoint getOmega() {
        return omega;
    }

}
