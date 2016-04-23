/**
 * WriteOutStreamer.java
 * 
 * Created on 23-Apr-2016 12:28:01
 * 
 */
package net.perspective.draw.workers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;
import javax.inject.Inject;
import net.perspective.draw.CanvasView;
import net.perspective.draw.geom.DrawItem;
import net.perspective.draw.serialise.BasicStrokePersistenceDelegate;
import net.perspective.draw.serialise.FigurePersistenceDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class WriteOutStreamer extends Task {

    @Inject private CanvasView view;
    private File file;

    private static final Logger logger = LoggerFactory.getLogger(WriteOutStreamer.class.getName());

    @Inject
    public WriteOutStreamer() {
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected Object call() throws Exception {
        logger.info("Save started...");
        return new Serialiser();
    }

    @Override
    public void done() {
        logger.info("Save completed.");
    }

    final class Serialiser {

        net.perspective.draw.serialise.XMLEncoder encoder;
        ZipOutputStream zos = null;
        BeanInfo figureInfo;
        
        Serialiser() {
            logger.info("Serialisation initialised.");
            try {
                this.make();
            } catch (IOException e) {
                logger.warn(e.getMessage());
            } finally {
                if (zos != null) {
                    try {
                        zos.close();
                    } catch (IOException e) {
                        logger.info(e.getMessage());
                    }
                }
            }
        }

        public void make() throws IOException {
            setBeanInfo();
            FileOutputStream fos = new FileOutputStream(file);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));

            ZipEntry entry = new ZipEntry("content/pictures.xml");
            zos.putNextEntry(entry);

            /**
             * Currently no images being stored
             */
 
            /** 
             * List<ImageItem> pictures = view.getImageItems();
             */

            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            /**
             * encoder.writeObject(pictures);
             */
            encoder.finished();
            zos.closeEntry();

            /**
             * Write out images
             */

            entry = new ZipEntry("content/canvas.xml");
            zos.putNextEntry(entry);

            List<DrawItem> drawings = view.getDrawings();
            encoder = new net.perspective.draw.serialise.XMLEncoder(zos);
            encoder.setPersistenceDelegate(java.awt.BasicStroke.class,
                new BasicStrokePersistenceDelegate());
            encoder.setPersistenceDelegate(net.perspective.draw.geom.Figure.class,
                new FigurePersistenceDelegate());
            encoder.setExceptionListener((Exception ex) -> {
                logger.warn(ex.getMessage());
            });
            
            encoder.writeObject(drawings);
            encoder.finished();
            zos.closeEntry();
        }

        public void setBeanInfo() {
            try {
                figureInfo = Introspector.getBeanInfo(net.perspective.draw.geom.Figure.class);
            } catch (IntrospectionException e) {
            }
            for (PropertyDescriptor pd : figureInfo.getPropertyDescriptors()) {
                if (pd.getName().equals("path")) {
                        pd.setValue("transient", Boolean.TRUE);
                }
            }
        }
    }

}
