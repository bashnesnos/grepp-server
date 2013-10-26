package org.smltools.grepp;

import org.smltools.grepp.config.ParamsHolder
import org.smltools.grepp.output.WgrepOutput
import org.smltools.grepp.processors.FileProcessor
import org.smltools.grepp.filters.FilterChainFactory
import org.smltools.grepp.config.Param
import org.smltools.grepp.config.ParamsHolderFactory
import groovy.util.logging.Slf4j

@Slf4j
public class GreppWorkerBuilder {
	private String requestId
	private ParamsHolder params
	private WgrepOutput<?, ?> output
	private static ParamsHolderFactory<?> paramsFactory
	
	public static void setFactory(ParamsHolderFactory<?> factory) {
		this.paramsFactory = factory
	} 
	
	public GreppWorkerBuilder setParams(def args) {
		this.params = paramsFactory.getParamsHolder(args)
		return this
	}
	
	public GreppWorkerBuilder setRequestId(String requestId) {
		this.requestId = requestId
	}
	
	public GreppWorkerBuilder setParams(ParamsHolder params_) {
		this.params = params_
		return this
	}
	
	public GreppWorkerBuilder setOutput(WgrepOutput<?,?> output) {
		this.output = output
		return this
	}
	
	public GreppWorker buildWorker() {
		if (params != null) {
			if (requestId == null) {
				requestId = UUID.randomUUID().toString()
			}
			
			if (output == null) {
				log.debug('Creating default output')
				output = new MongoOutput(params, requestId)
			}
			
			return new GreppWorker(new FileProcessor(output, FilterChainFactory.createFileFilterChain(params), !params.checkParamIsEmpty(Param.FILE_MERGING)), requestId, params.getProcessingData())
		}
		else {
			throw new IllegalStateException("Params should be supplied!");
		}
	} 
}
