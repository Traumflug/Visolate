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

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

public class GCodeFileWriter {
  
  /**
   * This is the number format used for all numbers in the gcode.
   */
  private static final NumberFormat gCodeFormat =
      new DecimalFormat("###.#####", new DecimalFormatSymbols(Locale.ENGLISH));
  private static final double MMPERINCH = 25.4;
  
  private static final Color3f G_CODE_COLOR_RAPID = new Color3f(1.0f, 0.0f, 0.0f);
  private static final Color3f G_CODE_COLOR_PLUNGE = new Color3f(0.0f, 0.0f, 1.0f);
  private static final Color3f G_CODE_COLOR_MILLING = new Color3f(0.0f, 1.0f, 1.0f);

  public GCodeFileWriter() {
  }
  
  public void open(final File outputFile) throws IOException {
    out = new FileWriter(outputFile);
  }

  public void close() throws IOException {
    out.close();
  }

  /**
   * If set to true, output metric coordinates instead of imperial ones.
   */
  private boolean isMetric = true;
  
  public boolean getIsMetric() {
    return isMetric;
  }

  public void setIsMetric(final boolean isMetric) {
    this.isMetric = isMetric;
  }
  
  /**
   * If set to true, output absolute coordinates instead of relative ones.
   */
  private boolean isAbsolute = true;
  
  public boolean getIsAbsolute() {
    return isAbsolute;
  }

  public void setIsAbsolute(final boolean isAbsolute) {
    this.isAbsolute = isAbsolute;
  }
  
  /**
   * If we use absolute coordinates, then this
   * is the X-value for the left upper corner.
   */
  private double xOffset = 0.0;
  
  public double getXOffset() {
    return xOffset;
  }

  public void setXOffset(final double offset) {
    this.xOffset = offset;
  }
  
  public void setYOffset(final double offset) {
    this.yOffset = offset;
  }
  
  /**
   * We move this much above origin for traveling.
   * This is not to be converted from inches.
   */
  private double zClearance = 1.0;
  
  public double getZClearance() {
    return zClearance;
  }

  public void setZClearance(final double zClearance) {
    this.zClearance = zClearance;
  }
  
  /**
   * When cutting, the head should have this z-coordinate, in mm or inch.
   * This is not to be converted from inches.
   */
  private double zCuttingHeight = -0.1;
  
  public double getZCuttingHeight() {
    return zCuttingHeight;
  }

  public void setZCuttingHeight(final double zCuttingHeight) {
    this.zCuttingHeight = zCuttingHeight;
  }
  
  public void setPlungeFeedrate(final double plungeFeedrate) {
    this.plungeFeedrate = plungeFeedrate;
  }
  
  public void setMillingFeedrate(final double millingFeedrate) {
    this.millingFeedrate = millingFeedrate;
  }
  
  public Point3d getCurrentPosition() {
    return currentPosition;
  }
  
  public List<GCodeStroke> getGCodeStrokes() {
    return gCodeStrokes;
  }
  
  public void preAmble() throws IOException {

    if (isMetric) {
      out.write("G21\n");    // millimeters
    } else {
      out.write("G20\n");    // inches
    }
    out.write("G17\n");     // X-Y plane
    out.write("G40\nG49\n"); // Cancel tool lengh & cutter dia compensation
    //    w.write("G53\n");     // Motion in machine co-ordinate system
    out.write("G80\n");    // Cancel any existing motion cycle

    if (isAbsolute) {
      out.write("G90\n");    // Absolute distance mode
    } else {
      out.write("G91\n");    // Relative distance mode
    }
    
    currentPosition = new Point3d(0.0, 0.0, 0.0);
    currentFeedrate = millingFeedrate;
    gCodeStrokes.clear();
  }
  
  public void postAmble() throws IOException {

    cutterUp();
    
    Point2d p = new Point2d(0.0, 0.0);
    rapidMovement(p); //rapid to origin

    out.write("M5\n"); // Spindle Stop
    out.write("M2\n"); // End of program
  }

  /**
   * Add a G-code for moving up the cutter straight to zClearance at rapid feedrate.
   *
   * @throws IOException
   */
  public void cutterUp() throws IOException {

    if (isAbsolute) {
      out.write("G0 Z" + gCodeFormat.format(zClearance) + "\n");
    }
    else {
      out.write("G0 Z" + gCodeFormat.format(zCuttingHeight - zClearance) + "\n");
    }
    
    currentPosition.z = zClearance;
    gCodeStrokes.add(new GCodeStroke(currentPosition, G_CODE_COLOR_RAPID));
  }

  /**
   * Add a G-code for moving down the cutter to zCuttingHeight at plunge feedrate.
   *
   * @throws IOException
   */
  public void cutterDown() throws IOException {

    if (isAbsolute) {
      out.write("G1 Z" + gCodeFormat.format(zCuttingHeight) +
                " F" + gCodeFormat.format(plungeFeedrate) + "\n");
    }
    else {
      out.write("G1 Z" + gCodeFormat.format(zClearance - zCuttingHeight) +
                " F" + gCodeFormat.format(plungeFeedrate) + "\n");
    }

    currentPosition.z = zCuttingHeight;
    currentFeedrate = plungeFeedrate;
    gCodeStrokes.add(new GCodeStroke(currentPosition, G_CODE_COLOR_PLUNGE));
  }

  /**
   * Add a G-code for a rapid movement.
   *
   * @param target where the machine should move
   *
   * @throws IOException
   */
  public void rapidMovement(final Point2d target) throws IOException {

    if (isAbsolute) {
      out.write("G0 X" + gCodeFormat.format(convertUnits(target.x) + xOffset) +
                " Y" + gCodeFormat.format(convertUnits(target.y) + yOffset));
    }
    else {
      out.write("G0 X" + gCodeFormat.format(convertUnits(target.x - currentPosition.x)) +
                " Y" + gCodeFormat.format(convertUnits(target.y - currentPosition.y)));
    }
    out.write("\n");

    currentPosition.x = target.x;
    currentPosition.y = target.y;
    gCodeStrokes.add(new GCodeStroke(currentPosition, G_CODE_COLOR_RAPID));
  }

  /**
   * Add a G-code for a linear movement at milling feedrate.
   *
   * @param target where the machine should move
   *
   * @throws IOException
   */
  public void linearMovement(final Point2d target) throws IOException {

    if (isAbsolute) {
      out.write("G1 X" + gCodeFormat.format(convertUnits(target.x) + xOffset) +
                " Y" + gCodeFormat.format(convertUnits(target.y) + yOffset));
    }
    else {
      out.write("G1 X" + gCodeFormat.format(convertUnits(target.x - currentPosition.x)) +
                " Y" + gCodeFormat.format(convertUnits(target.y - currentPosition.y)));
    }
    if (currentFeedrate != millingFeedrate) {
      out.write(" F" + gCodeFormat.format(millingFeedrate));
      currentFeedrate = millingFeedrate;
    }
    out.write("\n");

    currentPosition.x = target.x;
    currentPosition.y = target.y;
    gCodeStrokes.add(new GCodeStroke(currentPosition, G_CODE_COLOR_MILLING));
  }
  
  private double convertUnits(final double x) {
    if (isMetric) {
      return x * MMPERINCH;
    }
    return x;
  }

  private FileWriter out;
  
  private Point3d currentPosition;
  private double currentFeedrate;
    
  private double yOffset = 0.0;
  
  private double plungeFeedrate = 0.5;
  private double millingFeedrate = 1.0;
  
  private List<GCodeStroke> gCodeStrokes = new LinkedList<GCodeStroke>();


  public class GCodeStroke {

    GCodeStroke(Point3d target, Color3f color) {
      this.color = color;
      this.target = new Point3f((float) target.x, (float) target.y, (float) target.z);
    }

    Color3f color;
    Point3f target;
  }
}
