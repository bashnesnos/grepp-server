<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<r:require module="jquery-dev"/>
<r:require module="application"/>
<r:layoutResources/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Logs page</title>
</head>
<body>
  <div class="body">
  	  <div class="commandDiv">
	  	<label for="commandLine">&gt;&gt; </label><input id="commandLine" type="text" autofocus onkeyup="submitOnCtrlEnter($(this).val(), event)"/>
	  </div>
	  <div class="alertsDiv">
	  	<p id="alertsArea" onclick="javascript:$(this).html('')"></p>
	  </div>
	  <div class="logsDiv">
	  	<pre id="logArea"></pre>
	  </div>
	  <g:javascript>

				$(document).ready(function() {
					findLogs("ls");
					$('#alertsArea').html('Enter your command to the line above. Type \'opt\' to view the options');
					
					$('#commandLine').autocomplete(
					{
						source: function(request, response) {
									var getOptions = $.get("getOptions",
										function(data) {
											console.log(request);
											var resultList = new Array();
											withGreppOptions(data.options, function(curPrefix, flag, flagVal){
												var fullOpt = curPrefix + flag;
												console.log(fullOpt);
												if (fullOpt.search(request.term) > -1) {
													console.log("matched");
													resultList.push({label: fullOpt + " : " + flagVal, value: fullOpt});
												}
											});
											response(resultList);
										}
										, "json");
								}
					})
					
				});
							
		</g:javascript>
  </div>
  <r:layoutResources/>
</body>
</html>