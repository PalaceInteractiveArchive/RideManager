package network.palace.ridemanager.utils;

import org.bukkit.Location;

public class RideMathUtil {

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

    /**
     * Get the slope of the bezier curve at a certain point t
     *
     * @param t  the point along the curve to calculate
     * @param p0 the point controlling the curvature of the curve
     * @param p1 the starting point
     * @param p2 the ending point
     * @return the angle (in degrees) at a certain point t along the bezier curve
     */
    public static float getBezierAngleAt(double t, Location p0, Location p1, Location p2) {
        if (p1 == null || p2 == null) return 0f;
        double x = 2 * (1 - t) * (p0.getX() - p1.getX()) + 2 * t * (p2.getX() - p0.getX());
        double z = 2 * (1 - t) * (p0.getZ() - p1.getZ()) + 2 * t * (p2.getZ() - p0.getZ());
        double radAngle = Math.atan2(z, x);
        return (float) Math.toDegrees(radAngle);
    }
}
