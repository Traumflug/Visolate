m4_changecom(`<!--', `-->')
m4_define(`PORK_URL', `http://www.bluesock.org/pork')
m4_define(`HOME_URL', `http://www.mit.edu/~vona')
m4_define(`EMAIL', `vona@mit.edu')
m4_dnl define(`HOME_PHONE', `')
m4_define(`WORK_PHONE', `(617) 253 6532, fax (617) 253 6849')
m4_define(`LAB_PHONES')
m4_define(`LAB_SMAIL', `Computer Science and Artificial Intelligence Laboratory<br/>Massachusetts Institute of Technology<br/>Bldg. 32, Rm. 376<br/>32 Vassar St.<br/>Cambridge M.A., 02139')
m4_define(`WORK_SMAIL', `LAB_SMAIL')
m4_define(`DISCLAIMER', `<p>THIS INFORMATION AND/OR SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS INFORMATION AND/OR SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>')

m4_ifdef(`B_WIDTH', `', `m4_define(`B_WIDTH', `0')')

m4_define(`SYM_SEC', `&#167;$1')

m4_define(`SYM_TAB', `&nbsp;&nbsp;&nbsp;&nbsp;')

m4_define(`TABLE_START',
`<table border="B_WIDTH" cellspacing="0" cellpadding="0">')

m4_define(`ITEMLIST_START',
`<table class="itemlist" border="B_WIDTH" cellspacing="0" cellpadding="0">')

m4_define(`TABLE_START_FULL',
`<table border="B_WIDTH" cellspacing="0" cellpadding="0" width="100%">')

m4_define(`SEPARATOR', `<h$2 class="separator"><a name="$1"/a>$1</h$2>')

m4_dnl PUB(link, title, authors, pub, year)
m4_define(`PUB',
`<tr valign="top">
  <td><a href="HOME_URL/publications/$1">$2</a></td>
  <td>&nbsp;</td>
  <td>$3</td>
  <td>&nbsp;</td>
  <td>($4 $5)</td>
</tr>')

m4_define(`mICRA', `<a href="http://ieeexplore.ieee.org/xpl/conhome.jsp?punumber=1000639">ICRA</a>')

m4_define(`mIEEEAC', `<a href="http://ieeexplore.ieee.org/xpl/conhome.jsp?punumber=1000024">IEEEAC</a>')

m4_define(`mIROS', `<a href="http://ieeexplore.ieee.org/xpl/conhome.jsp?punumber=1000393">IROS</a>')

m4_define(`mWAFR', `<a href="http://www.wafr.org/">WAFR</a>')

m4_dnl m4_define(`IMG_SM', <table><tr><td align="left">med res</td><td align="right">high res</td></tr><tr><td colspan="2" align="center"><a href="$1-med.$2"><img src="$1-small.$2"/></a></td></tr><tr><td colspan="2" align="center">$3</td></tr></table>)

m4_define(`IMG_SM', <table border="0"><tr><td align="center"><table><tr><td align="right"><font size="0"><a href="$1-med.$2">medium</a>|<a href="$1.$2">high</a> resolution</font></td></tr><tr><td align="center"><a href="$1-med.$2"><img src="$1-small.$2"/></a></td></tr></table></td></tr><tr><td align="center">$3</td></tr></table>)


m4_define(`PDF_THUMB', <table border="0"><tr><td align="center"><a href="$1.pdf"><img src="$1.png"/></a></td></tr><tr><td align="center">$2</td></tr></table>)
