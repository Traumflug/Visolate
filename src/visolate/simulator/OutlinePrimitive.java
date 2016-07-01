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

import javax.vecmath.*;

public class OutlinePrimitive extends MacroPrimitive {

  public OutlinePrimitive(List<MacroExpression> exprs) {
    super(exprs);
  }

  @Override
  protected PrimitiveInstance getInstanceInternal(List<Double> actuals) {

    int i = 0;

    int exposure = (int) getParam(i++, actuals);

    if (exposure != EXPOSURE_ON) {
      return null;
    }

    int n = (int) getParam(i++, actuals);

    List<Point2d> pts = new LinkedList<Point2d>();
    while (i < (actuals.size()-1)) {
      pts.add(new Point2d(getParam(i++, actuals), getParam(i++, actuals)));
    }

    double rotation = getParam(i++, actuals);

    return new OutlineInstance(n, pts, rotation);
  }

  protected String getName() {
    return "outline";
  }
}
