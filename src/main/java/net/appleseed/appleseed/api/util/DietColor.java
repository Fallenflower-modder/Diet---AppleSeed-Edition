package net.appleseed.appleseed.api.util;

public record DietColor(int r, int g, int b, int a) {

    public static DietColor fromInt(int color) {
        return new DietColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
    }

    public static DietColor fromHex(String hex) {
        String s = hex.replace("#", "");
        if (s.length() == 8) {
            return fromInt((int) Long.parseLong(s, 16));
        } else if (s.length() == 6) {
            return fromInt(0xFF000000 | Integer.parseInt(s, 16));
        }
        return new DietColor(255, 255, 255, 255);
    }

    public int toInt() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
