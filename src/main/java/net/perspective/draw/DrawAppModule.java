/**
 * DrawAppModule.java
 *
 * Created on 20 Sept 2025 14:06:39
 *
 */

/**
 * Copyright (c) 2025 e-conomist
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw;

import javax.inject.Provider;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import net.perspective.draw.event.DrawAreaListener;
import net.perspective.draw.event.FigureHandler;
import net.perspective.draw.event.MapHandler;
import net.perspective.draw.event.RotationHandler;
import net.perspective.draw.event.SelectionHandler;
import net.perspective.draw.event.SketchHandler;
import net.perspective.draw.event.TextHandler;
import net.perspective.draw.event.behaviours.BehaviourContext;
import net.perspective.draw.event.behaviours.FigureItemBehaviour;
import net.perspective.draw.event.behaviours.GroupedItemBehaviour;
import net.perspective.draw.event.behaviours.MapItemBehaviour;
import net.perspective.draw.event.behaviours.PictureItemBehaviour;
import net.perspective.draw.event.behaviours.TextItemBehaviour;
import net.perspective.draw.event.keyboard.DummyKeyHandler;
import net.perspective.draw.event.keyboard.KeyListener;
import net.perspective.draw.event.keyboard.MapKeyHandler;
import net.perspective.draw.event.keyboard.MoveKeyHandler;
import net.perspective.draw.event.keyboard.TextKeyHandler;
import net.perspective.draw.geom.Picture;
import net.perspective.draw.geom.StreetMap;
import net.perspective.draw.util.G2;
import net.perspective.draw.workers.ImageLoadWorker;
import net.perspective.draw.workers.PDFWorker;
import net.perspective.draw.workers.PNGWorker;
import net.perspective.draw.workers.ReadInFunnel;
import net.perspective.draw.workers.SVGWorker;
import net.perspective.draw.workers.WriteOutStreamer;

/**
 *
 * @author ctipper
 */

@Module(subcomponents = {FxAppComponent.class})
public class DrawAppModule {

    @Provides
    @Singleton
    ApplicationController provideApplicationController(Provider<DrawingArea> drawingAreaProvider, 
            Provider<CanvasView> viewProvider, Provider<Gesticulate> applicationProvider,
            DrawAppComponent component) {
        ApplicationController applicationController = new ApplicationController(drawingAreaProvider, viewProvider, applicationProvider);
        component.inject(applicationController);
        return applicationController;
    }

    @Provides
    @Singleton
    DrawingArea provideDrawingArea(CanvasView view, ApplicationController controller, DrawAppComponent component) {
        DrawingArea drawingArea = new DrawingArea(view, controller);
        component.inject(drawingArea);
        return drawingArea;
    }

    @Provides
    @Singleton
    CanvasView provideCanvasView(Provider<DrawingArea> drawareaProvider,
    Provider<ApplicationController> controllerProvider,
    Provider<TextController> textControllerProvider,
    DrawAppComponent component) {
        CanvasView view = new CanvasView(drawareaProvider, controllerProvider, textControllerProvider);
        component.inject(view);
        return view;
        
    }

    @Provides
    @Singleton
    CanvasTransferHandler provideCanvasTransferHandler() {
        return new CanvasTransferHandler();
    }

    @Provides
    @Singleton
    Dropper provideDropper() {
        return new Dropper();
    }

    @Provides
    @Singleton
    DrawAreaListener provideDrawAreaListener() {
        return new DrawAreaListener();
    }

    @Provides
    @Singleton
    FigureHandler provideFigureHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        FigureHandler figureHandler = new FigureHandler(drawarea, view);
        component.inject(figureHandler);
        return figureHandler;
    }

    @Provides
    @Singleton
    SketchHandler provideSketchHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        SketchHandler sketchHandler = new SketchHandler(drawarea, view);
        component.inject(sketchHandler);
        return sketchHandler;
    }

    @Provides
    @Singleton
    RotationHandler provideRotationHandler(DrawingArea drawarea, CanvasView view) {
        return new RotationHandler(drawarea, view);
    }

    @Provides
    @Singleton
    SelectionHandler provideSelectionHandler(DrawingArea drawarea,
            CanvasView view, ApplicationController controller) {
        return new SelectionHandler(drawarea, view, controller);
    }

    @Provides
    @Singleton
    TextHandler provideTextHandler(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        TextHandler textHandler = new TextHandler(drawarea, view);
        component.inject(textHandler);
        return textHandler;
    }

    @Provides
    @Singleton
    MapHandler provideMapHandler() {
        return new MapHandler();
    }

    @Provides
    @Singleton
    KeyListener provideKeyListener() {
        return new KeyListener();
    }

    @Provides
    @Singleton
    DummyKeyHandler provideDummyKeyHandler() {
        return new DummyKeyHandler();
    }

    @Provides
    @Singleton
    MapKeyHandler provideMapKeyHandler(DrawingArea drawarea, CanvasView view) {
        return new MapKeyHandler(drawarea, view);
    }

    @Provides
    @Singleton
    MoveKeyHandler provideMoveKeyHandler(DrawingArea drawarea, CanvasView view) {
        return new MoveKeyHandler(drawarea, view);
    }

    @Provides
    @Singleton
    TextKeyHandler provideTextKeyHandler(DrawingArea drawarea, CanvasView view, 
            ApplicationController controller, DrawAppComponent component) {
        TextKeyHandler textKeyHandler = new TextKeyHandler(drawarea, view, controller);
        component.inject(textKeyHandler);
        return textKeyHandler;    }

    @Provides
    @Singleton
    BehaviourContext provideBehaviourContext() {
        return new BehaviourContext();
    }

    @Provides
    @Singleton
    FigureItemBehaviour provideFigureItemBehaviour() {
        return new FigureItemBehaviour();
    }

    @Provides
    @Singleton
    PictureItemBehaviour providePictureItemBehaviour() {
        return new PictureItemBehaviour();
    }

    @Provides
    @Singleton
    GroupedItemBehaviour provideGroupedItemBehaviour() {
        return new GroupedItemBehaviour();
    }

    @Provides
    @Singleton
    MapItemBehaviour provideMapItemBehaviour() {
        return new MapItemBehaviour();
    }

    @Provides
    @Singleton
    TextItemBehaviour provideTextItemBehaviour(DrawingArea drawarea, CanvasView view, DrawAppComponent component) {
        TextItemBehaviour textItemBehaviour = new TextItemBehaviour(drawarea, view);
        component.inject(textItemBehaviour);
        return textItemBehaviour;
    }

    @Provides
    @Singleton
    MapController provideMapController() {
        return new MapController();
    }

    @Provides
    @Singleton
    TextController provideTextController(DrawingArea drawarea, Provider<CanvasView> viewProvider,
            ApplicationController controller, DrawAppComponent component) {
        TextController textController = new TextController(drawarea, viewProvider, controller);
        component.inject(textController);
        return textController;
    }

    @Provides
    @Singleton
    ShareUtils provideShareUtils() {
        return new ShareUtils();
    }

    @Provides
    @Singleton
    ReadInFunnel provideReadInFunnel() {
        return new ReadInFunnel();
    }

    @Provides
    @Singleton
    WriteOutStreamer provideWriteOutStreamer() {
        return new WriteOutStreamer();
    }

    @Provides
    @Singleton
    ImageLoadWorker provideImageLoadWorker() {
        return new ImageLoadWorker();
    }

    @Provides
    @Singleton
    PDFWorker providePDFWorker() {
        return new PDFWorker();
    }

    @Provides
    @Singleton
    SVGWorker provideSVGWorker() {
        return new SVGWorker();
    }

    @Provides
    @Singleton
    PNGWorker providePNGWorker() {
        return new PNGWorker();
    }

    @Provides
    @Singleton
    G2 provideG2(Provider<DrawingArea> drawareaProvider, 
            Provider<ApplicationController> controllerProvider,
            Provider<TextController> textControllerProvider) {
        return new G2(drawareaProvider, controllerProvider, textControllerProvider);
    }

    @Provides
    Picture providePicture() {
        return new Picture();
    }

    @Provides
    StreetMap provideStreetMap() {
        return new StreetMap();
    }

}
