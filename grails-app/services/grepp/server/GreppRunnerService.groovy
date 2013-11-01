package grepp.server

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask
import java.util.regex.Pattern

import org.smltools.grepp.GreppWorkerBuilder
import org.smltools.grepp.GreppWorker
import org.smltools.grepp.config.ConfigHolder
import org.smltools.grepp.config.ParamsHolderFactory
import org.smltools.grepp.config.PredictingParamsHolderFactory
import org.smltools.grepp.util.GreppUtil

import sun.swing.SwingUtilities2.LSBCacheEntry;
import groovy.util.logging.Slf4j

@Slf4j
class GreppRunnerService {
	static scope = "session"
	
	ConcurrentHashMap<String, FutureTask<String>> workers = new ConcurrentHashMap<String, FutureTask<String>>()	
	File curDir = new File(".")
	Pattern pathPattern = Pattern.compile(/ (.*)/) 
	ParamsHolderFactory<?> paramsFactory
	
	public GreppRunnerService() {
		def configHolder = new ConfigHolder(GreppUtil.getResourcePathOrNull("config.xml"), GreppUtil.getResourcePathOrNull("config.xsd"))
		paramsFactory = configHolder.getParamsHolderFactory()
	}
	
	private def listFiles() {
		if (curDir == null) {
			log.debug("curDir was nulled somehow")
			curDir = new File(".")
		}
		
		def resultList = [".."]
		resultList.addAll(
			curDir.listFiles().sort {
				(it.isDirectory() ? -1000 : 0) + it.getName().length()
			}.collect {
				it.getName() + (it.isDirectory() ? "/" : "")
			}
		)

		["result": resultList]
	}
	
    def runGrepp(String params_) {
		switch(params_) {
			case ~/ls ?.*/:
				return listFiles()
				break
			case ~/cd ?.*/:
				def pathMatcher = pathPattern.matcher(params_)
				if (pathMatcher.find()) {
					String path = pathMatcher.group(1)
					try {
						if (path.startsWith("/") || path.find(/[A-Z]:/)) {
							curDir = new File(path)	
						}
						else {
							curDir = new File("${curDir.getAbsolutePath()}\\$path")
						}
					}
					catch (FileNotFoundException ex) {
						log.debug("Can't find a file indetified by" + path, ex)
						return ["error": "File not found"]
					}
				}
				return listFiles()
				break
			default:
				GreppWorkerBuilder workerBuilder = new GreppWorkerBuilder(paramsFactory, curDir)
				workerBuilder.setParams(params_.split(' '))
				GreppWorker worker = workerBuilder.buildWorker()
				FutureTask<String> greppFuture = new FutureTask<String>(worker, "done");
				String currentRequest = worker.getId()
				workers.put(currentRequest, greppFuture)
				greppFuture.run()
				return ["requestId" : currentRequest]
		}
		
    }
	
	def cancelRequest(String reqId) {
		log.debug("Attempting to cancel request {}", reqId)
		def greppFuture = workers.get(reqId)
		if (greppFuture != null) {
			greppFuture.cancel(true)
			LogEntry.collection.findAndRemove(requestId: reqId)
			workers.remove(reqId)
			log.debug("Cancelled successfully")
		}
		else {
			log.debug("Such request doesn't exist")
		}
	}
	
	def getResults(String reqId) {
		log.debug("Attempting to fetch logs for request {}", reqId)
		if (reqId == null) return [["error":"Invalid requestId"]]
		def greppFuture = workers.get(reqId)
		if (greppFuture != null) {
			def cursor = LogEntry.collection.find(requestId: reqId)
			def currentBatch = cursor.toArray().collect {it as LogEntry}
			currentBatch.sort {it.id}
			cursor.close()
			currentBatch.each {it.delete(flush: true)}
			if (greppFuture.isDone()) {
				log.debug("Background task finished, removing key")
				workers.remove(reqId)
			}
			return currentBatch
		}
		return [["error":"No more entries"]]
	}
}
