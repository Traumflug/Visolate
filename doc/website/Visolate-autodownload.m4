m4_define(`APPLET_WIDTH', `595')
m4_define(`APPLET_HEIGHT', `853')
m4_define(`APPNAME', `Visolate')
m4_define(`PKGNAME', `visolate')
m4_define(`CLASSNAME', `Main')

m4_define(`TITLE', `APPNAME Applet')
m4_define(`INFO_PAGE', `APPNAME-info.html')
m4_define(`MAIN_CLASS', `PKGNAME.CLASSNAME')

<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML//EN">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="Author" content="Marsette Vona">
<title>TITLE</title>
</head>

<body bgcolor="FFFFFF">

<p align="center">
<a href="INFO_PAGE">info</a>&nbsp;|&nbsp;<a href="APPNAME.html">applet without library auto-download</a>&nbsp;|&nbsp;<a href="APPNAME.jnlp">webstart</a>&nbsp;|&nbsp;<a href="APPNAME.jar">download</a>&nbsp;|&nbsp;<a href="PKGNAME">browse source</a>&nbsp;|&nbsp;<a href="javadoc-APPNAME/index.html">javadoc</a>
</p>

<p align="center">
<applet code="org.jdesktop.applet.util.JNLPAppletLauncher"
        width=APPLET_WIDTH height=APPLET_HEIGHT
        archive="APPNAME.jar,
                 http://download.java.net/media/applet-launcher/applet-launcher.jar,
                 http://download.java.net/media/java3d/webstart/release/j3d/latest/j3dcore.jar,
                 http://download.java.net/media/java3d/webstart/release/j3d/latest/j3dutils.jar,
                 http://download.java.net/media/java3d/webstart/release/vecmath/latest/vecmath.jar,
                 http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jar,
                 http://download.java.net/media/gluegen/webstart/gluegen-rt.jar">
    <param name="codebase_lookup" value="false">
    <param name="subapplet.classname" value="MAIN_CLASS">
    <param name="subapplet.displayname" value="APPNAME">
    <param name="jnlpNumExtensions" value="1">
    <param name="jnlpExtension1" value="http://download.java.net/media/java3d/webstart/release/java3d-latest.jnlp">
    <param name="progressbar" value="true">
    <param name="noddraw.check" value="true">
</applet>
</p>

</body>
</html>
