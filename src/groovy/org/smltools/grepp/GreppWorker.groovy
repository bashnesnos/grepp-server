package org.smltools.grepp

import org.smltools.grepp.processors.DataProcessor
import groovy.util.logging.Slf4j

@Slf4j
class GreppWorker implements Runnable {

	private DataProcessor<?> greppProcessor
	private Object data
	private String requestId
	
	public GreppWorker(DataProcessor<?> processor, String requestId, Object data) {
		this.greppProcessor = processor
		this.data = data
		this.requestId = requestId
		debugMessage("created")
	}
	
	public String getId() {
		return requestId
	}
	
	private void debugMessage(String message) {
		log.debug("GreppWorker[${requestId}] ${message}")
	}
	
	@Override
	public void run() {
		debugMessage("started")
		greppProcessor.process(data)
		debugMessage("finished")
	}

}
