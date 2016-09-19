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
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureType;
import net.perspective.draw.util.CanvasPoint;
import net.perspective.draw.util.R2;
import net.perspective.draw.util.V2;

/**
 *
 * @author ctipper
 */

public class FigureItemBehaviour implements ItemBehaviours {
    
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;

    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        boolean found = false;

        FigureType type = ((Figure) item).getType();
        if (!type.equals(FigureType.SKETCH)
            && !type.equals(FigureType.POLYGON)
            && !type.equals(FigureType.LINE)) {
            List<CanvasPoint[]> vertices = ((Figure) item).getVertices();
            CanvasPoint centre = item.rotationCentre();
            for (CanvasPoint[] vertex : vertices) {
                if (context.getRegion(vertex[0]).contains(listener.getStartX(), listener.getStartY())) {
                    int quad = R2.quadrant(vertex[1], centre);
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
            
            if (!found && item.contains(listener.getStartX(), listener.getStartY())) {
                view.setSelected(index);
                context.setContainment(ContainsType.SHAPE);
                found = true;
            }
        } else if (item.contains(listener.getStartX(), listener.getStartY())) {
            // All other figures
            view.setSelected(index);
            context.setContainment(ContainsType.SHAPE);
            found = true;
        }
        return found;
    }
    
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        ContainsType contains;
        DrawingType drawType;

        FigureType type = ((Figure) item).getType();
        CanvasPoint st = item.getStart();
        CanvasPoint en = item.getEnd();
        switch (type) {
            case LINE:
                // item is Line
                // allows alternate drag of end-points
                if (context.getRegion(st).contains(listener.getStartX(), listener.getStartY())) {
                    st.translate(xinc, yinc);
                    item.setStart(st.x, st.y);
                    ((Figure) item).setPoints(DrawingType.LINE);
                    ((Figure) item).setPath();
                } else if (context.getRegion(en).contains(listener.getStartX(), listener.getStartY())) {
                    en.translate(xinc, yinc);
                    item.setEnd(en.x, en.y);
                    ((Figure) item).setPoints(DrawingType.LINE);
                    ((Figure) item).setPath();
                } else {
                    item.moveShape(xinc, yinc);
                }
                break;
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
                switch (type) {
                    case SQUARE:
                        drawType = DrawingType.RECTANGLE;
                        break;
                    case CIRCLE:
                        drawType = DrawingType.ELLIPSE;
                        break;
                    default:
                        drawType = DrawingType.ISOSCELES;
                        break;
                }
                if (!context.getContainment().equals(ContainsType.SHAPE)
                    && !context.getContainment().equals(ContainsType.NONE)) {
                    if (context.getContains().equals(ContainsType.NONE)) {
                        context.setContains(R2.permute(context.getContainment(), R2.quadrant(st, item.rotationCentre())));
                    }
                    contains = context.getContains();
                } else {
                    contains = context.getContainment();
                }
                if (context.getSgndArea() < 0) {
                    context.setSgndArea(((Figure) item).sgnd_area());
                }
                if (context.getQuad() == -1) {
                    int quad = R2.quadrant(item.getTop()[1], item.rotationCentre());
                    if (context.getSgndArea() >= 0 && (quad == 0 || quad == 2)) {
                        context.setQuad(R2.quadrant(item.getBottom()[1], item.rotationCentre()));
                    } else {
                        context.setQuad(quad);
                    }
                }
                double[] flip = R2.flip(context.getQuad());
                double cos_t = flip[0];
                double sin_t = flip[1];
                double t = item.getAngle();
                double delta = V2.norm_angle(4 * t + 2 * Math.PI) / 2;
                CanvasPoint inc = V2.rot(xinc, yinc, t - delta);
                switch (contains) {
                    case TL:
                        st.translate((cos_t - sin_t) * inc.x, (cos_t + sin_t) * inc.y);
                        en.translate((-cos_t + sin_t) * inc.x, (-cos_t - sin_t) * inc.y);
                        item.setStart(st.x, st.y);
                        item.setEnd(en.x, en.y);
                        ((Figure) item).setPoints(drawType);
                        ((Figure) item).setPath();
                        break;
                    case BL:
                        st.translate((cos_t + sin_t) * inc.x, (-cos_t + sin_t) * inc.y);
                        en.translate((-cos_t - sin_t) * inc.x, (cos_t - sin_t) * inc.y);
                        item.setStart(st.x, st.y);
                        item.setEnd(en.x, en.y);
                        ((Figure) item).setPoints(drawType);
                        ((Figure) item).setPath();
                        break;
                    case BR:
                        st.translate((-cos_t + sin_t) * inc.x, (-cos_t - sin_t) * inc.y);
                        en.translate((cos_t - sin_t) * inc.x, (cos_t + sin_t) * inc.y);
                        item.setStart(st.x, st.y);
                        item.setEnd(en.x, en.y);
                        ((Figure) item).setPoints(drawType);
                        ((Figure) item).setPath();
                        break;
                    case TR:
                        st.translate((-cos_t - sin_t) * inc.x, (cos_t - sin_t) * inc.y);
                        en.translate((cos_t + sin_t) * inc.x, (-cos_t + sin_t) * inc.y);
                        item.setStart(st.x, st.y);
                        item.setEnd(en.x, en.y);
                        ((Figure) item).setPoints(drawType);
                        ((Figure) item).setPath();
                        break;
                    case SHAPE:
                        item.moveShape(xinc, yinc);
                        break;
                    case NONE:
                    default:
                        break;
                }
                break;
            default:
                // All other Figures
                item.moveShape(xinc, yinc);
                break;
        }
    }
}
