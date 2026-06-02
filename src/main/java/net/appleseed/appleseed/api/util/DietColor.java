package net.appleseed.appleseed.api.util;

/**
 * Immutable record representing an RGBA color value.
 * <p>
 * Each component is in the range {@code [0, 255]}. Provides static factory methods
 * for creating colors from packed integer values and hex strings.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * DietColor red = DietColor.fromInt(0xFFFF0000);
 * DietColor blue = DietColor.fromHex("#0000FF");
 * DietColor custom = new DietColor(255, 128, 0, 255);
 * int packed = custom.toInt();
 * }</pre>
 *
 * @param r the red component (0-255)
 * @param g the green component (0-255)
 * @param b the blue component (0-255)
 * @param a the alpha component (0-255)
 */
public record DietColor(int r, int g, int b, int a) {

    /**
     * Creates a {@code DietColor} from a packed ARGB integer.
     * <p>
     * Component layout: {@code 0xAARRGGBB}.
     *
     * @param color the packed ARGB color value
     * @return a new {@code DietColor} instance
     */
    public static DietColor fromInt(int color) {
        return new DietColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
    }

    /**
     * Creates a {@code DietColor} from a hex string.
     * <p>
     * Accepts formats: {@code "#RRGGBB"}, {@code "#AARRGGBB"}, {@code "RRGGBB"}, or {@code "AARRGGBB"}.
     * 6-digit strings are treated as fully opaque (alpha = 255).
     * Falls back to opaque white if the string cannot be parsed.
     *
     * @param hex the hex color string, with or without leading {@code #}
     * @return a new {@code DietColor} instance
     */
    public static DietColor fromHex(String hex) {
        String s = hex.replace("#", "");
        if (s.length() == 8) {
            return fromInt((int) Long.parseLong(s, 16));
        } else if (s.length() == 6) {
            return fromInt(0xFF000000 | Integer.parseInt(s, 16));
        }
        return new DietColor(255, 255, 255, 255);
    }

    /**
     * Converts this color to a packed ARGB integer.
     * <p>
     * Component layout: {@code 0xAARRGGBB}.
     *
     * @return the packed ARGB color value
     */
    public int toInt() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}