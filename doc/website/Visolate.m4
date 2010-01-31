m4_define(`APPLET_WIDTH', `595')
m4_define(`APPLET_HEIGHT', `853')
m4_define(`APPNAME', `Visolate')
m4_define(`PKGNAME', `visolate')
m4_define(`CLASSNAME', `Main')

m4_define(`TITLE', `APPNAME Applet')
m4_define(`INFO_PAGE', `APPNAME-info.html')
m4_define(`MAIN_CLASS', `PKGNAME/CLASSNAME.class')

<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML//EN">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="Author" content="Marsette Vona">
<title>TITLE</title>
</head>

<body bgcolor="FFFFFF">

<p align="center">
<!-- <a href="Visolate-info.html">info</a>&nbsp;|&nbsp -->
<!-- <a href="Visolate-autodownload.html">JOGL auto-download applet</a>&nbsp;|&nbsp; -->
<!-- <a href="Visolate.html">applet</a>&nbsp;|&nbsp; -->
<a href="Visolate.jnlp">webstart</a>&nbsp;|&nbsp;
<a href="#Download">download</a>&nbsp;|&nbsp;
<a href="visolate">browse source</a>&nbsp;|&nbsp;
<a href="javadoc-Visolate/index.html">javadoc</a>
</p>

<p align="center">
<!--"CONVERTED_APPLET"-->
<!-- HTML CONVERTER -->
<OBJECT 
    classid = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
    codebase = "http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=1,5,0,0"
m4_dnl    codebase = "http://java.sun.com/update/1.4.2/jinstall-1_4-windows-i586.cab#Version=1,4,0,0"
    WIDTH = "APPLET_WIDTH" HEIGHT = "APPLET_HEIGHT" >
    <PARAM NAME = CODE VALUE = "MAIN_CLASS" >
m4_dnl    <PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.5">
    <PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.4">
    <PARAM NAME = "scriptable" VALUE = "false">
    <PARAM NAME = "archive" value = "APPNAME.jar">

<COMMENT>
	<EMBED 

m4_dnl TBD OSX
m4_dnl            type = "application/x-java-applet;version=1.5" \
            type = "application/x-java-applet;version=1.4" \
            CODE = "MAIN_CLASS" \
            ARCHIVE = "APPNAME.jar" \
            WIDTH = "APPLET_WIDTH" \
            HEIGHT = "APPLET_HEIGHT" \
	    scriptable = false \
	    pluginspage = "http://java.sun.com/products/plugin/index.html#download">
	    <NOEMBED>
m4_dnl            No Java 2 SDK, Standard Edition v 1.5 support for applet!!
            No Java 2 SDK, Standard Edition v 1.4 support for applet!!
      </NOEMBED>
	</EMBED>
</COMMENT>
</OBJECT>


<!--"END_CONVERTED_APPLET"-->

</p>

</body>
</html>
