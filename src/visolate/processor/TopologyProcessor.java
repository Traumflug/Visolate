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

public class TopologyProcessor extends Processor {

  public static final double EPS = 1.0/256.0;

  public TopologyProcessor(Visolate visolate) {
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

    visolate.resetInnerProgressBar(height);

    try {

//      display.getStill(tile);
      tile = display.getStill(); //work around j3d bug
      buffer = tile.getRaster().getDataBuffer(); //work around j3d bug

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {

          int color = getPixel(x, y) & 0xffffff;

          if (!ignoreColors.contains(new Integer(color)))
            mergeNets(x, y, left, top, color);
          
          if (thread.isInterrupted())
            return;
        }

        visolate.tickInnerProgressBar();
      }
      
    } catch (InterruptedException e) {
      thread.interrupt(); //reset interrupt status
    }
  }

  private void mergeNets(final int x, final int y, final double left, final double top, final int color) 
    throws InterruptedException {
    
    double px = left+x/((double) dpi);
    double py = top-y/((double) dpi);

//    System.out.println("merge " + Model.colorToString(color) + " at (" + x + ", " + y +
//                       ") -> (" + px + ", " + py + ")");

//    System.out.println("ignoring " + Model.colorToString(color));
    ignoreColors.add(new Integer(color));

    Set<Net> nets = new LinkedHashSet<Net>();

    model.getNetsAtPoint(px, py, 1.0/dpi, nets);

//    System.out.println(nets.size() + " nets at point");

    if (nets.isEmpty()) {
      return;
    }

    if (nets.size() <= 1) {
      return;
    }

    mergeNets(nets);

    frames++;
  }

  public static void mergeNets(Collection<Net> nets) {

    Set<Net> superNet = null;
    
    for (Net net : nets) {
      if (superNet == null) {
        superNet = net.getSuperNet();
      }
    }
    
    if (superNet == null) {
      superNet = new LinkedHashSet<Net>();
    }

    for (Net net : nets) {
    	net.setSuperNet(superNet);
    }
  }

  private int getPixel(int x, int y) {
    return buffer.getElem(y*canvasWidthPels + x);
  }

  protected void processStarted() {

    frames = 1;

    visolate.clearSelection();

    tile = display.makeBufferedImage(canvasWidthPels, canvasHeightPels);
    Raster raster = tile.getRaster();
    buffer = raster.getDataBuffer();

    model.setToolDiameter(0.0);

    model.enableBorderGeometry(true);
    model.enableLineGeometry(false);
    model.enableVoronoiGeometry(false);
    model.enableFlatGeometry(true);
    model.enableGCodeGeometry(false);
    
    model.setTranslucent2D(true);

    model.clearPaths();
    model.clearGCode();

    Set<Integer> netColors = model.getNetColors();

    ignoreColors = new LinkedHashSet<Integer>();

    for (Iterator<Integer> it = netColors.iterator(); it.hasNext(); ) {

      int c = it.next().intValue();

//      System.out.println("base color: " + Model.colorToString(c));

      int cr = (c & 0xff0000) >> 16;
      int cg = (c & 0xff00) >> 8;
      int cb = c & 0xff;

      int r = 0;
      int g = 0;
      int b = 0;

      for (int i = 1; i < 8; i++) {

        r += (cr >> i) + ((cr >> (i-1)) & 1);
        g += (cg >> i) + ((cg >> (i-1)) & 1);
        b += (cb >> i) + ((cb >> (i-1)) & 1);

        int j = ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

//        System.out.println("ignoring " + Model.colorToString(j));
        ignoreColors.add(new Integer(j));
      }
    }

//    System.out.println("ignoring " + Model.colorToString(0));
    ignoreColors.add(new Integer(0));
  }

  private void restoreModel() {

    model.setToolDiameter(toolDiameterWas);

    model.enableBorderGeometry(borderGeometryWas);
    model.enableLineGeometry(lineGeometryWas);
    model.enableVoronoiGeometry(voronoiGeometryWas);
    model.enableFlatGeometry(flatGeometryWas);
    model.enableGCodeGeometry(gcodeGeometryWas);
    
    model.setTranslucent2D(wasTranslucent);
  }

  protected void processInterrupted() {
    restoreModel();
  }

  protected void processCompleted() {
    tile = null;
    restoreModel();
//    System.out.println(frames + " frames");
  }

  protected BufferedImage tile = null;

  private DataBuffer buffer;

  private Set<Integer> ignoreColors;

  private int frames;
}
