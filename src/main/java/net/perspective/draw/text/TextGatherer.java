/**
 * TextGatherer.java
 * 
 * Created on 22 May 2026 08:16:06
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
package net.perspective.draw.text;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author ctipper
 */

public class TextGatherer {

    protected String textGatherer(String text) {
        // Strip BOM, direction-override, zero-width, and non-breaking characters
        String cleaned = text.replaceAll("[\uFEFF\u200B\u00A0\u202A\u202B\u202C\u202D\u202E\u2066\u2067\u2068\u2069]", "");

        // Normalise line endings, whitespace-only lines, leading/trailing whitespace
        cleaned = cleaned.replaceAll("\r\n|\r", "\n")
                         .replaceAll("(?m)^[ \t]+$", "")
                         .strip();

        // Collapse more than 2 consecutive blank lines to 2
        cleaned = cleaned.replaceAll("\n{4,}", "\n\n\n");

        if (cleaned.length() > 1100) {
            return null;
        }

        // Reflow any line exceeding 15 words into multiple lines
        List<String> reflowed = Arrays.stream(cleaned.split("\n", -1))
            .flatMap(line -> {
                if (line.isBlank()) {
                    return Stream.of(line);
                }
                String[] words = line.trim().split("\\s+");
                if (words.length <= 15) {
                    return Stream.of(line);
                }
                int chunks = (words.length + 14) / 15;
                return IntStream.range(0, chunks)
                    .mapToObj(i -> Arrays.stream(words, i * 15, Math.min((i + 1) * 15, words.length))
                        .collect(Collectors.joining(" ")));
            })
            .collect(Collectors.toList());

        if (reflowed.size() > 20) {
            return null;
        }
        return String.join("\n", reflowed);
    }

}
