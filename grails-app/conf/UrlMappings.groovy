class UrlMappings {

	static mappings = {
         "/home"(controller: "home")
		 "/home/renderLogs/$id?"(controller: "home", action: "renderLogs")			
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
