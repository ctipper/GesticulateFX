/*
 * BehaviourContext.java
 * 
 * Created on Oct 27, 2014 2:21:27 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
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
    private ContainsType containment = ContainsType.NONE;
    
    public boolean select(Figure item, int index) {
        return strategy.selectItem(item, index);
    }

    public void alter(Figure item, double xinc, double yinc) {
        strategy.alterItem(item, xinc, yinc);
    }
    
    public void setBehaviour(ItemBehaviours s) {
        this.strategy = s;
    }

    public ContainsType getContainment() {
        return containment;
    }

    public void setContainment(ContainsType containment) {
        this.containment = containment;
    }

    public Area getRegion(CanvasPoint p) {
        Rectangle2D rect;
        rect = new Rectangle2D.Double(p.x - 20.0, p.y - 20.0, 40.0, 40.0);
        return new Area(rect);
    }
}
