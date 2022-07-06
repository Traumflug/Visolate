/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *               2012 Markus Hitter <mah@jump-ing.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package visolate.simulator;

import java.util.*;

public class CirclePrimitive extends MacroPrimitive {

  public CirclePrimitive(final List<MacroExpression> exprs) {
    super(exprs);
  }

  public PrimitiveInstance getInstanceInternal(List<Double> actuals) {

    int i = 0;

    int exposure = (int) getParam(i++, actuals);

    if (exposure != EXPOSURE_ON)
      return null;

    double diameter = (Double) getParam(i++, actuals);
    double x = (Double) getParam(i++, actuals);
    double y = (Double) getParam(i++, actuals);

    return new CircleInstance(diameter, x, y);
  }

  public String getName() {
    return "circle";
  }
}
