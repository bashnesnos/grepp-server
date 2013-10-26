package grepp.server

import org.smltools.grepp.MongoOutput
import groovy.util.logging.Slf4j;

@Slf4j
class HomeController {

	def greppRunnerService
	
    def index() {
		//log.debug("Avavilable files {}", (Object) new File('.').list())
		def requestId = greppRunnerService.runGrepp("app application.properties") 
		render "Running $requestId"
	}
}
