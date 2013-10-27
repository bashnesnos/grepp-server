onmessage = function(event) {
	if (event.data.response == null || event.data.response.indexOf('No more entries') == -1) {
		postMessage({requestId: event.data.requestId});
	}
	return;
}