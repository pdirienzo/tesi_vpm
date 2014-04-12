<!DOCTYPE HTML>
<html>
<head>
<title>Live migration</title>
<link href="css/smoothness/jquery-ui-1.10.4.css" rel="stylesheet" />
<link href="css/bootstrap.css" rel="stylesheet" />
<link href="css/jquery.switchButton.css" rel="stylesheet" />
<link href="css/tabs_style.css" rel="stylesheet" />
<script src="js/jquery-1.11.0.js"></script>
<script src="js/jquery-ui-1.10.4.js"></script>
<script src="js/jquery-ui-contextmenu.js"></script>
<script src="js/jquery.switchButton.js"></script>

<%@page import="java.util.Properties"%>

<script type="text/javascript">
	
	<%
	Properties props = (Properties)application.getAttribute("properties");
	%>
			//global variables
			var WARNING_THRESHOLD= <%=Float.parseFloat(props.getProperty("warning_treshold"))%>;
			var DANGER_THRESHOLD=<%=Float.parseFloat(props.getProperty("danger_treshold"))%>;
			
			var REFRESH_INTERVAL = 5000; //time in ms of info refreshing
			var timedFunction; //this contains the timed function for the
			                   //dashboard refreshing logic

			$(function() {
				$("#tabs").tabs({
					beforeActivate:function( event, ui ) {
						 if(ui.oldTab.index() == 0) //if we are leaving from dashboard
							 clearInterval(timedFunction); //we cancel the refresh operation
					}
				});
				
				$.ajaxSetup ({
		    		// Disable caching of AJAX responses
		    		cache: false
				});
				
				$( "#vpm-alert" ).dialog({
					autoOpen : false,
					buttons : [ {
		    			text : "Ok",
		    			click: function(){
		    				$(this).dialog("close");
		    				}
		    			}]
				});
				
			});
			
			function showDialog(title,message){
				$("#vpm-alert").dialog("option","title",title);
				$("#vpm-alert p").text(message);
				$("#vpm-alert").dialog("open");
			}
			 
		</script>
</head>
<style>
#share {
	background-color: #CCC;
	background-repeat: repeat;
	height: auto;
}

#share .container {
	border-top: 1px solid #DDD3CD;
	/* [disabled]background-image: url(../images/share/pattern.png); */
	background-repeat: repeat-x;
	padding: 20px 0;
}

#share h2 {
	color: #3F3F3F;
	font-size: 18px;
	line-height: 1em;
}

#share a { color:#794F34;}



</style>
</head>


<body>
	<div id="wrapper">
		<div id="header">
			<div id="tabs" class="centered">
				<ul>
					<li><a href="new_dashboard.html">Dashboard</a></li>
					<li><a href="migration.html">Migration</a></li>
					<li><a href="ovs_network.html">Networking</a></li>
					<li><a href="settings.html">Settings</a></li>
				</ul>
			</div>
		</div>
	</div>
	<section id="share">
  	<div class="container">
  	<h2>Con la collaborazione di:</h2>
    	<div class="row">
            <div class="span4" style="float:left">
            	<div class="span4 text-center"><br />
            	<img src="images/customLogo.png" alt="confindustria" />
        		</div>
           	</div>
            <div class="span4" style="float:left">
            	<div class="span4 text-center"><br />
            	<img src="images/garland_logo.gif" alt="confindustria" />
        		</div>
        	</div>
        	<div class="span4" style="float:left">
            	<div class="span4 text-center"><br />
            	<img src="images/federico2.png" alt="confindustria" />
        		</div>
        	</div>
          </div>
        </div>
  </section>

	
</body>

<div id="vpm-alert" title="Info">
	<p></p>
</div>

</html>
