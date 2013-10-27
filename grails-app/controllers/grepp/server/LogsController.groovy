package grepp.server

import org.springframework.web.servlet.ModelAndView
import grails.converters.JSON
import grails.web.JSONBuilder

class LogsController {
	
	static scope = "session"
	
	def greppRunnerService
	
    def index() {
		render(view: "/logs/index")
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
	
	def cancel() {
		render greppRunnerService.cancelRequest(params.id)
	}
}
