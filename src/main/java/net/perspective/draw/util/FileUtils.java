/*
 *  FileUtils.java
 * 
 *  Created on Nov 8, 2012 8:31:03 PM
 * 
 */

/**
 * Copyright (c) 2023 Christopher Tipper
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

import java.io.File;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ctipper
 */

public class FileUtils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Ensure file has given file-extension
     * 
     * @param file the {@link java.io.File}
     * @param extension file extension
     * @return the {@link java.io.File}
     * 
     * @author ctipper
     */
    public static File cleanseFileName(File file, String extension) {
        String ext, fileName;
        File f;

        fileName = file.getName();

        // parsed regex is "\.(\w+)$"
        Pattern extPattern = Pattern.compile("\\.(\\w+)$");
        Matcher matcher = extPattern.matcher(file.getName());
        if (matcher.find()) {
            ext = matcher.group(1).toLowerCase(); // text only
        } else {
            ext = "blank";
        }

        if (!ext.equals("blank")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        fileName = fileName + "." + extension;

        f = new File(file.getParent(), fileName);
        return f;
    }

    /**
     * Returns a uniform image name with png extension
     * 
     * @param index the image index
     * @return an image name
     * 
     * @author ctipper
     */
    public static String getImageName(int index) {
        StringBuilder name = new StringBuilder();
        try (Formatter formatter = new Formatter(name)) {
            formatter.format("%04d", index);
            name.append(".png");
        }
        return name.insert(0, "Image").toString();
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     * 
     * @param path the image path
     * @return an {@link javax.swing.ImageIcon}
     */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.warn("Couldn't find file: " + path);
            return null;
        }
    }

}
