<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<r:require module="jquery-dev"/>
<r:require module="application"/>
<r:layoutResources/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Logs page</title>
</head>
<body>
  <div class="body">
  	  <div class="commandDiv">
	  	<label for="commandLine">&gt;&gt; </label><input id="commandLine" type="text" onkeyup="cmdLineKeyUp($(this).val(), event)" title="${legend}"/>
	  </div>
	  <div class="alertsDiv">
	  	<a id="saveBtn" href="javascript:onDownload();">Save</a>
	  	<span id="alertsArea" class="alertsArea" onclick="javascript:$(this).html('')"></span>
	  </div>
 	  <div class="logsDiv">
	 	<pre id="logArea"></pre>
	  </div>
	  <g:javascript>

			var logsFetcher = new Worker("${createLinkTo(dir: 'js', file: 'logsFetcher.js')}");
			logsFetcher.onmessage = function(event) {
				//console.log("Got event from fetcher " + event.data.requestId);
				if (event.data.requestId != null) {
					fetchAllLogs(event.data.requestId)
				} else {
					curRequestId = null;
				}
			}
			
			var curRequestId;
			var curFileName;
			var lastCommand;
						
			function placeAlert(alertHtml) {
				$('#alertsArea').html(alertHtml);
			}						
						
			function cmdLineKeyUp(requestStr, e) {
				var evtobj = window.event ? event : e;
				var unicode = e.keyCode ? e.keyCode : e.charCode;
			
				if (evtobj.ctrlKey) {
					if (unicode == 32) { //Space was pressed
						$('#commandLine').autocomplete( "search", "." );
					}
				} else {
					if (unicode == 13) { // Enter was pressed
						findLogs(requestStr);
					} 
					else if (unicode == 27) { // Escape was pressed
						$('#commandLine').val('');
					} 
//					else if (unicode == 8) { //tab was pressed
//						var curVal = $('#commandLine').val('');
//						
//					}
				}
				$('#commandLine').tooltip("close");				
			}
			
			function updateArea(data) {
				var logArea = $("#logArea");
				var alertsArea = $('#alertsArea');
				var curAlertHtml = alertsArea.html();
				alertsArea.html((curAlertHtml.indexOf('Nothing yet') == -1 ? curAlertHtml
						: '')
						+ $.map(data, function(entry, idx) {
							return entry.error
						}).join("<br/>"));
			
				logArea.html(logArea.html() + (data.length < 2 ? "<br/>" : "")
						+ $.map(data, function(entry, idx) {
							return entry.data
						}).join("<br/>"));
			}
			
			function fetchAllLogs(requestId) {
				var getLogs = $.get("getLogs/" + requestId, function(logs) {
					updateArea(logs);
					//console.log("Got logs for " + requestId);
					logsFetcher.postMessage({
						response : logs.length > 0 ? logs[0] : null,
						requestId : requestId
					});
				}, "json");
			}
			
			function withGreppOptions(options, callback) {
				for (opt in options) {
					var curPrefix = opt;
					var curFlags = options[opt];
					for (flag in curFlags) {
						var flagVal = curFlags[flag];
						// console.log(flag + ":" + flagVal);
						if (flagVal != null && flagVal != "") {
							callback(curPrefix, flag, flagVal);
						} else {
							// console.log("skipped");
						}
					}
				}
			}
			
			function findLogs(requestString) {
				placeAlert('Nothing yet');

				$('#logArea').html('');
				if (curRequestId != null) {
					$.get("cancel/" + curRequestId);
				}
				lastCommand = requestString;
				var startProcessing = $.get("start", {
					request : requestString
				}, function(data) {
					if (data.error != null) {
						curFileName = 'error.txt';
						placeAlert(data.error);
					}
					
					if (data.result != null) {
						$('#commandLine').val('');
						curFileName = requestString + '_results.txt';
						updateArea([ {
							data : data.result
						} ]);
					} else if (data.options != null) {
						$('#commandLine').val('');
						var resultList = new Array();
						withGreppOptions(data.options, function(curPrefix, flag, flagVal){
							resultList.push(curPrefix + flag + ":" + flagVal);
						});
						curFileName = 'options.txt';
						// console.log(resultList);
						updateArea([ {
							data : resultList
						} ]);
					} else if (data.requestId != null) {
						curRequestId = data.requestId;
						curFileName = data.fileName;
						//console.log("Got requestId");
						logsFetcher.postMessage({
							requestId : curRequestId
						});
					}
				}, "json");
			}			
			
			function onDownload() {
				var contentToDownload = $('#logArea').html();
				if (contentToDownload != null && contentToDownload != '') {
					var bb = new MSBlobBuilder();
					bb.append(contentToDownload.replace(/<br>/g, '\r\n'));
					var blob1 = bb.getBlob("text/plain");
					window.navigator.msSaveOrOpenBlob(blob1, curFileName);
				}
				else {
					$('#alertsArea').html('Nothing to save');	
				}
			}

			$(document).ready(function() {
				$("#commandLine").focus();
				$("#commandLine").tooltip({
					tooltipClass: "commandTooltip"
				});

				$( "#saveBtn" ).button({ icons: { primary: "ui-icon-disk" }}); 
				
				findLogs("ls");
				$('#alertsArea').html('Enter your command to the line above. Type \'opt\' to view the options');
				
				$('#commandLine').autocomplete(
				{
					source: function(request, response) {
								var resultList = new Array();
								var lastSpace = request.term.lastIndexOf(' ');
								var lastBit = request.term.substring(lastSpace + 1, request.term.length);
								var conservedBit = request.term.substring(0, lastSpace + 1);
								
								if (lastCommand.search(/^(?:(?:ls)|(?:cd))/) > -1) {
									$.each($('#logArea').html().split('<br>'), function(idx, fileName) {
										if (fileName != '' && fileName != '..' && fileName.search(lastBit) > -1) {
											resultList.push({label: "file: " + fileName, value: conservedBit + fileName});
										}
									});
								}
								
								var getOptions = $.get("getOptions",
									function(data) {
										//console.log(request);
										withGreppOptions(data.options, function(curPrefix, flag, flagVal){
											var fullOpt = curPrefix + flag;
											//console.log(fullOpt);
											if (request.term == "" || fullOpt.search(request.term) > -1) {
												//console.log("matched");
												resultList.push({label: fullOpt + " : " + flagVal, value: conservedBit + fullOpt});
											}
										});
									}
									, "json");
								
								getOptions.done(function() {
									console.log(resultList)
									response(resultList);
								});
							}
				})
				
			});
						
		</g:javascript>
  </div>
  <r:layoutResources/>
</body>
</html>