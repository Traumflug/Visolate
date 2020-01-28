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

public class BasicSimulatorUI implements SimulatorUI {

  public boolean askContinue(int line, int seq) {

    StringBuffer msgBuf = new StringBuffer();

    msgBuf.append("Continue after line ");
    msgBuf.append(Integer.toString(line));
    msgBuf.append(" ");

    if (seq >= 0) {
      msgBuf.append("(sequence number ");
      msgBuf.append(Integer.toString(seq));
      msgBuf.append(")");
    }

    msgBuf.append("[y/n]?");

    System.out.println(msgBuf.toString());

    // TBD

    return false;
  }
}
