		 var version_buildString =  "Version -0.00  08_23_2020:15:00__war=0822.war";
         var fatalErrorBeginMarker = "$$$_FATAL_BEGIN_$$$";
         var fatalErrorEndMarker = "$$$_FATAL_END_$$$";
         var ajaxType = 0;
         var ajaxRequest;
         var programName;
         var restart = false;
         var allDivNames = new Array();
         var base64ImageStringArray = null;
         var targetDownloadFilePathsArray = null;
         var probabilityValueArray = null;
         var imageDataURLArray = null;
         var imageDataURLMap = null;
         var targetDownloadFilesMap = null;
         var span_RangeThresholdValue = null;
         var selected_thresholdImage = null;
         var range_thresholdSlider = null;
         var downloadFilePathAndName = null;
         var anchor_downloadFile = null;
         var range_slider_minValue = 0;
         var range_slider_maxValue = 0;
         var range_slider_stepValue = 0;
         
         
         
         function startup() {
        	 console.log("startup()...invoked");
        	 console.log(version_buildString);
        	 sessionStorage.clear();
        	 setAjaxStyle();
        	 loadAllDivNames();
        	 getNeuralNetworkNames();
        	 span_RangeThresholdValue = document.getElementById("span_thresholdValue");
        	 range_thresholdSlider = document.getElementById("range_threshold");
        	 anchor_downloadFile = document.getElementById("anchor_downloadFile");
        	 console.log("startup()...exit.");
         }
         
         function copyStackTrace() {
         	console.log("copyStackTrace()...invoked...");
         	
         	var textArea = document.getElementById("errorStackTraceTextArea");
         	textArea.select();
         	document.execCommand("copy");
         	
         	console.log("copyStackTrace()...exit...");

         }
         
         
         function displayNeuralNetworkList(responseText) {
        	 console.log("displayNeuralNetworkList()...invoked...");
        	 var neuralNetworkNames = JSON.parse(responseText);   	 
        	 let option;
        	 let dropdown_neuralNetworkName = document.getElementById("select_neuralNetworkName");
        	 
        	 for(let i=0; i<neuralNetworkNames.length; i++) {
        		 option = document.createElement('option');
        	 	 option.text = neuralNetworkNames[i];
        	 	 option.value = neuralNetworkNames[i];
        	 	 dropdown_neuralNetworkName.add(option);
         	 }
        	 
        	 let div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        	 div_selectNeuralNetworkName.style.display = "block";
        	 div_getThresholdImagesButton = document.getElementById("div_getThresholdImagesButton");
        	 div_getThresholdImagesButton.style.display = "block";

        	 console.log("displayNeuralNetworkList()...exit...");
         }
         
         function displayThresholdImageElements() {
        	 console.log("displayThresholdImageElements()...invoked.");
        	 
        	 hideAllDivs();
        	         	 
        	 selected_thresholdImage = document.getElementById("img_threshold");
        	 //get first label
        	 var minValueLabel = probabilityValueArray[0];
        	 //alert("displayThresholdImageElements()...minValueLabel=" + minValueLabel);
        	 var imageSrcURL = imageDataURLMap.get(minValueLabel);
        	 //alert("imageSrcURL=" + imageSrcURL);
        	 selected_thresholdImage.src = imageSrcURL;
        	 div_thresholdImage = document.getElementById("div_thresholdImage");
        	 div_thresholdImage.style.display = "block";
        	 //window.URL.revokeObjectURL(imageSrcURL); 
        	 displayThresholdRangeSlider();
        	 console.log("displayThresholdImageElements()...exit.");
         }
         
         function displayThresholdImage_BLOB(imageByteArray) {
        	 /*
        	 //var imgSrcURL = URL.createObjectURL(imageBlob);
        	 img_threshold = document.getElementById("img_threshold");
        	 //img_threshold.src =imgSrcURL;
        	 //window.URL.revokeObjectURL(imgSrcURL);
             var imageBlob = new Blob([imageByteArray], {type: "image/png"});
             alert(imageBlob);
             alert(imageBlob.size);
             alert(imageBlob.type);
        	 var imgSrcURL = URL.createObjectURL(imageBlob);
        	 img_threshold.src =imgSrcURL;
        	 div_thresholdImage = document.getElementById("div_thresholdImage");
        	 div_thresholdImage.style.display = "block";
        	 window.URL.revokeObjectURL(imgSrcURL);
			 */
         }
         
         function displayThresholdRangeSlider() {
        	 console.log("displayThresholdRangeSlider()...invoked.");
        	 var div_rangeSlider = document.getElementById("div_thresholdSlider");
        	 
             var span_leftRangeLabel = document.getElementById("span_leftRangeLabel");
             span_leftRangeLabel.innerHTML = "0" + range_thresholdSlider.min + "&nbsp;";
             
             var span_rightRangeLabel = document.getElementById("span_rightRangeLabel");
             span_rightRangeLabel.innerHTML = "&nbsp;" + "0" + range_thresholdSlider.max;
        	 
        	 div_rangeSlider.style.display = "block";
        	 range_thresholdSlider.value = range_thresholdSlider.min;
        	 span_RangeThresholdValue.innerHTML = "0" + range_thresholdSlider.value;


        	 range_thresholdSlider.focus();
        	 console.log("displayThresholdRangeSlider()...exit.");
         }
         
         function doErrorAlert(msg1, msg2, okFn) {
         	console.log("doErrorAlert()...invoked...");
         	console.log("msg1=" + msg1);
         	console.log("msg2=" + msg2);

         	hideAllDivs();
     
         	var errorBox = $("#errorBox");
         	errorBox.find(".message").text(msg1);
         	errorBox.find(".ok").unbind().click(function()
             {
         		errorBox.hide();
             });
         	errorBox.find(".ok").click(okFn);
             console.log("doErrorAlert()...msg=" + msg1);
             message2Element = document.getElementById("errorBoxMessage2");
             message2Element.innerHTML = msg2;
             message2Element.style.visibility = "block";
             errorBox.show();
                          
             if(msg2.includes("Could not establish sshConnection")) {
             	doAlert("Could not establish connection with remote server, possible temporary network problem.", alertOK);
             }
            div_spacer = document.getElementById("div_spacer");
            div_spacer.style.display = "block";
          	console.log("doErrorAlert()...exit...");
         }
         
         function downloadFile() {
        	 console.log("downloadFile()...invoked.");
        	 var key = range_thresholdSlider.value;
        	 console.log("key=" + key);
        	 var downloadFilePathAndName = targetDownloadFilesMap.get(key);
        	 console.log("downloadTargetFile name=" + downloadFilePathAndName);
        	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadFilePathAndName;
        	 anchor_downloadFile.click();
        	 console.log("downloadFile()...exit.");
         }
         
         function downloadFileViaAjax(fileName) {
        	 console.log("downloadFileViaAjax()...invoked.");

        	 var ajaxRequest = getAjaxRequest();
        	 
          	 var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadFilePathAndName;
          	 var encodedUrl = encodeURI(url);
          	
        	 ajaxRequest.open("GET", encodedUrl, true);
        	 ajaxRequest.setRequestHeader('my-custom-header', 'custom-value'); // adding some headers (if needed)

        	 ajaxRequest.onload = function (event) {
        		 var blob = req.response;
        		 var fileName = null;
        		 var contentType = req.getResponseHeader("content-type");

        		 // IE/EDGE seems not returning some response header
        		 if (req.getResponseHeader("content-disposition")) {
        			 var contentDisposition = req.getResponseHeader("content-disposition");
        			 fileName = contentDisposition.substring(contentDisposition.indexOf("=")+1);
        		 } else {
        			 fileName = "unnamed." + contentType.substring(contentType.indexOf("/")+1);
        		 }

        		 /*
        		 if (window.navigator.msSaveOrOpenBlob) {
        			 // Internet Explorer
        			 window.navigator.msSaveOrOpenBlob(new Blob([blob], {type: contentType}), fileName);
        		 } else {
        			 var el = document.getElementById("href_download");
        			 el.href = window.URL.createObjectURL(blob);
        			 el.download = fileName;
        			 el.click();
        		 }
        		 */
        		 processFileDownloadResponse(ajaxRequest);
        	 };
        	 
        	 req.send();
        	 console.log("downloadFileViaAjax()...exit.");
         }
         
         function errorAlertOK() {
         	
 			console.log("errorAlertOK()...invoked...");
         	
         	hideAllDivs();
         	
         	var stackTraceTextArea = document.getElementById("errorStackTraceTextArea");
         	stackTraceTextArea.value = stackTraceData;
         	
         	var div_stackTrace = document.getElementById("div_errorStackTrace");
         	div_stackTrace.style.display = "block";
         	
         	var popUpErrorButton = document.getElementById("div_popUpErrorButton");
         	popUpErrorButton.style.display = "block";

         	var startOverDiv = document.getElementById("div_startOver");
         	startOverDiv.style.display = "block";
         	
         	var startOverButton = document.getElementById("startOverButton");
         	startOverButton.style.display = "inline-block";
         	console.log("errorAlertOK()...exit...");
         	
         }
         
         
         function getAjaxRequest() {
          	var ajaxRequest = null;
          	
          	switch(ajaxType) {
          	  case 1:
          		  ajaxRequest = new XMLHttpRequest();
          	      break;
          	  case 2:
          		  ajaxRequest = new ActiveXObject("Msxml2.XMLHTTP");
          		  break;
          	  case 3:  
          		  ajaxRequest = new ActiveXObject("Microsoft.XMLHTTP");
          	} 
          	return ajaxRequest;
          }
         
         
         function getNeuralNetworkNames() {

         	console.log("getNeuralNetworkNames()...invoked...");

         	var ajaxRequest = getAjaxRequest();
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getNeuralNetworkNames";
         	
         	var encodedUrl = encodeURI(url);
         	ajaxRequest.open('get', encodedUrl, true);
         	
             ajaxRequest.onreadystatechange=function() {
                 
                 if (ajaxRequest.readyState==4 && ajaxRequest.status==200) {
                     //console.log(ajaxRequest.responseText);
                     if(ajaxRequest.responseText.includes("Unexpected Error")) {
                     	var errorBeginIndex = ajaxRequest.responseText.indexOf(fatalErrorBeginMarker) + 19;
                  		var errorEndIndex = ajaxRequest.responseText.indexOf(fatalErrorEndMarker);
                  		var errorData = ajaxRequest.responseText.substring(errorBeginIndex, errorEndIndex);
                      	var errorArray = errorData.split("&");
                     	var msg1 = errorArray[0];
                     	var msg2 = errorArray[1];
                     	stackTraceData = errorArray[2];
                     	doErrorAlert(msg1, msg2, errorAlertOK);
                     	return;
                     }
                     displayNeuralNetworkList(ajaxRequest.responseText);
                 }
                 if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                 	alert("The server is not responding.")
                 	return;
                 }
                 
         	}
          	ajaxRequest.send();
          	console.log("getNeuralNetworkNames()...exit...");
         
         }
         
         function getProbabilityValueSubstring(aFilePath) {
        	 
        	 var beginIndexMarker = "thresh";
        	 var beginIndexAdjustment = beginIndexMarker.length+1;
        	 
        	 var beginIndex = aFilePath.indexOf(beginIndexMarker) + beginIndexAdjustment;
        	 var endIndex = aFilePath.indexOf(".png");
        	 
        	 var valueString = aFilePath.substring(beginIndex, endIndex);
        	 
        	 return valueString;
     
         }
         
         function getThresholdImages() {
        	 
           	console.log("getThresholdImages()...invoked...");
           	
           	var selectElement = document.getElementById("select_neuralNetworkName");
           	var selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value

           	var ajaxRequest = getAjaxRequest();
           	var paramString = "&neuralNetworkName=" + selectedNeuralNetworkName; 
           	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getThresholdImages" + paramString;
           	
           	var encodedUrl = encodeURI(url);
           	ajaxRequest.open('get', encodedUrl, true);
         
            ajaxRequest.onreadystatechange=function() {
                   
                   if (ajaxRequest.readyState==4 && ajaxRequest.status==200) {
                       //console.log(ajaxRequest.responseText);
                	   
                       if(ajaxRequest.responseText.includes("Unexpected Error")) {
                       	var errorBeginIndex = ajaxRequest.responseText.indexOf(fatalErrorBeginMarker) + 19;
                    		var errorEndIndex = ajaxRequest.responseText.indexOf(fatalErrorEndMarker);
                    		var errorData = ajaxRequest.responseText.substring(errorBeginIndex, errorEndIndex);
                        	var errorArray = errorData.split("&");
                       	var msg1 = errorArray[0];
                       	var msg2 = errorArray[1];
                       	stackTraceData = errorArray[2];
                       	doErrorAlert(msg1, msg2, errorAlertOK);
                       	return;
                       }
                       
                       processThresholdImagesResponse(ajaxRequest.responseText);
                   }
                   if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                   	alert("The server is not responding.")
                   	return;
                   }
                   
           	}
           
            	ajaxRequest.send();
            	console.log("getThresholdImages()...exit...");
          }
         
         function hideAllDivs() {
        	 console.log("hideAllDivs()...invoked...");
        	 var divName = null;
        	 var divElement = null;
        	 
        	 for(var i=0;i<allDivNames.length;i++) {
        		divName = allDivNames[i];
        		divElement = document.getElementById(divName);
        		divElement.style.display = "none";
        	 }
        	 console.log("hideAllDivs()...exit...");
         }
         
         function preProcessGetThresholdImages() {
        	 console.log("preProcessGetThresholdImages()...invoked.");
        	 getThresholdImages();
        	 console.log("preProcessGetThresholdImages()...exit.");
         }
         
         function loadAllDivNames() {
        	 console.log("loadAllDivNames()...invoked.");
        	 allDivNames.push("div_selectNeuralNetworkName");
        	 allDivNames.push("div_getThresholdImagesButton");
        	 allDivNames.push("div_thresholdImage");
        	 allDivNames.push("div_thresholdSlider");
        	 allDivNames.push("div_errorStackTrace");
        	 allDivNames.push("errorBox");
        	 allDivNames.push("div_selectNeuralNetworkName");
        	 console.log("loadAllDivNames()...exit.");
         }
         
         /*
          * 
          */
         function processFileDownloadResponse(ajaxRequest) {
        	 
        	 var fileAsBase64String = ajaxRequest.responseText;
        	 var imageSrcURLPrefix = "data:image/jpg;base64,";
        	 var hrefURL = imageSrcURLPrefix + aBase64String;
         }
         
         /*
          * The response from the server is an array delimited by :@:
          * array[0] = base64Strings 
          * array[1] = names of the image files
          */
          function processThresholdImagesResponse(ajaxResponseText) {
        	  
        	  console.log("processThresholdImagesResponse()...invoked.")
        	  imageDataURLArray = new Array();
        	  probabilityValueArray = new Array();
        	  targetDownloadFilePathsArray = new Array();
        	  
        	  
        	  var responseArray = ajaxResponseText.split(":@:");

        	  var base64Strings = responseArray[0];
        	  base64ImageStringArray = base64Strings.split(",");
        	  var filePathsString = responseArray[1];
        	  var imageFilePathsArray = filePathsString.split(",");
        	  //alert("tempFilePathsArray length=" + tempFilePathsArray.length);
        	  var aFilePath = null;
        	  var anImageFileAndPath = null;
        	  var priorPath = "";
        	  var anImageSrcURL = null;
        	  var imageSrcURLPrefix = "data:image/png;base64,";
        	  var aBase64String = null;
        	  var slashIndex = 0;
        	  var downloadTargetFile = null;
        	  var pngMarker = ".png";
        	  var pngIndex = 0;
        	  
        	  for(var i=0; i<base64ImageStringArray.length; i++) {
        		  aBase64String = base64ImageStringArray[i];
        		  anImageSrcURL = imageSrcURLPrefix + aBase64String;
        		  imageDataURLArray.push(anImageSrcURL);
        	  }
        	  
        	  var substringIndex = 1;
        	  
        	  for(var i=0; i<imageFilePathsArray.length; i++) {
        		  if(i>0) {
        			  priorPath = anImageFileAndPath;
        		  }
        		  anImageFileAndPath = imageFilePathsArray[i].trim();
        		  if(i==0) {
        			  setRangeValue(anImageFileAndPath, "min");
        		  }
        		  else if(i==imageFilePathsArray.length-1) {
        			  setRangeValue(anImageFileAndPath, "max");
        		  }
        		  else if(i==1) {
        			  setRangeStep(priorPath, anImageFileAndPath);
        		  }
  
        		  pngIndex = anImageFileAndPath.indexOf(pngMarker);
        		  downloadTargetFile = anImageFileAndPath.substring(0, pngIndex);
        		  downloadTargetFile = downloadTargetFile + ".dlabel.nii";
        		  targetDownloadFilePathsArray.push(downloadTargetFile);
        		  aProbabilityValue = getProbabilityValueSubstring(imageFilePathsArray[i]);
        		  if(aProbabilityValue.startsWith(".")) {
        			  aProbabilityValue = "0" + aProbabilityValue;
        		  }
        		  probabilityValueArray.push(aProbabilityValue);
        	  }
        	  
        	  
        	  //now load the map of imageSrcURL objects and targetDownloadFiles
        	  imageDataURLMap = new Map();
        	  targetDownloadFilesMap = new Map();
        	  console.log("processThresholdImagesResponse()...loading map.")

        	  for(var i=0; i<probabilityValueArray.length; i++) {
        		  //console.log("setting imageURLMap value, key=" + probabilityValueArray[i]);
        		  imageDataURLMap.set(probabilityValueArray[i], imageDataURLArray[i]);
        		  //console.log("setting targetDownloadFile value, key=" + probabilityValueArray[i]);
        		  //console.log("setting targetDownloadFile, value=" + targetDownloadFilePathsArray[i]);
        		  targetDownloadFilesMap.set(probabilityValueArray[i], targetDownloadFilePathsArray[i]);
        	  }
        	  
        	  displayThresholdImageElements();
        	  console.log("processThresholdImagesResponse()...exit.")
          }
         

         function setAjaxStyle() {
	        var ajaxRequestForCompatibility;  // The variable that makes Ajax possible!
	
	        try {
	           // Opera 8.0+, Firefox, Safari 
	           ajaxRequestForCompatibility = new XMLHttpRequest();
	           ajaxType = 1;
	        } catch (e) {
	            // Internet Explorer Browsers
	               try {
	            	   ajaxRequestForCompatibility = new ActiveXObject("Msxml2.XMLHTTP");
	                   ajaxType = 2;
	               } catch (e) {
	                  
	               try {
	            	   ajaxRequestForCompatibility = new ActiveXObject("Microsoft.XMLHTTP");
	                   ajaxType = 3;
	               } catch (e) {
	
	                  // Something went wrong
	                  ajaxType = -1;
	                  }
	               }
	            }
	        ajaxRequestForCompatibility = null;
         }
         
         function setRangeStep(path1, path2) {
        	 
        	 var value1 = getProbabilityValueSubstring(path1);
        	 var value2 = getProbabilityValueSubstring(path2);
        	 var stepValue = value2 - value1;
        	 var stepValue2Digits = stepValue.toFixed(2);        	 
        	 
        	 range_thresholdSlider.step = stepValue2Digits;
         }
         
         function setRangeValue(aFilePath, propToSet) {
        	 console.log("setRangeValue()...invoked");
        	 
        	 var valueString = getProbabilityValueSubstring(aFilePath);
        	 
        	 if(propToSet.includes("min")) {
        		 range_thresholdSlider.min = valueString;
        	 }
        	 else if(propToSet.includes("max")) {
        		 range_thresholdSlider.max = valueString;
        	 }
         }
         
         function trackThresholdValue(thresholdRangeControl) {
        	 //console.log("trackThresholdValue()...invoked.");
        	 span_RangeThresholdValue.innerHTML = range_thresholdSlider.value;
        	 selected_thresholdImage.src = imageDataURLMap.get(thresholdRangeControl.value);
        	 //console.log("trackThresholdValue()...exit.");
         }
         
