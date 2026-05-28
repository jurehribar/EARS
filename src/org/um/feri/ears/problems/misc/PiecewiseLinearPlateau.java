package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * PiecewiseLinearPlateau - 2D piecewise linear function with a flat plateau
 *
 * This function creates a flat plateau at height 6 in the region [3,5] x [3,5],
 * with linear sections in all other regions. The function is continuous but has
 * discontinuous gradients at region boundaries.
 *
 * Function definition:
 * F(x,y) = x+y          if x<3, y<3
 *        = 3+y          if 3≤x≤5, y<3
 *        = x+3          if x<3, 3≤y≤5
 *        = 6            if 3≤x≤5, 3≤y≤5 (PLATEAU)
 *        = (x-2)+y      if x>5, y<3
 *        = x+(y-2)      if x<3, y>5
 *        = (x-2)+(y-2)  if x>5, y>5
 *
 * Domain: x, y in [-10, 10]
 * Global minimum: f(x, y) → -∞ as x,y → -∞
 * Plateau region: [3, 5] × [3, 5] with constant value 6
 */
public class PiecewiseLinearPlateau extends DoubleProblem {

    /**
     * Constructor - creates a 2D piecewise linear plateau problem
     */
    public PiecewiseLinearPlateau() {
        super("PiecewiseLinearPlateau", 2, 1, 1, 0);

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -10.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 10.0));
    }

    @Override
    public double eval(double[] ds) {
        if (ds.length != 2) {
            throw new IllegalArgumentException("PiecewiseLinearPlateau is only defined for 2 dimensions");
        }

        double x = ds[0];
        double y = ds[1];

        // Region 1: x < 3, y < 3
        if (x < 3.0 && y < 3.0) {
            return x + y;
        }

        // Region 2: 3 ≤ x ≤ 5, y < 3
        if (x >= 3.0 && x <= 5.0 && y < 3.0) {
            return 3.0 + y;
        }

        // Region 3: x < 3, 3 ≤ y ≤ 5
        if (x < 3.0 && y >= 3.0 && y <= 5.0) {
            return x + 3.0;
        }

        // Region 4: 3 ≤ x ≤ 5, 3 ≤ y ≤ 5 (PLATEAU)
        if (x >= 3.0 && x <= 5.0 && y >= 3.0 && y <= 5.0) {
            return 6.0;
        }

        // Region 5: x > 5, y < 3
        if (x > 5.0 && y < 3.0) {
            return (x - 2.0) + y;
        }

        // Region 6: x < 3, y > 5
        if (x < 3.0 && y > 5.0) {
            return x + (y - 2.0);
        }

        // Region 7: x > 5, y > 5
        if (x > 5.0 && y > 5.0) {
            return (x - 2.0) + (y - 2.0);
        }

        // Edge cases: x > 5, 3 ≤ y ≤ 5
        if (x > 5.0 && y >= 3.0 && y <= 5.0) {
            return (x - 2.0) + y;
        }

        // Edge cases: 3 ≤ x ≤ 5, y > 5
        if (x >= 3.0 && x <= 5.0 && y > 5.0) {
            return x + (y - 2.0);
        }

        // Default case (should not reach here with valid bounds)
        return x + y;
    }

    /**
     * Get information about the plateau region
     * @return String description of plateau
     */
    public String getPlateauInfo() {
        return "Plateau at height 6 in region [3, 5] × [3, 5]";
    }
}


