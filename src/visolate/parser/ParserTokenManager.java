/* Generated By:JavaCC: Do not edit this line. ParserTokenManager.java */
package visolate.parser;


public class ParserTokenManager implements ParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
//private final int jjStartNfaWithStates_0(int pos, int kind, int state)
//{
//   jjmatchedKind = kind;
//   jjmatchedPos = pos;
//   try { curChar = input_stream.readChar(); }
//   catch(java.io.IOException e) { return pos + 1; }
//   return jjMoveNfa_0(state, pos + 1);
//}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 36:
         return jjStopAtPos(0, 73);
      case 37:
         return jjStopAtPos(0, 5);
      case 42:
         return jjStopAtPos(0, 6);
      case 44:
         return jjStopAtPos(0, 70);
      case 46:
         return jjStopAtPos(0, 42);
      case 47:
         return jjStopAtPos(0, 72);
      case 65:
         jjmatchedKind = 52;
         return jjMoveStringLiteralDfa1_0(0x800000003000L, 0x0L);
      case 66:
         jjmatchedKind = 53;
         return jjMoveStringLiteralDfa1_0(0x2000000000000L, 0x0L);
      case 67:
         return jjStopAtPos(0, 64);
      case 68:
         jjmatchedKind = 37;
         return jjMoveStringLiteralDfa1_0(0x1c00000000L, 0x0L);
      case 69:
         return jjMoveStringLiteralDfa1_0(0x9000000000000L, 0x0L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x40000000000000L, 0x0L);
      case 71:
         jjmatchedKind = 57;
         return jjMoveStringLiteralDfa1_0(0x3ffe04000L, 0x0L);
      case 73:
         jjmatchedKind = 40;
         return jjMoveStringLiteralDfa1_0(0x1004400000000400L, 0x0L);
      case 74:
         return jjStopAtPos(0, 41);
      case 76:
         jjmatchedKind = 55;
         return jjMoveStringLiteralDfa1_0(0x8000000000000800L, 0x0L);
      case 77:
         jjmatchedKind = 59;
         return jjMoveStringLiteralDfa1_0(0x380000000000L, 0x6L);
      case 78:
         jjmatchedKind = 20;
         return jjMoveStringLiteralDfa1_0(0x4000000000000000L, 0x0L);
      case 79:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x8L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0x2000000000000000L, 0x0L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x0L, 0x30L);
      case 84:
         return jjStopAtPos(0, 56);
      case 88:
         return jjStopAtPos(0, 38);
      case 89:
         return jjStopAtPos(0, 39);
      case 90:
         return jjStopAtPos(0, 58);
      case 120:
         return jjStopAtPos(0, 71);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0, long active1)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, active1);
      return 1;
   }
   switch(curChar)
   {
      case 48:
         return jjMoveStringLiteralDfa2_0(active0, 0x381c01e04000L, active1, 0L);
      case 51:
         return jjMoveStringLiteralDfa2_0(active0, 0x6000000L, active1, 0L);
      case 53:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000L, active1, 0L);
      case 55:
         return jjMoveStringLiteralDfa2_0(active0, 0xf0000000L, active1, 0L);
      case 57:
         return jjMoveStringLiteralDfa2_0(active0, 0x300000000L, active1, 0L);
      case 66:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStopAtPos(1, 48);
         break;
      case 67:
         if ((active0 & 0x400000000000L) != 0L)
            return jjStopAtPos(1, 46);
         else if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         break;
      case 68:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000L, active1, 0L);
      case 69:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000000000000L, active1, 0L);
      case 70:
         if ((active1 & 0x8L) != 0L)
            return jjStopAtPos(1, 67);
         else if ((active1 & 0x10L) != 0L)
            return jjStopAtPos(1, 68);
         break;
      case 73:
         if ((active0 & 0x8000000000000L) != 0L)
            return jjStopAtPos(1, 51);
         break;
      case 77:
         if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(1, 13);
         break;
      case 78:
         if ((active0 & 0x400L) != 0L)
            return jjStopAtPos(1, 10);
         else if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         break;
      case 79:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000000000000L, active1, 0x6L);
      case 80:
         if ((active0 & 0x1000000000000000L) != 0L)
            return jjStopAtPos(1, 60);
         else if ((active0 & 0x8000000000000000L) != 0L)
            return jjStopAtPos(1, 63);
         break;
      case 82:
         if ((active1 & 0x20L) != 0L)
            return jjStopAtPos(1, 69);
         break;
      case 83:
         if ((active0 & 0x800000000000L) != 0L)
            return jjStopAtPos(1, 47);
         else if ((active0 & 0x4000000000000L) != 0L)
            return jjStopAtPos(1, 50);
         else if ((active0 & 0x40000000000000L) != 0L)
            return jjStopAtPos(1, 54);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0, active1);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(0, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, active1);
      return 2;
   }
   switch(curChar)
   {
      case 48:
         if ((active0 & 0x200000L) != 0L)
            return jjStopAtPos(2, 21);
         else if ((active0 & 0x10000000L) != 0L)
            return jjStopAtPos(2, 28);
         else if ((active0 & 0x100000000L) != 0L)
            return jjStopAtPos(2, 32);
         else if ((active0 & 0x80000000000L) != 0L)
            return jjStopAtPos(2, 43);
         break;
      case 49:
         if ((active0 & 0x400000L) != 0L)
            return jjStopAtPos(2, 22);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(2, 29);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStopAtPos(2, 33);
         else if ((active0 & 0x400000000L) != 0L)
            return jjStopAtPos(2, 34);
         else if ((active0 & 0x100000000000L) != 0L)
            return jjStopAtPos(2, 44);
         break;
      case 50:
         if ((active0 & 0x800000L) != 0L)
            return jjStopAtPos(2, 23);
         else if ((active0 & 0x800000000L) != 0L)
            return jjStopAtPos(2, 35);
         else if ((active0 & 0x200000000000L) != 0L)
            return jjStopAtPos(2, 45);
         break;
      case 51:
         if ((active0 & 0x1000000L) != 0L)
            return jjStopAtPos(2, 24);
         else if ((active0 & 0x1000000000L) != 0L)
            return jjStopAtPos(2, 36);
         break;
      case 52:
         if ((active0 & 0x4000L) != 0L)
            return jjStopAtPos(2, 14);
         else if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(2, 27);
         else if ((active0 & 0x40000000L) != 0L)
            return jjStopAtPos(2, 30);
         break;
      case 53:
         if ((active0 & 0x80000000L) != 0L)
            return jjStopAtPos(2, 31);
         break;
      case 54:
         if ((active0 & 0x2000000L) != 0L)
            return jjStopAtPos(2, 25);
         break;
      case 55:
         if ((active0 & 0x4000000L) != 0L)
            return jjStopAtPos(2, 26);
         break;
      case 68:
         if ((active0 & 0x1000L) != 0L)
            return jjStopAtPos(2, 12);
         break;
      case 71:
         if ((active0 & 0x4000000000000000L) != 0L)
            return jjStopAtPos(2, 62);
         break;
      case 73:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x2L);
      case 77:
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x4L);
      case 83:
         if ((active0 & 0x2000000000000000L) != 0L)
            return jjStopAtPos(2, 61);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0, active1);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(1, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, 0L, active1);
      return 3;
   }
   switch(curChar)
   {
      case 77:
         if ((active1 & 0x4L) != 0L)
            return jjStopAtPos(3, 66);
         break;
      case 78:
         if ((active1 & 0x2L) != 0L)
            return jjStopAtPos(3, 65);
         break;
      default :
         break;
   }
   return jjStartNfa_0(2, 0L, active1);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
//private final void jjAddStates(int start, int end)
//{
//   do {
//      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
//   } while (start++ != end);
//}
//private final void jjCheckNAddTwoStates(int state1, int state2)
//{
//   jjCheckNAdd(state1);
//   jjCheckNAdd(state2);
//}
//private final void jjCheckNAddStates(int start, int end)
//{
//   do {
//      jjCheckNAdd(jjnextStates[start]);
//   } while (start++ != end);
//}
//private final void jjCheckNAddStates(int start)
//{
//   jjCheckNAdd(jjnextStates[start]);
//   jjCheckNAdd(jjnextStates[start + 1]);
//}
private final int jjMoveNfa_0(int startState, int curPos)
{
//   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 2;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 8)
                        kind = 8;
                     jjCheckNAdd(1);
                  }
                  else if ((0x280000000000L & l) != 0L)
                  {
                     if (kind > 7)
                        kind = 7;
                  }
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  kind = 8;
                  jjCheckNAdd(1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 2 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_1()
{
   return jjMoveNfa_1(0, 0);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_1(int startState, int curPos)
{
//   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfffffbffffffdbffL & l) == 0L)
                     break;
                  kind = 15;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  kind = 15;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 15)
                     kind = 15;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_2()
{
   return jjMoveNfa_2(1, 0);
}
private final int jjMoveNfa_2(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
               case 0:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 16)
                     kind = 16;
                  jjCheckNAdd(0);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 18)
                     kind = 18;
                  jjstateSet[jjnewStateCnt++] = 2;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
               case 2:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 18)
                     kind = 18;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, "\45", "\52", null, null, null, "\111\116",
"\114\116", "\101\104\104", "\101\115", "\107\60\64", null, null, null, null, null,
"\116", "\107\60\60", "\107\60\61", "\107\60\62", "\107\60\63", "\107\63\66",
"\107\63\67", "\107\65\64", "\107\67\60", "\107\67\61", "\107\67\64", "\107\67\65",
"\107\71\60", "\107\71\61", "\104\60\61", "\104\60\62", "\104\60\63", "\104", "\130",
"\131", "\111", "\112", "\56", "\115\60\60", "\115\60\61", "\115\60\62", "\111\103",
"\101\123", "\105\102", "\102\103", "\111\123", "\105\111", "\101", "\102", "\106\123",
"\114", "\124", "\107", "\132", "\115", "\111\120", "\120\117\123", "\116\105\107",
"\114\120", "\103", "\115\117\111\116", "\115\117\115\115", "\117\106", "\123\106",
"\123\122", "\54", "\170", "\57", "\44", };
public static final String[] lexStateNames = {
   "DEFAULT",
   "TEXT_EXPECTED",
   "ALNUMS_EXPECTED",
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 2, 1, 1, 0, -1, -1, 0, -1, -1, -1, -1, -1, -1,
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
};
static final long[] jjtoToken = {
   0xffffffffffffffe1L, 0x3ffL,
};
static final long[] jjtoSkip = {
   0x1eL, 0x0L,
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[3];
private final int[] jjstateSet = new int[6];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public ParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public ParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 3; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 3 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   if (jjmatchedPos < 0)
   {
      if (image == null)
         t.image = "";
      else
         t.image = image.toString();
      t.beginLine = t.endLine = input_stream.getBeginLine();
      t.beginColumn = t.endColumn = input_stream.getBeginColumn();
   }
   else
   {
      String im = jjstrLiteralImages[jjmatchedKind];
      t.image = (im == null) ? input_stream.GetImage() : im;
      t.beginLine = input_stream.getBeginLine();
      t.beginColumn = input_stream.getBeginColumn();
      t.endLine = input_stream.getEndLine();
      t.endColumn = input_stream.getEndColumn();
   }
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken()
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;

   switch(curLexState)
   {
     case 0:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
             curChar = input_stream.BeginToken();
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 15;
       jjmatchedPos = -1;
       curPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       break;
     case 2:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_2();
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           TokenLexicalActions(matchedToken);
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

void TokenLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      default :
         break;
   }
}
}
