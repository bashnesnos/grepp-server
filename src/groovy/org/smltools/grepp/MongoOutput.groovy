package org.smltools.grepp

import grepp.server.LogEntry

import org.smltools.grepp.config.ParamsHolder;
import org.smltools.grepp.filters.enums.Event;
import org.smltools.grepp.output.SimpleOutput
import org.smltools.grepp.output.WgrepOutput

class MongoOutput extends SimpleOutput {

	private String requestId
	
	public MongoOutput(ParamsHolder params_, String requestId) {
		super(params_)
		this.requestId = requestId
	}
	
	
	@Override
	protected void printNotFiltered(Object data) {
		if (data != null)
		{
			LogEntry newEntry = new LogEntry()
			newEntry.setData(data)
			newEntry.setRequestId(requestId);
			newEntry.save(flush:true)
		}
		else
		{
			log.trace("data is null, not printing it")
		}
	}


}
