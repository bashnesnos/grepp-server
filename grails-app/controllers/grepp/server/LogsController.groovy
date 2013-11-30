package grepp.server

import org.springframework.web.servlet.ModelAndView
import grails.converters.JSON
import grails.web.JSONBuilder

class LogsController {
	
	static scope = "session"
	def errors
	def greppRunnerService
	
    def index() {
		return new ModelAndView("/logs/index", ["legend":greppRunnerService.getLegend()])
	}
	
	def start(){
		try {
			render greppRunnerService.runGrepp(params.request) as JSON
		}
		catch (FileNotFoundException ex) {
			log.debug("Can't find requested file", ex)
			render (["error": "File not found"]) as JSON
		}
	}
	
	def getLogs() {
		render greppRunnerService.getResults(params.id) as JSON
	}
	
	def getOptions(){
		render greppRunnerService.getOptions() as JSON
	}
	
	def cancel() {
		render greppRunnerService.cancelRequest(params.id)
	}
}
