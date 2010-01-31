/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **/
 
package visolate.simulator;

import java.util.*;

public class Macro {

  private static final String cvsid =
  "$Id: Macro.java,v 1.1.1.1 2004/06/24 05:46:01 vona Exp $";


  public Macro(final String name, final List<MacroPrimitive> primitives) {
    this.name = name;
    this.primitives = primitives;
    System.out.println(toString());
  }

  public String getName() {
    return name;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();

    buf.append("macro \"");
    buf.append(name);
    buf.append("\"");

    for (MacroPrimitive prim : primitives) {
      buf.append("\n  ");
      buf.append(prim.toString());
    }

    return buf.toString();
  }

  public MacroInstance getInstance(final List<Double> modifiers) {
    return new MacroInstance(primitives, modifiers);
  }

  private String name;

  private List<MacroPrimitive> primitives = new LinkedList<MacroPrimitive>();
}
