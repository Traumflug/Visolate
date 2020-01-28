/* Generated By:JavaCC: Do not edit this line. Parser.java */
package visolate.parser;

import visolate.simulator.*;
import java.util.*;

public class Parser implements ParserConstants {

  public static final int HALT = 0;
  public static final int OPTSTOP = 1;

  public void setSimulator(Simulator simulator) {
    this.simulator = simulator;
  }

  public static void main(String args[]) throws ParseException {
    Parser parser = new Parser(System.in);
    parser.setSimulator(new Simulator());
    parser.Input();
  }

  private static double getModifier(List<Double> modifiers, int index) {
    return ((Double) modifiers.get(index)).doubleValue();
  }

  private Simulator simulator;

  final public void Input() throws ParseException {
    boolean stop = false;
    label_1: while (true) {
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case PARAM_DELIM:
      case CODE_DELIM:
      case COMMENT:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 43:
      case 44:
      case 45:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case PARAM_DELIM:
        Parameter();
        break;
      case CODE_DELIM:
      case COMMENT:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 43:
      case 44:
      case 45:
        stop = Code();
        if (stop)
          return;
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(0);
  }

  final public void Parameter() throws ParseException {
    jj_consume_token(PARAM_DELIM);
    label_2: while (true) {
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case IN:
      case LN:
      case ADD:
      case AM:
      case 46:
      case 47:
      case 54:
      case 60:
      case 63:
      case 65:
      case 66:
      case 67:
      case 68:
      case 69:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      ParameterCode();
    }
    jj_consume_token(PARAM_DELIM);
  }

  final public boolean Code() throws ParseException {
    int stop = -1;
    Token seq = null;
    Token t;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 20:
      jj_consume_token(20);
      seq = jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case COMMENT:
    case 21:
    case 22:
    case 23:
    case 24:
    case 25:
    case 26:
    case 27:
    case 28:
    case 29:
    case 30:
    case 31:
    case 32:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 43:
    case 44:
    case 45:
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case COMMENT:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32:
      case 33:
        GCode();
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
        case 34:
        case 35:
        case 36:
        case 37:
        case 38:
        case 39:
        case 40:
        case 41:
          DCode();
          break;
        default:
          jj_la1[4] = jj_gen;
          ;
        }
        break;
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
        DCode();
        break;
      case 43:
      case 44:
      case 45:
        stop = MCode();
        break;
      default:
        jj_la1[5] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
    t = jj_consume_token(CODE_DELIM);

    return (stop == HALT) || ((stop == OPTSTOP)
        && !simulator.askContinue(t.beginLine, ((seq != null) ? Integer.parseInt(seq.image) : -1)));
  }

  final public void GCode() throws ParseException {
    Token txt;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 21:
      jj_consume_token(21);
      simulator.setMode(Simulator.RAPID);
      break;
    case 22:
      jj_consume_token(22);
      simulator.setMode(Simulator.LINEAR);
      break;
    case 23:
      jj_consume_token(23);
      simulator.setMode(Simulator.CW);
      break;
    case 24:
      jj_consume_token(24);
      simulator.setMode(Simulator.CCW);
      break;
    case COMMENT:
      jj_consume_token(COMMENT);
      txt = jj_consume_token(TEXT);
      simulator.comment(txt.beginLine, txt.image);
      break;
    case 25:
      jj_consume_token(25);
      simulator.setMode(Simulator.POLYGON);
      break;
    case 26:
      jj_consume_token(26);
      simulator.setMode(Simulator.LINEAR);
      break;
    case 27:
      jj_consume_token(27);
      break;
    case 28:
      jj_consume_token(28);
      break;
    case 29:
      jj_consume_token(29);
      simulator.setMetric();
      break;
    case 30:
      jj_consume_token(30);
      simulator.set360(false);
      break;
    case 31:
      jj_consume_token(31);
      simulator.set360(true);
      break;
    case 32:
      jj_consume_token(32);
      simulator.setIncremental(false);
      break;
    case 33:
      jj_consume_token(33);
      simulator.setIncremental(true);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void DCode() throws ParseException {
    boolean flash = false;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
    case 39:
    case 40:
    case 41:
      CoordinateDCode();
      break;
    case 34:
    case 35:
    case 36:
    case 37:
      flash = NonCoordinateDCode();
      if (flash)
        simulator.addFlash();
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void CoordinateDCode() throws ParseException {
    String[] c = new String[4];
    Coordinates(c);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 34:
    case 35:
    case 36:
    case 37:
      NonCoordinateDCode();
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
    if ((c[2] != null) || (c[3] != null))
      simulator.setCenter(c[2], c[3]);

    if ((c[0] != null) || (c[1] != null))
      simulator.setPosition(c[0], c[1]);
  }

  final public boolean NonCoordinateDCode() throws ParseException {
    Token aperture = null;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 34:
      jj_consume_token(34);
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case DIGITS:
        aperture = jj_consume_token(DIGITS);
        break;
      default:
        jj_la1[10] = jj_gen;
        ;
      }
      if (aperture == null)
        simulator.setExposure(Simulator.OPEN);
      else
        simulator.setAperture(Integer.parseInt("1" + aperture.image));

      return false;

    case 35:
      jj_consume_token(35);
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case DIGITS:
        aperture = jj_consume_token(DIGITS);
        break;
      default:
        jj_la1[11] = jj_gen;
        ;
      }
      if (aperture == null)
        simulator.setExposure(Simulator.CLOSED);
      else
        simulator.setAperture(Integer.parseInt("2" + aperture.image));

      return false;

    case 36:
      jj_consume_token(36);
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case DIGITS:
        aperture = jj_consume_token(DIGITS);
        break;
      default:
        jj_la1[12] = jj_gen;
        ;
      }
      if (aperture == null)
        simulator.setExposure(Simulator.FLASH);
      else
        simulator.setAperture(Integer.parseInt("3" + aperture.image));

      return true;

    case 37:
      jj_consume_token(37);
      aperture = jj_consume_token(DIGITS);
      simulator.setAperture(Integer.parseInt(aperture.image));
      return false;

    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    // never reached
  }

  final public void Coordinates(String[] c) throws ParseException {
    label_3: while (true) {
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 38:
        XCoord(c);
        break;
      case 39:
        YCoord(c);
        break;
      case 40:
        ICoord(c);
        break;
      case 41:
        JCoord(c);
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 38:
      case 39:
      case 40:
      case 41:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_3;
      }
    }
  }

  final public void XCoord(String[] c) throws ParseException {
    jj_consume_token(38);
    c[0] = Coord();
  }

  final public void YCoord(String[] c) throws ParseException {
    jj_consume_token(39);
    c[1] = Coord();
  }

  final public void ICoord(String[] c) throws ParseException {
    jj_consume_token(40);
    c[2] = Coord();
  }

  final public void JCoord(String[] c) throws ParseException {
    jj_consume_token(41);
    c[3] = Coord();
  }

  final public String Coord() throws ParseException {
    Token sign = null, digits;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case SIGN:
      sign = jj_consume_token(SIGN);
      break;
    default:
      jj_la1[16] = jj_gen;
      ;
    }
    digits = jj_consume_token(DIGITS);

    return ((sign != null) ? sign.image : "") + digits.image;
  }

  final public double Double() throws ParseException {
    Token sign = null, digitsBefore = null, digitsAfter = null;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case SIGN:
      sign = jj_consume_token(SIGN);
      break;
    default:
      jj_la1[17] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case DIGITS:
      digitsBefore = jj_consume_token(DIGITS);
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 42:
        jj_consume_token(42);
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
        case DIGITS:
          digitsAfter = jj_consume_token(DIGITS);
          break;
        default:
          jj_la1[18] = jj_gen;
          ;
        }
        break;
      default:
        jj_la1[19] = jj_gen;
        ;
      }
      break;
    case 42:
      jj_consume_token(42);
      digitsAfter = jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[20] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    double value = Double.parseDouble(
        ((digitsBefore != null) ? digitsBefore.image : "") + ((digitsAfter != null) ? "." + digitsAfter.image : ""));

    if ((sign != null) && ("-".equals(sign.image)))
      value = -value;

    return value;
  }

  final public int MCode() throws ParseException {
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 43:
      jj_consume_token(43);

      return HALT;

    case 44:
      jj_consume_token(44);

      return OPTSTOP;

    case 45:
      jj_consume_token(45);

      return HALT;

    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    // never reached
  }

  final public void ParameterCode() throws ParseException {
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 46:
      InputEncoding();
      break;
    case 67:
      Offset();
      break;
    case 54:
      FormatStatement();
      break;
    case 60:
      ImagePolarity();
      break;
    case 63:
      LayerPolarity();
      break;
    case ADD:
      ApertureDescription();
      break;
    case AM:
      ApertureMacro();
      break;
    case 47:
      AxisSelect();
      break;
    case IN:
      ImageName();
      break;
    case 65:
    case 66:
      Mode();
      break;
    case 68:
      ScaleFactor();
      break;
    case LN:
      LayerName();
      break;
    case 69:
      StepAndRepeat();
      break;
    default:
      jj_la1[22] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void InputEncoding() throws ParseException {
    Token e;
    jj_consume_token(46);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 47:
      e = jj_consume_token(47);
      break;
    case 48:
      e = jj_consume_token(48);
      break;
    case 49:
      e = jj_consume_token(49);
      break;
    case 50:
      e = jj_consume_token(50);
      break;
    case 51:
      e = jj_consume_token(51);
      break;
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(CODE_DELIM);
    if (!"AS".equals(e.image) && !"IS".equals(e.image))
      System.err.println("WARNING: unsupported image encoding \"" + e.image + "\"");
  }

  final public void AxisSelect() throws ParseException {
    Token a, b;
    jj_consume_token(47);
    jj_consume_token(52);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
      a = jj_consume_token(38);
      break;
    case 39:
      a = jj_consume_token(39);
      break;
    default:
      jj_la1[24] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(53);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
      b = jj_consume_token(38);
      break;
    case 39:
      b = jj_consume_token(39);
      break;
    default:
      jj_la1[25] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(CODE_DELIM);
    System.err.println("WARNING: ignoring axis select ASA" + a.image + "B" + b.image);
  }

  final public void FormatStatement() throws ParseException {
    Token ignore, mode, fmt;
    jj_consume_token(54);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 55:
    case 56:
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 55:
        ignore = jj_consume_token(55);
        break;
      case 56:
        ignore = jj_consume_token(56);
        break;
      default:
        jj_la1[26] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      if ("T".equals(ignore.image))
        simulator.ignoreTrailingZeros();
      break;
    default:
      jj_la1[27] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 40:
    case 52:
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 52:
        mode = jj_consume_token(52);
        break;
      case 40:
        mode = jj_consume_token(40);
        break;
      default:
        jj_la1[28] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      if ("I".equals(mode.image))
        simulator.setIncremental(true);
      else
        simulator.setIncremental(false);
      break;
    default:
      jj_la1[29] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 20:
      jj_consume_token(20);
      jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[30] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 57:
      jj_consume_token(57);
      jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[31] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
      jj_consume_token(38);
      fmt = jj_consume_token(DIGITS);
      if (fmt.image.length() != 2) {
        System.err.println("WARNING: ignoring bad X format, not 2 digits: " + fmt);
      } else {
        simulator.setFormatX(Integer.parseInt(fmt.image.substring(0, 1)), Integer.parseInt(fmt.image.substring(1, 2)));
      }
      break;
    default:
      jj_la1[32] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 39:
      jj_consume_token(39);
      fmt = jj_consume_token(DIGITS);
      if (fmt.image.length() != 2) {
        System.err.println("WARNING: ignoring bad Y format, not 2 digits: " + fmt);
      } else {
        simulator.setFormatY(Integer.parseInt(fmt.image.substring(0, 1)), Integer.parseInt(fmt.image.substring(1, 2)));
      }
      break;
    default:
      jj_la1[33] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 58:
      jj_consume_token(58);
      jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[34] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 37:
      jj_consume_token(37);
      jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[35] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 59:
      jj_consume_token(59);
      jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[36] = jj_gen;
      ;
    }
    jj_consume_token(CODE_DELIM);
  }

  final public void ImageName() throws ParseException {
    Token name;
    jj_consume_token(IN);
    name = jj_consume_token(TEXT);
    jj_consume_token(CODE_DELIM);
    System.out.println("image name: " + name.image);
  }

  final public void ImagePolarity() throws ParseException {
    Token polarity;
    jj_consume_token(60);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 61:
      polarity = jj_consume_token(61);
      break;
    case 62:
      polarity = jj_consume_token(62);
      break;
    default:
      jj_la1[37] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(CODE_DELIM);
    System.err.println("WARNING: ignoring image polarity IP" + polarity.image);
  }

  final public void LayerName() throws ParseException {
    Token name;
    jj_consume_token(LN);
    name = jj_consume_token(TEXT);
    jj_consume_token(CODE_DELIM);
    System.out.println("layer name: " + name.image);
  }

  final public void LayerPolarity() throws ParseException {
    Token polarity;
    jj_consume_token(63);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 64:
      polarity = jj_consume_token(64);
      break;
    case 37:
      polarity = jj_consume_token(37);
      break;
    default:
      jj_la1[38] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(CODE_DELIM);
    System.err.println("WARNING: ignoring layer polarity LP" + polarity.image);
  }

  final public void Mode() throws ParseException {
    Token mode;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 65:
      mode = jj_consume_token(65);
      break;
    case 66:
      mode = jj_consume_token(66);
      break;
    default:
      jj_la1[39] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(CODE_DELIM);
    if ("MOMM".equals(mode.image))
      simulator.setMetric();
  }

  final public void Offset() throws ParseException {
    double a = Double.NaN, b = Double.NaN;
    jj_consume_token(67);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 52:
      jj_consume_token(52);
      a = Double();
      break;
    default:
      jj_la1[40] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 53:
      jj_consume_token(53);
      b = Double();
      break;
    default:
      jj_la1[41] = jj_gen;
      ;
    }
    jj_consume_token(CODE_DELIM);
    System.err.println(
        "WARNING: ignoring offset OF" + ((!Double.isNaN(a)) ? ("A" + a) : "") + ((!Double.isNaN(b)) ? ("B" + b) : ""));
  }

  final public void ScaleFactor() throws ParseException {
    double a = Double.NaN, b = Double.NaN;
    jj_consume_token(68);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 52:
      jj_consume_token(52);
      a = Double();
      break;
    default:
      jj_la1[42] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 53:
      jj_consume_token(53);
      b = Double();
      break;
    default:
      jj_la1[43] = jj_gen;
      ;
    }
    jj_consume_token(CODE_DELIM);
    System.err.println("WARNING: ignoring scale factor SF" + ((!Double.isNaN(a)) ? ("A" + a) : "")
        + ((!Double.isNaN(b)) ? ("B" + b) : ""));
  }

  final public void StepAndRepeat() throws ParseException {
    Token x = null;
    Token y = null;
    double i = Double.NaN;
    double j = Double.NaN;
    jj_consume_token(69);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
      jj_consume_token(38);
      x = jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[44] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 39:
      jj_consume_token(39);
      y = jj_consume_token(DIGITS);
      break;
    default:
      jj_la1[45] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 40:
      jj_consume_token(40);
      i = Double();
      break;
    default:
      jj_la1[46] = jj_gen;
      ;
    }
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 41:
      jj_consume_token(41);
      j = Double();
      break;
    default:
      jj_la1[47] = jj_gen;
      ;
    }
    jj_consume_token(CODE_DELIM);
    System.err.println("WARNING: ignoring step and repeat SR" + ((x != null) ? ("X" + x.image) : "")
        + ((y != null) ? ("Y" + y.image) : "") + ((!Double.isNaN(i)) ? ("I" + i) : "")
        + ((!Double.isNaN(j)) ? ("J" + j) : ""));
  }

  final public void ApertureDescription() throws ParseException {
    Token number;
    Token typeToken;
    List<Double> modifiers = new LinkedList<Double>();
    jj_consume_token(ADD);
    number = jj_consume_token(DIGITS2);
    typeToken = jj_consume_token(ALNUMS);
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 70:
      jj_consume_token(70);
      ModifierList(modifiers);
      break;
    default:
      jj_la1[48] = jj_gen;
      ;
    }
    jj_consume_token(CODE_DELIM);
    int num = Integer.parseInt(number.image);

    String type = typeToken.image;

    if ("C".equals(type)) {

      switch (modifiers.size()) {
      case 0:
        System.err.println("WARNING: ignoring circle aperture with no modifiers on line " + number.beginLine);
        break;
      case 1:
        simulator.addAperture(new CircleAperture(num, getModifier(modifiers, 0)));
        break;
      case 2:
        simulator.addAperture(new CircleAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1)));
        break;
      case 3:
        simulator.addAperture(
            new CircleAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1), getModifier(modifiers, 2)));
        break;
      default:
        System.err.println("WARNING: ignoring circle aperture with extra modifiers on line " + number.beginLine);
        break;
      }

    } else if ("R".equals(type)) {

      switch (modifiers.size()) {
      case 0:
      case 1:
        System.err.println("WARNING: ignoring rectangle aperture with < 2 modifiers on line " + number.beginLine);
        break;
      case 2:
        simulator.addAperture(new RectangleAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1)));
        break;
      case 3:
        simulator.addAperture(new RectangleAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1),
            getModifier(modifiers, 2)));

        break;
      case 4:
        simulator.addAperture(new RectangleAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1),
            getModifier(modifiers, 2), getModifier(modifiers, 3)));
        break;
      default:
        System.err.println("WARNING: ignoring rectangle aperture with extra modifiers on line " + number.beginLine);
        break;
      }

    } else if ("O".equals(type)) {

      switch (modifiers.size()) {
      case 0:
      case 1:
        System.err.println("WARNING: ignoring obround aperture with < 2 modifiers on line " + number.beginLine);
        break;
      case 2:
        simulator.addAperture(new ObroundAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1)));
        break;
      case 3:
        simulator.addAperture(
            new ObroundAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1), getModifier(modifiers, 2)));
        break;
      case 4:
        simulator.addAperture(new ObroundAperture(num, getModifier(modifiers, 0), getModifier(modifiers, 1),
            getModifier(modifiers, 2), getModifier(modifiers, 3)));
        break;
      default:
        System.err.println("WARNING: ignoring obround aperture with extra modifiers on line " + number.beginLine);
        break;
      }

    } else if ("P".equals(type)) {

      switch (modifiers.size()) {
      case 0:
      case 1:
        System.err.println("WARNING: ignoring polygon aperture with < 2 modifiers on line " + number.beginLine);
        break;
      case 2:
        simulator.addAperture(new PolygonAperture(num, getModifier(modifiers, 0), (int) getModifier(modifiers, 1)));
        break;
      case 3:
        simulator.addAperture(new PolygonAperture(num, getModifier(modifiers, 0), (int) getModifier(modifiers, 1),
            getModifier(modifiers, 2)));
        break;
      case 4:
        simulator.addAperture(new PolygonAperture(num, getModifier(modifiers, 0), (int) getModifier(modifiers, 1),
            getModifier(modifiers, 2), getModifier(modifiers, 3)));
      case 5:
        simulator.addAperture(new PolygonAperture(num, getModifier(modifiers, 0), (int) getModifier(modifiers, 1),
            getModifier(modifiers, 2), getModifier(modifiers, 3), getModifier(modifiers, 4)));
        break;
      default:
        System.err.println("WARNING: ignoring polygon aperture with extra modifiers on line " + number.beginLine);
        break;
      }

    } else {

      try {
        simulator.addAperture(new MacroAperture(Integer.parseInt(number.image), simulator.getMacro(type), modifiers));

      } catch (NoSuchElementException e) {
        System.err.println("WARNING: ignoring aperture definition on line " + number.beginLine
            + " which references unknown aperture macro \"" + type + "\"");
      }
    }
  }

  final public void ModifierList(List<Double> list) throws ParseException {
    double modifier;
    modifier = Double();
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
      jj_consume_token(38);
      ModifierList(list);
      break;
    default:
      jj_la1[49] = jj_gen;
      ;
    }
    list.add(0, new Double(modifier));
  }

  final public void ApertureMacro() throws ParseException {
    Token name;
    List<MacroPrimitive> primitives = new LinkedList<MacroPrimitive>();
    jj_consume_token(AM);
    name = jj_consume_token(TEXT);
    jj_consume_token(CODE_DELIM);
    PrimitiveList(primitives);
    simulator.addMacro(new Macro(name.image, primitives));
  }

  final public void PrimitiveList(final List<MacroPrimitive> primitives) throws ParseException {
    MacroPrimitive primitive;
    primitive = Primitive();
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case CODE_DELIM:
    case DIGITS:
      PrimitiveList(primitives);
      break;
    default:
      jj_la1[50] = jj_gen;
      ;
    }
    if (primitive != null) {
      primitives.add(0, primitive);
    }
  }

  final public MacroPrimitive Primitive() throws ParseException {
    List<MacroExpression> exprs = new LinkedList<MacroExpression>();
    Token numberToken;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case CODE_DELIM:
      jj_consume_token(CODE_DELIM);

      return null;

    case DIGITS:
      numberToken = jj_consume_token(DIGITS);
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 70:
        jj_consume_token(70);
        ExprList(exprs);
        break;
      default:
        jj_la1[51] = jj_gen;
        ;
      }
      jj_consume_token(CODE_DELIM);
      int primitiveNumber = Integer.parseInt(numberToken.image);
      switch (primitiveNumber) {
      case 1:
        return new CirclePrimitive(exprs);
      case 2:
        return new LineVectorPrimitive(exprs);
      case 20:
        return new LineVectorPrimitive(exprs);
      case 21:
        return new LineCenterPrimitive(exprs);
      case 22:
        return new LineLLPrimitive(exprs);
      case 3:
        return null;
      case 4:
        return new OutlinePrimitive(exprs);
      case 5:
        return new PolygonPrimitive(exprs);
      case 6:
        return new MoirePrimitive(exprs);
      case 7:
        return new ThermalPrimitive(exprs);
      default:
        System.err.println("WARNING: ignoring unknown macro primitive type " + primitiveNumber);
        return null;
      }

    default:
      jj_la1[52] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    // never reached
  }

  final public void ExprList(final List<MacroExpression> exprs) throws ParseException {
    MacroExpression expr;
    expr = Expr();
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 70:
      jj_consume_token(70);
      ExprList(exprs);
      break;
    default:
      jj_la1[53] = jj_gen;
      ;
    }
    exprs.add(0, expr);
  }

  final public MacroExpression Expr() throws ParseException {
    MacroExpression lhs = null;
    MacroExpression rhs = null;

    Token op = null;
    lhs = MulExpr();
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case SIGN:
      op = jj_consume_token(SIGN);
      rhs = MulExpr();
      break;
    default:
      jj_la1[54] = jj_gen;
      ;
    }
    if (rhs == null)
      return lhs;

    return new MacroBinOp((("+".equals(op.image)) ? MacroBinOp.ADD : MacroBinOp.SUBTRACT), lhs, rhs);
  }

  final public MacroExpression MulExpr() throws ParseException {
    MacroExpression lhs = null;
    MacroExpression rhs = null;
    Token op = null;
    lhs = UnaryExpr();
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case 38:
    case 71:
    case 72:
      switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
      case 38:
        op = jj_consume_token(38);
        break;
      case 71:
        op = jj_consume_token(71);
        break;
      case 72:
        op = jj_consume_token(72);
        break;
      default:
        jj_la1[55] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      rhs = UnaryExpr();
      break;
    default:
      jj_la1[56] = jj_gen;
      ;
    }
    if (op == null)
      return lhs;

    int operator = MacroBinOp.MULTIPLY;

    if ("-".equals(op.image))
      operator = MacroBinOp.DIVIDE;

    return new MacroBinOp(operator, lhs, rhs);
  }

  final public MacroExpression UnaryExpr() throws ParseException {
    double value;
    Token index;
    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
    case SIGN:
    case DIGITS:
    case 42:
      value = Double();

      return new MacroConstant(value);

    case 73:
      jj_consume_token(73);
      index = jj_consume_token(DIGITS);

      return new MacroVariable(Integer.parseInt(index.image));

    default:
      jj_la1[57] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    // never reached
  }

  public ParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[58];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
    jj_la1_0();
    jj_la1_1();
    jj_la1_2();
  }

  private static void jj_la1_0() {
    jj_la1_0 = new int[] { 0xfff04060, 0xfff04060, 0x3c00, 0x100000, 0x0, 0xffe04000, 0xffe04000, 0xffe04000, 0x0, 0x0,
        0x100, 0x100, 0x100, 0x0, 0x0, 0x0, 0x80, 0x80, 0x100, 0x0, 0x100, 0x0, 0x3c00, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x100000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x140, 0x0, 0x140, 0x0, 0x80, 0x0, 0x0, 0x180, };
  }

  private static void jj_la1_1() {
    jj_la1_1 = new int[] { 0x3bff, 0x3bff, 0x9040c000, 0x0, 0x3fc, 0x3bff, 0x3bff, 0x3, 0x3fc, 0x3c, 0x0, 0x0, 0x0,
        0x3c, 0x3c0, 0x3c0, 0x0, 0x0, 0x0, 0x400, 0x400, 0x3800, 0x9040c000, 0xf8000, 0xc0, 0xc0, 0x1800000, 0x1800000,
        0x100100, 0x100100, 0x0, 0x2000000, 0x40, 0x80, 0x4000000, 0x20, 0x8000000, 0x60000000, 0x20, 0x0, 0x100000,
        0x200000, 0x100000, 0x200000, 0x40, 0x80, 0x100, 0x200, 0x0, 0x40, 0x0, 0x0, 0x0, 0x0, 0x0, 0x40, 0x40,
        0x400, };
  }

  private static void jj_la1_2() {
    jj_la1_2 = new int[] { 0x0, 0x0, 0x3e, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x3e, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, 0x6,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x40, 0x0, 0x0, 0x40, 0x0, 0x40, 0x0, 0x180, 0x180, 0x200, };
  }

  public Parser(java.io.InputStream stream) {
    this(stream, null);
  }

  public Parser(java.io.InputStream stream, String encoding) {
    try {
      jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    ReInit(stream, null);
  }

  public void ReInit(java.io.InputStream stream, String encoding) {
    try {
      jj_input_stream.ReInit(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  public Parser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  public Parser(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  public void ReInit(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 58; i++)
      jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null)
      token = token.next;
    else
      token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null)
      token = token.next;
    else
      token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null)
        t = t.next;
      else
        t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt = token.next) == null)
      return (jj_ntk = (token.next = token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[74];
    for (int i = 0; i < 74; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 58; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1 << j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1 << j)) != 0) {
            la1tokens[32 + j] = true;
          }
          if ((jj_la1_2[i] & (1 << j)) != 0) {
            la1tokens[64 + j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 74; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[]) jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
