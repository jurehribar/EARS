package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Inverted Hemispheres Problem
 *
 * A 2D function with two inverted hemispherical dents defined as:
 * z(x,y) = baseHeight - max(0, sqrt(radius^2 - (x-centerOffset)^2 - y^2),
 *                              sqrt(radius^2 - (x+centerOffset)^2 - y^2))
 *
 * Domain: x,y ∈ [-10, 10]
 * Global optima: (-centerOffset, 0) and (centerOffset, 0) with z = baseHeight - radius
 */
public class InvertedHemispheres extends DoubleProblem {

    private final double baseHeight;
    private final double centerOffset;
    private final double radius;

    /**
     * Constructor with default parameters: baseHeight=2, centerOffset=2.0, radius=1.7
     */
    public InvertedHemispheres() {
        this(2.0, 2.0, 1.7);
    }

    /**
     * Constructor with custom parameters
     *
     * @param baseHeight the base height of the surface (default 2.0)
     * @param centerOffset the offset of hemisphere centers from origin (default 1.3)
     * @param radius the radius of the hemispheres (default 1.0)
     */
    public InvertedHemispheres(double baseHeight, double centerOffset, double radius) {
        super("InvertedHemispheres", 2, 2, 1, 0);
        this.baseHeight = baseHeight;
        this.centerOffset = centerOffset;
        this.radius = radius;

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -4.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 4.0));
    }

    @Override
    public double eval(double[] x) {
        double xCoord = x[0];
        double yCoord = x[1];

        // Calculate right hemisphere value centered at (centerOffset, 0)
        double distSqRight = Math.pow(xCoord - centerOffset, 2) + Math.pow(yCoord, 2);
        double hemRight = 0.0;
        if (distSqRight <= radius * radius) {
            hemRight = Math.sqrt(radius * radius - distSqRight);
        }

        // Calculate left hemisphere value centered at (-centerOffset, 0)
        double distSqLeft = Math.pow(xCoord + centerOffset, 2) + Math.pow(yCoord, 2);
        double hemLeft = 0.0;
        if (distSqLeft <= radius * radius) {
            hemLeft = Math.sqrt(radius * radius - distSqLeft);
        }

        // z = baseHeight - max(0, hemRight, hemLeft)
        double maxHem = Math.max(0.0, Math.max(hemRight, hemLeft));
        return baseHeight - maxHem;
    }

    public double getBaseHeight() {
        return baseHeight;
    }

    public double getCenterOffset() {
        return centerOffset;
    }

    public double getRadius() {
        return radius;
    }
}

