/*
 * FigureItemBehaviour.java
 * 
 * Created on Oct 27, 2014 2:21:50 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.util.List;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.R2;
import org.apache.log4j.Logger;

/**
 *
 * @author ctipper
 */

public class FigureItemBehaviour implements ItemBehaviours {
    
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private BehaviourContext context;

    public boolean selectItem(Figure item, int index, double startx, double starty) {
        boolean found = false;

        FigureType type = item.getType();
        if (!type.equals(FigureType.SKETCH)
            && !type.equals(FigureType.POLYGON)
            && !type.equals(FigureType.LINE)) {
            List<CanvasPoint> vertices = ((Figure) item).getVertices();
            CanvasPoint centre = item.rotationCentre();
            for (CanvasPoint vertex : vertices) {
                if (context.getRegion(vertex).contains(startx, starty)) {
                    int quad = R2.quadrant(vertex, centre);
                    switch (quad) {
                        case 0:
                            view.setSelected(index);
                            context.setContainment(ContainsType.TR);
                            found = true;
                            break;
                        case 1:
                            view.setSelected(index);
                            context.setContainment(ContainsType.TL);
                            found = true;
                            break;
                        case 2:
                            view.setSelected(index);
                            context.setContainment(ContainsType.BL);
                            found = true;
                            break;
                        case 3:
                            view.setSelected(index);
                            context.setContainment(ContainsType.BR);
                            found = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            
            if (!found && item.contains(startx, starty)) {
                view.setSelected(index);
                context.setContainment(ContainsType.SHAPE);
                found = true;
            }
            
            Logger.getLogger(FigureItemBehaviour.class.getName()).debug("Containment: " + context.getContainment().toString());
        } else if (item.contains(startx, starty)) {
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
        CanvasPoint st = item.getStart();
        CanvasPoint en = item.getEnd();
        switch (type) {
            case LINE:
                // item is Line
                // allows alternate drag of end-points
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
                break;
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
            case RECTANGLE:
            case ELLIPSE:
            case ISOSCELES:
                if (context.getContains().isEmpty()) {
                    // copy and permute to contains
                    for (ContainsType cont : context.getContainment()) {
                        context.setContains(R2.permute(cont, st, item.rotationCentre()));
                    }
                }
                ContainsType c = context.getContainment().pollFirst();
                //Logger.getLogger(FigureItemBehaviour.class.getName()).debug("Contains: " + context.getContains().toString());
                if (!c.equals(ContainsType.SHAPE)
                    && !c.equals(ContainsType.NONE)) {
                    contains = context.getContains().pollFirst();
                    if (contains.equals(ContainsType.NONE)) {
                        context.setContains(contains);
                        contains = context.getContains().pollFirst();
                    }
                    context.setContains(contains);
                } else {
                    contains = c;
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
                context.setContainment(c);
                break;
            default:
                // All other Figures
                item.moveFigure(xinc, yinc);
                break;
        }
    }
}
