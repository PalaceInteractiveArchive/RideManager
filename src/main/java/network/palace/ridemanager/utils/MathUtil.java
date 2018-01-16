package network.palace.ridemanager.utils;

public class MathUtil {

    /**
     * Calculation for the incline/decline actions
     *
     * @param x        The amount of blocks after the startX to calculate
     * @param startX   X value of the start location
     * @param endX     X value of the end location
     * @param startY   Y value of the start location
     * @param endY     Y value of the end location
     * @param inverted Whether or not to use the inverted formula. Use this for movement types 2 and 4
     * @return The y value to use for incline/decline actions
     */
    public static double calcIncline(double x, double startX, double endX, double startY, double endY, boolean inverted) {
        double var1 = inverted ? endX : startX;
        double var2 = inverted ? endY : startY;
        int var3 = inverted ? -1 : 1;
        return var3 * (Math.pow(x - var1, 2) / Math.pow(endX - startX, 2)) * (endY - startY) + var2;
    }

    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static double square(double num) {
        return num * num;
    }
}
