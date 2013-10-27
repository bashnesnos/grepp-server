package grepp.server

import org.springframework.web.servlet.ModelAndView
import grails.converters.JSON
import grails.web.JSONBuilder

class HomeController {

	def greppRunnerService
	
    def index() {
		render(view: "/home/index")
	}
	
	def start(){
		try {
			def reqId = greppRunnerService.runGrepp(params.request)
			render reqId
		}
		catch (FileNotFoundException ex) {
			log.debug("Can't find requested file", ex)
			render "File not found"
		}
	}
	
	def renderLogs() {
		render greppRunnerService.getResults(params.id) as JSON
	}
}
