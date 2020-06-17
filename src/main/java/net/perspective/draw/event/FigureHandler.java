/*
 * FigureHandler.java
 * 
 * Created on Oct 19, 2013 8:24:51 PM
 * 
 */
package net.perspective.draw.event;

import javafx.scene.paint.Color;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.DrawingArea;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.geom.ArrowLine;
import net.perspective.draw.geom.ArrowType;
import net.perspective.draw.geom.Figure;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureFactoryImpl;

/**
 * 
 * @author ctipper
 */

public class FigureHandler implements Handler {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    private final FigureFactory figurefactory;

    public FigureHandler() {
        this.figurefactory = new FigureFactoryImpl();
    }

    @Override
    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem() == null || drawarea.getDrawType() == null) {
            return;
        }
        // add figure to canvas
        Figure item = (Figure) view.getNewItem();
        item.setEndPoints();
        item.updateProperties(drawarea);
        view.setNewItem(item);
        view.resetNewItem();
    }

    @Override
    public void downEvent() {
    }

    @Override
    public void hoverEvent() {
    }

    @Override
    public void dragEvent() {
        DrawingType drawType = drawarea.getDrawType();
        // Create figure
        Figure item = figurefactory.createFigure(drawType);
        // Initialise stroke and fill
        item.setStroke(drawarea.getPlainStroke());
        item.setColor(Color.web(drawarea.getThemeFillColor()));
        item.setFillColor(Color.web(drawarea.getThemeBackgroundColor()));
        // Initialise figure
        item.setStart(listener.getStartX(), listener.getStartY());
        item.setEnd(listener.getTempX(), listener.getTempY());
        item.setPoints(drawType);
        item.setPath();
        if (drawType == DrawingType.LINE || drawType == DrawingType.HORIZONTAL || drawType == DrawingType.VERTICAL) {
            // Create arrow line
            if (drawarea.getArrow().equals(ArrowType.END) || drawarea.getArrow().equals(ArrowType.BOTH)) {
                ArrowLine arrow = new ArrowLine(item);
                arrow.setArrowStroke(drawarea.getStroke());
                arrow.setArrowType(drawarea.getArrow());
                arrow.setEndPoints();
                item = arrow;
            }
        }
        view.setNewItem(item);
        view.setDrawing(true);
    }

}
