/*
 * DrawingArea.java
 * 
 * Created on Oct 20, 2013 10:56:32 AM
 * 
 */
package net.perspective.draw;

import com.google.inject.Injector;
import java.awt.BasicStroke;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.perspective.draw.enums.DrawingType;
import net.perspective.draw.enums.HandlerType;
import net.perspective.draw.event.*;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.geom.ArrowType;

import static net.perspective.draw.CanvasTransferHandler.COPY;
import static net.perspective.draw.CanvasTransferHandler.MOVE;

/**
 * 
 * @author ctipper
 */

@Singleton
public class DrawingArea {

    @Inject
    private Injector injector;
    @Inject
    private CanvasView view;
    @Inject
    private ApplicationController controller;
    @Inject
    private DrawAreaListener listener;
    @Inject
    private CanvasTransferHandler transferhandler;
    private SubScene canvas;
    private Group root;

    private DrawingType drawtype;
    private java.awt.Stroke stroke, plainstroke;
    private Color color, fillcolor;
    private int transparency;
    private ArrowType arrowtype;
    private DrawItem marquee;
    private boolean multiSelectEnabled;
    private Transferable clipboard;

    private ContextMenu contextmenu;
    private EventHandler<ContextMenuEvent> contextlistener;
    private EventHandler<TouchEvent> popuplistener;
    private EventHandler<InputEvent> arealistener;

    java.util.List<String> strokeStrings = Arrays.asList("stroke1", "stroke2", "stroke3", "stroke4",
            "stroke5", "stroke6", "stroke7", "stroke8", "stroke9");
    java.util.List<Float> strokeTypes = Arrays.asList(1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f, 10.0f);
    java.util.List<java.util.List<Float>> dashes1 = Arrays.asList(
            Arrays.asList(1.5f, 1.5f), Arrays.asList(1.5f, 1.5f), Arrays.asList(2.0f, 2.0f),
            Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f), Arrays.asList(5.0f, 5.0f),
            Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f), Arrays.asList(10.0f, 10.0f));
    java.util.List<java.util.List<Float>> dashes2 = Arrays.asList(
            Arrays.asList(2.0f, 2.0f), Arrays.asList(3.0f, 3.0f), Arrays.asList(4.0f, 4.0f),
            Arrays.asList(5.0f, 5.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f));
    java.util.List<java.util.List<Float>> dashes3 = Arrays.asList(
            Arrays.asList(4.0f, 4.0f), Arrays.asList(6.0f, 6.0f), Arrays.asList(8.0f, 8.0f),
            Arrays.asList(10.0f, 10.0f), Arrays.asList(12.0f, 12.0f), Arrays.asList(14.0f, 14.0f),
            Arrays.asList(16.0f, 16.0f), Arrays.asList(18.0f, 18.0f), Arrays.asList(20.0f, 20.0f));
    java.util.List<java.util.List<Float>> dashes4 = Arrays.asList(Arrays.asList(3.0f, 2.0f, 2.0f, 2.0f),
            Arrays.asList(5.0f, 3.0f, 3.0f, 3.0f),
            Arrays.asList(8.0f, 4.0f, 4.0f, 4.0f),
            Arrays.asList(10.0f, 5.0f, 5.0f, 5.0f),
            Arrays.asList(12.0f, 6.0f, 6.0f, 6.0f),
            Arrays.asList(16.0f, 8.0f, 8.0f, 8.0f),
            Arrays.asList(18.0f, 10.0f, 10.0f, 10.0f),
            Arrays.asList(24.0f, 12.0f, 12.0f, 12.0f),
            Arrays.asList(28.0f, 14.0f, 14.0f, 14.0f));

    /**
     * Creates a new instance of <code>DrawingArea</code>
     */
    @Inject
    public DrawingArea() {
    }

    void init(double width, double height) {
        root = new Group();
        canvas = new SubScene(root, width, height);
        canvas.setFill(Color.web(controller.getThemeBackgroundColor()));
        contextmenu = new ContextMenu();
        contextlistener = null;
        view.setDrawingListener();
        this.prepareDrawing();
        this.setDrawType(DrawingType.SKETCH);
        this.arrowtype = ArrowType.NONE;
        listener.initializeHandlers(canvas);
        this.addContextMenu();
        this.changeHandlers(HandlerType.SELECTION);
        controller.getStrokeTypeProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            Integer strokeId = strokeStrings.indexOf(newValue);
            String strokeStyle = controller.getStrokeStyleProperty().getValue();
            setStrokeType(strokeId, strokeStyle);
        });
        controller.getStrokeStyleProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String strokeType = controller.getStrokeTypeProperty().getValue();
            Integer strokeId = strokeStrings.indexOf(strokeType);
            setStrokeType(strokeId, newValue);
        });
        controller.getColorProperty().addListener((ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
            setColor(newValue);
        });
        controller.getFillColorProperty().addListener((ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
            setFillColor(newValue);
        });
    }

    public void prepareDrawing() {
        Integer strokeId = strokeStrings.indexOf(controller.getStrokeTypeProperty().getValue());
        String strokeStyle = controller.getStrokeStyleProperty().getValue();
        setStrokeType(strokeId, strokeStyle);
        this.color = controller.getColorProperty().getValue();
        this.fillcolor = controller.getFillColorProperty().getValue();
        this.transparency = controller.getWireframeWhen().then(0).otherwise(100).intValue();
        view.clearView();
        this.clear();
    }

    public void setTheme(){
        canvas.setFill(Color.web(controller.getThemeBackgroundColor()));
        view.setSelected(-1);
    }

    public String getThemeFillColor() {
        return controller.getThemeFillColor();
    }

    public String getThemeBackgroundColor() {
        return controller.getThemeBackgroundColor();
    }

    public String getThemeAccentColor() {
        return controller.getThemeAccentColor();
    }

    public void clear() {
        ((Group) canvas.getRoot()).getChildren().clear();
    }

    public void changeHandlers(HandlerType h) {
        this.resetContextHandlers();
        switch (h) {
            case SELECTION:
                listener.setEventHandler(injector.getInstance(SelectionHandler.class));
                this.setContextHandlers();
                break;
            case FIGURE:
                listener.setEventHandler(injector.getInstance(FigureHandler.class));
                break;
            case ROTATION:
                listener.setEventHandler(injector.getInstance(RotationHandler.class));
                this.setContextHandlers();
                break;
            case SKETCH:
                listener.setEventHandler(injector.getInstance(SketchHandler.class));
                break;
            default:
                break;
        }
        view.setSelected(-1);
        view.setDrawing(false);
    }

    public void resetContextHandlers() {
        canvas.setOnContextMenuRequested(null);
        canvas.setOnTouchStationary(null);
        canvas.setOnMousePressed(null);
        canvas.setOnTouchPressed(null);
    }

    public void setContextHandlers() {
        canvas.setOnContextMenuRequested(contextlistener);
        canvas.setOnTouchStationary(popuplistener);
        canvas.setOnMousePressed(arealistener);
        canvas.setOnTouchPressed(arealistener);
    }

    public void addContextMenu() {
        MenuItem menuCut = new MenuItem("Cut");
        menuCut.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                clipboard = transferhandler.createTransferable();
                transferhandler.exportDone(clipboard, MOVE);
                view.setSelected(-1);
            }
        });
        MenuItem menuCopy = new MenuItem("Copy");
        menuCopy.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                clipboard = transferhandler.createTransferable();
                transferhandler.exportDone(clipboard, COPY);
            }
        });
        MenuItem menuPaste = new MenuItem("Paste");
        menuPaste.setOnAction((ActionEvent e) -> {
            if (clipboard != null) {
                transferhandler.importData(clipboard);
                view.setSelected(-1);
            }
        });
        contextmenu.getItems().addAll(menuCut, menuCopy, menuPaste);
        SeparatorMenuItem groupSeparator = new SeparatorMenuItem();
        MenuItem menuGroup = new MenuItem("Group Selection");
        menuGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.groupSelection();
            }
        });
        MenuItem menuUnGroup = new MenuItem("Ungroup Selection");
        menuUnGroup.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.ungroupSelection();
            }
        });
        contextmenu.getItems().addAll(groupSeparator, menuGroup, menuUnGroup);
        SeparatorMenuItem sendSeparator = new SeparatorMenuItem();
        MenuItem menuSBItem = new MenuItem("Send Backwards");
        menuSBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.sendBackwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuBFItem = new MenuItem("Bring Forwards");
        menuBFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.bringForwards();
                view.setSelected(-1);
            }
        });
        MenuItem menuSTBItem = new MenuItem("Send to Back");
        menuSTBItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.sendToBack();
                view.setSelected(-1);
            }
        });
        MenuItem menuBTFItem = new MenuItem("Bring to Front");
        menuBTFItem.setOnAction((ActionEvent e) -> {
            if (view.getSelected() != -1) {
                view.bringToFront();
                view.setSelected(-1);
            }
        });
        contextmenu.getItems().addAll(sendSeparator, menuSBItem, menuBFItem, menuSTBItem, menuBTFItem);
        contextlistener = (ContextMenuEvent event) -> {
            contextmenu.show(canvas, event.getScreenX(), event.getScreenY());
        };
        popuplistener = (TouchEvent event) -> {
            TouchPoint touch = event.getTouchPoints().get(0);
            contextmenu.show(canvas, touch.getScreenX(), touch.getScreenY());
        };
        arealistener = (InputEvent event) -> {
            if (contextmenu.isShowing()) {
                contextmenu.hide();
            }
        };
    }

    private void setStrokeType(Integer strokeId, String strokeStyle) {
        switch (strokeStyle) {
            case "style1": // Plain stroke
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                resetArrow();
                break;
            case "style2":
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f));
                resetArrow();
                break;
            case "style3":
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes2.get(strokeId)), 0.0f));
                resetArrow();
                break;
            case "style4":
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes3.get(strokeId)), 0.0f));
                resetArrow();
                break;
            case "style5":
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes4.get(strokeId)), 0.0f));
                resetArrow();
                break;
            case "style6": // Arrow at start
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                setArrow(ArrowType.END);
                break;
            case "style7": // Arrow at start
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f));
                setArrow(ArrowType.END);
                break;
            case "style8": // Arrow at both ends
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                setArrow(ArrowType.BOTH);
                break;
            case "style9": // Arrow at both ends
                setStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, getDashes(dashes1.get(strokeId)), 0.0f));
                setArrow(ArrowType.BOTH);
                break;
            default:
                resetArrow();
        }
        setPlainStroke(new BasicStroke(strokeTypes.get(strokeId), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private float[] getDashes(java.util.List<Float> items) {
        float[] value;
        value = new float[4];

        int i = 0;
        for (Float item : items) {
            value[i] = item;
            i++;
        }
        return value;
    }

    public SubScene getScene() {
        return canvas;
    }

    public Group getCanvas() {
        return root;
    }

    public CanvasView getView() {
        return view;
    }

    public void setDrawType(DrawingType s) {
        drawtype = s;
    }

    public DrawingType getDrawType() {
        return drawtype;
    }

    public void setStroke(java.awt.Stroke stroke) {
        this.stroke = stroke;
        view.updateSelectedItem();
    }

    public java.awt.Stroke getStroke() {
        return this.stroke;
    }

    public void setPlainStroke(java.awt.Stroke stroke) {
        this.plainstroke = stroke;
        view.updateSelectedItem();
    }

    public java.awt.Stroke getPlainStroke() {
        return plainstroke;
    }

    public void setColor(Color color) {
        this.color = color;
        view.updateSelectedItem();
    }

    public Color getColor() {
        return this.color;
    }

    public void setFillColor(Color fillcolor) {
        this.fillcolor = fillcolor;
        view.updateSelectedItem();
    }

    public Color getFillColor() {
        return this.fillcolor;
    }

    public void setTransparency(int transparency) {
        this.transparency = transparency;
        view.updateSelectedItem();
    }

    public int getTransparency() {
        return this.transparency;
    }

    public void setArrow(ArrowType arrowtype) {
        this.arrowtype = arrowtype;
        view.updateSelectedItem();
    }

    public ArrowType getArrow() {
        return arrowtype;
    }

    public void resetArrow() {
        arrowtype = ArrowType.NONE;
        view.updateSelectedItem();
    }

    public void setMarquee(DrawItem marquee) {
        this.marquee = marquee;
    }

    public DrawItem getMarquee() {
        return marquee;
    }

    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        this.multiSelectEnabled = multiSelectEnabled;
    }

    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

}
