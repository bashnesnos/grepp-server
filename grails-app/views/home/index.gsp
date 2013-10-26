<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
<r:require module="jquery"/>
<r:layoutResources/>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<title>Main page</title>
</head>
<body>
  <div class="body">
	  <p id="logArea"></p>
	  <g:javascript>
			function updateArea(data) {
	    		var logArea = $("#logArea");
	    		logArea.html(logArea.html() + data);
			}
		</g:javascript>
		<g:remoteLink action="renderLogs" id="${requestId}" onSuccess="updateArea(data)">
	    	Fetch logs
		</g:remoteLink>
  </div>
  <r:layoutResources/>
</body>
</html>