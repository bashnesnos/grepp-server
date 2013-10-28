onmessage = function(event) {
	if (event.data.response == null || event.data.response.error == null) {
		postMessage({requestId: event.data.requestId});
	}
	return;
}