		 var version_buildString = "Version beta_3.0  0901_2021:02:36__war=NPDownloader_0901.war"; 
         var fatalErrorBeginMarker = "$$$_FATAL_BEGIN_$$$";
         var fatalErrorEndMarker = "$$$_FATAL_END_$$$";
         var ajaxType = 0;
         var ajaxRequest;
         var autoScrollEnabled = false; 
         var programName;
         var restart = false;
         var allDivNames = new Array();
         var base64ImageStringArray = null;
         var targetDownloadFilePathsArray = null;
         var probabilityValueArray = null;
         var imageDataURLArray = null;
         var imageDataURLMap = null;
         var targetDownloadFilesMap = null;
         var number_RangeThresholdValue = null;
         var selected_thresholdImage = null;
         var range_thresholdSlider = null;
         var downloadFilePathAndName = null;
         var anchor_downloadFile = null;
         var range_slider_minValue = 0;
         var range_slider_maxValue = 0;
         var range_slider_stepValue = 0;
         var autoScrollHelpMsg = "double click slider button to toggle auto-scroll on/off";
         var autoScrollHelpPending = true;
   	     var div_snackbar = null;
         var ajaxRequest_startTime = null;
         var currentNetworkMapImage = null;
         var DELIMITER_NETWORK_MAP_ITEMS = "&@&";
         var DELIMITER_NETWORK_MAP_DATA = "$@$";
         var download_target_map_nii = null;
         var downloadZIP_path = null;
         var downloadDisabled = true;
         var downloadDisabledMessage = "Downloads disabled pending study review";
         var loggingEnabled = true;
         var oldConsoleLog = null;
         var selectElement = null;
         var div_uploadProgress = null;
         var progress_upload = null;
         var div_unzipProgress = null;
         var token = null;
         var studyMenuIDArray = new Array();

         
         /**
          * 1) Move title and change font
          * 2) Change tabs to just text items
          * 3) splash image below tab selections
          * 4) remove extra twitter button
          * 5) Change fonts in captions
          * 
          * @returns
          */
         
         
         function startup() {
        	 console.log("startup()...invoked");
        	 //console.log = function() {};
        	 console.log(version_buildString);
        	 div_uploadProgress = document.getElementById("div_uploadProgress");
        	 progress_upload = document.getElementById("progress_upload");
        	 div_unzipProgress = document.getElementById("div_unzipProgress");
        	 sessionStorage.clear();
        	 setAjaxStyle();
        	 getMenuData();
        	 console.log("startup()...end");
         }
         
         function continueStartup() {
        	 console.log("continueStartup()...invoked");
        	 startupMenu();
        	 loadAllDivNames();
        	 hideAllDivs();
        	 selectElement = document.getElementById("select_neuralNetworkName");
           	 var div_submitNotification = document.getElementById("div_submitNotification");
        	 div_submitNotification.style.display = "block";
        	 var anchor_ABCD_combined = document.getElementById("a_ABCD_combined_clusters");
        	 menuClicked(anchor_ABCD_combined, true, true);
        	 number_RangeThresholdValue = document.getElementById("number_thresholdValue");
        	 range_thresholdSlider = document.getElementById("range_threshold");
        	 anchor_downloadFile = document.getElementById("anchor_downloadFile");
        	 
        	 range_thresholdSlider.addEventListener('dblclick', function (e) {
        		   toggleAutoScroll();
        	 });
        	 
       	     div_snackbar = document.getElementById("snackbar");
        	 number_inputThresholdValueControl = document.getElementById("number_thresholdValue");

        	 resetSelectedTab();
        	 
        	 // do a little centering adjustment so that the menu element
        	 // lines up nicely with the dropdown
        	 var element_ul_menu_atlasType = document.getElementById("ul_submenu").parentElement;
        	 var x_offset = getOffset(element_ul_menu_atlasType).left;
        	 //was -27
        	 var new_x_offset = x_offset - 148;
        	 var new_px_value = new_x_offset + "px";
        	 //myEl.style.position = "absolute";
        	 element_ul_menu_atlasType.style.left = new_px_value;
        	 
        	 // do same adjustment for button
        	 var button_dscalar = document.getElementById("button_downloadNetworkDscalar");
        	 var button_dscalar_x_offset = getOffset(button_dscalar).right;
        	 var new_button_dscalar_x_offset = button_dscalar_x_offset - 78;
        	 button_dscalar.style.right = new_button_dscalar_x_offset;
        	 
        	 
        	 initializeDragDrop();

       	     //registerScrollEventListener();
        	 console.log("continueStartup()...exit.");
        	 //toggleLogging();
         }
         
         function registerScrollEventListener() {
        	 
        	 document.addEventListener('keydown', function(e){
        		    if(e.keyCode === 40) {
        		        //down();
        		        e.preventDefault();
        		    } else if(e.keyCode === 38) {
        		        //up();
        		        e.preventDefault();
        		    }
        	 })
        	 
         }
         
         function addTableRow(tableId) {
            console.log("addTableRow()...invoked.");
            
       	 	var networkTypes = document.getElementsByClassName("select_networkType");
       	 	if(networkTypes.length == 3) {
       	 		doAlert("The maximum number of network type entries is 3", alertOK);
       	 		return;
       	 	}

 			var table = document.getElementById(tableId);

 			var rowCount = table.rows.length;
 			var row = table.insertRow(rowCount);
 			row.className = "admin";

 			var cell1 = row.insertCell(0);
 			cell1.innerHTML = "Network Display Name";
 			cell1.className = "admin";

 			var cell2 = row.insertCell(1);
 			var td_addNetworkType = document.getElementById("td_selectNetworkType");
 			var innerHTML = td_addNetworkType.innerHTML;
 			cell2.className = "admin";
 			
 			cell2.innerHTML = innerHTML;
 			
 			/*
 			var element2 = document.createElement("input");
 			element2.type = "text";
            element2.className = "option_folder_name";
            element2.size = "25";
            cell2.appendChild(element2);
            */
            console.log("addTableRow()...exit.");

 		}

         function deleteTableRow(tableID) {
            console.log("deleteTableRow()...invoked.");
            
 			var table = document.getElementById(tableID);
 			var rowCount = table.rows.length;
            table.deleteRow(rowCount-1);
            console.log("deleteTableRow()...exit.");

         }
         
         function alertOK() {
        	 console.log("alertOK()...invoked.")
        	 enableScroll();
         }
         
       
         function copyStackTrace() {
         	console.log("copyStackTrace()...invoked...");
         	
         	var textArea = document.getElementById("errorStackTraceTextArea");
         	textArea.select();
         	document.execCommand("copy");
         	
         	console.log("copyStackTrace()...exit...");

         }
         
         function buildMenuIdDropdown() {
        	 console.log("buildMenuIdDropdown()...invoked...");
        	 var dropdown_menuIDs = document.getElementById("select_menuId");
        	 dropdown_menuIDs.length = 0;
        	 var option = null;
        	 
        	 option = document.createElement('option');
        	 option.text = "choose a study id";
        	 option.classList.add("menuIdOption");
        	 option.value = "none selected";
    	 	 dropdown_menuIDs.add(option);

        	 for(let i=0; i<studyMenuIDArray.length; i++) {
        		 option = document.createElement('option');
        	 	 option.text = studyMenuIDArray[i];
        	 	 option.classList.add("menuIdOption");
        	 	 option.value = studyMenuIDArray[i];
        	 	 dropdown_menuIDs.add(option);
         	 }
        	 console.log("buildMenuIdDropdown()...exit...");
         }
         
         
         function buldNeuralNetworkDropdownList(responseText) {
        	 console.log("displayNeuralNetworkList()...invoked...");
        	 var neuralNetworkNames = JSON.parse(responseText); 
        	 //alert(neuralNetworkNames);
        	 let option;
        	 let selectedIndex = 0;
        	 let dropdown_neuralNetworkName = document.getElementById("select_neuralNetworkName");
        	 
        	 for(let i=0; i<neuralNetworkNames.length; i++) {
        		 option = document.createElement('option');
        	 	 option.text = neuralNetworkNames[i];
        	 	 option.classList.add("networkOption");
        	 	 if(option.text.includes("combined_clusters")) {
        	 		selectedIndex = i; 
        	 	 }
        	 	 option.value = neuralNetworkNames[i];
        	 	 dropdown_neuralNetworkName.add(option);
         	 }
        	 
        	 dropdown_neuralNetworkName.selectedIndex = selectedIndex;
        	 let div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        	 //div_selectNeuralNetworkName.style.display = "block";
        	 //div_getThresholdImagesButton = document.getElementById("div_getThresholdImagesButton");
        	 //div_getThresholdImagesButton.style.display = "block";

        	 console.log("displayNeuralNetworkList()...exit...");
         }
         
         function buildStudyMenu(responseData) {
        	 console.log("buildStudyMenu()...invoked, responseData follows");
        	 console.log(responseData);
        	 
        	 var menuArray = responseData.split("::");
        	 var menuEntry = null;
        	 var studyEntry = null;
        	 var menuEntryArray = null;
        	 var submenuOptionsArray = null;
        	 var menuInnerHTML = "";
        	 var surfaceVolumeType = null;
        	 
        	 for(var i=0; i<menuArray.length; i++) {
        		 menuEntry = menuArray[i];
        		 menuEntryArray = menuEntry.split(":");
        		 studyEntry = menuEntryArray[0];
        		 
            	 var openParenIndex = menuEntry.indexOf("(");
            	 var closeParenIndex = menuEntry.indexOf(")");
            	 var studyName = menuEntry.substring(openParenIndex+1, closeParenIndex);
            	 var dashIndex = menuEntry.indexOf("-");
            	 var shortId = menuEntry.substring(0, dashIndex).trim();
            	 
            	 openParenIndex = studyEntry.lastIndexOf("(");
            	 closeParenIndex = studyEntry.lastIndexOf(")");
            	 surfaceVolumeType = studyEntry.substring(openParenIndex+1, closeParenIndex);
            	 
            	 submenuOptionsArray = menuEntryArray[1].split(",");
        		 menuInnerHTML += buildStudyMenuEntry(studyEntry);
        		 menuInnerHTML += buildSubmenuOptionEntries(submenuOptionsArray, studyName, shortId, surfaceVolumeType);
        	 }
        	 
        	//menuInnerHTML += tag_endLI;
        	//menuInnerHTML += tag_endUL;
        	 
        	buildMenuIdDropdown();
        	
        	var targetMenuParent = document.getElementById("ul_submenu");
        	targetMenuParent.innerHTML = menuInnerHTML; 
        	
       	    console.log("buildStudyMenu()...exit.");
         }
         
         function buildSubmenuOptionEntries(optionsArray, studyName, shortId, surfaceVolumeType) {
        	 //<li class=\x22subSubMenu\x22><a class=\x22subSubMenu\x22  data-study=\x22${studyName}\x22 onmouseover=\x22showSubSubMenu(this)\x22" 
             //+ "onmouseout=\x22mouseOut(this)\x22 onclick=\x22menuClicked(this, false, true )\x22>${displayName} data-networkId=\x22${networkId}</a></li>";

        	 console.log("buildSubmenuOptionEntries()...invoked, optionsArray=" + optionsArray);

        	 var optionEntry = null;
        	 var liEntry = null;
        	 var openParenIndex = null;
        	 var closeParenIndex = null;
        	 var displayText = null;
        	 var networkId = null;
        	 var menuHTML = "";
        	 var id = null;
        	 
        	 
        	 for(var i=0; i<optionsArray.length; i++) {
        		 optionEntry = optionsArray[i];
            	 openParenIndex = optionEntry.indexOf("(");
            	 closeParenIndex = optionEntry.indexOf(")");
            	 displayText = optionEntry.substring(0, openParenIndex).trim();
            	 networkId = optionEntry.substring(openParenIndex+1, closeParenIndex).trim();
            	 id = "a_" + shortId + "_" + networkId;
        		 liEntry = template_li_subSubMenu;
        		 liEntry = liEntry.replace(idReplacementMarker, id);
        		 liEntry = liEntry.replace(networkIdReplacementMarker, networkId);
        		 liEntry = liEntry.replace(studyReplacementMarker, studyName);
        		 liEntry = liEntry.replace(displayNameReplacementMarker, displayText);
        		 liEntry = liEntry.replace(surfaceVolumeTypeReplacementMarker, surfaceVolumeType);
        		 menuHTML += liEntry;
        	 }
        	 menuHTML += tag_endUL;
        	 menuHTML += tag_endLI;

        	 return menuHTML;
         }
         
         function buildStudyMenuEntry(menuEntry) {
        	 console.log("buildStudyMenuEntry()...invoked, menuEntry=" + menuEntry);
        	 // ABCD - Template Matching (abcd_template_matching) (surface, volume)
        	 var openParenIndex = menuEntry.indexOf("(");
        	 var closeParenIndex = menuEntry.indexOf(")");
        	 var studyFolderName = menuEntry.substring(openParenIndex+1, closeParenIndex);
        	 studyMenuIDArray.push(studyFolderName);
        	 var dashIndex = menuEntry.indexOf("-");
        	 var shortId = menuEntry.substring(0, dashIndex).trim();
        	 var displayText = menuEntry.substring(0, openParenIndex).trim();
        	 var studyName = menuEntry.substring(openParenIndex+1, closeParenIndex);
        	 
        	 openParenIndex = menuEntry.lastIndexOf("(");
        	 var dataTypes = menuEntry.substring(openParenIndex);
        	 
        	 var liTag = template_li_submenu;
        	 liTag = liTag.replaceAll(idReplacementMarker, shortId);
        	 liTag = liTag.replace(displayNameReplacementMarker, displayText);
        	 var ulTag = template_ul_subSubMenu;
        	 ulTag = ulTag.replace(idReplacementMarker, shortId);
        	 
        	 console.log("buildStudyMenuEntry()...invoked, response=" + liTag + "\n" + ulTag);
        	 return liTag + ulTag;
        	 
        	 
        	 //console.log("buildStudyMenuEntry()...invoked, displayText=" + displayText);
        	 //console.log("buildStudyMenuEntry()...invoked, studyName=" + studyName);
        	 //console.log("buildStudyMenuEntry()...invoked, dataTypes=" + dataTypes);

         }
         
         //function disableScroll() {
        	 //console.log("disableScroll()...invoked.");
        	 //var body_element = document.getElementById("body");
        	 //body_element.classList.add("disable_scrolling");
         //}
         
         //function enableScroll() {
        	// console.log("enableScroll()...invoked.");
        	 //var body_element = document.getElementById("body");
        	 //body_element.classList.remove("disable_scrolling");
         //}
         
 
         
         function displayThresholdImageElements() {
        	 console.log("displayThresholdImageElements()...invoked.");

        	 hideAllDivs();
        	 
        	 var div_menu = document.getElementById("div_submenu");
        	 div_menu.style.display = "block";
        	         	 
        	 selected_thresholdImage = document.getElementById("img_threshold");
        	 //get first label
        	 var minValueLabel = probabilityValueArray[0];
        	 console.log("probabilityValueArray[0]=" + probabilityValueArray[0]);
        	 //alert("displayThresholdImageElements()...minValueLabel=" + minValueLabel);
        	 var imageSrcURL = imageDataURLMap.get(minValueLabel);
        	 console.log("displayThresholdImageElements()...minValueLabel=" + minValueLabel);
        	 //alert("imageSrcURL=" + imageSrcURL);
        	 selected_thresholdImage.src = null;
           	 let div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        	 //div_selectNeuralNetworkName.style.display = "block";
           	 var div_networkMapImage = document.getElementById("div_networkMapImage");
        	 
        	 div_thresholdImage = document.getElementById("div_thresholdImage");
        	 div_thresholdImage.style.display = "block";
        	 selected_thresholdImage.src = imageSrcURL;
        	 
        	 var div_networkMapImage = document.getElementById("div_networkMapImage");
        	 div_networkMapImage.style.display = "block";
        	 //selected_thresholdImage.src = imageSrcURL;
        	 
        	 var div_dummyNetworkMap = document.getElementById("div_dummyNetworkMap");
        	 //var div_instructions = document.getElementById("div_instructions");
        	 
        	 //div_instructions.style.display = "block";
        	 

        	 //window.URL.revokeObjectURL(imageSrcURL); 
        	 displayThresholdRangeSlider();
        	 
    		 var div_selectNeuralNetwork = document.getElementById("div_selectNeuralNetworkName");

    		 //NOTE: overlapping displays in menu as 'integration zone'
        	 if(selectedSubmenuAnchor.id.includes("single") || selectedSubmenu.id.includes("overlapping")) {
        		 div_selectNeuralNetwork.style.display = "block";
        		 div_dummyNetworkMap.style.display = "none";
        	 }
        	 
        	 if(selectedSubmenuAnchor.id.includes("overlapping")) {
          		 div_selectNeuralNetwork.style.display = "none"; 
        	 }
        	 
        	 if(selectedSubmenuAnchor.id.includes("combined")) {
        		 div_networkMapImage.style.display = "none";
          		 div_selectNeuralNetwork.style.display = "none"; 
        		 div_dummyNetworkMap.style.display = "block";
        		 //div_dummyNetworkMap.style.visibility = "hidden";
		         /*
        		 var img_networkMap = document.getElementById("img_networkMap");
        		 img_networkMap.src = "/NetworkProbabilityDownloader/images/DCAN.png";
        		 */
        	 }
        	
        	 else {
        		 div_networkMapImage.style.display = "block";
        	 }
         	 
         	 var div_submitNotification = document.getElementById("div_submitNotification");
         	 div_submitNotification.style.display = "none";
         	 
             var ul_atlasMenu = document.getElementById("ul_atlasMenu");
             ul_atlasMenu.style.display = "block";
                	 
        	 trackThresholdValue(true);

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
             span_leftRangeLabel.innerHTML = range_thresholdSlider.min + "&nbsp;";
             
             var span_rightRangeLabel = document.getElementById("span_rightRangeLabel");
             span_rightRangeLabel.innerHTML = "&nbsp;" + range_thresholdSlider.max;
        	 
        	 div_rangeSlider.style.display = "block";
        	 range_thresholdSlider.value = range_thresholdSlider.min;
        	 number_RangeThresholdValue.value = range_thresholdSlider.value;


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
         
         function doAlert(msg, okFn) {
        	disableScroll();
         	var alertBox = $("#alertBox");
             alertBox.find(".message").text(msg);
             alertBox.find(".ok").unbind().click(function()
             {
                 alertBox.hide();
             });
             alertBox.find(".ok").click(okFn);
             console.log("doAlert()...msg=" + msg);
             alertBox.show();
         }
         
         
         function doSubmissionAlert(selectedNeuralNetworkName) {

         	console.log("doSubmissionAlert()...invoked.");
         	         	
         	var div_selectNeuralNetwork = document.getElementById("div_selectNeuralNetworkName");
         	div_selectNeuralNetwork.style.display = "none";
         	         	
         	//var textArea = document.getElementById("textArea_progressUpdateId");
         	//textArea.value = "Retrieving images for " + selectedNeuralNetworkName;
        	//textArea.scrollTop = textArea.scrollHeight - textArea.getBoundingClientRect().height;
         	alert("progressDiv=" + div_progressUpdateDiv);

         }
         
         function downloadFile(choice) {
        	 console.log("downloadFile()...invoked.");
        	 
        	 if(downloadDisabled) {
        		 doAlert(downloadDisabledMessage, alertOK);
        		 return;
        	 }
        	 if(choice==1) {
            	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadZIP_path;
            	 anchor_downloadFile.click();
            	 return;
        	 }
        	 else if(choice==2) {
        		 downloadFilePathAndName = download_target_map_nii;
            	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadFilePathAndName;
            	 anchor_downloadFile.click();
            	 return;
        	 }
        	 var key = range_thresholdSlider.value;
        	 if(key.indexOf(".")==-1) {
        		 key = key + ".0";
        	 }
        	 console.log("key=" + key);
        	 downloadFilePathAndName = targetDownloadFilesMap.get(key);
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

         	/*
         	var startOverDiv = document.getElementById("div_startOver");
         	startOverDiv.style.display = "block";
         	
         	var startOverButton = document.getElementById("startOverButton");
         	startOverButton.style.display = "inline-block";
         	*/
         	console.log("errorAlertOK()...exit...");
         	
         }
         
         
         function getAjaxRequest() {
        	console.log("getAjaxRequest()...invoked.");
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
        	console.log("getAjaxRequest()...exit: ajaxRequest=" + ajaxRequest);
          	return ajaxRequest;
          }
         
         function getMenuData() {
         	console.log("getMenuData()...invoked...");
         	
         	ajaxRequest_startTime = performance.now();

           	//var paramString = "&selectedStudy=" + selectedStudy;
           	//paramString += "&selectedDataType=" + selectedDataType;
           	//alert("paramString=" + paramString);

         	var ajaxRequest = getAjaxRequest();
         	
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getMenuData";
         	
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
                    //var responseArray = ajaxRequest.responseText.split("!@!");
                  	//var div_submitNotification = document.getElementById("div_submitNotification");
               	    //div_submitNotification.style.display = "none";
 
                    var responseArray = ajaxRequest.responseText.split("&&&");
                    token = responseArray[0];
               	    buildStudyMenu(responseArray[1]);
               	    continueStartup();
               	    //buldNeuralNetworkDropdownList(responseArray[1]);
                    //processThresholdImagesResponse(responseArray[2]);
                }
                if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                	alert("The server is not responding.")
                	return;
                }
       		
         	}
          	ajaxRequest.send();
         	console.log("getMenuData()...exit...");
         }
         
         /* getNeuralNetworkNames() returns the list of neural network names that display
          * in the dropdown when Single Networks is the selected menu choice.
          * 
          * Additionally, it returns all image files for Combined Networks that appear in
          * the main image panel.
          * 
          */
         function getNeuralNetworkNames() {

         	console.log("getNeuralNetworkNames()...invoked...");
         	
         	ajaxRequest_startTime = performance.now();

           	var paramString = "&selectedStudy=" + selectedStudy;
           	paramString += "&selectedDataType=" + selectedDataType;
           	//alert("paramString=" + paramString);

         	var ajaxRequest = getAjaxRequest();
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getNeuralNetworkNames"
         		      + paramString;
         	
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
                     var responseArray = ajaxRequest.responseText.split("!@!");
                   	 var div_submitNotification = document.getElementById("div_submitNotification");
                	 div_submitNotification.style.display = "none";
           
                	 console.log(responseArray[0]);
                	 console.log(responseArray[1]);

                	 //buildStudyMenu(responseArray[0]);
                	 buldNeuralNetworkDropdownList(responseArray[0]);
                     processThresholdImagesResponse(responseArray[1]);
                 }
                 if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                 	alert("The server is not responding.")
                 	return;
                 }
                 
         	}
          	ajaxRequest.send();
          	console.log("getNeuralNetworkNames()...exit...");
         
         }
         
         function getOffset(el) {
        	  const rect = el.getBoundingClientRect();
        	  return {
        	    left: rect.left + window.scrollX,
        	    top: rect.top + window.scrollY
        	  };
        	}
         
         function getProbabilityValueSubstring(aFilePath) {
        	 
        	 var beginIndexMarker = "thresh";
        	 var beginIndexAdjustment = 0;
        	 
        	 if(!aFilePath.includes("thresh1.0")) {
        		 beginIndexAdjustment = beginIndexMarker.length+1; 
        	 }
        	 else {
        		 beginIndexAdjustment = beginIndexMarker.length;
        	 }
        	 
        	 var beginIndex = aFilePath.indexOf(beginIndexMarker) + beginIndexAdjustment;
        	 var endIndex = aFilePath.indexOf(".png");
        	 
        	 var valueString = aFilePath.substring(beginIndex, endIndex);
        	 
        	 return valueString;
     
         }
         
         function getRawProbabilityValue(aFilePath) {
        	 
            	//console.log("getRawProbabilityValue()...invoked...fileName=" + aFilePath);

        		var beginIndex = aFilePath.indexOf("_thresh") + 7;
        		var endIndex = aFilePath.lastIndexOf(".");
        		
        		var digitPortion = aFilePath.substring(beginIndex, endIndex);
            	//console.log("getRawProbabilityValue()...exit...digitPortion=" + digitPortion);
        		return digitPortion;
         }
         
         function getThresholdImages() {
        	 
           	console.log("getThresholdImages()...invoked...");
           	
         	ajaxRequest_startTime = performance.now();
    
           	//var alertMsg = "Retrieving image data for " + selectedNeuralNetworkName;
           	//doAlert(alertMsg, alertOK);
           	
        	 var div_submitNotification = document.getElementById("div_submitNotification");
        	 div_submitNotification.style.display = "block";
        	 
          	 var div_selectNeuralNetwork = document.getElementById("div_selectNeuralNetworkName");
         	 div_selectNeuralNetwork.style.display = "none";
        	 //scrollToProgressIndicator();

           	
           	var ajaxRequest = getAjaxRequest();
           	console.log("selectedNeuralNetworkName=" + selectedNeuralNetworkName);
           	var paramString = "&neuralNetworkName=" + selectedNeuralNetworkName; 
           	paramString += "&selectedStudy=" + selectedStudy;
           	paramString += "&selectedDataType=" + selectedDataType;
           	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getThresholdImages" + paramString;
           	console.log("getThresholdImages()...url=" + url);

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
                       
                       var responseArray = ajaxRequest.responseText.split("!@!");
                       
                       if(selectedSubmenuAnchor.id.includes("combined")) {
                            processThresholdImagesResponse(responseArray[1]);
                       }
                       else {
                    	   priorSelectedDataType = selectedDataType;
                    	   var combinedDataArray = responseArray[1].split(DELIMITER_NETWORK_MAP_DATA);
                    	   /* first, process the image data for the Network Probabilistic Map  */
                    	   processNetworkProbabilityMapData(combinedDataArray[0]);
                    	   /* now process all image files for the TEMPLATE MATCHNG PROBABILISTIC image panel */
                    	   processThresholdImagesResponse(combinedDataArray[1]);
                       }
                   }
                   if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                   	alert("The server is not responding.")
                   	return;
                   }
                   
           	}

            	ajaxRequest.send();
            	console.log("getThresholdImages()...exit...");
          }
         
        
         
         function handleContactUsClicked() {
             console.log("handleContactUsClicked()...invoked.");
             
             var div_overview = document.getElementById("div_overview");
             var div_download = document.getElementById("div_download");
             var div_resources = document.getElementById("div_resources");
             var div_midbAtlas = document.getElementById("div_midbAtlas");
             var div_contactUs = document.getElementById("div_contactUs");
             
             div_overview.style.display = "none";
             div_resources.style.display = "none";
             div_download.style.display = "none";
             div_midbAtlas.style.display = "none";
             div_contactUs.style.display = "block";
             
             console.log("handleContactUsClicked()...exit.");

         }
         
         function handleFileUploadResponse(responseText) {
             console.log("handleFileUploadResponse()...invoked.");
             doAdminAlert(responseText);
             
             console.log("handleFileUploadResponse()...exit.");

         }
         
         function handleTabSelected(id) {

        	 //alert("handleTabSelected()...invoked, id=" + id);
             console.log("handleTabSelected()...invoked, id=" + id);
             
        	 var tab_home = document.getElementById("tab_home");
        	 var tab_overview = document.getElementById("tab_overview");
        	 var tab_resources = document.getElementById("tab_resources");
        	 var tab_download = document.getElementById("tab_download");
        	 var tab_contactUs = document.getElementById("tab_contactUs");
        	 var tab_midbAtlas = document.getElementById("tab_midbAtlas");
        	 

        	 tab_home.checked = false;
        	 tab_overview.checked = false;
        	 tab_download.checked = false;
        	 tab_resources.checked = false;
        	 tab_midbAtlas.checked = false;
        	 tab_contactUs.checked = false;



             var div_home = document.getElementById("div_home");
             var div_overview = document.getElementById("div_overview");
             var div_download = document.getElementById("div_download");
             var div_resources = document.getElementById("div_resources");
             var div_midbAtlas = document.getElementById("div_midbAtlas");
             var div_contactUs = document.getElementById("div_contactUs");
             var heading_sitename = document.getElementById("sitename");
             var div_admin = document.getElementById("div_admin");
             var div_addStudy = document.getElementById("div_addStudy");
             
             div_home.style.display = "none";
             div_overview.style.display = "none";
             div_resources.style.display = "none";
             div_download.style.display = "none";
             div_midbAtlas.style.display = "none";
             div_contactUs.style.display = "block";


             //var id = element.id;
             
             if(id.includes("tab_home")) {
            	 tab_home.checked = true;
            	 div_home.style.display = "block";
                 div_overview.style.display = "none";
                 div_download.style.display = "none";
                 div_resources.style.display = "none";
                 div_midbAtlas.style.display = "none";
                 div_contactUs.style.display = "none"; 
                 div_admin.style.display = "none";
             }
             else if(id.includes("tab_overview")) {
               tab_overview.checked = true;
               div_overview.style.display = "block";
               div_download.style.display = "none";
               div_resources.style.display = "none";
               div_midbAtlas.style.display = "none";
               div_contactUs.style.display = "none";
               div_admin.style.display = "none";
               //heading_sitename.scrollIntoView();
             }
             else if(id.includes("tab_download")) {
               tab_download.checked = true;
               div_overview.style.display = "none";
               div_resources.style.display = "none";
               mouseOutInstructions();
               div_download.style.display = "block";
               div_midbAtlas.style.display = "none";
               div_contactUs.style.display = "none";
               div_admin.style.display = "none";
             }
             else if(id.includes("tab_resources")) {
            	 tab_resources.checked = true;
                 div_overview.style.display = "none";
                 div_resources.style.display = "block";
                 div_download.style.display = "none";
                 div_midbAtlas.style.display = "none";
                 div_contactUs.style.display = "none";
                 div_admin.style.display = "none";
             }
             else if(id.includes("tab_midbAtlas")) {
            	 tab_midbAtlas.checked = true;
                 div_overview.style.display = "none";
                 div_resources.style.display = "none";
                 div_download.style.display = "none";
                 div_midbAtlas.style.display = "block";
                 div_contactUs.style.display = "none";
                 div_admin.style.display = "none";
             }
             else if(id.includes("tab_contactUs")) {
            	 tab_contactUs.checked = true;
                 div_overview.style.display = "none";
                 div_resources.style.display = "none";
                 div_download.style.display = "none";
                 div_midbAtlas.style.display = "none";
                 div_admin.style.display = "none";
                 div_contactUs.style.display = "block";
             }
             else if(id.includes("div_admin")) {
                 div_overview.style.display = "none";
                 div_resources.style.display = "none";
                 div_download.style.display = "none";
                 div_midbAtlas.style.display = "none";
                 div_contactUs.style.display = "none";
          	     var anchor_addStudy = document.getElementById("a_addStudy");
          	     anchor_addStudy.style.color = "#FFC300";
                 //div_dropZone.style.display = "block";
                 div_admin.style.display = "block";
                 div_addStudy.style.display = "block";
             }
             
             console.log("handleTabSelected()...exit.");
        }

         
         function hideAllDivs() {
        	 console.log("hideAllDivs()...invoked...");
        	 var divName = null;
        	 var divElement = null;
        	 
        	 for(var i=0;i<allDivNames.length;i++) {
        		divName = allDivNames[i];
        		divElement = document.getElementById(divName);
        		if(divElement != null) {
        			divElement.style.display = "none";
        		}
        	 }
        	 console.log("hideAllDivs()...exit...");
         }
         


         
         function playRangeSlider() {
        	 range_thresholdSlider.value = 0.98;
        	 range_thresholdSlider.value = 0.99;
        	 range_thresholdSlider.value = 1.0;
         }
         
         function preProcessGetThresholdImages(isDropdownChanged) {
        	 console.log("preProcessGetThresholdImages()...invoked.");

        	 hideAllDivs();
        	 
        	 //when dropdown changes, we don't go into menuClicked() function
        	 //so we must set the selectedNeuralNetworkName
        	 if(isDropdownChanged) {
                 selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value; 
        	 }

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
        	 allDivNames.push("div_networkMapImage");
        	 allDivNames.push("div_submenu");
        	 allDivNames.push("div_networkMapImage");
        	 allDivNames.push("div_dummyNetworkMap");


        	 //allDivNames.push("div_overview");
        	 //allDivNames.push("div_resources");

        	 console.log("loadAllDivNames()...exit.");
         }
         
         function mouseOverInstructions() {
        	   console.log("mouseOverInstructions()...invoked.");
        	   //instructionsSpan = document.getElementById("span_instructions");
        	   //instructionsSpan.style.display = "block";
         }
         
         function mouseOutInstructions() {
      	       console.log("mouseOutInstructions()...invoked.");
        	   //instructionsSpan = document.getElementById("span_instructions");
        	   //instructionsSpan.style.display = "none";
         }

         function notifyUploadComplete(event) {
        	 console.log("notifyUploadComplete()...invoked, e=" + event);
             div_uploadProgress.style.display = "none";
             div_unzipProgress.style.display = "block";
         }
         

         /*
          * 
          */
         function processFileDownloadResponse(ajaxRequest) {
        	 
        	 var fileAsBase64String = ajaxRequest.responseText;
        	 var imageSrcURLPrefix = "data:image/jpg;base64,";
        	 var hrefURL = imageSrcURLPrefix + aBase64String;
         }
         
         function processNetworkProbabilityMapData(responseData) {
        	 
       	  	  console.log("processNetworkProbabilityMapData()...invoked.")

       	  	  var networkMapDataArray = responseData.split(DELIMITER_NETWORK_MAP_ITEMS);
       	  	  var base64_png_for_map = networkMapDataArray[0];
       	  	  download_target_map_nii = networkMapDataArray[1];
       	  	  console.log(download_target_map_nii);
       	  	  var imageSrcURLPrefix = "data:image/png;base64,";
       	  	  
       	  	  var mapImageSrcURL = imageSrcURLPrefix + base64_png_for_map;
       	  	  
       	  	  var networkProbabilityMapImage = document.getElementById("img_networkMap");
       	  	  
       	  	  networkProbabilityMapImage.src = mapImageSrcURL;
       	  	  
       	  	  console.log("processNetworkProbabilityMapData()...exit.")

         }
              
          function processThresholdImagesResponse(ajaxResponseText) {
        	  
        	  console.log("processThresholdImagesResponse()...invoked.")
        	  
        	  var ajaxRequest_endTime = performance.now();
        	  var ajaxRequest_elapsedTime = ajaxRequest_startTime - ajaxRequest_endTime;
        	  
        	  console.log("processThresholdImagesResponse()...ajaxRequest_elapsedTime=" + ajaxRequest_elapsedTime);
        	  
        	  imageDataURLArray = new Array();
        	  probabilityValueArray = new Array();
        	  targetDownloadFilePathsArray = new Array();
        	  
        	  
        	  var responseArray = ajaxResponseText.split(":@:");

        	  var base64Strings = responseArray[0];
        	  console.log("processThresholdImagesResponse()...base64Strings.length=" + base64Strings.length);

        	  base64ImageStringArray = base64Strings.split(",");
        	  console.log("processThresholdImagesResponse()...size of base64ImageStringArray=" + base64ImageStringArray.length);
        	  //console.log("b64_0=" + base64ImageStringArray[0]);
        	  //console.log("b64_1=" + base64ImageStringArray[1]);
        	  var filePathsString = responseArray[1];
        	  //console.log("processThresholdImagesResponse()...size of filePathsString=" + filePathsString.length);

        	  var imageFilePathsArray = filePathsString.split(",");
        	  //console.log("processThresholdImagesResponse()...size of filePathsString=" + filePathsString.length);
        	  //console.log("processThresholdImagesResponse()...size of imageFilePathsArray=" + imageFilePathsArray.length);
        	  //console.log(imageFilePathsArray[0]);
        	  //console.log(filePathsString);
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
        	  
        	  //currentNetworkMapImage = base64ImageStringArray[0];
        	  
        	  for(var i=0; i<base64ImageStringArray.length; i++) {
        		  aBase64String = base64ImageStringArray[i];
        		  anImageSrcURL = imageSrcURLPrefix + aBase64String;
        		  imageDataURLArray.push(anImageSrcURL);
        	  }
        	  
        	  //var compareResult = imageDataURLArray[98].localeCompare(imageDataURLArray[99]);
        	  //console.log("String.compare result=" + compareResult);
        	  
        	  var substringIndex = 1;
        	  
        	  for(var i=0; i<imageFilePathsArray.length; i++) {
        		  if(i>0) {
        			  priorPath = anImageFileAndPath;
        		  }
        		  anImageFileAndPath = imageFilePathsArray[i].trim();
        		  if(i==0) {
        			  var zipPathIndex = anImageFileAndPath.lastIndexOf("/");
        			  var downloadZIP_root = anImageFileAndPath.substring(0, zipPathIndex+1);
        			  var downloadZIP_fileName = downloadZIP_root.substring(0, zipPathIndex);
        			  var fileName = anImageFileAndPath.substring(0, zipPathIndex);
        			  var fileNameIndex = fileName.lastIndexOf("/");
        			  fileName = fileName.substring(fileNameIndex+1);
        			  fileName = fileName + ".zip";
        			  downloadZIP_path = downloadZIP_root + fileName;
        			  console.log("downloadZIP_path=" + downloadZIP_path);
        			  console.log("imageFileAndPath for min=" + anImageFileAndPath);
        			  setRangeValue(anImageFileAndPath, "min");
        		  }
        		  else if(i==imageFilePathsArray.length-1) {
        			  console.log("imageFileAndPath for max=" + anImageFileAndPath);
        			  setRangeValue(anImageFileAndPath, "max");
        		  }
        		  else if(i==1) {
        			  setRangeStep(priorPath, anImageFileAndPath);
        		  }
  
        		  pngIndex = anImageFileAndPath.indexOf(pngMarker);
        		  downloadTargetFile = anImageFileAndPath.substring(0, pngIndex);
        		  downloadTargetFile = downloadTargetFile + ".dlabel.nii";
        		  targetDownloadFilePathsArray.push(downloadTargetFile);
        		  aProbabilityValue = getRawProbabilityValue(imageFilePathsArray[i]);
        		  
        		  if(aProbabilityValue.startsWith(".")) {
        			  aProbabilityValue = "0" + aProbabilityValue;
        			  //console.log(aProbabilityValue);
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
        	  //enableScroll();
       	  
        	  console.log("processThresholdImagesResponse()...exit.");
          }
         

         

         function resetSelectedTab() {
        	 console.log("resetSelectedTab()...invoked");
        	 var tab_home = document.getElementById("tab_home");
        	 var tab_overview = document.getElementById("tab_overview");
        	 var tab_resources = document.getElementById("tab_resources");
        	 var tab_download = document.getElementById("tab_download");

        	 tab_home.checked = true;
        	 tab_overview.checked = false;
        	 tab_resources.checked = false;
        	 tab_download.checked = false;

        	 handleTabSelected(tab_home.id);
        	 console.log("resetSelectedTab()...exit");
         }
         
         function scrollToInstructions() {
       	     console.log("scrollToInstructions()...invoked.")
    		 document.getElementById("h4_instructions").scrollIntoView({ behavior: 'smooth', block: 'center' });
         }
         
         function scrollToProgressIndicator() {
        	 //alert("scrolled...");
        	 disableScroll();
       	     console.log("scrollToProgressIndicator()...invoked.")
    		 document.getElementById("div_submitNotification").scrollIntoView({ behavior: 'smooth', block: 'end' });
         }  
         
         function setAjaxStyle() {
        	console.log("setAjaxStyle()...invoked");
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
        	console.log("setAjaxStyle()...exit: ajaxStyle=" + ajaxType);

         }
         
         function setRangeStep(path1, path2) {
        	 
        	 var value1 = getProbabilityValueSubstring(path1);
        	 console.log("setting slider step value 1=" + value1);
        	 var value2 = getProbabilityValueSubstring(path2);
        	 console.log("setting slider step value 2=" + value2);
        	 var stepValue = value2 - value1;
        	 var stepValue2Digits = stepValue.toFixed(2);        	 
        	 console.log("setting slider step value=" + stepValue2Digits);
        	 range_thresholdSlider.step = stepValue2Digits;
        	 number_inputThresholdValueControl.step = stepValue2Digits;
         }
         
         function setRangeValue(aFilePath, propToSet) {
        	 console.log("setRangeValue()...invoked");
        	 
        	 //var valueString = getProbabilityValueSubstring(aFilePath);
        	 var valueString = getRawProbabilityValue(aFilePath);
        	 
        	 if(propToSet.includes("min")) {
        		 console.log("setting min range value:" + valueString);
        		 range_thresholdSlider.min = valueString;
        		 number_inputThresholdValueControl.min = valueString;
        	 }
        	 else if(propToSet.includes("max")) {
        		 console.log("setting max range value:" + valueString);
        		 range_thresholdSlider.max = valueString;
        		 number_inputThresholdValueControl.max = valueString;
        	 }
         }
         
         function sliderRefreshGate() {
        	 trackThresholdValue();
        	 /*
        	 if(range_thresholdSlider.disabled) {
        		 return;
        	 }
        	 range_thresholdSlider.disabled = true; 
        	 setTimeout(trackThresholdValue, 100);
        	 //range_thresholdSlider.disabled = false; 
        	 */
         }
         
         function showSnackbar(message, timeoutMS) {
        	 
        	  console.log("showSnackbar()...invoked...message=" + message);
        	  div_snackbar.innerHTML = message;

        	  // Add the "show" class to DIV
        	  div_snackbar.className = "show";

        	  // After 3 seconds, remove the show class from DIV
        	  setTimeout(function(){ div_snackbar.className = div_snackbar.className.replace("show", ""); }, timeoutMS);
         } 
         
         function toggleAutoScroll() {
        	        	         	 
        	 if(autoScrollEnabled) {
        		 autoScrollEnabled = false;
        		 message = "auto-scroll disabled"
            	 console.log("toggleAutoScroll()...autoScrollEnabled=" + autoScrollEnabled);
        	 }
        	 else {
        		 autoScrollEnabled = true;
        		 message = "auto-scroll enabled";
            	 console.log("toggleAutoScroll()...autoScrollEnabled=" + autoScrollEnabled);
        	 }
        	 showSnackbar(message, 2000);
         }
         
         function trackInputNumberChange() {
        	 
        	 //console.log("trackInputNumberChange()...invoked.");
        	 var newValue = number_inputThresholdValueControl.value;
        	 
        	 if(newValue.startsWith(".")) {
        		 newValue = "0" + newValue;
        		 number_inputThresholdValueControl.value = newValue;
        	 }
        	 
        	 range_thresholdSlider.value = number_inputThresholdValueControl.value;
        	 //console.log("trackInputNumberChange()...new value =" + range_thresholdSlider.value); 
        	 trackThresholdValue(false);
         }
         
         
         function trackThresholdValue(isFirstTrackingEvent) {
        	 //console.log("trackThresholdValue()...invoked...autoScrollEnabled=" + autoScrollEnabled);
        	 /*
        	 if(autoScrollEnabled) {
        		 console.log("autoScrolling into view...");
        		 document.getElementById("div_thresholdImage").scrollIntoView({ behavior: 'smooth', block: 'center' });
        	 }
        	 */
        	 
        	 if(range_thresholdSlider==null) {
        		 range_thresholdSlider = document.getElementById("range_threshold");
        	 }
        	 var usedAdjustedLabel = false;
        	 var selectedValue = range_thresholdSlider.value;
        	 
        	 if(selectedValue.indexOf(".")== -1) {
        		 usedAdjustedLabel = true;
        	 }
        	 
        	 
        	 /*
        	 if(range_thresholdSlider.value==1) {
        		 //console.log("adjusting range_thresholdSlider.value to 1.0");
        		 usedAdjustedLabel = true;
        	 }
        	 */
        	 
        	 if(usedAdjustedLabel) {
        		 //console.log("setting imageSrc with key=" + adjustedLabel);
            	 number_RangeThresholdValue.value = selectedValue + ".0";
            	 
            	 selected_thresholdImage.src = imageDataURLMap.get(number_RangeThresholdValue.value);
            	 //console.log(selected_thresholdImage.src);
        	 }
        	 
        	 else if(isFirstTrackingEvent) {
            	 //console.log("trackThresholdValue()...isFirstTrackingEvent=true");
            	 //console.log("trackThresholdValue()...probabilityValueArray[0]=" + probabilityValueArray[0]);
            	 //range_thresholdSlider.value = probabilityValueArray[1];
         	     //selected_thresholdImage.src = imageDataURLMap.get(probabilityValueArray[1]);
            	 range_thresholdSlider.value = probabilityValueArray[0];
         	     selected_thresholdImage.src = imageDataURLMap.get(probabilityValueArray[0]);
        	 }
        	 else {
        	    selected_thresholdImage.src = imageDataURLMap.get(range_thresholdSlider.value);
           	    //console.log("trackThresholdValue()...range_thresholdSlider.value=" + range_thresholdSlider.value);
           	    number_RangeThresholdValue.value = range_thresholdSlider.value; 
        	 }

        	 range_thresholdSlider.focus({preventScroll: true});
        	 range_thresholdSlider.disabled = false; 
        	 range_thresholdSlider.focus({preventScroll: false});
        	 
        	 /*
        	 if(autoScrollHelpPending) {
           	     //setTimeout(function(){ showSnackbar(autoScrollHelpMsg, 6000); }, 1000);
        		 div_snackbar.innerHTML = autoScrollHelpMsg;
           	     div_snackbar.className = "show_4";
           	     setTimeout(function(){ div_snackbar.className = div_snackbar.className.replace("show_4", ""); }, 4000);
           	     autoScrollHelpPending = false;
        	 }
        	 */
        	 //console.log("trackThresholdValue()...exit.");
         }
         
      
         var logger = function()
         {
             var oldConsoleLog = null;
             var pub = {};

             pub.enableLogger =  function enableLogger() 
                                 {
            	    
                                     if(oldConsoleLog == null)
                                         return;

                                     window['console']['log'] = oldConsoleLog;
                                 };

             pub.disableLogger = function disableLogger()
                                 {
                                     oldConsoleLog = console.log;
                                     window['console']['log'] = function() {};
                                 };

             return pub;
         }();
         
         function toggleLogging() {
        	 if(loggingEnabled) {
        		 loggingEnabled = false;
        		 logger.disableLogger();
        	 }
        	 else {
        		 loggingEnabled = true;
        		 logger.enableLogger();
        	 }
         }
         
         function toggleDownloadLock() {
        	 console.log("toggleDownloadLock()...invoked.");
        	 downloadDisabled = !downloadDisabled;
        	 console.log("toggleDownloadLock()...exit.");
         }
         
         function uploadMenuFiles(studyFolderName, availableDataTypes, menuEntry) {
        	 console.log("uploadFiles()...invoked.");
        	 
          	var ajaxRequest = getAjaxRequest();
         	//var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getNeuralNetworkNames";
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=uploadStudyFiles";
         	
         	var paramString = "&studyFolderName=" + studyFolderName;
         	paramString += "&menuEntry=" + menuEntry;
         	paramString += "&availableDataTypes=" + availableDataTypes;

         	
         	url += paramString;

         	var encodedUrl = encodeURI(url);
         	ajaxRequest.open('post', encodedUrl, true);
         	ajaxRequest.timeout = 600000*2;
   
         	ajaxRequest.onreadystatechange=function() {
         		console.log("onreadystatechange, responseText=" + ajaxRequest.responseText);
         		console.log("onreadystatechange, readyState=" + ajaxRequest.readyState);
         		console.log("onreadystatechange, status=" + ajaxRequest.status);

                //console.log(ajaxRequest.responseText);
                if(ajaxRequest.responseText.includes("Unexpected Error")) {
                	var errorBeginIndex = ajaxRequest.responseText.indexOf(fatalErrorBeginMarker) + 19;
             		var errorEndIndex = ajaxRequest.responseText.indexOf(fatalErrorEndMarker);
             		var errorData = ajaxRequest.responseText.substring(errorBeginIndex, errorEndIndex);
                 	var errorArray = errorData.split("&");
                	var msg1 = errorArray[0];
                	var msg2 = errorArray[1];
                	stackTraceData = errorArray[2];
                	var divSubmitNotification = document.getElementById("div_submitNotification");
                	divSubmitNotification.style.display = "none";
                	doErrorAlert(msg1, msg2, errorAlertOK);
                	console.log("uploadMenuFiles()...onreadystatechange...error");
                	console.log("msg1=" + msg1);
                	console.log("msg2=" + msg2);
                	console.log(stackTraceData);
                	return;
                }
                if (ajaxRequest.readyState == 4 && ajaxRequest.status == 200) {
                	div_uploadProgress.style.display = "none";
                	div_unzipProgress.style.display = "none";
                    handleFileUploadResponse(ajaxRequest.responseText);
         	    }

         	}
         	
         	ajaxRequest.upload.onprogress = function(e) {
         		
         		// https://stackoverflow.com/questions/32045093/xmlhttprequest-upload-addeventlistenerprogress-not-working
                //div_uploadProgress.style.display = "block";
				// if the file upload length is known, then show progress bar
				if (e.lengthComputable) {
	                div_uploadProgress.style.display = "block";
					//uploadProgress.classList.remove("hide-me");
					// total number of bytes being uploaded
					progress_upload.setAttribute("max", e.total);
					// total number of bytes that have been uploaded
					progress_upload.setAttribute("value", e.loaded);
					if(e.total == e.loaded) {
		                div_uploadProgress.style.display = "none";
		                div_unzipProgress.style.display = "block";
					}
					//console.log(e.loaded);
				}

			};
						
			// this does not work on firefox so using comparison of e.loaded == e.value -> see above
         	/*
			ajaxRequest.onloadend = function(e) {
         		console.log("onloadend");
         		// https://stackoverflow.com/questions/32045093/xmlhttprequest-upload-addeventlistenerprogress-not-working
                //div_uploadProgress.style.display = "block";
				// if the file upload length is known, then show progress bar
                div_uploadProgress.style.display = "none";
                div_unzipProgress.style.display = "block";
                
			};
			*/
         	
         	
          	ajaxRequest.send(zipFormData);
          	console.log("uploadFiles()...exit.");
         }
            
         
