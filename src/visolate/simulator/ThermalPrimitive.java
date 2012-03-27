/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
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

public class ThermalPrimitive extends MacroPrimitive {

  private static final String cvsid =
  "$Id: ThermalPrimitive.java,v 1.1.1.1 2004/06/24 05:46:01 vona Exp $";


  public ThermalPrimitive(final List<MacroExpression> exprs) {
    super(exprs);
  }

  protected PrimitiveInstance getInstanceInternal(final List<Double> actuals) {

    int i = 0;

    double x = getParam(i++, actuals);
    double y = getParam(i++, actuals);

    double od = getParam(i++, actuals);
    double id = getParam(i++, actuals);

    double xHairThickness = getParam(i++, actuals);

    double rotation = getParam(i++, actuals);

    return new ThermalInstance(x, y,
                               od, id,
                               xHairThickness,
                               rotation);
  }

  protected String getName() {
    return "thermal";
  }
}
