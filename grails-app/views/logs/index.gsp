<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<r:require module="jquery"/>
<r:require module="application"/>
<r:layoutResources/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Main page</title>
</head>
<body>
  <div class="body">
  	  <div class="commandDiv">
	  	<label for="commandLine">&gt;&gt; </label><input id="commandLine" type="text" autofocus onkeyup="submitOnCtrlEnter($(this).val(), event)"/>
	  </div>
	  <div class="logsDiv">
	  	<p id="logArea">Enter your command to the line above</p>
	  </div>
	  <g:javascript>
		
			function submitOnCtrlEnter(requestStr, e) { 
				var evtobj=window.event? event : e;
				var unicode=e.keyCode? e.keyCode : e.charCode;
				
				if (evtobj.ctrlKey)	{
				}
				else {
					if (unicode == 13) { //Enter was pressed
						 findLogs(requestStr);
					}
					else if (unicode == 27) { //Escape was pressed
						$('#commandLine').val('')
					}
				}
			}
	  
			var logsFetcher = new Worker("${createLinkTo(dir: 'js', file: 'logsFetcher.js')}");
			var curRequestId;
			
			function updateArea(data) {
	    		var logArea = $("#logArea");
	    		var curHtml = logArea.html(); 
	    		logArea.html((curHtml.indexOf('Nothing yet') == -1 ? curHtml : '') + (data.length < 2 ? "<br/>" : "") +	    		
		    		$.map(data, function(entry, idx){
		    			return entry.data
		    		}).join("<br/>")
		    	);
			}
			
			
			function fetchAllLogs(requestId) {
					var getLogs = $.get("getLogs/" + requestId
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
				else {
					curRequestId = null;
				}			
			}
			
			function findLogs(requestString){
					$('#logArea').html('Nothing yet<br/>');
					if (curRequestId != null) {
						$.get("cancel/" + curRequestId);
					}
					var startProcessing = $.get("start"
						, {request: requestString}
						, function(data) {
							if (data.error != null) {
								updateArea([{data: data.error}]);
							}
							else if (data.result != null) {
								$('#commandLine').val('');
								updateArea([{data: data.result}]);
							}
							else if (data.requestId != null) {
								curRequestId = data.requestId;
								logsFetcher.postMessage({requestId: curRequestId});
							}
						} 
						, "json"
					);
				}			
		</g:javascript>
  </div>
  <r:layoutResources/>
</body>
</html>