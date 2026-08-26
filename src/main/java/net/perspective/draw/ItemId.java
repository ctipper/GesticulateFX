/*
 * ItemId.java
 * 
 * Created on 26 Aug 2026 08:35:02
 * 
 */

 /*
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
package net.perspective.draw;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author ctipper
 */

public record ItemId(UUID id) {

    public ItemId {
        Objects.requireNonNull(id, "id");
    }

    public static ItemId freshId() {
        return new ItemId(UUID.randomUUID());
    }

    public static ItemId parse(String s) {
        return new ItemId(UUID.fromString(s));   // throws IllegalArgumentException if malformed
    }

    /** Parses the compact (22-char base64url) form produced by toCompact(). */
    public static ItemId parseCompact(String s) {
        Objects.requireNonNull(s, "s");
        byte[] bytes;
        try {
            bytes = Base64.getUrlDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid compact UUID: " + s, e);
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Invalid compact UUID length: " + s);
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new ItemId(new UUID(msb, lsb));
    }

    /** Compact 22-char base64url encoding — use for URLs, short display, etc. */
    public String toCompact() {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    }

    @Override
    public String toString() { return id.toString(); }

}
