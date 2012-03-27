/**
 * "Visolate" -- compute (Voronoi) PCB isolation routing toolpaths
 *
 * Copyright (C) 2004 Marsette A. Vona, III
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

package visolate;

import visolate.misc.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.text.*;
import javax.media.j3d.*;
import javax.vecmath.*;

public class Display extends JPanel {

  private static final String cvsid =
  "$Id: Display.java,v 1.10 2006/08/29 04:02:32 vona Exp $";

  public static final int MAX_UPDATE_FRAME_TIME = 100;

  public static final int NUM_UPDATE_FRAMES = 3;

  public static final int ACCEL_FACTOR = 10;
  
  public static final boolean DEF_PAN = true;
  public static final boolean DEF_TILT = false;
  public static final boolean DEF_ZOOM = true;

  public static final int DEF_WIDTH = 512;
  public static final int DEF_3D_HEIGHT = 512;

  public static final double VIEW_Z = 1.5;
  public static final double FRONT_CLIP = 0.1;
  public static final double DEF_BACK_CLIP = 10.0;

  public static final Point3d EYE = new Point3d(0.0, 0.0, 1.5);

  public static final int DEF_NATIVE_DPI = 90;
  public static final int MIN_NATIVE_DPI = 1;
  public static final int MAX_NATIVE_DPI = 1000;

  public static final int MIN_DPI = 10;
  public static final int MAX_DPI = 10000;
  
  public static final int ZOOM_FIELD_WIDTH = 6;

  public static final int LOC_FRACTION_DIGITS = 4;

  public static final int MIN_FRAME_TIME = 1000/50;

  public static final BoundingSphere BOUNDS =
  new BoundingSphere(new Point3d(0.0,0.0,0.0), Double.MAX_VALUE);

  public static final boolean IMMEDIATE_MODE = false;

  public Display(Visolate visolate) {

    Dimension d;

    this.visolate = visolate;

    setBackground(Color.WHITE);

    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      
    panel3D = new JPanel(); 
    panel3D.setMinimumSize(new Dimension(0, 0));
    panel3D.setPreferredSize(new Dimension(DEF_WIDTH, DEF_3D_HEIGHT));
    panel3D.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                         Integer.MAX_VALUE));
    panel3D.setBackground(Color.WHITE);
    panel3D.setBorder(BorderFactory.createTitledBorder("Model"));
    panel3D.setLayout(new BorderLayout());

    nativeDPI = DEF_NATIVE_DPI;
    dpi = nativeDPI;

    VirtualUniverse universe = new VirtualUniverse();
    javax.media.j3d.Locale locale = new javax.media.j3d.Locale(universe);

    rootBG = new BranchGroup();

    final WakeupCriterion frameCriterion = new WakeupOnElapsedFrames(0, true);
    Behavior frameBehavior = new Behavior() {

        public void initialize() {
          wakeupOn(frameCriterion);
        }

        public void processStimulus(java.util.Enumeration criteria) {

          synchronized (frameTasks) {

            for (Iterator<Runnable> it = frameTasks.iterator(); it.hasNext(); )
              it.next().run();

            frameTasks.clear();
          }

          wakeupOn(frameCriterion);
        }
      };
    frameBehavior.setSchedulingBounds(BOUNDS);
    rootBG.addChild(frameBehavior);

    centerTG = new TransformGroup();
    centerTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    centerTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    centerT3D = new Transform3D();

    tiltTG = new TransformGroup();
    tiltTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    tiltTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    tiltT3D = new Transform3D();

    uncenterTG = new TransformGroup();
    uncenterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    uncenterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    uncenterTG.setCapability(Group.ALLOW_CHILDREN_READ);
    uncenterTG.setCapability(Group.ALLOW_CHILDREN_WRITE);
    uncenterTG.setCapability(Group.ALLOW_CHILDREN_EXTEND);

    uncenterT3D = new Transform3D();

    rootBG.addChild(centerTG);
    centerTG.addChild(tiltTG);
    tiltTG.addChild(uncenterTG);

    BranchGroup viewBG = new BranchGroup();

    viewT3D = new Transform3D();
    viewT3D.setTranslation(new Vector3d(viewCenter.x, viewCenter.y, VIEW_Z));
    viewTG = new TransformGroup(viewT3D);
    viewTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    viewTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    ViewPlatform viewPlatform = new ViewPlatform();
    viewPlatform.setViewAttachPolicy(View.NOMINAL_SCREEN);
    viewTG.addChild(viewPlatform);
    viewBG.addChild(viewTG);
 
    view = new View();

    view.setTrackingEnable(false);
    view.setViewPolicy(View.SCREEN_VIEW);
    view.setProjectionPolicy(View.PARALLEL_PROJECTION);
    view.setScreenScalePolicy(View.SCALE_EXPLICIT);
    view.setWindowResizePolicy(View.VIRTUAL_WORLD);
    view.setWindowMovementPolicy(View.PHYSICAL_WORLD);
    view.setWindowEyepointPolicy(View.RELATIVE_TO_SCREEN);
    view.setVisibilityPolicy(View.VISIBILITY_DRAW_VISIBLE);
    view.setFrontClipPolicy(View.VIRTUAL_SCREEN);
    view.setBackClipPolicy(View.VIRTUAL_SCREEN);

    view.setPhysicalBody(new PhysicalBody());
    view.setPhysicalEnvironment(new PhysicalEnvironment());

    view.attachViewPlatform(viewPlatform);

    view.setFrontClipDistance(FRONT_CLIP);
    view.setBackClipDistance(DEF_BACK_CLIP);

//    Background background = new Background(new Color3f(0.0f, 0.0f, 0.0f));
    Background background = new Background(new Color3f(1.0f, 1.0f, 1.0f));
    background.setApplicationBounds(BOUNDS);

    if (IMMEDIATE_MODE)
      graphicsContext.setBackground(background);
    else
      rootBG.addChild(background);

    //lights
//    Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
//    Vector3f lDir1  = new Vector3f(-1.0f, -1.0f, -1.0f);
//    Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);
//    
//    AmbientLight aLgt = new AmbientLight(alColor);
//    aLgt.setInfluencingBounds(BOUNDS);
//    DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
//    lgt1.setInfluencingBounds(BOUNDS);
//    rootBG.addChild(aLgt);
//    rootBG.addChild(lgt1);

    if (!IMMEDIATE_MODE)
      locale.addBranchGraph(rootBG);

    locale.addBranchGraph(viewBG);

    //nav controls

    Box navBox = Box.createVerticalBox();
    navBox.setBorder(BorderFactory.createTitledBorder("Navigation"));

    Box navControlsBox = Box.createHorizontalBox();

    enablePanButton = new JCheckBox("pan");
    enablePanButton.setBackground(Color.WHITE);
    enablePanButton.setSelected(panEnabled);
    enablePanButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          enablePan(enablePanButton.isSelected());
        } });
    navControlsBox.add(enablePanButton);

    enableZoomButton = new JCheckBox("zoom [alt]");
    enableZoomButton.setBackground(Color.WHITE);
    enableZoomButton.setSelected(zoomEnabled);
    enableZoomButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          enableZoom(enableZoomButton.isSelected());
        } });
    navControlsBox.add(enableZoomButton);

    enableTiltButton = new JCheckBox("tilt [ctl]");
    enableTiltButton.setBackground(Color.WHITE);
    enableTiltButton.setSelected(panEnabled);
    enableTiltButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          enableTilt(enableTiltButton.isSelected());
        } });
    navControlsBox.add(enableTiltButton);

    navControlsBox.add(Box.createHorizontalGlue());

    nativeDPILabel = new JLabel("native DPI: ");
    navControlsBox.add(nativeDPILabel);

    nativeDPIField = new JTextField() { { columnWidth = getColumnWidth(); } };
    nativeDPIField.setHorizontalAlignment(JTextField.RIGHT);
    d = nativeDPIField.getPreferredSize();
    nativeDPIField.setMaximumSize(new Dimension(columnWidth*3, d.height));
    nativeDPIField.setPreferredSize(new Dimension(columnWidth*3, d.height));
    nativeDPIField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { updateNativeDPI(); } });
//    nativeDPIField.addFocusListener(new FocusAdapter() {
//        public void focusLost(FocusEvent e) { updateNativeDPI(); } });

    navControlsBox.add(nativeDPIField);

    navControlsBox.add(Box.createHorizontalGlue());

    fitButton = new JButton("fit");
    fitButton.setBackground(Color.WHITE);
    fitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fit();
        } });
    navControlsBox.add(fitButton);

    Box zoomBox = Box.createHorizontalBox();

    dpiLabel = new JLabel("DPI: ");
    zoomBox.add(dpiLabel);

    dpiField = new JTextField();
    dpiField.setHorizontalAlignment(JTextField.RIGHT);
    d = dpiField.getPreferredSize();
    dpiField.setMaximumSize(new Dimension(columnWidth*ZOOM_FIELD_WIDTH,
                                          d.height));
    dpiField.setPreferredSize(new Dimension(columnWidth*ZOOM_FIELD_WIDTH,
                                            d.height));
    dpiField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { updateDPI(); } });
//    dpiField.addFocusListener(new FocusAdapter() {
//        public void focusLost(FocusEvent e) { updateDPI(); } });
    zoomBox.add(dpiField);

    dpiSlider = new JSlider(MIN_DPI, MAX_DPI);
    dpiSlider.setBackground(Color.WHITE);
    dpiSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          setDPI(dpiSlider.getValue()); } });
    zoomBox.add(dpiSlider);

    oneToOneButton = new JButton("1:1");
    oneToOneButton.setBackground(Color.WHITE);
    oneToOneButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setOneToOne();
        } });
    zoomBox.add(oneToOneButton);

    Box locationBox = Box.createHorizontalBox();

    locationBox.add(Box.createHorizontalGlue());

    StringBuffer tmpBuf = new StringBuffer();

    tmpBuf.append("000.");
    for (int i = 0; i < LOC_FRACTION_DIGITS; i++)
      tmpBuf.append("0");

    String tmp = tmpBuf.toString();

    locationLabelX = new JLabel(tmp);
    locationLabelY = new JLabel(tmp);

    locationBox.add(locationLabelX);
    locationBox.add(new JLabel(", "));
    locationBox.add(locationLabelY);
    locationBox.add(new JLabel(" mm (in)"));

    locationBox.add(Box.createHorizontalGlue());

    fpsLabel = new JLabel("0000.00 FPS", JLabel.RIGHT);
    d = fpsLabel.getPreferredSize();
    fpsLabel.setMinimumSize(d);
    fpsLabel.setMaximumSize(d);
    locationBox.add(fpsLabel);

    navBox.add(navControlsBox);
    navBox.add(zoomBox);
    navBox.add(locationBox);

    Box box = Box.createVerticalBox();
    box.add(panel3D);
    box.add(navBox);

    setLayout(new BorderLayout());
    add(box, "Center");

    Dimension orig = getPreferredSize();

    d = navBox.getPreferredSize();
    navBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
    panel3D.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                         Integer.MAX_VALUE));
    setPreferredSize(orig);

    setResizable(true);

    updateView();

    enablePan(DEF_PAN);
    enableTilt(DEF_TILT);
    enableZoom(DEF_ZOOM);

    if (IMMEDIATE_MODE) {
      renderThread = new RenderThread();
      renderThread.start();
    }
  }

  public void addFrameTask(final Runnable task) {
    frameTasks.add(task);
    view.repaint();
  }

  public void addNotify() {
    super.addNotify();

    canvas3D = new MyCanvas3D(getGC(getParent()));
    view.addCanvas3D(canvas3D);
    panel3D.add(canvas3D, "Center");
  }

  private class RenderThread extends Thread {

    RenderThread() {
      super("visolate render thread");
      setDaemon(true);
    }

    public void run() {

      for (;;) {

//        System.out.println("Starting delay: " + System.currentTimeMillis());

        if (lastTime >= 0) {

          long diffTime;

          for (diffTime = System.currentTimeMillis() - lastTime;
               diffTime < MIN_FRAME_TIME;
               diffTime = System.currentTimeMillis() - lastTime)
            Thread.yield();

        }

        lastTime = System.currentTimeMillis();

//        System.out.println("Starting render: " + System.currentTimeMillis());

        graphicsContext.clear();

        cmt.setIdentity();

        render(rootBG);

        canvas3D.swap();

        canvas3D.postSwap();

//        System.out.println("Finishing render: " + System.currentTimeMillis());
      }
    }

    @SuppressWarnings("unchecked")
	private void render(Node node) {

//      System.out.println("render: " + System.currentTimeMillis());

      if (node instanceof Group) {

        Group group = (Group) node;

        if (group instanceof TransformGroup) {
          cmtWas.set(cmt);
          ((TransformGroup) group).getTransform(temp);
          cmt.mul(temp);
          graphicsContext.setModelTransform(cmt);
        }

        for (Enumeration<Node> e = (Enumeration<Node>) group.getAllChildren(); e.hasMoreElements(); ) {
          render(e.nextElement());
        }

        if (group instanceof TransformGroup) {
          cmt.set(cmtWas);
          graphicsContext.setModelTransform(cmt);
        }

      } else if (node instanceof Shape3D) {

        Shape3D shape = (Shape3D) node;

//        graphicsContext.draw((Shape3D) node);

        Appearance appearance = shape.getAppearance();

//        assert appearance != null;

        if (appearance != null)
          graphicsContext.setAppearance(appearance);

        for (Enumeration e = shape.getAllGeometries(); e.hasMoreElements(); ) {
          Geometry geometry = (Geometry) e.nextElement();
//          assert geometry != null;
          if (geometry != null)
            graphicsContext.draw(geometry);
        }
      }
    }

    private Transform3D cmt = new Transform3D();
    private Transform3D temp = new Transform3D();
    private Transform3D cmtWas = new Transform3D();
    private long lastTime = -1;
  }

  private void updateNativeDPI() {
    try {

      int tmp = Integer.parseInt(nativeDPIField.getText());

      if ((tmp >= MIN_NATIVE_DPI) && (tmp <= MAX_NATIVE_DPI))
        nativeDPI = tmp;
    
      updateView();

    } catch (NumberFormatException e) { }
  }

  private void updateDPI() {
    try {

      int tmp = Integer.parseInt(dpiField.getText());

      if ((tmp >= MIN_DPI) && (tmp <= MAX_DPI))
        dpi = tmp;
      
      updateView();

    } catch (NumberFormatException e) { }
  }

  public double getVirtualCanvasWidth() {
    return ((double) canvas3D.getWidth())/dpi;
  }

  public double getVirtualCanvasHeight() {
    return ((double) canvas3D.getHeight())/dpi;
  }

  public int getCanvasWidth() {
    return canvas3D.getWidth();
  }

  public int getCanvasHeight() {
    return canvas3D.getHeight();
  }

  private void updateView() {
    
    synchronized (view) {

      viewUpdatePending = NUM_UPDATE_FRAMES;

      centerTG.setTransform(centerT3D);
      uncenterTG.setTransform(uncenterT3D);
      
      tiltT3D.setEuler(new Vector3d(tiltX, tiltY, 0.0));
      tiltTG.setTransform(tiltT3D);

      screenScale = 0.0254*dpi/nativeDPI;

      viewT3D.setTranslation(new Vector3d(viewCenter.x, viewCenter.y, VIEW_Z));
      viewTG.setTransform(viewT3D);

      view.setScreenScale(screenScale);
    }

//    view.repaint();

//    System.out.println("view center: " + viewCenter + "\n");

    nativeDPIField.setText(Integer.toString(nativeDPI));
    dpiField.setText(Integer.toString(dpi));
    dpiSlider.setValue(dpi);
  }

  private void setResizable(boolean resizable) {
    (JOptionPane.getFrameForComponent(canvas3D)).setResizable(resizable);
  }

  public void setOneToOne() {
    setDPI(nativeDPI);
  }

  public void fit() {

    Rect bounds = visolate.getModel().getBoardBounds();

    double horizontalDPI = canvas3D.getWidth()/bounds.width;
    double verticalDPI = canvas3D.getHeight()/bounds.height;

    dpi = (int) Math.floor(Math.min(horizontalDPI, verticalDPI));

    viewCenter.x = bounds.x + bounds.width/2;
    viewCenter.y = bounds.y + bounds.height/2;

    centerT3D.setTranslation(new Vector3d(viewCenter.x, viewCenter.y, 0.0));
    uncenterT3D.setTranslation(new Vector3d(-viewCenter.x, -viewCenter.y, 0.0));
    updateView();
  }

  public int getDPI() {
    return dpi;
  }

  public void setDPI(int dpi) {
    this.dpi = dpi;
    updateView();
  }

  public void setCenter(double x, double y) {
    viewCenter.x = x;
    viewCenter.y = y;
    updateView();
  }

  public void waitForViewUpdate() throws InterruptedException {

    long t = System.currentTimeMillis();

    while (viewUpdatePending > 0) {
      Thread.yield();
      
      if ((System.currentTimeMillis() - t) > MAX_UPDATE_FRAME_TIME) {
        view.repaint();
        t = System.currentTimeMillis();
      }
    }
  }

  public void setCenter(Point2d center) {
    viewCenter.set(center);
    updateView();
  }

  public Point2d getCenter(Point2d center) {
    center.set(viewCenter);
    return center;
  }

  public void destroy() {
    //universe.cleanup();
  }

  public void reset() {

    boolean doFit = false;

    if (modelBG == null)
      doFit = true;
    else
      modelBG.detach();
  
    modelBG = visolate.getModel().getSceneGraph();
    
    if (doFit)
      fit();

    Rect bounds = visolate.getModel().getBoardBounds();
    view.setBackClipDistance(10.0 + Math.max(bounds.width, bounds.height));

    uncenterTG.addChild(modelBG);

    enablePanButton.setEnabled(true);
    enableTiltButton.setEnabled(true);
    enableZoomButton.setEnabled(true);
    nativeDPIField.setEnabled(true);
    nativeDPILabel.setEnabled(true);

  }

  public void processFinished() {
    enableControls(true);
    setCenter(viewCenterWas);
//    setResizable(true);
  }

  public void processStarted() { 
    enableControls(false);
    viewCenterWas.set(viewCenter);
//    setResizable(false);
  }

  public void enableControls(boolean enable) {

    if (!enable) {

      panWasEnabled = panEnabled;
      tiltWasEnabled = tiltEnabled;
      zoomWasEnabled = zoomEnabled;

      enablePan(false);
      enableTilt(false);
      enableZoom(false);

    } else {
      enablePan(panWasEnabled);
      enableTilt(tiltWasEnabled);
      enableZoom(zoomWasEnabled);
    }

    enablePanButton.setEnabled(enable);
    enableTiltButton.setEnabled(enable);
    enableZoomButton.setEnabled(enable);
    nativeDPIField.setEnabled(enable);
    nativeDPILabel.setEnabled(enable);
  }

  private void enableZoom(boolean enable) {

    if (enableZoomButton.isSelected() != enable)
      enableZoomButton.setSelected(enable);

    if (enable == zoomEnabled)
      return;
    
    zoomEnabled = enable;

    fitButton.setEnabled(enable);
    oneToOneButton.setEnabled(enable);
    dpiLabel.setEnabled(enable);
    dpiField.setEnabled(enable);
    dpiSlider.setEnabled(enable);
  }

  private void enablePan(boolean enable) {

    if (enablePanButton.isSelected() != enable)
      enablePanButton.setSelected(enable);

    if (enable == panEnabled)
      return;

    panEnabled = enable;
  }

  private void enableTilt(boolean enable) {
    
    if (enableTiltButton.isSelected() != enable)
      enableTiltButton.setSelected(enable);

    if (enable == tiltEnabled)
      return;

    tiltEnabled = enable;

    if (!enable) {
      tiltX = 0.0;
      tiltY = 0.0;
      updateView();
    }
  }

  private void updateZoom(int delta, boolean accel) {

    if (!zoomEnabled)
      return;

    delta *= ACCEL_FACTOR;

    if (accel)
      delta *= ACCEL_FACTOR;

//    System.out.println("updateZoom: " + delta);

    int newDPI = dpi+delta;

    if (newDPI < MIN_DPI)
      newDPI = MIN_DPI;
    if (newDPI > MAX_DPI)
      newDPI = MAX_DPI;

    dpi = newDPI;

    updateView();
  }

  private void updatePan(int deltaX, int deltaY, boolean accel) {

    if (!panEnabled)
      return;

    if (accel) {
      deltaX *= ACCEL_FACTOR;
      deltaY *= ACCEL_FACTOR;
    }

//    System.out.println("updatePan: " + deltaX + ", " + deltaY);

    viewCenter.x -= deltaX/((double) dpi);
    viewCenter.y -= deltaY/((double) dpi);
    
    updateView();
  }

  private void updateTilt(int deltaX, int deltaY, boolean accel) {

    if (!tiltEnabled)
      return;

    if (accel) {
      deltaX *= ACCEL_FACTOR;
      deltaY *= ACCEL_FACTOR;
    }

//    System.out.println("updateTilt: " + deltaX + ", " + deltaY);

    tiltY += deltaX/((double) dpi);
    tiltX += deltaY/((double) dpi);
    
    updateView();
  }

  public BufferedImage makeBufferedImage() {
    return makeBufferedImage(canvas3D.getWidth(), canvas3D.getHeight());
  }

  public BufferedImage makeBufferedImage(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }
   
  public BufferedImage getStill()
    throws InterruptedException {
    
//    return getStill(makeBufferedImage()); //work around j3d bug

    int width = canvas3D.getWidth(); 
    int height = canvas3D.getHeight();
    
    ImageComponent2D imageComponent =
      new ImageComponent2D(ImageComponent.FORMAT_RGB,
                           width, height,
                           false, //byRef
                           false); //yUp
    
    raster =
      new javax.media.j3d.Raster(new Point3f(0.0f, 0.0f, 0.0f),
                                 javax.media.j3d.Raster.RASTER_COLOR, 
                                 0, 0,
                                 width, height,
                                 imageComponent,
                                 null); 

    waitForViewUpdate();

    readRasterPending = true;

    view.repaint();

    while (readRasterPending)
      Thread.yield();

    (canvas3D.getGraphicsContext3D()).readRaster(raster); 

    return raster.getImage().getImage();
  }

//work around j3d bug
//  public BufferedImage getStill(BufferedImage bufferedImage)
//    throws InterruptedException {
//
//    int width = bufferedImage.getWidth();
//    int height = bufferedImage.getHeight();
//
//    ImageComponent2D imageComponent =
//      new ImageComponent2D(ImageComponent.FORMAT_RGB,
//                           bufferedImage,
//                           true, //byRef
//                           false); //yUp
//    
//    raster =
//      new javax.media.j3d.Raster(new Point3f(0.0f, 0.0f, 0.0f),
//                                 javax.media.j3d.Raster.RASTER_COLOR, 
//                                 0, 0,
//                                 width, height,
//                                 imageComponent,
//                                 null); 
//
//    waitForViewUpdate();
//
//    readRasterPending = true;
//
//    view.repaint();
//
//    while (readRasterPending)
//      Thread.yield();
//
//    (canvas3D.getGraphicsContext3D()).readRaster(raster); 
//
//    return bufferedImage;
//  }

  /**
   * @return coordinate under the mouse in Inch.
   */
  private double mouseX(final MouseEvent e) {
    return e.getX() / ((double) dpi) + viewCenter.x - getVirtualCanvasWidth()/2;
  }

  /**
   * @return coordinate under the mouse in Inch.
   */
  private double mouseY(final MouseEvent e) {
    return
      (canvas3D.getHeight() - e.getY()) / ((double) dpi) +
      viewCenter.y - getVirtualCanvasHeight()/2;
  }

  private class MyCanvas3D extends Canvas3D {
                
    MyCanvas3D(GraphicsConfiguration gc) {

      super(gc);

      graphicsContext = getGraphicsContext3D();
      setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      setBackground(Color.BLACK);
//      setSize(DEF_WIDTH, DEF_3D_HEIGHT);
      setMonoscopicViewPolicy(View.CYCLOPEAN_EYE_VIEW);
      setStereoEnable(false);
      setLeftManualEyeInImagePlate(EYE);
      setRightManualEyeInImagePlate(EYE);

      if (IMMEDIATE_MODE)
        stopRenderer();
      
      addMouseListener(new MouseAdapter() {
          
          public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
          } 
          
          public void mouseClicked(MouseEvent e) {
            visolate.mouseClicked(mouseX(e), mouseY(e), e.getModifiersEx());
          } });
      
      addMouseMotionListener(new MouseMotionAdapter() {
          
          private DecimalFormat locFormat = new DecimalFormat();
          
            {
              locFormat.setMaximumFractionDigits(LOC_FRACTION_DIGITS);
              locFormat.setMinimumFractionDigits(LOC_FRACTION_DIGITS);
            }
          
          public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            
//            System.out.println("mouse dragged (" + x + ", " + y +
//                               ") modifiers: " +
//                               e.getMouseModifiersText(e.getModifiers()) +
//                               "; modifiersEx: " +
//                               e.getModifiersExText(e.getModifiersEx()));

            if ((e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK | 
                                       MouseEvent.BUTTON2_DOWN_MASK)) != 0)
              updateZoom(y-lastY, e.isShiftDown());
            else if ((e.getModifiersEx() & (MouseEvent.CTRL_DOWN_MASK | 
                                            MouseEvent.BUTTON3_DOWN_MASK)) != 0)
              updateTilt(x-lastX, y-lastY, e.isShiftDown());
            else
              updatePan(x-lastX, lastY-y, e.isShiftDown());
            lastX = x;
            lastY = y;
          }
          
          public void mouseMoved(MouseEvent e) {
            locationLabelX.setText(locFormat.format(25.4 * mouseX(e)) + "(" + locFormat.format(mouseX(e)) + ")");
            locationLabelY.setText(locFormat.format(25.4 * mouseY(e)) + "(" + locFormat.format(mouseY(e)) + ")");
            
          } });
      
      addMouseWheelListener(new MouseWheelListener() {
          public void mouseWheelMoved(MouseWheelEvent e) {
            updateZoom(e.getWheelRotation(), e.isShiftDown());
          } });
      
      addKeyListener(new KeyAdapter() {

          public void keyReleased(KeyEvent e) {

            if (!e.isAltDown()) {
              switch (e.getKeyCode()) {
              case KeyEvent.VK_LEFT:
                updatePan(-1, 0, e.isShiftDown()); return;
              case KeyEvent.VK_RIGHT:
                updatePan(+1, 0, e.isShiftDown()); return;
              case KeyEvent.VK_UP:
                updatePan(0, +1, e.isShiftDown()); return;
              case KeyEvent.VK_DOWN:
                updatePan(0, -1, e.isShiftDown()); return;
              }
            } else {
              switch (e.getKeyCode()) {
              case KeyEvent.VK_LEFT:
                updateTilt(-1, 0, e.isShiftDown()); return;
              case KeyEvent.VK_RIGHT:
                updateTilt(+1, 0, e.isShiftDown()); return;
              case KeyEvent.VK_UP:
                updateTilt(0, +1, e.isShiftDown()); return;
              case KeyEvent.VK_DOWN:
                updateTilt(0, -1, e.isShiftDown()); return;
              }
            }

            visolate.keyReleased(e);
          } 

          public void keyTyped(KeyEvent e) {
          }
        });

      addComponentListener(new ComponentAdapter() {
          public void componentResized(ComponentEvent e) {
            updateView();
          } });

    }

    public void postSwap() {

      super.postSwap();

      synchronized (view) {
        if (viewUpdatePending > 0)
          viewUpdatePending--;
      }

      if (readRasterPending) {
        graphicsContext.readRaster(raster);
        readRasterPending = false;
      }

      long newTime = System.currentTimeMillis();
      
      if (lastTime >= 0) {
        double fps = 1000.0/(newTime-lastTime);
//        System.out.println("FPS: " + fps);
        fpsLabel.setText(fpsFormat.format(fps) + " FPS");
      }

      lastTime = newTime;
    }

    private long lastTime = -1;
  }


  private GraphicsConfiguration getGC(Container container) {

    GraphicsDevice graphicsDevice =
      (container != null) ?
      container.getGraphicsConfiguration().getDevice() :
      GraphicsEnvironment.
      getLocalGraphicsEnvironment().
      getDefaultScreenDevice();

    try {

      return getGC(graphicsDevice);

    } catch (RuntimeException e) {

      GraphicsDevice[] gs = 
        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

      RuntimeException ex = null;

      for (int j = 0; j < gs.length; j++) { 
        try {
          return getGC(gs[j]);
        } catch (RuntimeException e2) {
          ex = e2;
        }
      }
      
      throw ex;
    }
  }

  private GraphicsConfiguration getGC(GraphicsDevice graphicsDevice) {

    System.out.println("graphics device: " + graphicsDevice.getIDstring());

    int[][] format = new int[][] {{8, 8, 8}, {5, 6, 5}};
    int[] depth = new int[] {24, 16, 8};

    GraphicsConfigTemplate3D gct = new GraphicsConfigTemplate3D(); 
    gct.setDoubleBuffer(GraphicsConfigTemplate3D.PREFERRED);
    gct.setStereo(GraphicsConfigTemplate3D.UNNECESSARY);
    gct.setSceneAntialiasing(GraphicsConfigTemplate3D.UNNECESSARY);
    GraphicsConfiguration fallback = graphicsDevice.getBestConfiguration(gct);

    GraphicsConfiguration gc = null;

    for (int f = 0; f < format.length; f++) {
      for (int d = 0; d < depth.length; d++) {

        gct.setRedSize(format[f][0]);
        gct.setGreenSize(format[f][1]);
        gct.setBlueSize(format[f][2]);

        gct.setDepthSize(depth[d]);

        gc = graphicsDevice.getBestConfiguration(gct);

        if (gc != null)
          break;
      }
    }

    if (gc == null)
      gc = fallback;

    if (gc == null)
      throw
        new RuntimeException("FATAL ERROR: failed to initialize 3D graphics");

    rBits = gc.getColorModel().getComponentSize(0);
    gBits = gc.getColorModel().getComponentSize(1);
    bBits = gc.getColorModel().getComponentSize(2);

    dBits = gct.getDepthSize();

    rCeil = 1 << rBits;
    gCeil = 1 << gBits;
    bCeil = 1 << bBits;
    
//    System.out.println("ceilings: " +
//                       rCeil + ", " + gCeil + ", " + bCeil);
    
    rSpread = 256/rCeil;
    gSpread = 256/gCeil;
    bSpread = 256/bCeil;
    
//    System.out.println("spreads: " +
//                       rSpread + ", " + gSpread + ", " + bSpread);
      
    System.out.println("pixel format " +
                       rBits + ":" + gBits + ":" + bBits +
                       " " + dBits + "-bit depth");
    return gc;
  }

  public int getRBits() {
    return rBits;
  }

  public int getGBits() {
    return gBits;
  }

  public int getBBits() {
    return bBits;
  }

  public int getDepthBits() {
    return dBits;
  }

  public static double intensity(double r, double g, double b) {
    return (r+g+b)/3.0;
  }

  public static double intensity(Color3b c) {
    return ((c.x & 0xff)/255.0 +
            (c.y & 0xff)/255.0 +
            (c.z & 0xff)/255.0)/3.0;
  }

  private static double rnd(double min, double max) {

    min = Math.max(0.0, min);
    max = Math.min(1.0, max);

    return min + Math.random()*(max-min);
  }

  private static void randomSwap(double[] c) {

    int i = (int) Math.floor(Math.random()*c.length);
    int j = (int) Math.floor(Math.random()*c.length);

    double t = c[i];
    c[i] = c[j];
    c[j] = t;
  }

  public Color3b getRandomColor() {

    double y = 0.5;

    double sum = 3.0*y;

    double[] c = new double[3];

    c[0] = rnd(sum-2.0, sum);

    c[1] = rnd(sum-c[0]-1.0, sum-c[0]);

    c[2] = sum-(c[0]+c[1]);

    randomSwap(c);
    randomSwap(c);
    randomSwap(c);
    randomSwap(c);

    double r = c[0];
    double g = c[1];
    double b = c[2];

    return new Color3b((byte) (((int) Math.floor(r*rCeil))*rSpread),
                       (byte) (((int) Math.floor(g*gCeil))*gSpread),
                       (byte) (((int) Math.floor(b*bCeil))*bSpread));
  }

  private Visolate visolate;

  private JPanel panel3D;

  private Canvas3D canvas3D;

  private int nativeDPI;
  private int dpi;
  private double screenScale;

  private BranchGroup rootBG;
  private TransformGroup centerTG;
  private TransformGroup uncenterTG;
  private TransformGroup tiltTG;
  private BranchGroup modelBG = null;

  private Transform3D centerT3D;
  private Transform3D uncenterT3D;
  private Transform3D tiltT3D;

  private Point2d viewCenter = new Point2d();
  private Point2d viewCenterWas = new Point2d();

  private Transform3D viewT3D;
  private TransformGroup viewTG;

  private View view;

  private JButton fitButton;
  private JButton oneToOneButton;
  private JTextField nativeDPIField;
  private JTextField dpiField;
  private JLabel nativeDPILabel;
  private JSlider dpiSlider;
  private int columnWidth;

  private JCheckBox enableZoomButton;
  private JCheckBox enableTiltButton;
  private JCheckBox enablePanButton;

  private boolean zoomEnabled = true;
  private boolean panEnabled = true;
  private boolean tiltEnabled = true;

  private boolean zoomWasEnabled = true;
  private boolean tiltWasEnabled = true;
  private boolean panWasEnabled = true;

  private double tiltX = 0.0;
  private double tiltY = 0.0;

  private int lastX;
  private int lastY;

  private JLabel locationLabelX;
  private JLabel locationLabelY;

  private JLabel dpiLabel;

  private int viewUpdatePending = NUM_UPDATE_FRAMES;

  private javax.media.j3d.Raster raster = null;
  private boolean readRasterPending = false;

  private int rBits, gBits, bBits, dBits;
  private int rCeil, gCeil, bCeil;
  private int rSpread, gSpread, bSpread;

  private NumberFormat fpsFormat =
  new DecimalFormat() { {
    setMaximumFractionDigits(2); 
    setMinimumFractionDigits(2); } };

  private JLabel fpsLabel;

  private GraphicsContext3D graphicsContext;

  private Thread renderThread;

  private Collection<Runnable> frameTasks =
  Collections.synchronizedList(new LinkedList<Runnable>());
}
