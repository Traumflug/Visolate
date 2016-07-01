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

import javax.media.j3d.GeometryArray;

import visolate.misc.*;

public class MacroAperture extends Aperture {

  public MacroAperture(final int number, final Macro macro, final List<Double> modifiers) {
    super(number);

    this.macro = macro;
    this.modifiers = modifiers;

    System.out.println(toString());

    instance = macro.getInstance(modifiers);
  }

  public String toString() {

    StringBuffer buf = new StringBuffer();

    buf.append("Aperture ");
    buf.append(Integer.toString(number));
    buf.append(": macro");
    buf.append(" name = ");
    buf.append(macro.getName());
    buf.append("; modifiers: ");

    for (Iterator<Double> it = modifiers.iterator(); it.hasNext(); ) {
      buf.append(it.next().toString());
      if (it.hasNext())
        buf.append(", ");
    }

    return buf.toString();
  }

  public Rect getBounds() {
    return instance.getBounds();
  }

  protected void makeBounds() {
    assert false;
  }

  public double getWidth(double direction) {
    return instance.getWidth(direction);
  }

  public Collection<GeometryArray> getGeometries() {
    return instance.getGeometries();
  }

  protected void makeGeometries() {
    assert false;
  }

  protected void offsetChanged() {
    instance.setOffset(offset);
  }

  protected void inverseChanged() {
    instance.setInverse(inverse);
  }

  private Macro macro;
  private List<Double> modifiers;
  private MacroInstance instance;
}
