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

package visolate.processor;

import java.util.*;
import java.awt.image.*;
import visolate.*;
import visolate.model.*;

public class FatnessProcessor extends Processor {

  public FatnessProcessor(Visolate visolate) {
    super(visolate);
  }

  public void processTile(int r, int c,
                          int ulx, int uly,
                          int width, int height,
                          double left, double bottom,
                          double right, double top) {

//    System.out.println("processTile: " +
//                       "(row, col) = (" + r + ", " + c + "); " +
//                       "ul = (" + ulx + ", " + uly + "); " + 
//                       "dimensions = (" + width + ", " + height  + "); " +
//                       "lbrt = " +
//                       left + ", " + bottom + ", " + right + ", " + top + ")");

    if (width < 2)
      return;

    try {

//      display.getStill(tile);
      tile = display.getStill(); //work around j3d bug
      buffer = tile.getRaster().getDataBuffer(); //work around j3d bug

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {

          int color = getPixel(x, y) & 0xffffff;

          if (color == 0)
            continue;

          Net net = model.getNet(color);

          if (net != null)
            net.incrementAreaPixels();
          else
            misses++;

          if (thread.isInterrupted())
            return;
        }
      }
      
    } catch (InterruptedException e) {
      thread.interrupt(); //reset interrupt status
    }
  }

  private int getPixel(int x, int y) {
    return buffer.getElem(y*canvasWidthPels + x);
  }

  protected void processStarted() {

    tile = display.makeBufferedImage(canvasWidthPels, canvasHeightPels);
    Raster raster = tile.getRaster();
    buffer = raster.getDataBuffer();

    // The sender will restore these when done.
    model.enableBorderGeometry(false);
    model.enableLineGeometry(false);
    model.enableGCodeGeometry(false);
    model.setTranslucent2D(false);

    Collection<Net> nets = model.getNets();

    for (Net net :nets) {
      net.resetArea(dpi);
    }
  }

  protected void processCompleted() {

    tile = null;

    System.out.println(misses + " misses");

    Collection<Net> nets = model.getNets();

    if (nets.size() == 0)
      return;

    double avg = 0.0;
    int n = 0;
    for (Iterator<Net> it = nets.iterator(); it.hasNext(); ) {

      Net net = (Net) it.next();

      if (net.getLength() <= 0.0)
        continue;

      if (net.getArea() <= 0.0)
        continue;
        
      avg += net.getFatness();
      n++;

//      net.dump();
    }

    avg /= (double) n;

    System.out.println("avg fatness = " + avg);
  }

  protected BufferedImage tile = null;

  private DataBuffer buffer;

  private int misses = 0;
}
