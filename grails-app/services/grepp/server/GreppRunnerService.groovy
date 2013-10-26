package grepp.server

import org.smltools.grepp.GreppWorkerBuilder
import org.smltools.grepp.GreppWorker
import org.smltools.grepp.config.ConfigHolder
import org.smltools.grepp.config.ParamsHolderFactory
import org.smltools.grepp.config.PredictingParamsHolderFactory
import org.smltools.grepp.util.GreppUtil
import groovy.util.logging.Slf4j

@Slf4j
class GreppRunnerService {
	
	public GreppRunnerService() {
		def configHolder = new ConfigHolder(GreppUtil.getResourcePathOrNull("config.xml"), GreppUtil.getResourcePathOrNull("config.xsd"))
		ParamsHolderFactory<?> paramsFactory = new PredictingParamsHolderFactory(configHolder)
		GreppWorkerBuilder.setFactory(paramsFactory) 
	}
	
    String runGrepp(String params_) {
		GreppWorkerBuilder workerBuilder = new GreppWorkerBuilder()
		workerBuilder.setParams(params_.split(' '))
		GreppWorker worker = workerBuilder.buildWorker()		
		worker.run()
		return worker.getId()
    }
}
