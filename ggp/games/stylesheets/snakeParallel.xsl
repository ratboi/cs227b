<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<html>
<head>
<title><xsl:value-of select="match/@id"/></title>
<script type='text/javascript'>
var step = 1;
var steps = <xsl:value-of select="count(match/herstory/state)"/>*1;

function showstate (stepnum)
 {var oldstate = document.getElementById(step);
  var newstate = document.getElementById(stepnum);
  var counter = document.getElementById('stepcount');
  var leftcell = document.getElementById('left');
  var rightcell = document.getElementById('right');
  var oldstep = document.getElementById('step' + step);
  var newstep = document.getElementById('step' + stepnum);
  step=stepnum;
  oldstate.style.display = 'none';
  newstate.style.display = '';
  if (stepnum == 1) {leftcell.src = '/docserver/gamemaster/images/greyleft.gif'}
	 else {leftcell.src = '/docserver/gamemaster/images/blackleft.gif'};
  counter.innerHTML = step;
  if (stepnum == steps) {rightcell.src = '/docserver/gamemaster/images/greyright.gif'}
	 else {rightcell.src = '/docserver/gamemaster/images/blackright.gif'}
  if (oldstep) {oldstep.style.backgroundColor = '#ffff99'};
  if (newstep) {newstep.style.backgroundColor = '#ffffcc'}}

</script>
</head>
<body leftmargin='0' topmargin='0' marginwidth='0' marginheight='0' bgcolor='#ffffcc'>

<center>
<table width='800' height='65' cellspacing='0' cellpadding='0' border='0'><tr> 
<td><img src='/docserver/gamemaster/images/ggp.gif' width='480' height='65' border='0'/></td>
<td width='100%' align='center' valign='center'><b>
<xsl:value-of select="match/@id"/><br/>
<xsl:value-of select="match/game"/><br/>
Startclock: <xsl:value-of select="match/startclock"/>
Playclock: <xsl:value-of select="match/playclock"/>
Steplimit: <xsl:value-of select="match/steplimit"/>
</b></td>
</tr></table>
<table width='800' cellpadding='0' cellspacing='0' border='0'>
<tr><td width='100%' height='2' bgcolor='#336699'></td></tr>
</table>
</center>

<div style='margin-left:10px; margin-right:10px; margin-top:10px; margin-bottom:10px'>
<center><table width='800'><tr>
<td width='50%' align='center' valign='top'>

<xsl:for-each select="match/herstory/state">
  <div>
	<xsl:attribute name='id'><xsl:value-of select="stepnum"/></xsl:attribute>
	<xsl:attribute name='style'>display:none</xsl:attribute>

		<!-- Game specific stuff goes here -->
		<style type="text/css" media="all">
			#cell
			{
				width:	46px;
				height: 46px;
				float:	left;
				border: 2px solid #FFC;
				background-color: #CCCCCC;
			}
		</style>
		
		<div id="main" style="position:relative; width:400px; height:200px">
			<!-- Draw Board -->
			<div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/>
			<div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/>
			<div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/>
			<div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/><div id="cell"/>
					
			<!-- Draw tokens -->
			<xsl:for-each select="fact[relation='cell']">
				<xsl:variable name="x" select="50 * (./argument[2] - 1 + ((./argument[1]-1) * 4)) + 2"/>
				<xsl:variable name="y" select="50 * (4-./argument[3]) + 2"/>

				<div>
					<xsl:if test="./argument[4]='snake'">
						<xsl:attribute name="style">
							<xsl:value-of select="concat('position:absolute; left:', $x ,'px; top:', $y ,'px; width:46px; height:46px; background-color:#CC0000')"/>
						</xsl:attribute>		
					</xsl:if>
					<xsl:if test="./argument[4]='body'">
						<xsl:attribute name="style">
							<xsl:value-of select="concat('position:absolute; left:', $x ,'px; top:', $y ,'px; width:46px; height:46px; background-color:#CCCC00')"/>
						</xsl:attribute>		
					</xsl:if>
				</div>
			</xsl:for-each>
		</div>
		<!-- Game specific stuff goes here -->
  </div>
</xsl:for-each>
<table><tr>
<td valign='center'><img id='left' src='/docserver/gamemaster/images/greyleft.gif' border='0' onClick='showstate(Math.max(step-1,1))'/></td>
<td valign='center'>Step <span id='stepcount'>1</span></td>
<td valign='center'><img id='right' src='/docserver/gamemaster/images/blackright.gif' border='0' onClick='showstate(Math.min(step+1,steps))'/></td>
</tr></table>
</td>

<td width='50%' align='center' valign='top'>
<table cellpadding='3' cellspacing='0' border='1' bgcolor='#ffffcc'>
  <tr bgcolor='#ffff99'>
	<th>Step Number</th>
	<xsl:for-each select="match/player">
	  <th><xsl:value-of select="."/></th>
	</xsl:for-each>
  </tr>
  <xsl:for-each select="match/history/move">
	<tr>
		<td style='cursor:pointer' align='center' bgcolor='#ffff99'>
		  <xsl:attribute name='id'>step<xsl:value-of select="./stepnum"/></xsl:attribute>
		  <xsl:attribute name='onClick'>
			<xsl:value-of select="concat('showstate(',stepnum,')')"/>
		  </xsl:attribute>
		  <xsl:value-of select="stepnum"/>
		</td>
	  <xsl:for-each select="action">
		<td>
		<xsl:choose>
		  <xsl:when test = "function">
			(<xsl:value-of select="function"/>
			 <xsl:for-each select="argument">&#160;<xsl:value-of select="."/></xsl:for-each>)<br/>
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:value-of select="."/>
		  </xsl:otherwise>
		</xsl:choose>
		</td>
	  </xsl:for-each>
	</tr>
  </xsl:for-each>
  <xsl:if test="match/rewards">
	<tr bgcolor='#ffff99'>
	  <xsl:if test="count(match/herstory/state) = '1'">
		<td align='center' bgcolor='#ffffcc'>
		  <xsl:attribute name='id'>step<xsl:value-of select="count(match/herstory/state)"/></xsl:attribute>
		  Final
		</td>
	  </xsl:if>
	  <xsl:if test="count(match/herstory/state) != '1'">
		<td style='cursor:pointer' align='center' bgcolor='#ffff99'>
		  <xsl:attribute name='id'>step<xsl:value-of select="count(match/herstory/state)"/></xsl:attribute>
		  <xsl:attribute name='onClick'>
			<xsl:value-of select="concat('showstate(&quot;',count(match/herstory/state),'&quot;)')"/>
		  </xsl:attribute>
		  Final
		</td>
	  </xsl:if>
	  <xsl:for-each select="match/rewards/reward">
	<td align='center'><xsl:value-of select="."/></td>
	  </xsl:for-each>
	</tr>
  </xsl:if>
</table>
</td>

</tr></table>
</center>
</div>

<xsl:if test="/match/stepnum">
	<script type='text/javascript'>showstate(<xsl:value-of select="/match/stepnum + 1" />)</script>
</xsl:if>
<xsl:if test="not(/match/stepnum)">
	<script type='text/javascript'>showstate(1)</script>
</xsl:if>

<br/>
<center>
<table width='800' cellpadding='0' cellspacing='0' border='0'>
<tr><td width='100%' height='2' bgcolor='#336699'></td></tr>
</table>
<div style='font-size:14px'>
Match information provided by <a href='http://games.stanford.edu'>Gamemaster</a>.
</div>
</center>

</body>
</html>
</xsl:template>
</xsl:stylesheet>