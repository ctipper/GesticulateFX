/**
 * TextFormatter.java
 *
 * Created on 26 May 2021 15:38:48
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
package net.perspective.draw.geom;

import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public static final int FONT_BOLD = 1;
    public static final int FONT_ITALIC = 2;
    public static final int FONT_UNDERLINED = 4;

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
     * @return the {@link javafx.scene.text.Text}
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
     * @param item A {@link net.perspective.draw.geom.Text} item
     */
    private void readTextItem(Text item) {
        String content = normalizeText(item.getText());
        /**
         * A naive implementation would use a StringReader jdom api has a
         * warning about file encodings use an InputStream
         *
         * @see
         * <a href="https://www.jdom.org/docs/apidocs/org/jdom2/input/SAXBuilder.html#build-java.io.Reader-">https://www.jdom.org/docs/apidocs/</a>
         */
        ByteArrayInputStream r = new ByteArrayInputStream(content.getBytes());
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

    private String getFlattenedText(List<Content> fragment) {
        String cdata = "";
        Iterator iterator = fragment.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof org.jdom2.Text text1) {
                cdata += text1.getText();
            } else if (o instanceof Element element) {
                cdata += getFlattenedText(element.getContent());
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
            case "SansSerif" -> as.addAttribute(TextAttribute.FAMILY, java.awt.Font.SANS_SERIF);
            case "Monospaced" -> as.addAttribute(TextAttribute.FAMILY, java.awt.Font.MONOSPACED);
            default -> as.addAttribute(TextAttribute.FAMILY, java.awt.Font.SERIF);
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
                if (c instanceof org.jdom2.Text text1) {
                    localoffset += text1.getText().length();
                    if ((offset <= start) && (localoffset >= end)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    } else if ((offset <= start) && (localoffset < end) && (localoffset >= start)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    } else if ((offset <= start) && (localoffset < end) && (localoffset < start)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    } else if ((offset > start) && (localoffset >= end) && (offset >= end)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    } else if ((offset > start) && (localoffset >= end) && (offset < end)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    } else if ((offset > start) && (localoffset < end) && (offset < end)) {
                        tt = new javafx.scene.text.Text(text1.getText());
                    }
                    setFxFontAttributes(tt, item.getFont(), item.getSize(), fontstyle, item.getColor());
                    textlist.add(tt);
                    i++;
                } else if (c instanceof Element element) {
                    Element e = element.clone();
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
            if (c instanceof Element element) {
                indices.push(i);
                liststack.push(list);
                newstack.push(newlist);
                list = element.getContent();
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
            if (element instanceof org.jdom2.Text text1) {
                offset += text1.getText().length();
            } else if (element instanceof Element element1) {
                if (getLocalOffset(element1.getContent()) != 0) {
                    if (element1.getName().equalsIgnoreCase("b")) {
                        as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, offset,
                                offset + getLocalOffset(element1.getContent()));
                    } else if (element1.getName().equalsIgnoreCase("i")) {
                        as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, offset,
                                offset + getLocalOffset(element1.getContent()));
                    } else if (element1.getName().equalsIgnoreCase("u")) {
                        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, offset,
                                offset + getLocalOffset(element1.getContent()));
                    }
                    as = setFormattingAttributes(element1.getContent(), as);
                }
            }
        }
        return as;
    }

    private int getLocalOffset(List<Content> fragment) {
        // Calculate length of Element text
        int localoffset = 0;
        Iterator iterator = fragment.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof org.jdom2.Text text1) {
                localoffset += text1.getText().length();
            } else if (o instanceof Element element) {
                localoffset += getLocalOffset(element.getContent());
            }
        }
        return localoffset;
    }

}
