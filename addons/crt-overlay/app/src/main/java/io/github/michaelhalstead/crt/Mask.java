package io.github.michaelhalstead.crt;

/** The CRT mask: black with varying alpha only, so it can only darken. Pure Java, unit-tested. */
final class Mask {
    static final float MAX = 1.5f;
    static final float DEFAULT = 1.0f;

    enum Preset {
        ARCADE("arcade", "Arcade 240p"),
        CONSOLE("console", "Console 240p"),
        TV480("tv480", "TV 480i");

        final String id, label;

        Preset(String id, String label) {
            this.id = id;
            this.label = label;
        }

        static Preset of(String id) {
            for (Preset p : values()) if (p.id.equals(id)) return p;
            return CONSOLE;
        }
    }

    private Mask() {}

    static float scan(int y, int h, int lines, float depth) {
        double p = (double) h / lines;
        double d = (y - Math.floor(y / p) * p) / p - 0.5;
        double q = d / 0.3;
        return (float) (depth * (1 - Math.exp(-q * q)));
    }

    static float slot(int x, int y, float depth) {
        boolean col = (x % 6) >= 4;
        boolean gap = ((y + ((x % 12) < 6 ? 0 : 2)) % 4) == 0;
        return (col || gap) ? depth : 0f;
    }

    static float grille(int x, float depth) {
        return (x % 3) == 2 ? depth : 0f;
    }

    static float corners(int x, int y, int w, int h, float radius) {
        double cx = (w - 1) / 2.0, cy = (h - 1) / 2.0;
        double ex = Math.max(Math.abs(x - cx) - (cx - radius), 0);
        double ey = Math.max(Math.abs(y - cy) - (cy - radius), 0);
        return clamp((Math.hypot(ex, ey) - radius + 6) / 6);
    }

    static float vignette(int x, int y, int w, int h, float v) {
        double cx = (w - 1) / 2.0, cy = (h - 1) / 2.0;
        double nx = Math.abs(x - cx) / (w / 2.0), ny = Math.abs(y - cy) / (h / 2.0);
        double t = clamp((Math.hypot(nx, ny) - 0.75) / 0.7);
        return (float) (Math.pow(t, 1.5) * v);
    }

    private static float clamp(double v) {
        return (float) Math.min(1, Math.max(0, v));
    }

    /** Alpha 0..1 of pixel (x, y) on a w x h screen; 0.95 at most except in the corners. */
    static float alpha(int x, int y, int w, int h, Preset pr, float s) {
        return finish(pr, s, scan(y, h, pr == Preset.TV480 ? 480 : 240, pr == Preset.ARCADE ? .75f * s : pr == Preset.CONSOLE ? .6f * s : .5f * s),
                pr == Preset.ARCADE ? slot(x, y, .22f * s) : pr == Preset.TV480 ? grille(x, .15f * s) : 0f,
                vignette(x, y, w, h, pr == Preset.ARCADE ? .5f * s : .35f * s),
                s == 0f ? 0f : corners(x, y, w, h, pr == Preset.CONSOLE || pr == Preset.TV480 ? 40 : 60));
    }

    private static float finish(Preset pr, float s, float scan, float mid, float vig, float corner) {
        float a = 1 - (1 - scan) * (1 - mid) * (1 - vig) * (1 - corner);
        return corner >= 1f ? 1f : Math.min(0.95f, Math.max(0f, a));
    }

    /** ARGB pixels, black with the alpha in the top byte. Same maths as alpha(), with rows and columns hoisted. */
    static int[] build(int w, int h, Preset pr, float s) {
        int lines = pr == Preset.TV480 ? 480 : 240;
        float sd = pr == Preset.ARCADE ? .75f * s : pr == Preset.CONSOLE ? .6f * s : .5f * s;
        float vd = pr == Preset.ARCADE ? .5f * s : .35f * s;
        float rad = pr == Preset.ARCADE ? 60 : 40;
        double cx = (w - 1) / 2.0, cy = (h - 1) / 2.0;
        float[] rowScan = new float[h];
        double[] rowEy = new double[h], rowNy2 = new double[h];
        for (int y = 0; y < h; y++) {
            rowScan[y] = scan(y, h, lines, sd);
            rowEy[y] = Math.max(Math.abs(y - cy) - (cy - rad), 0);
            double ny = Math.abs(y - cy) / (h / 2.0);
            rowNy2[y] = ny * ny;
        }
        float[] colMid = new float[w];
        double[] colEx = new double[w], colNx2 = new double[w];
        for (int x = 0; x < w; x++) {
            colMid[x] = pr == Preset.TV480 ? grille(x, .15f * s) : 0f;
            colEx[x] = Math.max(Math.abs(x - cx) - (cx - rad), 0);
            double nx = Math.abs(x - cx) / (w / 2.0);
            colNx2[x] = nx * nx;
        }
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float mid = pr == Preset.ARCADE ? slot(x, y, .22f * s) : colMid[x];
                double t = clamp((Math.sqrt(colNx2[x] + rowNy2[y]) - 0.75) / 0.7);
                float vig = (float) (t * Math.sqrt(t) * vd);
                float corner = s == 0f ? 0f : clamp((Math.sqrt(colEx[x] * colEx[x] + rowEy[y] * rowEy[y]) - rad + 6) / 6);
                px[y * w + x] = Math.round(finish(pr, s, rowScan[y], mid, vig, corner) * 255f) << 24;
            }
        }
        return px;
    }

    static int percent(float s) {
        return Math.round(s * 100f);
    }
}
