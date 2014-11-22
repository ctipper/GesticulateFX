/*
 * BehaviourContext.java
 * 
 * Created on Oct 27, 2014 2:21:27 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.DrawingArea;
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
    @Inject DrawingArea drawarea;
    private ContainsType containment = ContainsType.NONE, contains = ContainsType.NONE;
    
    public boolean select(Figure item, int index, double startx, double starty) {
        return strategy.selectItem(item, index, startx, starty);
    }

    public void alter(Figure item, double xinc, double yinc) {
        strategy.alterItem(item, xinc, yinc);
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
    }

    public Area getRegion(CanvasPoint p) {
        Rectangle2D rect;
        rect = new Rectangle2D.Double(p.x - 10.0, p.y - 10.0, 20.0, 20.0);
        return new Area(rect);
    }
}
