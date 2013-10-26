package grepp.server

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask

import org.smltools.grepp.GreppWorkerBuilder
import org.smltools.grepp.GreppWorker
import org.smltools.grepp.config.ConfigHolder
import org.smltools.grepp.config.ParamsHolderFactory
import org.smltools.grepp.config.PredictingParamsHolderFactory
import org.smltools.grepp.util.GreppUtil

import groovy.util.logging.Slf4j

@Slf4j
class GreppRunnerService {
	ConcurrentHashMap<String, FutureTask<String>> workers = new ConcurrentHashMap<String, FutureTask<String>>()	
	
	public GreppRunnerService() {
		def configHolder = new ConfigHolder(GreppUtil.getResourcePathOrNull("config.xml"), GreppUtil.getResourcePathOrNull("config.xsd"))
		ParamsHolderFactory<?> paramsFactory = new PredictingParamsHolderFactory(configHolder)
		GreppWorkerBuilder.setFactory(paramsFactory) 
	}
	
    String runGrepp(String params_) {
		GreppWorkerBuilder workerBuilder = new GreppWorkerBuilder()
		workerBuilder.setParams(params_.split(' '))
		GreppWorker worker = workerBuilder.buildWorker()
		FutureTask<String> greppFuture = new FutureTask<String>(worker, "done");
		String currentRequest = worker.getId()
		workers.put(currentRequest, greppFuture)
		greppFuture.run()
		return currentRequest
		
    }
	
	def getResults(String reqId) {
		log.debug("Attempting to fetch logs for request {}", reqId)
		def greppFuture = workers.get(reqId)
		if (greppFuture != null) {
			def cursor = LogEntry.collection.find(requestId: reqId)
			def currentBatch = cursor.toArray()
			cursor.close()
			currentBatch.each {(it as LogEntry).delete(flush: true)}
			if (greppFuture.isDone()) {
				log.debug("Background task finished, removing key")
				workers.remove(reqId)
			}
			return currentBatch
		}
		return "No more entries"
	}
}
