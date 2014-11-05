/*
 * FigureItemBehaviour.java
 * 
 * Created on Oct 27, 2014 2:21:50 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.event.ContainsRotator;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;

/**
 *
 * @author ctipper
 */

public class FigureItemBehaviour implements ItemBehaviours {
    
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private BehaviourContext context;

    public boolean selectItem(Figure item, int index) {
        boolean found = false;

        FigureType type = item.getType();
        if (!type.equals(FigureType.SKETCH)
            && !type.equals(FigureType.POLYGON)
            && !type.equals(FigureType.LINE)) {
            if (context.getRegion(item.getTop()).
                contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.TL);
                found = true;
            } else if (context.getRegion(item.getDown()).
                contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.BL);
                found = true;
            } else if (context.getRegion(item.getBottom()).
                contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.BR);
                found = true;
            } else if (context.getRegion(item.getUp()).
                contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.TR);
                found = true;
            } else if (item.contains(drawarea.getStartX(), drawarea.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.SHAPE);
                found = true;
            }
        } else if (item.contains(drawarea.getStartX(), drawarea.getStartY())) {
            // All other figures
            view.setSelected(index);
            context.setContainment(ContainsType.SHAPE);
            found = true;
        }
        return found;
    }
    
    public void alterItem(Figure item, double xinc, double yinc) {
        ContainsType contains;
        
        FigureType type = item.getType();
        if (type.equals(FigureType.LINE)) {
            // item is Line
            // allows alternate drag of end-points
            CanvasPoint st = item.getStart();
            CanvasPoint en = item.getEnd();
            if (context.getRegion(st).contains(drawarea.getStartX(), drawarea.getStartY())) {
                st.translate(xinc, yinc);
                item.setStart(st.x, st.y);
                item.setPoints();
                item.setPath();
            } else if (context.getRegion(en).contains(drawarea.getStartX(), drawarea.getStartY())) {
                en.translate(xinc, yinc);
                item.setEnd(en.x, en.y);
                item.setPoints();
                item.setPath();
            } else {
                item.moveFigure(xinc, yinc);
            }
        } else if (!type.equals(FigureType.SKETCH)
            && !type.equals(FigureType.POLYGON)
            && !type.equals(FigureType.LINE)) {
            CanvasPoint st = item.getStart();
            CanvasPoint en = item.getEnd();
            if (!context.getContainment().equals(ContainsType.SHAPE)
                && !context.getContainment().equals(ContainsType.NONE)) {
                contains = ContainsRotator.permute(context.getContainment(), item.getAngle());
            } else {
                contains = context.getContainment();
            }
            switch (contains) {
                case TL:
                    st.translate(xinc, yinc);
                    item.setStart(st.x, st.y);
                    item.setEnd(en.x, en.y);
                    item.setPoints();
                    item.setPath();
                    break;
                case BL:
                    st.translate(xinc, 0);
                    en.translate(0, yinc);
                    item.setStart(st.x, st.y);
                    item.setEnd(en.x, en.y);
                    item.setPoints();
                    item.setPath();
                    break;
                case BR:
                    en.translate(xinc, yinc);
                    item.setStart(st.x, st.y);
                    item.setEnd(en.x, en.y);
                    item.setPoints();
                    item.setPath();
                    break;
                case TR:
                    st.translate(0, yinc);
                    en.translate(xinc, 0);
                    item.setStart(st.x, st.y);
                    item.setEnd(en.x, en.y);
                    item.setPoints();
                    item.setPath();
                    break;
                case SHAPE:
                    item.moveFigure(xinc, yinc);
                    break;
                case NONE:
                default:
                    break;
            }
        } else { // All other Figures
            item.moveFigure(xinc, yinc);
        }
    }
}
