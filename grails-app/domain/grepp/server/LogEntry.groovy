package grepp.server

import org.bson.types.ObjectId

class LogEntry {

    static constraints = {
    }
	
	ObjectId id
	java.lang.String data
	java.lang.String requestId
}
