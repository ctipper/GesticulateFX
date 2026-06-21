/*
 * Messages.java
 *
 * Created on 9 Jun 2026
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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central accessor for localised UI strings.
 *
 * <p>Strings live in {@code net/perspective/draw/resources/ui.properties}
 * (the en-GB base) and locale variants such as {@code ui_de.properties} and
 * {@code ui_pt_BR.properties}. Property files are read as UTF-8 (Java 9+), so
 * translations may contain non-ASCII characters directly.</p>
 *
 * <p>The locale is taken from the environment as the sole authority: German,
 * French, Spanish, Polish and Brazilian/European Portuguese load their
 * bundle, every other locale resolves to the en-GB base. The same
 * {@link ResourceBundle} is
 * fed to the {@code FXMLLoader} (for {@code %key} references in the FXML) and
 * used here for programmatic strings (status messages, the about box).</p>
 *
 * @author ctipper
 */

public final class Messages {

    private static final String BUNDLE_NAME = "net.perspective.draw.resources.ui";

    private static final Logger logger = LoggerFactory.getLogger(Messages.class.getName());

    private static final Locale LOCALE = selectLocale();
    private static final ResourceBundle bundle;

    static {
        // align programmatic formatting (numbers, dates) with the chosen locale
        Locale.setDefault(LOCALE);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, LOCALE);
    }

    private Messages() {
    }

    /**
     * Resolve the UI locale from the environment over the supported set.
     * German, French, Spanish and Polish map to the language; Portuguese is split
     * into Brazilian ({@code pt_BR}) and European ({@code pt_PT}, the default
     * for any non-Brazilian Portuguese); anything else falls back to en-GB.
     *
     * @return the selected locale
     */
    private static Locale selectLocale() {
        Locale def = Locale.getDefault();
        return switch (def.getLanguage()) {
            case "de", "fr", "es", "pl" -> Locale.of(def.getLanguage());
            case "pt" -> "BR".equals(def.getCountry()) ? Locale.of("pt", "BR") : Locale.of("pt", "PT");
            default -> Locale.UK;
        };
    }

    /**
     * The shared resource bundle for the selected locale.
     *
     * @return the bundle
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Look up a localised string.
     *
     * @param key  the bundle key
     * @return the localised string, or {@code !key!} if the key is missing
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            logger.warn("Missing UI string for key: {}", key);
            return '!' + key + '!';
        }
    }

    /**
     * Look up a localised string and substitute arguments using
     * {@link MessageFormat}.
     *
     * @param key   the bundle key
     * @param args  arguments to substitute into the pattern
     * @return the formatted, localised string
     */
    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

}
