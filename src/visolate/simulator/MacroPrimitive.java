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

public abstract class MacroPrimitive {

  public static final int EXPOSURE_OFF = 0;
  public static final int EXPOSURE_ON = 1;
  public static final int EXPOSURE_REVERSE = 2;

  public MacroPrimitive(List<MacroExpression> exprs) {
    this.exprs = exprs;
  }

  public PrimitiveInstance getInstance(List<Double> modifiers) {

    List<Double> actuals = new ArrayList<Double>();

    for (Iterator<MacroExpression> it = exprs.iterator(); it.hasNext(); )
      actuals.add(new Double(((MacroExpression) it.next()).
                             getValue(modifiers)));

    return getInstanceInternal(actuals);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append(getName());
    buf.append(" ");

    for (Iterator<MacroExpression> it = exprs.iterator(); it.hasNext(); ) {
      buf.append((it.next()).toString());
      if (it.hasNext()) {
        buf.append(", ");
      }
    }

    return buf.toString();
  }

  protected double getParam(int index, List<Double> actuals) {
    return (actuals.get(index));
  }

  protected abstract String getName();

  protected abstract PrimitiveInstance getInstanceInternal(List<Double> actuals);

  protected List<MacroExpression> exprs;
}
