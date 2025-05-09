package net.rickiekarp.shapass.math;

import net.rickiekarp.core.math.noise.NoiseConfig;

import java.awt.image.BufferedImage;

public class PerlinNoise2D {
    private double time;
    private final BufferedImage image;
    private final NoiseConfig noiseConfig;

    public PerlinNoise2D(NoiseConfig config) {
        noiseConfig = config;
        setTime(0);
        image = new BufferedImage(config.getNoiseInput().getX(), config.getNoiseInput().getY(), BufferedImage.TYPE_INT_RGB);
    }

    public double getNoise(double dx, double dy, int frequency, double timeShift) {
        double noise = noise((dx * frequency) + timeShift, (dy * frequency) + timeShift);
        noise = (noise - 1) / 2;
        return noise;
    }

    public BufferedImage getNoiseImage() {
        for(int y = 0; y < noiseConfig.getNoiseInput().getY(); y++) {
            for(int x = 0; x < noiseConfig.getNoiseInput().getX(); x++) {
                double dx = (double) x / noiseConfig.getNoiseInput().getY();
                double dy = (double) y / noiseConfig.getNoiseInput().getY();
                double noise = getNoise(dx, dy, noiseConfig.getNoiseInput().getFrequency(), time);
                int r = (int) (noise * noiseConfig.getRedMultiplier());
                int g = r * noiseConfig.getGreenMultiplier();
                int rgb = r * 0x10000 + g;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    public void setTime(double newTime) {
        time = newTime;
    }

    public void incrementTime() {
        time += noiseConfig.getNoiseInput().getTimeIncrement();
    }

    private static double noise(double x, double y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int g1 = p[p[xi] + yi];
        int g2 = p[p[xi + 1] + yi];
        int g3 = p[p[xi] + yi + 1];
        int g4 = p[p[xi + 1] + yi + 1];

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double d1 = grad(g1, xf, yf);
        double d2 = grad(g2, xf - 1, yf);
        double d3 = grad(g3, xf, yf - 1);
        double d4 = grad(g4, xf - 1, yf - 1);

        double u = fade(xf);
        double v = fade(yf);

        double x1Inter = lerp(u, d1, d2);
        double x2Inter = lerp(u, d3, d4);

        return lerp(v, x1Inter, x2Inter);
    }

    private static double lerp(double amount, double left, double right) {
        return ((1 - amount) * left + amount * right);
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double grad(int hash, double x, double y) {
        return switch (hash & 3) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            case 3 -> -x - y;
            default -> 0;
        };
    }

    static final int[] p = new int[512], permutation = { 151,160,137,91,90,15,
            131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
            190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
            88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
            77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
            102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
            135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
            5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
            223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
            129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
            251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
            49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
            138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    };

    static { for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i]; }
}