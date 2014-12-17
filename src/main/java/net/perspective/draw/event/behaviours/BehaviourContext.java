/*
 * BehaviourContext.java
 * 
 * Created on Oct 27, 2014 2:21:27 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.inject.Singleton;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

@Singleton
public class BehaviourContext {

    ItemBehaviours strategy;
    private ContainsType containment, contains;
    private int quad;
    private double sgnd_area;

    public BehaviourContext() {
        this.quad = -1;
        this.sgnd_area = -1d;
        this.containment = ContainsType.NONE;
        this.contains = ContainsType.NONE;
    }
    
    public boolean select(Figure item, int index) {
        return strategy.selectItem(this, item, index);
    }

    public void alter(Figure item, double xinc, double yinc) {
        strategy.alterItem(this, item, xinc, yinc);
    }
    
    public void setBehaviour(ItemBehaviours s) {
        this.strategy = s;
    }

    public void setContainment(ContainsType containment) {
        this.containment = containment;
    }

    public ContainsType getContainment() {
        return containment;
    }

    public void setContains(ContainsType containment) {
        this.contains = containment;
    }

    public ContainsType getContains() {
        return contains;
    }
    
    public void resetContainment() {
        containment = ContainsType.NONE;
        contains = ContainsType.NONE;
        quad = -1;
        sgnd_area = -1d;
    }

    public void setQuad(int quad) {
        this.quad = quad;
    }

    public int getQuad() {
        return quad;
    }

    public void setSgndArea(double sgndarea) {
        this.sgnd_area = sgndarea;
    }

    public double getSgndArea() {
        return sgnd_area;
    }

    public Area getRegion(CanvasPoint p) {
        Rectangle2D rect;
        rect = new Rectangle2D.Double(p.x - 10.0, p.y - 10.0, 20.0, 20.0);
        return new Area(rect);
    }
}
