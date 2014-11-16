/*
 * SelectionHandler.java
 * 
 * Created on Oct 19, 2013 8:20:36 PM
 * 
 */
package net.perspective.draw.event;

import com.google.inject.Injector;
import java.util.List;
import javafx.scene.input.TouchPoint;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.ContainsType;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.geom.Figure;

/**
 *
 * @author ctipper
 */

public class SelectionHandler implements Handler {

    @Inject Injector injector;
    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private BehaviourContext context;
    
    public void upEvent() {
        context.resetContainment();
    }

    public void downEvent() {
        boolean found = false;
        List<Figure> drawings = view.getDrawings();
        context.setContainment(ContainsType.NONE);
        if (!drawings.isEmpty()) {
            int i = drawings.size() - 1;
            do {
                Figure figure = drawings.get(i);
                context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                if (drawarea.getStartTouches() == null) {
                    found = context.select(figure, i, drawarea.getStartX(), drawarea.getStartY());
                } else {
                    List<TouchPoint> starters = drawarea.getStartTouches();
                    for (TouchPoint starter : starters) {
                        found = context.select(figure, i, starter.getX(), starter.getY());
                    }
                }
                if (found) {
                    break;
                }
                i--;
            } while (i >= 0);
        }
        if (context.getContainment().getLast().equals(ContainsType.NONE)) {
            view.setSelected(-1);
        }
    }

    public void dragEvent() {
        if (view.getSelected() != -1) {
            double xinc = drawarea.getTempX() - drawarea.getStartX();
            double yinc = drawarea.getTempY() - drawarea.getStartY();
            Figure item = view.getDrawings().get(view.getSelected());
            if (drawarea.getStartTouches() == null) {
                context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                context.alter(item, xinc, yinc);
            } else {
                List<TouchPoint> starters = drawarea.getStartTouches();
                List<TouchPoint> tempers = drawarea.getTempTouches();
                context.setBehaviour(injector.getInstance(FigureItemBehaviour.class));
                for (int i=0; i < starters.size(); i++) {
                    if (i < tempers.size()) {
                        xinc = tempers.get(i).getX() - starters.get(i).getX();
                        yinc = tempers.get(i).getY() - starters.get(i).getY();
                        context.alter(item, xinc, yinc);
                    }
                }
            }
            view.updateCanvasItem(view.getSelected(), item);
            view.moveSelection(view.getSelected());
            drawarea.setStartX(drawarea.getTempX());
            drawarea.setStartY(drawarea.getTempY());
        }
    }
}
