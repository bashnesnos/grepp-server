class UrlMappings {

	static mappings = {
        "/logs"(controller: "logs")
		"/logs/getLogs/$id?"(controller: "logs", action: "getLogs")
		"/logs/start"(controller: "logs", action: "start")
		"/logs/getOptions"(controller: "logs", action: "getOptions")
		"/logs/cancel/$id?"(controller: "logs", action: "cancel")			
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
