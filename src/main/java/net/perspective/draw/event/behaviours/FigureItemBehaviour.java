/*
 * FigureItemBehaviour.java
 * 
 * Created on Oct 27, 2014 2:21:50 PM
 * 
 */
package net.perspective.draw.event.behaviours;

import java.util.List;
import javafx.scene.Cursor;
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

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;

    @Override
    public boolean selectItem(BehaviourContext context, DrawItem item, int index) {
        boolean found = false;
        int quad;

        FigureType type = ((Figure) item).getType();
        if (!type.equals(FigureType.SKETCH) && !type.equals(FigureType.POLYGON) && !type.equals(FigureType.LINE)) {
            List<CanvasPoint[]> vertices = ((Figure) item).getVertices();
            CanvasPoint centre = item.rotationCentre();
            /**
             * Select vertices
             */
            for (CanvasPoint[] vertex : vertices) {
                if (context.getRegion(vertex[0]).contains(listener.getStartX(), listener.getStartY())) {
                    quad = R2.quadrant(vertex[1], centre);
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
            /**
             * Select edges
             */
            if (!found) {
                List<CanvasPoint[]> edges = ((Figure) item).getEdges();
                for (CanvasPoint[] edge : edges) {
                    if (context.getRegion(edge[0]).contains(listener.getStartX(), listener.getStartY())) {
                        quad = R2.quarter(edge[1], centre);
                        switch (quad) {
                            case 0:
                                view.setSelected(index);
                                context.setContainment(ContainsType.TT);
                                found = true;
                                break;
                            case 1:
                                view.setSelected(index);
                                context.setContainment(ContainsType.LL);
                                found = true;
                                break;
                            case 2:
                                view.setSelected(index);
                                context.setContainment(ContainsType.BB);
                                found = true;
                                break;
                            case 3:
                                view.setSelected(index);
                                context.setContainment(ContainsType.RR);
                                found = true;
                                break;
                            default:
                                break;
                        }
                        context.setEdgeDetected(found);
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

    @Override
    public void hoverItem(BehaviourContext context, DrawItem item) {
        switch (((Figure) item).getType()) {
            case LINE:
                CanvasPoint st = item.getStart();
                CanvasPoint en = item.getEnd();
                if (context.getRegion(st).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (context.getRegion(en).contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.CROSSHAIR);
                } else if (item.contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.OPEN_HAND);
                } else {
                    drawarea.getScene().setCursor(Cursor.DEFAULT);
                }
                break;
            case SQUARE:
            case CIRCLE:
            case TRIANGLE:
            case HEXAGON:
                List<CanvasPoint[]> vertices = ((Figure) item).getVertices();
                CanvasPoint centre = item.rotationCentre();
                boolean found = this.switchVertices(context, vertices, centre);
                List<CanvasPoint[]> edges = ((Figure) item).getEdges();
                found = found | this.switchVertices(context, edges, centre);
                if (!found) {
                    if (item.contains(listener.getTempX(), listener.getTempY())) {
                        drawarea.getScene().setCursor(Cursor.OPEN_HAND);
                    } else {
                        drawarea.getScene().setCursor(Cursor.DEFAULT);
                    }
                }
                break;
            default:
                // All other Figures
                if (item.contains(listener.getTempX(), listener.getTempY())) {
                    drawarea.getScene().setCursor(Cursor.OPEN_HAND);
                } else {
                    drawarea.getScene().setCursor(Cursor.DEFAULT);
                }
                break;
        }
    }

    @Override
    public void alterItem(BehaviourContext context, DrawItem item, double xinc, double yinc) {
        ContainsType contains;
        DrawingType drawType;

        FigureType type = ((Figure) item).getType();
        CanvasPoint st = item.getStart();
        CanvasPoint en = item.getEnd();
        if (context.getContainment().equals(ContainsType.SHAPE)) {
            if (type == FigureType.LINE) {
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
                    drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
                    item.moveShape(xinc, yinc);
                }
            } else {
                drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
                item.moveShape(xinc, yinc);
            }
        } else {

            switch (type) {
                case SQUARE:
                case CIRCLE:
                case TRIANGLE:
                case HEXAGON:
                    switch (type) {
                        case SQUARE:
                            drawType = DrawingType.RECTANGLE;
                            break;
                        case CIRCLE:
                            drawType = DrawingType.ELLIPSE;
                            break;
                        case HEXAGON:
                            drawType = DrawingType.HEXAGON;
                            break;
                        default:
                            drawType = DrawingType.ISOSCELES;
                            break;
                    }

                    /**
                     * Permute containment selectors
                     */
                    if (!context.getContainment().equals(ContainsType.SHAPE)
                            && !context.getContainment().equals(ContainsType.NONE)) {
                        if (context.getContains().equals(ContainsType.NONE) && !context.isEdgeDetected()) {
                            context.setContains(R2.permute(context.getContainment(), R2.quadrant(st, item.rotationCentre())));
                        }
                        if (context.getContains().equals(ContainsType.NONE) && context.isEdgeDetected()) {
                            context.setContains(R2.mutate(context.getContainment(), R2.quadrant(st, item.rotationCentre())));
                        }
                        contains = context.getContains();
                    } else {
                        contains = context.getContainment();
                    }

                    /**
                     * Adjust for quadrant of TL vertex
                     */
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

                    // retrieve increment correctors
                    int[] flip = R2.flip(context.getQuad());
                    int cos_t = flip[0];
                    int sin_t = flip[1];

                    // correct increment for angle of rotation
                    @SuppressWarnings("deprecation") 
                    double t = item.getAngle() + (item.isVertical() ? -Math.PI / 2 : 0);
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
                        case TT:
                            st.translate((0 - sin_t) * inc.x, (cos_t + 0) * inc.y);
                            en.translate((-0 + sin_t) * inc.x, (-cos_t - 0) * inc.y);
                            item.setStart(st.x, st.y);
                            item.setEnd(en.x, en.y);
                            ((Figure) item).setPoints(drawType);
                            ((Figure) item).setPath();
                            break;
                        case LL:
                            st.translate((cos_t + 0) * inc.x, (-0 + sin_t) * inc.y);
                            en.translate((-cos_t - 0) * inc.x, (0 - sin_t) * inc.y);
                            item.setStart(st.x, st.y);
                            item.setEnd(en.x, en.y);
                            ((Figure) item).setPoints(drawType);
                            ((Figure) item).setPath();
                            break;
                        case BB:
                            st.translate((-0 + sin_t) * inc.x, (-cos_t - 0) * inc.y);
                            en.translate((0 - sin_t) * inc.x, (cos_t + 0) * inc.y);
                            item.setStart(st.x, st.y);
                            item.setEnd(en.x, en.y);
                            ((Figure) item).setPoints(drawType);
                            ((Figure) item).setPath();
                            break;
                        case RR:
                            st.translate((-cos_t - 0) * inc.x, (0 - sin_t) * inc.y);
                            en.translate((cos_t + 0) * inc.x, (-0 + sin_t) * inc.y);
                            item.setStart(st.x, st.y);
                            item.setEnd(en.x, en.y);
                            ((Figure) item).setPoints(drawType);
                            ((Figure) item).setPath();
                            break;
                        case SHAPE:
                            drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
                            item.moveShape(xinc, yinc);
                            break;
                        case NONE:
                        default:
                            break;
                    }
                    break;
                default:
                    // All other Figures
                    drawarea.getScene().getRoot().setCursor(Cursor.CLOSED_HAND);
                    item.moveShape(xinc, yinc);
                    break;
            }
        }
    }

    private boolean switchVertices(BehaviourContext context, List<CanvasPoint[]> vertices, CanvasPoint centre) {
        boolean found = false;
        int octa;
        for (CanvasPoint[] vertex : vertices) {
            if (context.getRegion(vertex[0]).contains(listener.getTempX(), listener.getTempY())) {
                octa = R2.octant(vertex[1], centre);
                switch (octa) {
                    case 0:
                        drawarea.getScene().setCursor(Cursor.E_RESIZE);
                        found = true;
                        break;
                    case 1:
                        drawarea.getScene().setCursor(Cursor.NE_RESIZE);
                        found = true;
                        break;
                    case 2:
                        drawarea.getScene().setCursor(Cursor.N_RESIZE);
                        found = true;
                        break;
                    case 3:
                        drawarea.getScene().setCursor(Cursor.NW_RESIZE);
                        found = true;
                        break;
                    case 4:
                        drawarea.getScene().setCursor(Cursor.W_RESIZE);
                        found = true;
                        break;
                    case 5:
                        drawarea.getScene().setCursor(Cursor.SW_RESIZE);
                        found = true;
                        break;
                    case 6:
                        drawarea.getScene().setCursor(Cursor.S_RESIZE);
                        found = true;
                        break;
                    case 7:
                        drawarea.getScene().setCursor(Cursor.SE_RESIZE);
                        found = true;
                        break;
                }
            }
            if (found) {
                break;
            }
        }
        return found;
    }

}
