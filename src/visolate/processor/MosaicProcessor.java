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

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import visolate.*;

public class MosaicProcessor extends Processor {

  public MosaicProcessor(Visolate visolate) {
    super(visolate);
  }

  public void processTile(int r, int c, int ulx, int uly, int width, int height, double left, double bottom,
      double right, double top) {

//    System.out.println("processTile: " +
//                       "(row, col) = (" + r + ", " + c + "); " +
//                       "ul = (" + ulx + ", " + uly + "); " + 
//                       "dimensions = (" + width + ", " + height  + "); " +
//                       "lbrt = " +
//                       left + ", " + bottom + ", " + right + ", " + top + ")");

    AffineTransform transform = new AffineTransform();
    transform.translate(ulx, uly);

    try {
//      mosaicG2D.drawRenderedImage(display.getStill(tile), transform); //work around j3d bug
      mosaicG2D.drawRenderedImage(display.getStill(), transform);
    } catch (InterruptedException e) {
      thread.interrupt(); // reset interrupt status
    }
  }

  protected void processStarted() {

    mosaicWidth = modelWidthPels;
    mosaicHeight = modelHeightPels;

    System.out.println("mosaic: " + mosaicWidth + " x " + mosaicHeight);
    System.out.println("tile: " + canvasWidthPels + " x " + canvasHeightPels);

    mosaic = display.makeBufferedImage(mosaicWidth, mosaicHeight);
    tile = display.makeBufferedImage(canvasWidthPels, canvasHeightPels);
    mosaicG2D = (Graphics2D) mosaic.getGraphics();
  }

  protected void processCompleted() {
    mosaicG2D.dispose();
    mosaicG2D = null;
    tile = null;
  }

  protected int mosaicWidth;
  protected int mosaicHeight;

  protected BufferedImage mosaic = null;
  protected BufferedImage tile = null;

  protected Graphics2D mosaicG2D = null;
}
