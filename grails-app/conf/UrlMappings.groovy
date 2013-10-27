class UrlMappings {

	static mappings = {
        "/home"(controller: "home")
		"/home/renderLogs/$id?"(controller: "home", action: "renderLogs")
		"/home/start"(controller: "home", action: "start")			
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
