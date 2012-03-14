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
import visolate.misc.*;

public class Simulator {

  public static final double ARC_RESOLUTION = Math.PI/128.0;

  public static final int MAX_FORMAT = 6;

  //modes

  public static final int RAPID = 0;
  public static final int LINEAR = 1;

  public static final int POLYGON = 2;

  public static final int CW = 3;
  public static final int CCW = 4;

  public static final String[] MODE_NAME =
  new String[] {"rapid", "linear", "polygon", "cw", "ccw"};


  //exposures

  public static final int OPEN = 0;
  public static final int CLOSED = 1;
  public static final int FLASH = 2;

  public static final String[] EXPOSURE_NAME =
  new String[] {"open", "closed", "flash"};


  //defaults

  public static final int DEF_MODE = LINEAR;
  public static final Aperture DEF_APERTURE = null;
  public static final int DEF_EXPOSURE = CLOSED;
  public static final int DEF_X = 0;
  public static final int DEF_Y = 0;
  public static final int DEF_I = 0;
  public static final int DEF_J = 0;
  public static final int DEF_XSCALE = 10000;
  public static final int DEF_YSCALE = 10000;
  public static final boolean DEF_INCREMENTAL = false;
  public static final boolean DEF_IGNORELEADING = true;
  public static final boolean DEF_IGNORETRAILING = false;
  public static final boolean DEF_METRIC = false;
  public static final boolean DEF_ARC360 = false;
  public static final int DEF_X_BEFORE = 2;
  public static final int DEF_X_AFTER = 4;
  public static final int DEF_Y_BEFORE = 2;
  public static final int DEF_Y_AFTER = 4;

  public Simulator(SimulatorUI ui) {
    this.ui = ui;
  }

  public Simulator() {
    this(new BasicSimulatorUI());
  }

  public void reset() {

//    System.out.println("reset()");

    macros.clear();
    apertures.clear();
    vertices.clear();
    actions.clear();

    mode = DEF_MODE;

    aperture = DEF_APERTURE;

    exposure = DEF_EXPOSURE;

    p = getVertex(DEF_X, DEF_Y);

    ci = DEF_I;
    cj = DEF_J;

    xScale = DEF_XSCALE;
    yScale = DEF_YSCALE;

    incremental = DEF_INCREMENTAL;

    ignoreTrailing = DEF_IGNORETRAILING;

    metric = DEF_METRIC;

    arc360 = DEF_ARC360;
    
    xWidth = DEF_X_BEFORE + DEF_X_AFTER;
    yWidth = DEF_Y_BEFORE + DEF_Y_AFTER;
  }

  public void setMetric() {

//    System.out.println("setMetric()");

    metric = true;
  }

  public void ignoreTrailingZeros() {

//    System.out.println("setIgnoreTrailingZeros()");

    ignoreTrailing = true;
  }

  public void setFormatX(int before, int after) {

//    System.out.println("setFormatX(" + before + ", " + after + ")");

    if (!checkFormat(before) || !checkFormat(after))
      throw new SimulatorException("bad X format: " + before + "." + after);

    xWidth = before + after;

    xScale = (int) Math.pow(10.0, after);
//    System.out.println("xScale = " + xScale);
  }

  private boolean checkFormat(int format) {
    return (format >= 0) && (format <= MAX_FORMAT);
  }

  public void setFormatY(int before, int after) {

//    System.out.println("setFormatY(" + before + ", " + after + ")");

    if (!checkFormat(before) || !checkFormat(after))
      throw new SimulatorException("bad Y format: " + before + "." + after);

    yWidth = before + after;

    yScale = (int) Math.pow(10.0, after);
//    System.out.println("yScale = " + yScale);
  }

  public void setIncremental(boolean incremental) {

//    System.out.println("setIncremental(" + incremental + ")");

    this.incremental = incremental;
  }

  public void set360(boolean enabled) {

//    System.out.println("set360(" + enabled + ")");

    arc360 = enabled;
  }

  public void setMode(int mode) {

//    System.out.println("setMode(" + MODE_NAME[mode] + ")");

    if ((mode < 0) || (mode > 4))
      throw new SimulatorException("unknown mode: " + mode);
    
    this.mode = mode;
  }

  public void setAperture(int aperture) {

//    System.out.println("setAperture(" + aperture + ")");

    try {
      this.aperture = (Aperture) apertures.get(new Integer(aperture));
    } catch (NoSuchElementException e) {
      throw new SimulatorException("unknown aperture: " + aperture);
    }
  }

  public void setExposure(int exposure) {

//    System.out.println("setExposure(" + EXPOSURE_NAME[exposure] + ")");

    this.exposure = exposure;
  }


  public void setPosition(String x, String y) {

//    System.out.println("setPosition(" + x + ", " + y + ")");

    int xp = p.x;
    if (x != null)
      xp = parseX(x);

    int yp = p.y;
    if (y != null)
      yp = parseY(y);

    int absX = toAbsoluteX(xp);
    int absY = toAbsoluteY(yp);

//    System.out.println("setting position to absolute coords (" +
//                       absX + ", " + absY + ")");

    Vertex newP = getVertex(absX, absY);

    if ((mode == RAPID) && (exposure != CLOSED))
      System.out.println("WARNING: rapid move with exposure open or flash");
         
    if (mode == POLYGON) {

      throw new UnsupportedOperationException("TBD polygon mode");

    } else if ((exposure == FLASH) || ((exposure == OPEN) && p.equals(newP))) {

      actions.add(new Flash(aperture, newP));

    } else if (exposure == OPEN) {

      switch (mode) {

      case RAPID:
      case LINEAR: actions.add(new Segment(aperture, p, newP)); break;
        
      case CCW: addArc(newP, 1); break;
      case CW: addArc(newP, -1); break;
      }
    }
    
    p = newP;
  }

  public void addFlash() {
    actions.add(new Flash(aperture, p));
  }

  public void addArc(Vertex end, int dir) {
   
//    System.err.println(((arc360) ? "360 " : "quadrant ") +
//                       ((dir > 0) ? "CCW" : "CW") +
//                       " arc from " + p + " to " + end);


//    System.err.println("  (i, j) = (" + ci + ", " + cj + ")");

    int cx = p.x + ci;
    int cy = p.y + cj;

    if (!arc360) {
      cx = p.x + abs(ci)*sign(p.y-end.y)*dir;
      cy = p.y + abs(cj)*sign(p.x-end.x)*dir;
    }

//    System.err.println("  center (" + cx + ", " + cy + ")");

    int dx = p.x-cx;
    int dy = p.y-cy;

    double startT = Util.canonicalizeAngle(Math.atan2(dy, dx));
    double radius = Math.sqrt(dx*dx+dy*dy);

//    System.err.println("  start angle: " + Math.toDegrees(startT) + " deg");
//    System.err.println("  radius: " + radius + " mils");

    double csT = Math.cos(startT);
    double ssT = Math.sin(startT);

    dx = end.x-cx;
    dy = end.y-cy;

    double standardEndX =  dx*csT    - dy*(-ssT);
    double standardEndY =  dx*(-ssT) + dy*csT;

    double standardEndT =
      Util.canonicalizeAngle(Math.atan2(standardEndY, standardEndX));

//    System.err.println("  end angle (standard pos): " +
//                       Math.toDegrees(standardEndT) + " deg");

    double x = 0.0;
    double y = 0.0;

    Vertex prev = p;
    Vertex current = p;

    for (double t = ((dir > 0 ) ? 0.0 : 2.0*Math.PI);
         ((dir > 0) ? (t < standardEndT) : (t > standardEndT));
         t += (ARC_RESOLUTION*dir)) {

      x = radius*Math.cos(t);
      y = radius*Math.sin(t);

//      System.err.println("  current angle (standard pos): " +
//                         Math.toDegrees(t) + " deg");

      if (t > 0.0) {

        current = getVertex(cx+((int) Math.round(x*csT - y*ssT)),
                            cy+((int) Math.round(x*ssT + y*csT)));

//        System.err.println("  adding segment from " + prev + " to " + current);

        actions.add(new Segment(aperture, prev, current));
      }

      prev = current;
    }

//    System.err.println("  adding segment from " + prev + " to " + end);
    actions.add(new Segment(aperture, prev, end));
  }

  private int sign(int i) {
    if (i >= 0)
      return 1;
    else
      return -1;
  }

  private int abs(int i) {
    if (i >= 0)
      return i;
    else
      return -i;
  }

  public void comment(int line, String comment) {
    System.out.println("Comment at line " + line + ": " + comment);
  }

  private int parseX(String coord) {
   
    int x = 0;

    if (!ignoreTrailing) {

      x = Integer.parseInt(coord);

    } else {
      
      StringBuffer buf = new StringBuffer();
      buf.append(coord);
      
      for (int pad = xWidth - coord.length(); pad > 0; pad--)
        buf.append("0");
      
      x = Integer.parseInt(buf.toString());
    }

//    System.out.println("parsed x coord \"" + coord + "\" as " + x);
    return x;
  }

  private int parseY(String coord) {

    int y = 0;

    if (!ignoreTrailing) {

      y = Integer.parseInt(coord);

    } else {

      StringBuffer buf = new StringBuffer();
      buf.append(coord);
      
      for (int pad = yWidth - coord.length(); pad > 0; pad--)
        buf.append("0");
      
      y = Integer.parseInt(buf.toString());
    }

//    System.out.println("parsed y coord \"" + coord + "\" as " + y);
    return y;
  }

  private int toAbsoluteX(int x) {
    if (incremental)
      return p.x + x;
    else
      return x;
  }

  private int toAbsoluteY(int y) {
    if (incremental)
      return p.y + y;
    else
      return y;
  }

  private Vertex getVertex(int x, int y) {

    Vertex v = new Vertex(x, y);

    if ((vertices.keySet()).contains(v)) {

//      System.out.println("using existing vertex " + v);

      return (Vertex) vertices.get(v);

    } else {
      vertices.put(v, v);
      return v;
    }
  }

  public void computeInchCoordinates() {
	  for (Vertex vertex : vertices.values()) {
	      vertex.computeInchCoordinates(xScale, yScale, metric);
	    }
  }

  public void setCenter(String i, String j) {

//    System.out.println("setCenter(" + i + ", " + j + ")");

    if (i != null)
      ci = parseX(i);

    if (j != null)
      cj = parseY(j);
  }

  public void addMacro(final Macro macro) {

//    System.out.println("addMacro(" + macro + ")");

    macros.put(macro.getName(), macro);
  }

  public Macro getMacro(String name) {
    return (Macro) macros.get(name);
  }

  public Collection<Action> getActions() {
    return actions;
  }

  public void addAperture(Aperture aperture) {

//    System.out.println("addAperture(" + aperture + ")");

    apertures.put(new Integer(aperture.getNumber()), aperture);
  }

  public boolean askContinue(int line, int seq) {
    return ui.askContinue(line, seq);
  }

  private Map<String, Macro> macros = new LinkedHashMap<String, Macro>();
  private Map<Integer, Aperture> apertures = new LinkedHashMap<Integer, Aperture>();
  private Map<Vertex, Vertex> vertices = new LinkedHashMap<Vertex, Vertex>();
  private List<Action> actions = new LinkedList<Action>();

  private int mode = DEF_MODE;

  private Aperture aperture = DEF_APERTURE;

  private int exposure = DEF_EXPOSURE;

  private Vertex p = getVertex(DEF_X, DEF_Y);

  private int ci = DEF_I;
  private int cj = DEF_J;

  private int xScale = DEF_XSCALE;
  private int yScale = DEF_YSCALE;

  private boolean incremental = DEF_INCREMENTAL;

  private boolean ignoreTrailing = DEF_IGNORETRAILING;

  private boolean metric = DEF_METRIC;

  private boolean arc360 = DEF_ARC360;

  private int xWidth = DEF_X_BEFORE + DEF_X_AFTER;
  private int yWidth = DEF_Y_BEFORE + DEF_Y_AFTER;

  private SimulatorUI ui;
}
