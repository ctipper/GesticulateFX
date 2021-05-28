/**
 * TextFormatter.java
 *
 * Created on 26 May 2021 15:38:48
 *
 */
package net.perspective.draw.geom;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.StringReader;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class TextFormatter {

    private String text;
    private org.jdom2.Document currentdom;
    private SAXBuilder builder;
    private int offset;

    private static final Logger logger = LoggerFactory.getLogger(TextFormatter.class.getName());

    /** Creates a new instance of <code>TextFormatter</code> */
    public TextFormatter() {
        this.initbuilder();
        this.currentdom = new org.jdom2.Document();
    }

    private void initbuilder() {
        // Build the text field with SAX and Xerces, with no validation.
        builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        builder.setReuseParser(true);
        builder.setIgnoringBoundaryWhitespace(false);
        builder.setIgnoringElementContentWhitespace(false);
    }

    /**
     * Return formatted text to client
     *
     * @param item A Text item
     * @return An javafx.scene.text.Text
     */
    public javafx.scene.text.TextFlow readFxFormattedText(Text item) {
        // TODO handle javafx.scene.text.TextFlow
        // use raw text from DOM
        this.readTextItem(item);
        javafx.scene.text.TextFlow tf = new javafx.scene.text.TextFlow(new javafx.scene.text.Text(text));
        // Parse Text font attributes and apply
//        this.setFxFontAttributes(item, tf);
        offset = 0;
        // Set formatting attributes
        // tf = this.setFxStyleFormattingAttributes(currentdom.getContent(), tt);
        return tf;
    }

    /**
     * Return formatted text to client
     *
     * @param item A Text item
     * @return An AttributedString
     */
    public AttributedString readFormattedText(Text item) {
        // use raw text from DOM
        this.readTextItem(item);
        AttributedString as = new AttributedString(text);
        // Parse Text font attributes and apply
        this.setFontAttributes(item, as);
        offset = 0;
        // Set formatting attributes
        as = this.setFormattingAttributes(currentdom.getContent(), as);
        return as;
    }

    /**
     * Load Text content into formatter
     *
     * @param item A Text item
     */
    public javafx.scene.text.Text readFxText(Text item) {
        this.readTextItem(item);
        javafx.scene.text.Text tt = new javafx.scene.text.Text(text);
        tt = getFxFontAttributes(tt, item.getFont(), item.getSize(), item.getStyle(), item.getColor());
        return tt;
    }

    /**
     * Load Text content into formatter
     *
     * @param item A Text item
     */
    private void readTextItem(Text item) {
        String content = normalizeText(item.getText());
        StringReader r = new StringReader(content);
        try {
            currentdom = builder.build(r);
        } catch (JDOMException e) {
            logger.warn("Malformed input.");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        text = this.getFlattenedText(currentdom.getContent());
    }

    private String normalizeText(String content) {
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(content);
        if (!matcher.find()) {
            content = content.replaceAll("&", "&amp;");
            content = content.replaceAll("<", "&lt;");
            content = content.replaceAll(">", "&gt;");
            content = "<p>" + content + "</p>";
        }
        return content;
    }

    private String getFlattenedText(List fragment) {
        String cdata = "";
        Iterator iterator = fragment.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof org.jdom2.Text) {
                cdata += ((org.jdom2.Text) o).getText();
            } else if (o instanceof Element) {
                cdata += getFlattenedText(((Element) o).getContent());
            }
        }
        return cdata;
    }

    private javafx.scene.text.Text getFxFontAttributes(javafx.scene.text.Text tt, String fontfamily, double size, int fontstyle, javafx.scene.paint.Color color) {
        javafx.scene.text.Font f;
        // Serif is the default
        switch (fontfamily) {
            case "SansSerif":
                f = javafx.scene.text.Font.font("Arial",
                        ((fontstyle & java.awt.Font.BOLD) == java.awt.Font.BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & java.awt.Font.ITALIC) == java.awt.Font.ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                tt.setFont(f);
                tt.setFill(color);
                break;
            case "Monospaced":
                f = javafx.scene.text.Font.font("monospace",
                        ((fontstyle & java.awt.Font.BOLD) == java.awt.Font.BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & java.awt.Font.ITALIC) == java.awt.Font.ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                tt.setFont(f);
                tt.setFill(color);
                break;
            case "Serif":
            default:
                f = javafx.scene.text.Font.font("Times New Roman",
                        ((fontstyle & java.awt.Font.BOLD) == java.awt.Font.BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & java.awt.Font.ITALIC) == java.awt.Font.ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                tt.setFont(f);
                tt.setFill(color);
                break;
        }
        return tt;
    }

    private void setFontAttributes(Text item, AttributedString as) {
        // Serif is the default
        String font = item.getFont();
        switch (font) {
            case "SansSerif":
                as.addAttribute(TextAttribute.FAMILY, java.awt.Font.SANS_SERIF);
                break;
            case "Monospaced":
                as.addAttribute(TextAttribute.FAMILY, java.awt.Font.MONOSPACED);
                break;
            default:
                as.addAttribute(TextAttribute.FAMILY, java.awt.Font.SERIF);
                break;
        }
        as.addAttribute(TextAttribute.SIZE, item.getSize());
        as.addAttribute(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    private AttributedString setFormattingAttributes(List fragment, AttributedString as) {
        Iterator iterator = fragment.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof org.jdom2.Text) {
                offset += ((org.jdom2.Text) element).getText().length();
            } else if (element instanceof Element) {
                if (getLocalOffset(((Element) element).getContent()) != 0) {
                    if (((Element) element).getName().equalsIgnoreCase("b")) {
                        as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, offset,
                                offset + getLocalOffset(((Element) element).getContent()));
                    } else if (((Element) element).getName().equalsIgnoreCase("i")) {
                        as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, offset,
                                offset + getLocalOffset(((Element) element).getContent()));
                    } else if (((Element) element).getName().equalsIgnoreCase("u")) {
                        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, offset,
                                offset + getLocalOffset(((Element) element).getContent()));
                    }
                    as = setFormattingAttributes(((Element) element).getContent(), as);
                }
            }
        }
        return as;
    }

    private int getLocalOffset(List fragment) {
        // Calculate length of Element text
        int localoffset = 0;
        Iterator iterator = fragment.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof org.jdom2.Text) {
                localoffset += ((org.jdom2.Text) o).getText().length();
            } else if (o instanceof Element) {
                localoffset += getLocalOffset(((Element) o).getContent());
            }
        }
        return localoffset;
    }

}
