/*
 * SketchHandler.java
 * 
 * Created on Oct 27, 2013 10:22:09 PM
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
import net.perspective.draw.geom.FigureType;

/**
 * 
 * @author ctipper
 */

public class SketchHandler implements Handler  {

    @Inject private DrawingArea drawarea;
    @Inject private CanvasView view;
    @Inject private DrawAreaListener listener;
    private final FigureFactory figurefactory;

    public SketchHandler() {
        this.figurefactory = new FigureFactoryImpl();
    }

    @Override
    public void upEvent() {
        view.setDrawing(false);
        if (view.getNewItem().isEmpty() || drawarea.getDrawType().isEmpty()) {
            return;
        }
        // add figure to canvas
        Figure item = (Figure) view.getNewItem().get();
        item.setEndPoints();
        item.updateProperties(drawarea);
        view.setNewItem(item);
        view.resetNewItem();
    }

    @Override
    public void downEvent() {
        // Create figure
        Figure item = figurefactory.createFigure(drawarea.getDrawType().orElse(DrawingType.POINT));
        // Initialise stroke and fill
        item.setStroke(drawarea.getPlainStroke());
        item.setColor(Color.web(drawarea.getThemeFillColor()));
        item.setFillColor(Color.web(drawarea.getCanvasBackgroundColor()));
        // Initialise sketch
        item.setPoints(drawarea.getDrawType().orElse(DrawingType.POINT));
        item.addPoint(listener.getStartX(), listener.getStartY());
        item.setEndPoints();
    	item.setPath();
        view.setNewItem(item);
        view.setDrawing(true);
    }

    @Override
    public void clickEvent() {
    }

    @Override
    public void hoverEvent() {
    }

    @Override
    public void dragEvent() {
        // Create Figure
        Figure item = (Figure) view.getNewItem().orElse(null);
        if (item == null) return;
        if (!(item instanceof ArrowLine) && (drawarea.getArrow().equals(ArrowType.END) || drawarea.getArrow().equals(ArrowType.BOTH))) {
            // Create arrow line
            if (!((Figure) item).getType().equals(FigureType.POLYGON)) {
                ArrowLine arrow = new ArrowLine((Figure) item);
                arrow.setArrowStroke(drawarea.getStroke());
                arrow.setArrowType(drawarea.getArrow());
                arrow.setEndPoints();
                item = arrow;
            }
        }        // continue sketch
        item.addPoint(listener.getTempX(), listener.getTempY());
        item.setEndPoints();
        item.setPath();
        view.setNewItem(item);
    }

}
