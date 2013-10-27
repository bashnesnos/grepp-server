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
  	  <input id="commandArea" type="text"/>
	  <p id="logArea"></p>
	  <g:javascript>
			var logsFetcher = new Worker("${createLinkTo(dir: 'js', file: 'logsFetcher.js')}");
			
			function updateArea(data) {
	    		var logArea = $("#logArea");
	    		logArea.html(logArea.html() + "<br/>" +	    		
		    		$.map(data, function(entry, idx){
		    			return entry.data
		    		}).join("<br/>")
		    	);
			}
			
			
			function fetchAllLogs(requestId) {
					var getLogs = $.get("renderLogs/" + requestId
						, function(logs) {
							updateArea(logs);
							logsFetcher.postMessage({response: logs.length > 0 ? logs[0].data : null, requestId: requestId});
						}
						, "json" 
					);
			}
			
			logsFetcher.onmessage = function(event) {
				if (event.data.requestId != null) {
					fetchAllLogs(event.data.requestId)
				}			
			}
			
			$(document).ready(function() {
				$('#commandArea').on('change', function(){
					var startProcessing = $.get("start"
						, {request: $(this).val()}
						, function(requestId) {
							logsFetcher.postMessage({requestId: requestId});
						} 
					);
				});
			});
			
		</g:javascript>
  </div>
  <r:layoutResources/>
</body>
</html>