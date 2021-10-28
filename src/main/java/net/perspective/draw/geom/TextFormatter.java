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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Content;
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

    public static final int FONT_PLAIN = 1;
    public static final int FONT_BOLD = 2;
    public static final int FONT_ITALIC = 4;
    public static final int FONT_UNDERLINED = 8;

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
        // use raw text from DOM
        this.readTextItem(item);
        javafx.scene.text.TextFlow tf = new javafx.scene.text.TextFlow();
        offset = 0;
        // Parse Text font attributes and apply
        List<javafx.scene.text.Text> textlist = setFxFormattingAttributes(currentdom.getContent(), item);
        tf.getChildren().addAll(textlist);
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
        tt = setFxFontAttributes(tt, item.getFont(), item.getSize(), item.getStyle(), item.getColor());
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

    private javafx.scene.text.Text setFxFontAttributes(javafx.scene.text.Text tt, String fontfamily, double size, int fontstyle, javafx.scene.paint.Color color) {
        javafx.scene.text.Font f;
        // Serif is the default
        switch (fontfamily) {
            case "SansSerif":
                f = javafx.scene.text.Font.font("sansserif",
                        ((fontstyle & FONT_BOLD) == FONT_BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & FONT_ITALIC) == FONT_ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                if ((fontstyle & FONT_UNDERLINED) == FONT_UNDERLINED) {
                    tt.setUnderline(true);
                }
                tt.setFont(f);
                tt.setFill(color);
                tt.setSmooth(true);
                break;
            case "Monospaced":
                f = javafx.scene.text.Font.font("monospace",
                        ((fontstyle & FONT_BOLD) == FONT_BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & FONT_ITALIC) == FONT_ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                if ((fontstyle & FONT_UNDERLINED) == FONT_UNDERLINED) {
                    tt.setUnderline(true);
                }
                tt.setFont(f);
                tt.setFill(color);
                tt.setSmooth(true);
                break;
            case "Serif":
            default:
                f = javafx.scene.text.Font.font("serif",
                        ((fontstyle & FONT_BOLD) == FONT_BOLD
                                ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL),
                        ((fontstyle & FONT_ITALIC) == FONT_ITALIC
                                ? javafx.scene.text.FontPosture.ITALIC : javafx.scene.text.FontPosture.REGULAR),
                        size);
                if ((fontstyle & FONT_UNDERLINED) == FONT_UNDERLINED) {
                    tt.setUnderline(true);
                }
                tt.setFont(f);
                tt.setFill(color);
                tt.setSmooth(true);
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

    private List<javafx.scene.text.Text> setFxFormattingAttributes(List<Content> list, Text item) {
        javafx.scene.text.Text tt = new javafx.scene.text.Text();
        List<javafx.scene.text.Text> textlist = new ArrayList<>();
        int start = 0;
        int end = getFlattenedText(list).length();
        int fontstyle = 0;
        List<Content> newlist = new ArrayList<>();
        Stack<List<Content>> liststack = new Stack<>();
        Stack<List<Content>> newstack = new Stack<>();
        Stack<Integer> indices = new Stack<>();
        Stack<Element> elstack = new Stack<>();
        int localoffset = 0;
        boolean finished = false;
        int i = 0;
        Content c = list.get(0);
        while (!finished) {
            if (i < list.size()) {
                c = list.get(i);
                offset = localoffset;
                if (c instanceof org.jdom2.Text) {
                    localoffset += ((org.jdom2.Text) c).getText().length();
                    if ((offset <= start) && (localoffset >= end)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    } else if ((offset <= start) && (localoffset < end) && (localoffset >= start)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    } else if ((offset <= start) && (localoffset < end) && (localoffset < start)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    } else if ((offset > start) && (localoffset >= end) && (offset >= end)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    } else if ((offset > start) && (localoffset >= end) && (offset < end)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    } else if ((offset > start) && (localoffset < end) && (offset < end)) {
                        tt = new javafx.scene.text.Text(((org.jdom2.Text) c).getText());
                    }
                    setFxFontAttributes(tt, item.getFont(), item.getSize(), fontstyle, item.getColor());
                    textlist.add(tt);
                    i++;
                } else if (c instanceof Element) {
                    Element e = ((Element) c).clone();
                    if (e.getName().equalsIgnoreCase("b")) {
                        fontstyle ^= FONT_BOLD;
                    } else if (e.getName().equalsIgnoreCase("i")) {
                        fontstyle ^= FONT_ITALIC;
                    } else if (e.getName().equalsIgnoreCase("u")) {
                        fontstyle ^= FONT_UNDERLINED;
                    }
                    newlist.add(e);
                    elstack.push(e);
                    i++;
                }
            } else {
                if (!liststack.empty()) {
                    i = indices.pop();
                    list = liststack.pop();
                    Element e = elstack.pop();
                    if (e.getName().equalsIgnoreCase("b")) {
                        fontstyle ^= FONT_BOLD;
                    } else if (e.getName().equalsIgnoreCase("i")) {
                        fontstyle ^= FONT_ITALIC;
                    } else if (e.getName().equalsIgnoreCase("u")) {
                        fontstyle ^= FONT_UNDERLINED;
                    }
                    e.setContent(newlist);
                    newlist = newstack.pop();
                    continue;
                } else {
                    finished = true;
                }
            }
            if (c instanceof Element) {
                indices.push(i);
                liststack.push(list);
                newstack.push(newlist);
                list = ((Element) c).getContent();
                newlist = new ArrayList<>();
                i = 0;
            }
        }
        return textlist;
    }

    private AttributedString setFormattingAttributes(List<Content> fragment, AttributedString as) {
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
