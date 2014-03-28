<!--  Script part -->

<script>
	$("#set-controller-dialog").dialog({
		title : "Modifica impostazioni controller",
		buttons : [ {
			text : "Applica",
			click : function() {
				applyControllerSettings();
			}
		}, {
			text : "Annulla",
			click : function() {
				$(this).dialog("close");
			}
		} ],
		autoOpen : false,
		hide : "explode",
		modal : true
	});

	$("#set-controller").button().click(function() {
		$("#set-controller-dialog").dialog("open");
	});

	$("#add-host-dialog").dialog({
		title : "Aggiunta Hypervisor",
		buttons : [ {
			text : "Aggiungi",
			click : function() {
				//addNewHypervisor();
				$('#hypervisorform').submit();
			}
		}, {
			text : "Annulla",
			click : function() {
				$(this).dialog("close");
			}
		} ],
		autoOpen : false,
		hide : "explode",
		modal : true
	});

	$("#addNewHypervisor").button().click(function() {
		$("#add-host-dialog").dialog("open");
	});

	
	function applyControllerSettings() {
		ip = document.getElementById("set-hostname").value;
		port = document.getElementById("set-port").value;
		$.getJSON("AddController?ip=" + ip + "&port=" + port,
				function(response) {
					alert(response.status);
					getControllerSettings();
					$("#set-controller-dialog").dialog("close");

				});
	}

	function addNewHypervisor() {
		username = document.getElementById("addhost-username").value;
		host = document.getElementById("addhost-hostname").value;
		port = document.getElementById("addhost-port").value;
		$.getJSON("AddHypervisor?username=" + username + "&ip=" + host
				+ "&port=" + port, function(response) {
			alert(response.status);
			$("#add-host-dialog").dialog("close");
			getHypervisorSettings();

		});
	}

	function removeHypervisor(event) {

		hypervisor = event.target.id;

		/*$.getJSON("DeleteHypervisor?hostname=" + hypervisor,
				function(response) {
					getHypervisorSettings();

				});*/
		
	}
	
</script>

<!--  HTML PART -->
<link href="css/settings_style.css" rel="stylesheet" />

<div id="box">

	<%@page import="org.at.db.Controller"%>
	<%@page import="org.at.db.Database"%>
	<%@page import="org.at.db.Hypervisor" %>
	<%@page import="java.util.List" %>
	
	<% 
	Database d = new Database();
	d.connect();
	
	Controller c = d.getController();
	List<Hypervisor> hypervisors = d.getAllHypervisors();
	d.close();
	
	String hostname;
	String port;
	String webui;
	
	if(c!= null){
		hostname = c.getHostAddress();
		port = String.valueOf(c.getPort());
		webui = "http://"+c.getHostAddress()+":8080/ui/index.html";
	}else{
		hostname = "Non configurato";
		port = "Non configurato";
		webui = "Non configurato";
	}
	%>
	
	
	<div id="controller-box" class="cent">
		<h3>Impostazioni Controller Openflow</h3>
		<table>
			<tr>
				<td><b>Hostname</b></td>
				<td id="current-controller-hostname"><%=hostname%></td>
			</tr>
			<tr>
				<td><b>Port</b></td>
				<td id="current-controller-port"><%=port%></td>
			</tr>
			<tr>
				<td><b>Floodlight WebUi</b></td>
				<td id="current-controller-webui"><%=webui %></td>
			</tr>
		</table>
		<br>
		<button id="set-controller">Modifica</button>

		<!--  Message Box (Hidden by default) -->
		<div id="set-controller-dialog" title="Set Controller" style="">
			<h3>Impostazioni Openflow Controller</h3>
			<label for="set-hostname">Hostname</label><br> <input
				id="set-hostname" type="text" value="localhost" /><br> <label
				for="set-port">Port</label><br> <input id="set-port"
				type="text" value="8080" />
		</div>

	</div>

	<br>
	<hr>
	<br>

	<div id="hypervisors-box" class="cent">
		<h3>Hypervisors Registrati</h3>
		<table id="hyper-table" cellspacing="10"
			class="ui-widget ui-widget-content">
			<thead>
				<tr>
					<th>Username</th>
					<th>Hostname</th>
					<th>Port</th>
					<th></th>
				</tr>
			</thead>
			<tbody id="Hypervisor-list">
			<% for(Hypervisor h : hypervisors){ %>
				<tr>
					<td><%=h.getName()%></td>
					<td><%=h.getHostAddress()%></td>
					<td><%=h.getPort()%></td>
					<td><button id="<%=h.getHostAddress()%>" onclick="removeHypervisor(event);">Elimina</button></td>
				</tr>
			<% } %>
			</tbody>
		</table>
		<br>
		<button id="addNewHypervisor">Aggiungi</button>

		<!--  Message Box (Hidden by default) -->
		<div id="add-host-dialog" title="Aggiunta Hypervisor" style="">
			<h3>Aggiunta Hypervisor</h3>
			<form id="hypervisorform" action="/AT/prova.jsp" method="POST">
			
			<label for="addhost-username">Username</label><br> <input
				id="addhost-username" type="text" value="user" /><br> <label
				for="addhost-hostname">Host</label><br> <input
				id="addhost-hostname" type="text" value="localhost" /> <label
				for="addhost-port">Port</label><br> <input id="addhost-port"
				type="text" value="16514" />
			
			</form>
		</div>

	</div>
	
	<br>
	<hr>
	<br>
	
	<div id="settings-box" class="cent">
		<h3>Altri settaggi</h3>
		
	</div>
</div>


