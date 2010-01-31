
m4_include(`html-macros.m4')

m4_ifdef(`STRICT_XHTML',
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">',
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">')


<html>
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
m4_dnl <link rel="stylesheet" type="text/css" href="style.css" />
 <style type="text/css">
m4_include(`style.css')
 </style>
  m4_ifdef(`EXTRA_HEAD', EXTRA_HEAD)
 <title>Marsette Vona: m4_ifdef(`PAGE_PATH', PAGE_PATH:) PAGE_TITLE </title>
</head>

<body>

  TABLE_START
    <tr valign="top">
      <td nowrap="true">
        <font size="5"><a href="HOME_URL">Marsette Vona</a>:</font>
      </td>
      m4_ifdef(`PAGE_PATH',
     `<td><font size="5"><a href="index.html">PAGE_PATH</a>:</font></td>')
      <td><font size="5">PAGE_TITLE</font></td>
    </tr>
  </table>
