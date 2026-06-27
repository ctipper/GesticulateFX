/**
 * SVGRead.java
 *
 * Created on Jun 26, 2026
 *
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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
package net.perspective.draw.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transcodes SVG documents to {@link BufferedImage}s.
 *
 * @author ctipper
 */

public class SVGRead {

    /** Lower bound for the longest side when sizing from a viewBox, so small documents stay legible. */
    private static final float MIN_VIEWBOX_DIMENSION = 512f;

    private static final Logger logger = LoggerFactory.getLogger(SVGRead.class.getName());

    public SVGRead() {
    }

    /**
     * Transcode an SVG file.
     * <p>
     * Documents that declare an explicit {@code width} and {@code height} are rendered at
     * their intrinsic size. Documents that declare only a {@code viewBox} are sized from it,
     * preserving aspect ratio &mdash; otherwise Batik falls back to a square default that
     * distorts the picture's bounds.
     *
     * @see <a href="https://stackoverflow.com/a/11436655">How to get a BufferedImage from a SVG?</a>
     *
     * @param svgFile the file
     * @return a buffered image
     * @throws IOException
     */
    public BufferedImage rasterize(File svgFile) throws IOException {
        float[] box = viewBoxSize(svgFile);
        float width = box != null ? box[0] : 0f;
        float height = box != null ? box[1] : 0f;
        try (InputStream file = new FileInputStream(svgFile)) {
            // Files may be user-supplied (opened, dropped, or pasted from the clipboard),
            // so treat them as untrusted and forbid external resource resolution.
            return rasterize(new TranscoderInput(file), null, width, height, svgFile.getName(), false);
        }
    }

    /**
     * Transcode an SVG document bundled as a classpath resource under {@code /svg/},
     * overriding its fill colour.
     *
     * @param filename the resource name
     * @param fillColor the fill colour to apply
     * @param targetSize the size in pixels of the square box to render into (content aspect
     *     ratio is preserved, letterboxed)
     * @return a buffered image
     * @throws IOException
     */
    public BufferedImage rasterizeResource(String filename, String fillColor, int targetSize) throws IOException {
        try (InputStream file = getClass().getResourceAsStream("/svg/" + filename)) {
            // A square box suits the icon glyphs: many declare only a viewBox (no width/height),
            // and Batik would otherwise leave its default height in place, distorting the result.
            // Bundled resources are trusted, so external resource resolution stays enabled.
            return rasterize(new TranscoderInput(file), fillColor, targetSize, targetSize, filename, true);
        }
    }

    /**
     * Transcode an SVG document.
     *
     * @param input the transcoder input
     * @param fillColor the fill colour to apply, or {@code null} to leave the document defaults
     * @param targetWidth the output width in pixels, or {@code 0} to leave Batik's default
     * @param targetHeight the output height in pixels, or {@code 0} to leave Batik's default
     * @param name an identifier used for logging
     * @param allowExternalResources whether the document may resolve external resources;
     *     pass {@code false} for untrusted input to prevent it referencing local files or
     *     remote URLs (SSRF / local-file disclosure)
     * @return a buffered image
     * @throws IOException
     */
    private BufferedImage rasterize(TranscoderInput input, String fillColor, float targetWidth, float targetHeight, String name, boolean allowExternalResources) throws IOException {

        final BufferedImage[] imagePointer = new BufferedImage[1];

        // Rendering hints can't be set programatically, so
        // we override defaults with a temporary stylesheet.
        // These defaults emphasize quality and precision, and
        // are more similar to the defaults of other SVG viewers.
        // SVG documents can still override these defaults.
        String css = "svg {"
                + "shape-rendering: geometricPrecision;"
                + "text-rendering:  geometricPrecision;"
                + "color-rendering: optimizeQuality;"
                + "image-rendering: optimizeQuality;"
                + (fillColor != null ? "fill: " + fillColor + ";" : "")
                + "}";
        Path cssFile = Files.createTempFile(Files.createTempDirectory("temp-dir"), "batik-default-override-", ".css");
        FileUtils.writeStringToFile(cssFile.toFile(), css);
        TranscodingHints transcoderHints = new TranscodingHints();
        transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                SVGConstants.SVG_NAMESPACE_URI);
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        transcoderHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toUri().toString());
        transcoderHints.put(SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, allowExternalResources);
        if (targetWidth > 0f) {
            transcoderHints.put(SVGAbstractTranscoder.KEY_WIDTH, targetWidth);
        }
        if (targetHeight > 0f) {
            transcoderHints.put(SVGAbstractTranscoder.KEY_HEIGHT, targetHeight);
        }

        try {
            ImageTranscoder t = new ImageTranscoder() {

                @Override
                public BufferedImage createImage(int width, int height) {
                    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) throws TranscoderException {
                    imagePointer[0] = image;
                }
            };
            t.setTranscodingHints(transcoderHints);
            t.transcode(input, null);
        } catch (TranscoderException ex) {
            logger.error("Couldn't convert {}", name);
        } finally {
            cssFile.toFile().deleteOnExit();
        }

        return imagePointer[0];
    }

    /**
     * Determine a render size from a document's {@code viewBox}, used only when it declares no
     * usable {@code width}/{@code height} (in which case Batik defaults to a square, distorting
     * the aspect ratio). The longest side is raised to {@link #MIN_VIEWBOX_DIMENSION} so small
     * documents remain legible; aspect ratio is preserved.
     *
     * @param svgFile the file
     * @return {@code {width, height}}, or {@code null} to use Batik's intrinsic sizing
     */
    private static float[] viewBoxSize(File svgFile) {
        try (InputStream in = new FileInputStream(svgFile)) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            XMLStreamReader reader = factory.createXMLStreamReader(in);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    // the root element of an SVG document
                    if (isAbsolute(reader.getAttributeValue(null, "width"))
                            && isAbsolute(reader.getAttributeValue(null, "height"))) {
                        return null;
                    }
                    String viewBox = reader.getAttributeValue(null, "viewBox");
                    if (viewBox != null) {
                        String[] parts = viewBox.trim().split("[\\s,]+");
                        if (parts.length == 4) {
                            float w = Float.parseFloat(parts[2]);
                            float h = Float.parseFloat(parts[3]);
                            if (w > 0f && h > 0f) {
                                float scale = Math.max(1f, MIN_VIEWBOX_DIMENSION / Math.max(w, h));
                                return new float[]{ w * scale, h * scale };
                            }
                        }
                    }
                    return null;
                }
            }
        } catch (XMLStreamException | IOException | IllegalArgumentException ex) {
            // IllegalArgumentException covers an unsupported StAX property and a malformed
            // viewBox number (NumberFormatException); either way we fall back to intrinsic sizing.
            logger.warn("Couldn't read SVG dimensions from {}", svgFile.getName());
        }
        return null;
    }

    /** A length attribute that resolves to a fixed size (i.e. present and not a percentage). */
    private static boolean isAbsolute(String dimension) {
        return dimension != null && !dimension.isBlank() && !dimension.trim().endsWith("%");
    }

}
