package grepp.server

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask
import java.util.regex.Pattern
import javax.annotation.PostConstruct
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
	
	def grailsApplication
	
	ConcurrentHashMap<String, FutureTask<String>> workers = new ConcurrentHashMap<String, FutureTask<String>>()	
	def highestDir
	def defaultLogsDir = "."
	File curDir
	Pattern pathPattern = Pattern.compile(/ (.*)/) 
	ParamsHolderFactory<?> paramsFactory
	ConfigHolder configHolder
	
	public GreppRunnerService() {	
		configHolder = new ConfigHolder(GreppUtil.getResourcePathOrNull("config.xml"), GreppUtil.getResourcePathOrNull("config.xsd"))
		paramsFactory = configHolder.getParamsHolderFactory()
	}
	
	@PostConstruct
	public void init(){ 
		highestDir = resolveFile(grailsApplication.getFlatConfig()["highestDir"])
		if (highestDir != null) {
			highestDir = highestDir.getAbsolutePath()
		}
		curDir = resolveFile(grailsApplication.getFlatConfig()["logsDir"])
		
		if (curDir == null) {
			log.debug("Current dir shouldn't be null. Setting to default")
			curDir = new File(defaultLogsDir)
		}
		else {
			defaultLogsDir = curDir.getAbsolutePath()
		}
		
		log.debug("Initial directory set to ${curDir}")
	}
	
	private File resolveFile(String filePath) {
		if (filePath == null) {
			return null
		}
		
		if (filePath.contains("classpath:")) {
			resolveFile(GreppUtil.getResourcePathOrNull(filePath.replaceFirst("classpath:", '')))
		}
		else {
			def file = new File(filePath)
			if (file.exists()) {
				return file
			}
			else {
				log.debug("File denoted by path $filePath does not exist")
				return null
			}
		}
	}
	
	private def listFiles() {
		
		if (curDir == null) {
			log.debug("curDir was nulled somehow")
			curDir = highestDir != null ? new File(highestDir) : new File(defaultLogsDir)
		}
		
		def resultList = [".."]
		resultList.addAll(
			curDir.listFiles() != null ? curDir.listFiles().sort {
				(it.isDirectory() ? -1000 : 0) + it.getName().length()
			}.collect {
				it.getName() + (it.isDirectory() ? "/" : "")
			}
			: []
		)

		["result": resultList]
	}
	
	def getLegend() {
		//TODO: get legend from params factory
		return '[-[:option:]] [--:filter_option:] [-L LOG_ENTRY_PATTERN] [FILTER_PATTERN] [--dtime FROM_DATE TO_DATE] [FILENAME [FILENAME]]'
	}
	
	def getOptions() {
		def allOptions = [:]
		allOptions[""] = [
							"opt" : ["List available flags and commands"],
							"cd" : ["Change current directory"],
							"ls" : ["List current directory contents"]
						 ]
		allOptions.putAll(configHolder.getOptions())
		return ["options": allOptions]
	}
	
	private boolean checkCurDir() {
		if (highestDir != null) {
			def curAbsPath = curDir.getCanonicalPath()
			log.debug("Checking if $curAbsPath is inside $highestDir")
			return curAbsPath.startsWith(highestDir)
		}
		else {
			return true
		}
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
						if (curDir.isFile()) {
							log.debug("Can't cd into file")
							curDir = curDir.getParentFile()
							def result = listFiles()
							result["error"] = "Can't cd into file"
							return result
						}
					}
					catch (FileNotFoundException ex) {
						log.debug("Can't find a file indetified by" + path, ex)
						return ["error": "Dir not found"]
					}
				}
				if (!checkCurDir()) {
					curDir = new File(highestDir)
					def result = listFiles()
					result["error"] = "Reached highest dir. Not allowed to go outside."
					return result
				}
				else {
					return  listFiles()
				} 
				break
			case ~/opt/:
				return getOptions()
				break
			default:
				paramsFactory = configHolder.getParamsHolderFactory()
				GreppWorkerBuilder workerBuilder = new GreppWorkerBuilder(paramsFactory, curDir)
				def origParams = params_.split(' ')
				workerBuilder.setParams(origParams)
				GreppWorker worker = workerBuilder.buildWorker()
				FutureTask<String> greppFuture = new FutureTask<String>(worker, "done");
				String currentRequest = worker.getId()
				workers.put(currentRequest, greppFuture)
				greppFuture.run()
				def fileName = workerBuilder.getParams().getSpoolFileName()
				return ["requestId" : currentRequest, "fileName": fileName.startsWith(".") ? origParams[origParams.length-1] : fileName]
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
