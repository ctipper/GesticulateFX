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
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ctipper
 */

public class TextFormatter {

    private String text;
    private Document currentdom;
    private int offset;

    private static final Logger logger = LoggerFactory.getLogger(TextFormatter.class.getName());

    public static final int FONT_BOLD = 1;
    public static final int FONT_ITALIC = 2;
    public static final int FONT_UNDERLINED = 4;

    /** Creates a new instance of <code>TextFormatter</code> */
    public TextFormatter() {
        this.currentdom = null;
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
        for (Element child : currentdom.children()) {
            List<javafx.scene.text.Text> textlist = setFxFormattingAttributes(child, item);
            tf.getChildren().addAll(textlist);
        }
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
        for (Element child : currentdom.children()) {
            as = this.setFormattingAttributes(child, as);
        }
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
    public void readTextItem(Text item) {
        String content = normalizeText(item.getText());
        currentdom = Jsoup.parse(content, "", Parser.xmlParser());
        text = getFlattenedText(currentdom);
    }

    private String normalizeText(String content) {
        Pattern parpattern = Pattern.compile("(<p>)+(.*)(</p>)+", Pattern.DOTALL);
        Matcher matcher = parpattern.matcher(content);
        if (!matcher.find()) {
            // Decode any pre-existing entities before re-escaping
            content = Parser.unescapeEntities(content, false);
            content = content.replaceAll("&", "&amp;");
            content = content.replaceAll("<", "&lt;");
            content = content.replaceAll(">", "&gt;");
            content = "<p>" + content + "</p>";
        }
        return content;
    }

    private String getFlattenedText(Document doc) {
        return doc.wholeText();
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

    private List<javafx.scene.text.Text> setFxFormattingAttributes(Element element, Text item) {
        return setFxFormattingAttributesRecursive(element, item, 0);
    }

    private List<javafx.scene.text.Text> setFxFormattingAttributesRecursive(Element element, Text item, int fontstyle) {
        List<javafx.scene.text.Text> textlist = new ArrayList<>();
        for (Node node : element.childNodes()) {
            switch (node) {
                case TextNode textNode -> {
                    String nodeText = textNode.getWholeText();
                    javafx.scene.text.Text tt = new javafx.scene.text.Text(nodeText);
                    setFxFontAttributes(tt, item.getFont(), item.getSize(), fontstyle, item.getColor());
                    textlist.add(tt);
                }
                case Element el -> {
                    String tag = el.tagName().toLowerCase();
                    int newStyle = fontstyle;
                    switch (tag) {
                        case "b" -> newStyle ^= FONT_BOLD;
                        case "i" -> newStyle ^= FONT_ITALIC;
                        case "u" -> newStyle ^= FONT_UNDERLINED;
                        default -> {}
                    }
                    textlist.addAll(setFxFormattingAttributesRecursive(el, item, newStyle));
                }
                default -> {}
            }
        }
        return textlist;
    }

    private AttributedString setFormattingAttributes(Element element, AttributedString as) {
        for (Node node : element.childNodes()) {
            switch (node) {
                case TextNode textNode -> offset += textNode.getWholeText().length();
                case Element el -> {
                    int localLen = getLocalOffset(el);
                    if (localLen != 0) {
                        String tag = el.tagName().toLowerCase();
                        if (null != tag) switch (tag) {
                            case "b" -> as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, offset, offset + localLen);
                            case "i" -> as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, offset, offset + localLen);
                            case "u" -> as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, offset, offset + localLen);
                            default -> {}
                        }
                        as = setFormattingAttributes(el, as);
                    }
                }
                default -> {}
            }
        }
        return as;
    }

    private int getLocalOffset(Element element) {
        // Calculate length of Element text
        int localoffset = 0;
        for (Node node : element.childNodes()) {
            switch (node) {
                case TextNode textNode -> localoffset += textNode.getWholeText().length();
                case Element el -> localoffset += getLocalOffset(el);
                default -> {}
            }
        }
        return localoffset;
    }

}
