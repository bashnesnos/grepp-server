class UrlMappings {

	static mappings = {
        "/"(controller: "logs")
		"/getLogs/$id?"(controller: "logs", action: "getLogs")
		"/start"(controller: "logs", action: "start")
		"/getOptions"(controller: "logs", action: "getOptions")
		"/cancel/$id?"(controller: "logs", action: "cancel")			
        "500"(view:'/error')
	}
}
