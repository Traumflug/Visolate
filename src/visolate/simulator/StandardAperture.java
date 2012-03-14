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

public abstract class StandardAperture extends Aperture {

  public StandardAperture(int number, double holeX, double holeY) {
    super(number);

    this.holeX = holeX;
    this.holeY = holeY;
  }

  public StandardAperture(int number, double hole) {
    super(number);

    this.holeX = hole;
    this.holeY = -1;
  }

  public StandardAperture(int number) {
    super(number);

    this.holeX = -1;
    this.holeY = -1;
  }

  protected double holeX;
  protected double holeY;
}
