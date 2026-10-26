package com.gathin.tvhome;

import java.util.List;
import java.util.regex.Pattern;

/** The files ThemeProvider may write or delete under Config.dir, by the URI's path segments. */
final class ThemeFiles {
    static final String WALLPAPERS = "wallpapers";
    static final String FONTS = "fonts";
    private static final Pattern FONT = Pattern.compile("[A-Za-z0-9_.-]+\\.(ttf|otf)", Pattern.CASE_INSENSITIVE);
    /** The names tools/theme.sh accepts for a collection image, and the types WallpaperRotation reads. */
    private static final Pattern IMAGE = Pattern.compile("[A-Za-z0-9_.-]+\\.(jpg|jpeg|png|webp)",
            Pattern.CASE_INSENSITIVE);

    private ThemeFiles() {}

    /** The path relative to the theme directory, e.g. "config.json" or "wallpapers/dusk.jpg", or null if refused. */
    static String path(List<String> segments) {
        if (segments == null) return null;
        if (segments.size() == 1) {
            String name = segments.get(0);
            return Config.FILE_NAME.equals(name) || "wallpaper.jpg".equals(name) || "theme_revision".equals(name)
                    ? name : null;
        }
        if (segments.size() == 2 && WALLPAPERS.equals(segments.get(0))
                && (collectionImage(segments.get(1)) || WallpaperCaptions.FILE_NAME.equals(segments.get(1)))) {
            return WALLPAPERS + "/" + segments.get(1);
        }
        if (segments.size() == 2 && FONTS.equals(segments.get(0)) && fontFile(segments.get(1))) {
            return FONTS + "/" + segments.get(1);
        }
        return null;
    }

    /** True for "wallpapers" or "fonts" alone: a delete of it clears that folder's pictures or fonts. */
    static boolean isCollection(List<String> segments) {
        return segments != null && segments.size() == 1
                && (WALLPAPERS.equals(segments.get(0)) || FONTS.equals(segments.get(0)));
    }

    /** True for a file a collection folder holds: an image in wallpapers, a .ttf or .otf in fonts. */
    static boolean collectionFile(String folder, String name) {
        return FONTS.equals(folder) ? fontFile(name) : collectionImage(name);
    }

    static boolean fontFile(String name) {
        return name != null && !name.startsWith(".") && FONT.matcher(name).matches();
    }

    static boolean collectionImage(String name) {
        return name != null && !name.startsWith(".") && IMAGE.matcher(name).matches();
    }
}
