<!DOCTYPE HTML>
<html>
<head>
<title>Live migration</title>
<link rel="icon" type="image/x-icon" href="images/vpmicon.ico" />
<link href="css/smoothness/jquery-ui-1.10.4.css" rel="stylesheet" />
<link href="css/jquery.switchButton.css" rel="stylesheet" />
<link href="css/tabs_style.css" rel="stylesheet" />
<script src="js/jquery-1.11.0.js"></script>
<script src="js/jquery-ui-1.10.4.js"></script>
<script src="js/jquery-ui-contextmenu.js"></script>
<script src="js/jquery.switchButton.js"></script>
<!-- BOOTSTRAP  -->
<link href="css/bootstrap.css" rel="stylesheet" />
<script src="js/bootstrap.js"></script>
<!-- FONT AWESOME  -->
<link href="http://netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet" />
<link href="http://fonts.googleapis.com/css?family=Open+Sans" rel="stylesheet" type="text/css" />

<%@page import="java.util.Properties"%>
<%@page import="it.unina.cini.platino.network.types.OvsSwitch.Type"%>
<script type="text/javascript">
	
	<%
	Properties props = (Properties)application.getAttribute("properties");
	%>
			//global variables
			var WARNING_THRESHOLD= <%=Float.parseFloat(props.getProperty("warning_treshold"))%>;
			var DANGER_THRESHOLD=<%=Float.parseFloat(props.getProperty("danger_treshold"))%>;
			
			var ROOT_TYPE='<%=Type.ROOT.name() %>';
			var RELAY_TYPE='<%=Type.RELAY.name() %>';
			var LEAF_TYPE='<%=Type.LEAF.name() %>';
			var NULL_TYPE='<%=Type.NULL.name() %>';

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
			
			function showDialog2(title,message){
				$("#vpm-alert-label").html(title);
				$("#vpm-alert-info .modal-body").html(message);
				$("#vpm-alert-info").modal("show");
			}
			 
		</script>
</head>
<style>
#footer {
	background-color: #CCC;
	background-repeat: repeat;
	height: auto;
}

#footer .container {
	border-top: 1px solid #DDD3CD;
	/* [disabled]background-image: url(../images/share/pattern.png); */
	background-repeat: repeat-x;
	padding: 20px 0;
}

#footer h2 {
	color: #3F3F3F;
	font-size: 18px;
	line-height: 1em;
}

#footer a { color:#794F34;}



</style>
</head>


<body style="background:#CCC">
	<div id="wrapper">
		<div id="header">
			<div id="tabs" class="centered">
				<ul>
					<li><a href="new_dashboard.html">Dashboard</a></li>
					<li><a href="migration.html">Migration</a></li>
					<li><a href="ovs_network.html">Networking</a></li>
					<li><a href="path.html">Path</a></li>
					<li><a href="settingsBootstrap.html">Setting</a></li> 
				</ul>
			</div>
		</div>
	</div>
	<section id="footer">
  	<div class="container">
  	
    	<div class="row">
            <div class="span3" style="float:left">
            	<div class="span3 text-center"><br />
            	<a href="#"><img src="images/vpmLogo.png" alt="Virtual Puppet Master"/></a>
        		</div>
        	</div>
            <div class="span3" style="float:left">
            	<div class="span3 text-center"><br />
            	<a href="https://sites.google.com/site/progettoplatino/" target="_blank"><img src="images/customLogo.png" alt="PLATINO" /></a>
        		</div>
           	</div>
            <div class="span3" style="float:left">
            	<div class="span3 text-center"><br />
            	<a href="http://www.consorzio-cini.it/" target="_blank"><img src="images/garland_logo.gif" alt="CINI" /></a>
        		</div>
        	</div>
        	<div class="span3" style="float:left">
            	<div class="span3 text-center"><br />
            	<a href="http://www.unina.it/" target="_blank"><img src="images/federico2.png" alt="FEDERICO II"/></a>
        		</div>
        	</div>
          </div>
        </div>
  </section>

<div id="vpm-alert" title="Info">
	<p></p>
</div>
<!-- DIALOG FOR COMUNICATION -->
	<div class="modal fade" id="vpm-alert-info" tabindex="-1" role="dialog" aria-labelledby="vpm-alert-label" aria-hidden="true">
	  <div class="modal-dialog modal-sm" >
	    <div class="modal-content">
	      <div class="modal-header" style="background-color:#DDD">
	        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        <h4 class="modal-title" id="vpm-alert-label"></h4>
	      </div>
	      <div class="modal-body">
	      
		  </div>
	      <div class="modal-footer">
	      <p>
	        <button type="button" class="btn btn-info btn-lg btn-block" data-dismiss="modal">Okay</button>
	      </p>
	      </div>
	    </div>
	  </div>
	</div>
</body>



</html>
