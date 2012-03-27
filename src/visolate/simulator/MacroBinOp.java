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

public class MacroBinOp extends MacroExpression {

  private static final String cvsid =
  "$Id: MacroBinOp.java,v 1.1.1.1 2004/06/24 05:46:01 vona Exp $";


  public static final int ADD = 0;
  public static final int SUBTRACT = 1;
  public static final int MULTIPLY = 2;
  public static final int DIVIDE = 3;

  public static final String[] OP_TEXT = new String[] {"+", "-", "*", "/"};

  public MacroBinOp(int op, MacroExpression lhs, MacroExpression rhs) {
    this.op = op;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public double getValue(final List<Double> actuals) {

    double lVal = lhs.getValue(actuals);
    double rVal = rhs.getValue(actuals);

    switch (op) {
    case ADD: return lVal + rVal;
    case SUBTRACT: return lVal - rVal;
    case MULTIPLY: return lVal*rVal;
    case DIVIDE: return lVal/rVal;
    default: return Double.NaN;
    }
  }

  public String toString() {
    return lhs.toString() + OP_TEXT[op] + rhs.toString();
  }

  private MacroExpression lhs, rhs;

  private int op;
}
