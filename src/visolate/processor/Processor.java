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

import javax.media.j3d.*;

import visolate.*;
import visolate.misc.*;
import visolate.model.*;

public abstract class Processor {

  public static final double MOSAIC_BORDER_PELS = 8.0;

  public Processor(Visolate visolate) {
    this.visolate = visolate;
    model = visolate.getModel();
    display = visolate.getDisplay();
  }

  public void start() {
    thread.start();
  }

  public void stop() throws InterruptedException {
    while (thread.isAlive()) {
      thread.interrupt();
//      System.out.println("interrupt!");
      Thread.sleep(100);
    }
  }

  public abstract void processTile(int r, int c,
                                   int ulx, int uly,
                                   int width, int height,
                                   double left, double bottom,
                                   double right, double top);

  private void process() {
    boolean borderGeometryWas;
    boolean lineGeometryWas;
    boolean voronoiGeometryWas;
    boolean flatGeometryWas;
    boolean gcodeGeometryWas;
    boolean wasTranslucent;
    double toolDiameterWas;

    long startTime = System.currentTimeMillis();

    String processName = getClass().toString();
    processName = processName.substring(processName.lastIndexOf(".") + 1,
                                        processName.length());
    System.out.println((new Date(startTime)).toString() +
                       ": " + processName + " started");

    visolate.enableControls(false);
    model.enableControls(false);
    display.processStarted();

    borderGeometryWas = model.isBorderGeometryEnabled();
    lineGeometryWas = model.isLineGeometryEnabled();
    voronoiGeometryWas = model.isVoronoiGeometryEnabled();
    flatGeometryWas = model.isFlatGeometryEnabled();
    wasTranslucent = model.isTranslucent2D();
    gcodeGeometryWas = model.isGCodeGeometryEnabled();
    toolDiameterWas = model.getToolDiameter();

//    System.out.println("border: " + borderGeometryWas);
//    System.out.println("line: " + lineGeometryWas);
//    System.out.println("voronoi: " + voronoiGeometryWas);
//    System.out.println("flat: " + flatGeometryWas);
//    System.out.println("translucent: " + wasTranslucent);
//    System.out.println("gcode: " + gcodeGeometryWas);
//    System.out.println("tool diameter: " + toolDiameterWas);

    dpi = display.getDPI();

    System.out.println("DPI: " + dpi);

    Rect b = model.getModelBounds();

    mosaicBounds = new Rect(b.x - MOSAIC_BORDER_PELS/((double) dpi),
                           b.y - MOSAIC_BORDER_PELS/((double) dpi),
                           b.width + 2.0*MOSAIC_BORDER_PELS/((double) dpi),
                           b.height + 2.0*MOSAIC_BORDER_PELS/((double) dpi));

    canvasWidth = display.getVirtualCanvasWidth();
    canvasHeight = display.getVirtualCanvasHeight();

    canvasWidthPels = display.getCanvasWidth();
    canvasHeightPels = display.getCanvasHeight();

    modelWidth = mosaicBounds.width;
    modelHeight = mosaicBounds.height;

    modelWidthPels = (int) Math.ceil(dpi*modelWidth);
    modelHeightPels = (int) Math.ceil(dpi*modelHeight);

    rows = modelHeight/canvasHeight;
    cols = modelWidth/canvasWidth;

    numRows = (int) Math.ceil(rows);
    numCols = (int) Math.ceil(cols);

    visolate.resetProgressBar(numRows*numCols);

    processStarted();

    for (int r = 0; r < numRows; r++) {
      for (int c = 0; c < numCols; c++) {

        double left = c*canvasWidth;

        double top = modelHeight - r*canvasHeight;

        double right = left + canvasWidth;
        if (right > mosaicBounds.width)
          right = mosaicBounds.width;
          
        double bottom = top - canvasHeight;
        if (bottom < 0.0)
          bottom = 0.0;

//        int ulx = (int) Math.ceil(left*((double) dpi));
//        int uly = (int) Math.ceil((modelHeight-top)*((double) dpi));

        int ulx = c*canvasWidthPels;
        int uly = r*canvasHeightPels;

        int lrx = ulx+canvasWidthPels;
        if (lrx > modelWidthPels)
          lrx = modelWidthPels;

        int lry = uly+canvasHeightPels;
        if (lry > modelHeightPels)
          lry = modelHeightPels;

        int width = lrx-ulx;
        int height = lry-uly;

        left += mosaicBounds.x;
        bottom += mosaicBounds.y;
        right += mosaicBounds.x;
        top += mosaicBounds.y;

        double cx = left + canvasWidth/2;
        double cy = top - canvasHeight/2;

        display.setCenter(cx, cy);

        try {
          display.waitForViewUpdate();
        } catch (InterruptedException e) {
          thread.interrupt(); //re-set interrupt status
        }

        processTile(r, c,
                    ulx, uly,
                    width, height,
                    left, bottom, right, top);

        if (thread.isInterrupted()) {

          processInterrupted();

          visolate.enableControls(true);
          model.enableControls(true);
          display.processFinished();
          visolate.processFinished();

          long endTime = System.currentTimeMillis();
          System.out.println((new Date(endTime)).toString() +
                             ": " + processName + " interrupted " +
                             "(" + (endTime-startTime) + "ms)");
          return;
        }

        visolate.tickProgressBar();
      }
    }

    processCompleted();
    
    model.setToolDiameter(toolDiameterWas);
    model.setTranslucent2D(wasTranslucent);
    model.enableGCodeGeometry(gcodeGeometryWas);
    model.enableFlatGeometry(flatGeometryWas);
    model.enableVoronoiGeometry(voronoiGeometryWas);
    model.enableLineGeometry(lineGeometryWas);
    model.enableBorderGeometry(borderGeometryWas);

    visolate.enableControls(true);
    model.enableControls(true);
    display.processFinished();
    visolate.processFinished();
    
    long endTime = System.currentTimeMillis();
    System.out.println((new Date(endTime)).toString() +
                       ": " + processName + " finished (" +
                       (endTime-startTime) + "ms)");
  }
 
  protected void processStarted() {}

  protected void processInterrupted() {}

  protected void processCompleted() {}

  protected Thread thread = new Thread() {

        {
          setPriority(VirtualUniverse.getJ3DThreadPriority()-1);
          setDaemon(true);
        }

      public void run() {
        process();
      }
    };

  protected Visolate visolate;
  protected Model model;
  protected Display display;

  protected Rect mosaicBounds;

  protected int dpi;

  protected double modelWidth;
  protected double modelHeight;

  protected int modelWidthPels;
  protected int modelHeightPels;

  protected double canvasWidth;
  protected double canvasHeight;

  protected int canvasWidthPels;
  protected int canvasHeightPels;
  
  protected double rows;
  protected double cols;

  protected int numRows;
  protected int numCols;
}
