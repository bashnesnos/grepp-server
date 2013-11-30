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
		} else if (unicode == 27) { // Escape was pressed
			$('#commandLine').val('')
		}
	}
}


var logsFetcher = new Worker("${createLinkTo(dir: 'js', file: 'logsFetcher.js')}");
logsFetcher.onmessage = function(event) {
	console.log("Got event from fetcher " + event.data.requestId);
	if (event.data.requestId != null) {
		fetchAllLogs(event.data.requestId)
	} else {
		curRequestId = null;
	}
}

var curRequestId;
var curFileName;

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
		console.log("Got logs for " + requestId);
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
	$('#alertsArea').html('Nothing yet');
	$('#logArea').html('');
	if (curRequestId != null) {
		$.get("cancel/" + curRequestId);
	}
	var startProcessing = $.get("start", {
		request : requestString
	}, function(data) {
		if (data.error != null) {
			updateArea([ {
				data : data.error
			} ]);
		} else if (data.result != null) {
			$('#commandLine').val('');
			updateArea([ {
				data : data.result
			} ]);
		} else if (data.options != null) {
			$('#commandLine').val('');
			var resultList = new Array();
			withGreppOptions(data.options, function(curPrefix, flag, flagVal){
				resultList.push(curPrefix + flag + ":" + flagVal);
			});
			
			// console.log(resultList);
			updateArea([ {
				data : resultList
			} ]);
		} else if (data.requestId != null) {
			curRequestId = data.requestId;
			curFileName = data.fileName;
			console.log("Got requestId");
			logsFetcher.postMessage({
				requestId : curRequestId
			});
		}
	}, "json");
}