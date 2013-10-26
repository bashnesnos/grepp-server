package grepp.server

import org.springframework.web.servlet.ModelAndView
import grails.web.JSONBuilder

class HomeController {

	def greppRunnerService
	
    def index() {
		//log.debug("Avavilable files {}", (Object) new File('.').list())
		try {
			def reqId = greppRunnerService.runGrepp("app application.properties")
			render(view: "/home/index", model: [requestId: reqId])
			//render "blabla"
		}
		catch (FileNotFoundException ex) {
			log.debug("Can't find requested file", ex)
			render "File not found"
		} 
	}
	
	def renderLogs() {
		render greppRunnerService.getResults(params.id)
	}
}
