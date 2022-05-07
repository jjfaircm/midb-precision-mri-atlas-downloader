		 var version_buildString = BUILD_DATE = "Version beta_91.0  0506_2330_2022:17:24__war=NPDownloader_0506_2330_2022.war";
		 var enableTracing = true; 
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
         var downloadDisabled = false;
         var downloadDisabledMessage = "Downloads disabled pending peer review and paper acceptance. " +
                                       "Individualized maps are available via the " +
                                       "<a href=\"https://collection3165.readthedocs.io\" class=\"downloadMessage\" target=\"_blank\"" +
                                       " style=\"font-style: italic; text-decoration: none; border-bottom:1px solid white; \">ABCC (https://collection3165.readthedocs.io)</a>" +
                                       "<br>If you'd like to utilize the atlases prior to then please email hermosir@umn.edu";
         var downloadDialogueMessage = "Please enter your name and email address.  This information will be used only to keep you " +
                                       "informed of changes and updates to this website.";
         var loggingEnabled = true;
         var oldConsoleLog = null;
         var selectElement = null;
         var div_uploadProgress = null;
         var progress_upload = null;
         var div_unzipProgress = null;
         var token = null;
         var studySummaryMap = new Map();
         var studyDisplayNameMap = new Map();
         var studyMenuIDArray = new Array();
         var priorSelectedStudy = "none";
         var readyToDisplayDownloadDiv = false;
         var lastTokenActionTime = null;
         var cookieCheckDownloadTimer = null;
         var mobileDeviceActive = false;
         
         
         /**
          * 1) Move title and change font
          * 2) Change tabs to just text items
          * 3) splash image below tab selections
          * 4) remove extra twitter button
          * 5) Change fonts in captions
          * 
          * @returns
          */
         
         function isMobile() {
        	  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        	}

         
         
         function startup() {
        	 console.log("startup()...invoked");
 		     console.log("navigator=" + navigator.userAgent);

        	 
 		     if(navigator.userAgent.includes("obile")) {
 		    	 mobileDeviceActive = true;
 		     }
        	 
        	 //alert("startup");
        	 //console.log = function() {};
        	 console.log(version_buildString);
        	 number_RangeThresholdValue = document.getElementById("number_thresholdValue");
        	 //toggleLogging();
        	 var div_download = document.getElementById("div_download");
        	 div_download.style.display = "none";
         	 var div_submitNotification = document.getElementById("div_submitNotification");
         	 div_submitNotification.style.display = "block";
        	 div_uploadProgress = document.getElementById("div_uploadProgress");
        	 progress_upload = document.getElementById("progress_upload");
        	 div_unzipProgress = document.getElementById("div_unzipProgress");
        	 sessionStorage.clear();
        	 setAjaxStyle();
        	 resetSelectedTab();
        	 //doUpdatesAlert("File downloads are enabled now.");
        	 getMenuData();
        	 //getNetworkFolderNamesConfig();
        	 console.log("startup()...end");
         }
         
         function continueStartup() {
        	 console.log("continueStartup()...invoked");
        	 //number_RangeThresholdValue = document.getElementById("number_thresholdValue");
        	 startupMenu();
        	 loadAllDivNames();
        	 hideAllDivs();
        	 //resetSelectedTab();
        	 selectElement = document.getElementById("select_neuralNetworkName");
           	 var div_submitNotification = document.getElementById("div_submitNotification");
        	 div_submitNotification.style.display = "block";
        	 var anchor_ABCD_combined = document.getElementById("a_abcd_template_matching_combined_clusters");
        	 //alert("ready to auto click menu");
        	 menuClicked(anchor_ABCD_combined, true, true);
        	 range_thresholdSlider = document.getElementById("range_threshold");
        	 anchor_downloadFile = document.getElementById("anchor_downloadFile");
        	 
        	 range_thresholdSlider.addEventListener('dblclick', function (e) {
        		   toggleAutoScroll();
        	 });
        	 
       	     div_snackbar = document.getElementById("snackbar");
        	 number_inputThresholdValueControl = document.getElementById("number_thresholdValue");

        	 //resetSelectedTab();
        	 
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
        	 
        	 
        	 initializeDragDropAddStudy();
        	 initializeDragDropUpdateStudy();

        	 resetAddStudyForm()
        	 //doUpdatesAlert("File downloads are enabled now.");
        	 console.log("continueStartup()...exit.");
        	if(!enableTracing) {
				toggleLogging();
			}
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
 			row.className = "admin networkDisplayNameRow";

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
            
            var selectNTElements = document.getElementsByClassName("select_networkType");
            
            if(selectNTElements.length<2) {
            	doAdminAlert("At least 1 Available Network Type must be selected");
            	return;
            }
            
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
         
         function buildMenuIdDropdownForRemoveStudy() {
        	 console.log("buildMenuIdDropdownForRemoveStudy()...invoked...");
        	 var dropdown_menuIDs = document.getElementById("select_menuId_removeStudy");
        	 //dropdown_menuIDs.length = 0;
        	 
		     var j = 0;
		     var maxIndex = dropdown_menuIDs.options.length-1;
		     for(j=maxIndex; j>=0; j--) {
		    	 dropdown_menuIDs.remove(j);
		     }

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
        	 console.log("buildMenuIdDropdownForRemoveStudy()...exit...");
        	
         }
         
         function buildMenuIdDropdownForUpdateStudy() {
        	 console.log("buildMenuIdDropdownForUpdateStudy()...invoked...");
        	 var dropdown_menuIDs = document.getElementById("select_menuId_updateStudy");
        	 //dropdown_menuIDs.length = 0;
        	 
		     var j = 0;
		     var maxIndex = dropdown_menuIDs.options.length-1;
		     for(j=maxIndex; j>=0; j--) {
		    	 dropdown_menuIDs.remove(j);
		     }

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
        	 console.log("buildMenuIdDropdownForUpdateStudy()...exit...");
        	
         }
         
         function buildNetworkFolderNamesMap(singleNetworksConfigJSON) {
        	 console.log("buildNetworkFolderNamesMap()...invoked...");
        	 var singleNetworksAllConfigsArray = JSON.parse(singleNetworksConfigJSON);
        	 var currentConfig = null;
        	 var currentConfigId = null;
        	 var currentConfigEntriesArray = null;
        	         	 
        	 for(var i=0; i<singleNetworksAllConfigsArray.length; i++) {
        		 currentConfig = singleNetworksAllConfigsArray[i];
        		 currentConfigId = currentConfig.id;
        		 currentConfigEntriesArray = singleNetworksAllConfigsArray[i].folderNamesConfig;
            	 networkFolderNamesMap.set(currentConfigId, currentConfigEntriesArray);
        	 }
        	 
        	 console.log("buildNetworkFolderNamesMap()...exit...");
         }
                  
         function buildNeuralNetworkDropdownList() {
        	 console.log("buildNeuralNetworkDropdownList()...invoked...");

        	 var dropdown_neuralNetworkName = document.getElementById("select_neuralNetworkName");      	 
        	 
        	 //first clear existing options from dropdown
		     var j = 0;
		     var maxIndex = dropdown_neuralNetworkName.options.length -1;
		     for(j=maxIndex; j>=0; j--) {
		    	 dropdown_neuralNetworkName.remove(j);
		     }

        	 
        	 var option = null;
        	 var selectedIndex = 0;
        	 var folderNamesArray = networkFolderNamesMap.get(selectedStudy);
        	 //console.log(folderNamesArray);
        	 var entryArray = null;
        	 var anEntry = null;
        	 var displayName = null;
        	 var value = null;
        	 
        	 for(var i=0; i<folderNamesArray.length; i++) {
        		 anEntry = folderNamesArray[i];
        		 //console.log("entry=" + folderNamesArray[i]);
        		 entryArray = anEntry.split("=");
        		 displayName = entryArray[0].trim();
        		 value = entryArray[1].trim();
        		 
        		 option = document.createElement('option');
        		 option.text = displayName;
        		 option.value = value;
        	 	 option.classList.add("networkOption");
        	 	 if(value.includes("DMN")) {
         	 		selectedIndex = i; 
         	 	 }
        	 	 dropdown_neuralNetworkName.add(option);        		 
        	 }
        	 dropdown_neuralNetworkName.selectedIndex = selectedIndex;
        	 selectedNeuralNetworkName = dropdown_neuralNetworkName.options[dropdown_neuralNetworkName.selectedIndex].value;
        	 
        	 console.log("buildNeuralNetworkDropdownList()...exit...");
         }
         
         function buildStudySummaryMap(summaryJSON) {
        	 console.log("buildStudySummaryMap()...invoked.");
        	 var jsonSummaryList = JSON.parse(summaryJSON);
        	 var currentSummary = null;
        	 var summaryId = null;
        	 
        	 for(var i=0; i<jsonSummaryList.length; i++) {
        		 currentSummary = jsonSummaryList[i];
        		 summaryId = currentSummary.id;
        		 studySummaryMap.set(summaryId, currentSummary.entryList);
        	 }

         }
                  
         function buildStudyMenu(menuJSON) {
        	 console.log("buildStudyMenu()...invoked...");
        	         	 
        	 var menuJSON_Object = JSON.parse(menuJSON);	
        	 var menuEntryArray = menuJSON_Object.menuEntryList;
        	 var menuEntry = null;
        	 var studyDisplayName = null;
        	 var studyId = null;
        	 var submenuOptionsArray = null;
        	 var menuInnerHTML = "";
        	 var surfaceVolumeType = null;

        	 
        	 for(var i=0; i<menuEntryArray.length; i++) {
        		 menuEntry = menuEntryArray[i];
        		 studyDisplayName = menuEntry.displayName;
        		 studyId = menuEntry.id;
        		 //alert("studyId...studyDisplayName=" + studyId + "," + studyDisplayName);
            	 studyDisplayNameMap.set(studyId, studyDisplayName);
            	 surfaceVolumeType = menuEntry.dataType;
            	 submenuOptionsArray = menuEntry.subOptions;

        		 menuInnerHTML += buildStudyMenuEntry(menuEntry);
        		 menuInnerHTML += buildSubmenuOptionEntries(submenuOptionsArray, studyId, surfaceVolumeType, studyDisplayName);
        	 }

        	buildMenuIdDropdownForRemoveStudy();
        	buildMenuIdDropdownForUpdateStudy();

        	
        	var targetMenuParent = document.getElementById("ul_submenu");
        	targetMenuParent.innerHTML = menuInnerHTML; 
        	
       	    console.log("buildStudyMenu()...exit.");
         }
         
         
         function buildSubmenuOptionEntries(submenuOptionsArray, studyId, surfaceVolumeType, studyDisplayName) {
        	 console.log("buildSubmenuOptionEntries()...invoked.");
        	 
        	 var optionEntry = null;
        	 var liEntry = null;
        	 var subOptionDisplayText = null;
        	 var subOptionId = null;
        	 var menuHTML = "";
        	 
        	 for(var i=0; i<submenuOptionsArray.length; i++) {
        		 optionEntry = submenuOptionsArray[i];
            	 openParenIndex = optionEntry.indexOf("(");
            	 closeParenIndex = optionEntry.indexOf(")");
            	 subOptionDisplayText = optionEntry.substring(0, openParenIndex).trim();
            	 networkId = optionEntry.substring(openParenIndex+1, closeParenIndex).trim();
            	 subOptionId = studyId + "_" + networkId;
        		 liEntry = template_li_subSubMenu;
        		 liEntry = liEntry.replaceAll(idReplacementMarker, subOptionId);
        		 liEntry = liEntry.replace(networkIdReplacementMarker, networkId);
        		 liEntry = liEntry.replace(studyReplacementMarker, studyId);
        		 liEntry = liEntry.replace(displayNameReplacementMarker, subOptionDisplayText);
        		 liEntry = liEntry.replace(surfaceVolumeTypeReplacementMarker, surfaceVolumeType);
        		 liEntry = liEntry.replace(studyDisplayReplacementMarker, studyDisplayName);
        		 menuHTML += liEntry;
        	 }
        	 
        	 menuHTML += tag_endUL;
        	 menuHTML += tag_endLI;

        	 return menuHTML;

         }
                  
         function buildStudyMenuEntry(menuEntry) {
        	 console.log("buildStudyMenuEntry()...invoked, menuEntry.id=" + menuEntry.id + ", displayName=" + menuEntry.displayName);

        	 //the id is also the folder name on the server
        	 studyMenuIDArray.push(menuEntry.id);
        	 
        	 var liTag = template_li_submenu;
        	 liTag = liTag.replaceAll(idReplacementMarker, menuEntry.id);
        	 liTag = liTag.replace(displayNameReplacementMarker, menuEntry.displayName);
        	 var ulTag = template_ul_subSubMenu;
        	 ulTag = ulTag.replace(idReplacementMarker, menuEntry.id);
        	 
        	 console.log("buildStudyMenuEntry()...exit.");
        	 return liTag + ulTag;
         }
         
 
         function cancelDownloadDialogue() {
        	 console.log("cancelDownloadDialogue()...invoked.");
        	 var div_downloadDialogue = document.getElementById("div_fileDownloadDialogue");
        	 div_downloadDialogue.style.display = "none";
        	 console.log("cancelDownloadDialogue()...exit.");
         }
         
         function clearEmailAddressErrors() {
        	 console.log("clearEmailAddressErrors()...invoked.");

        	 var span_invalidEmailAddress = document.getElementById("span_invalidEmailAddress");
        	 span_invalidEmailAddress.style.visibility = "hidden";
        	 
        	 console.log("clearEmailAddressErrors()...exit.");
         }
         
         
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
         	 
         	 readyToDisplayDownloadDiv = true;
          	 var div_download = document.getElementById("div_download");
        	 div_download.style.display = "block";
         	 
             var ul_atlasMenu = document.getElementById("ul_atlasMenu");
             ul_atlasMenu.style.display = "block";
                	 
        	 trackThresholdValue(true);
        	 
        	 console.log("displayThresholdImageElements()...exit.");
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

        	 if(!adminLoginFocusPending) {
        		 range_thresholdSlider.focus();
        	 }
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
        
         
         function downloadFile(emailAlreadyValidated) {
        	 console.log("downloadFile()...invoked.");
        	 
    	     var div_downloadMessage = document.getElementById("downloadDialogueMessage");
        	 var div_downloadDialogue = document.getElementById("div_fileDownloadDialogue");
        	 var checkbox_subscribeOption = document.getElementById("checkbox_email");
 	
           	 //var optedOut = true;
        	 var optedOut = checkbox_subscribeOption.checked;
        	 var emailInfoValidated = false;
        	 
        	
        	 if(optedOut || emailAlreadyValidated) {
        		 emailInfoValidated = true;
        	 }
        	 else {
        		 // if the email dialogue has already been displayed for a prior download
        		 // then this call will return true and the dialogue will not be displayed
        		 // again
        		 emailInfoValidated = emailDialogueValidation();
        	 }
        	 
        	 if(!emailInfoValidated) {
        	     div_downloadMessage.innerHTML = downloadDialogueMessage;
            	 div_downloadDialogue.style.display = "block";
        		 var fnameInput = document.getElementById("fname");
        		 fnameInput.focus();
        		 return;
        	 }
        	      	 
        	 div_downloadDialogue.style.display = "none";
        	 
        		 
    		 if(!optedOut) {
        		 var fname = document.getElementById("fname").value;
        		 var lname = document.getElementById("lname").value;
        		 var email = document.getElementById("email").value;
        		 
        		 var fnameString = "fname=" + fname;
        		 var lnameString = "lname=" + lname;
        		 var emailString = "emailAddress=" + email;
        		         		 
        		 var idParameters = "&" + fnameString + "&" + lnameString + "&" + emailString;
        		 anchor_downloadFile.href += idParameters;
    		 }
    		 else {
    			 var optedOutString = "&optedOut=true";
    			 anchor_downloadFile.href += optedOutString;
    		 }
        		 
    		 anchor_downloadFile.click();
        	 console.log("downloadFile()...request triggered, exit");
        	 
        	 return;
        }
 
         
         function preprocessDownloadFile(choice) {
        	 console.log("preprocessDownloadFile()...invoked, choice=" + choice);
        	 
        	 if(downloadDisabled) {
        		 doAdminAlert(downloadDisabledMessage, true);
        		 //doAlert(downloadDisabledMessage, alertOK);
        		 return;
        	 }
 
        	 if(choice==1) {
        		 console.log("choice=1, path=" + downloadZIP_path);
            	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadZIP_path;
	        	 anchor_downloadFile.href += "&selectedStudy=" + selectedStudy;
	        	 anchor_downloadFile.href += "&selectedNeuralNetworkName=" + selectedNeuralNetworkName;
        	 }
        	 else if(choice==2) {
        		 console.log("choice=2, path=" + download_target_map_nii);
        		 downloadFilePathAndName = download_target_map_nii;
            	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadFilePathAndName;
	        	 anchor_downloadFile.href += "&selectedStudy=" + selectedStudy;
	        	 anchor_downloadFile.href += "&selectedNeuralNetworkName=" + selectedNeuralNetworkName;
        	 }
        	 else if(choice==0) {
	        	 var key = range_thresholdSlider.value;
	        	 if(key.indexOf(".")==-1) {
	        		 key = key + ".0";
	        	 }
	        	 console.log("key=" + key);
	        	 downloadFilePathAndName = targetDownloadFilesMap.get(key);
	        	 console.log("downloadTargetFile name=" + downloadFilePathAndName);
	        	 anchor_downloadFile.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + downloadFilePathAndName;
	        	 anchor_downloadFile.href += "&selectedStudy=" + selectedStudy;
	        	 anchor_downloadFile.href += "&selectedNeuralNetworkName=" + selectedNeuralNetworkName;
        	 }
        	 
    	     var div_downloadMessage = document.getElementById("downloadDialogueMessage");
        	 var div_downloadDialogue = document.getElementById("div_fileDownloadDialogue");
    	     div_downloadMessage.innerHTML = downloadDialogueMessage;
    	     
    		 var span_invalidEmailAddress = document.getElementById("span_invalidEmailAddress");
    		 span_invalidEmailAddress.style.visibility = "hidden";
        	 
        	 var checkbox_subscribeOption = document.getElementById("checkbox_email");
        	 	
           	 //var optedOut = true;
        	 var optedOut = checkbox_subscribeOption.checked;
        	 var emailInfoValidated = false;
        	 
        	
        	 if(optedOut) {
        		 emailInfoValidated = true;
        	 }
        	 else {
        		 // if the email dialogue has already been displayed for a prior download
        		 // then this call will return true and the dialogue will not be displayed
        		 // again
        		 var preprocessCheck = true;
        		 emailInfoValidated = emailDialogueValidation(preprocessCheck);
        	 }
        	 
        	 if(!emailInfoValidated) {
        	     div_downloadMessage.innerHTML = downloadDialogueMessage;
            	 div_downloadDialogue.style.display = "block";
        		 var fnameInput = document.getElementById("fname");
        		 fnameInput.focus();
        		 return;
        	 }
        	 else {
        		 var emailAlreadyValidated = true;
        		 downloadFile(emailAlreadyValidated);
        	 }
    	     
        	 console.log("preprocessDownloadFile()...exit.");
         }
         
         function downloadDatabaseData() {
        	 console.log("downloadDatabaseData()...invoked");
        	 var db_dropdown = document.getElementById("databaseDropdown");
        	 
        	 var selection = db_dropdown.options[db_dropdown.selectedIndex].value
        	 console.log("selection=" + selection);
        	 
    		 var div_webHitsView = document.getElementById("div_webHitsView");
    		 div_webHitsView.style.display = "none";
    		 
    		 var div_emailAddressesView = document.getElementById("div_emailAddressesView");
    		 div_emailAddressesView.style.display = "none";
    		 
    		 var div_fileDownloadsView = document.getElementById("div_fileDownloadsView");
    		 div_fileDownloadsView.style.display = "none";
    		 
    		 var div_adminAccessView = document.getElementById("div_adminAccessView");
    		 div_adminAccessView.style.display = "none";
    		 
    		 if(!selection.includes("downloadEmailAddresses")) {
    			 var div_dataBaseProgress = document.getElementById("db_progress");
    			 div_dataBaseProgress.style.display = "block";
    		 }
 
        	 
        	 switch(selection) {
        	 	case "downloadEmailAddresses":
        	 		downloadAdminFile("/midb/email_addresses.csv");
        	 		break;
        	 	case "viewEmailAddresses":
        	 		sendGetEmailAddressesJSON();
        	 		break;
        	 	case "viewWebHits":
        	 		sendGetWebHitsRequest();
        	 		break;
        	 	case "viewFileDownloads":
        	 		sendGetFileDownloadsJSON();
        	 		break; 
        	 	case "viewAdminAccess":
        	 		sendGetAdminAccessRecordsJSON();
        	 		break; 
        	 }
        	 
         }
         
         function downloadAdminFile(fileNameAndPath) {
           	 console.log("downloadAdminFile()...invoked.");
           	 
           	 var docName = null;

			 if(fileNameAndPath == "/midb/surface.zip") {
				var div_confirmAdminDownloadBox = document.getElementById("div_confirmAdminDownloadBox");
				div_confirmAdminDownloadBox.style.display = "none";
				var div_downloadFileProgress = document.getElementById("div_downloadFileProgress");
				div_downloadFileProgress.style.display = "block";
				console.log("setting timer");
				cookieCheckDownloadTimer = setInterval(checkDownloadCompleteCookie, 500);
			 }
           	 
           	 if(fileNameAndPath) {
           		 docName = fileNameAndPath;
           	 }
           	 else {
	             var select_DocId = document.getElementById("select_downloadAdminDocsDropdown");
	             docName = select_DocId.options[select_DocId.selectedIndex].value;

				 if(docName == "confirm_surface.zip") {
					confirmDownloadAdminFile("/midb/surface.zip");
					return;
				}
           	 }
	             
             if(docName == "unselected") {
            	 doAdminAlert("Please select a document");
            	 return;
             }
             
        	 var anchor_downloadAdminFiles = document.getElementById("anchor_downloadAdminFile");
        	 anchor_downloadAdminFiles.href = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=downloadFile&filePathAndName=" + docName;
        	 anchor_downloadAdminFiles.click();
        	 console.log("downloadAdminFile()...exit.");
         }

		 
		 function checkDownloadCompleteCookie() {
			console.log("checkDownloadCompleteCookie()...invoked");
			var docCookie = document.cookie;
			
			var cookies = docCookie.split(";");
			var keyValuePair = null;
			var cookieKey = null;
			var cookieValue = null;
			
			for(i=0; i<cookies.length;i++) {
				keyValuePair = cookies[i].split("=");
				cookieKey = keyValuePair[0];
				cookieValue = keyValuePair[1];

				if(cookieKey == "np_download_name" && cookieValue == "surface.zip") {
					clearInterval(cookieCheckDownloadTimer);
					var div_downloadFileProgress = document.getElementById("div_downloadFileProgress");
					div_downloadFileProgress.style.display = "none";
				}
			}
			console.log("checkDownloadCompleteCookie()...exit");
		}
         
         
         function emailDialogueValidation(preprocessCheck) {
        	 console.log("emailDialogueValidation()()...invoked.");
        	 
        	 var isValid = true;
        	 
        	 var firstName = document.getElementById("fname").value;
        	 var lastName = document.getElementById("lname").value;
        	 var emailAddress = document.getElementById("email").value;
        	 var errorMessage = null;
        	 
        	 var validEmailSyntax = validateEmail(emailAddress)
        	 
        	 if(firstName==null || lastName==null || email==null) {
        		 isValid = false;
        		 errorMessage = "Not all fields have been completed";
        	 }
        	 else if(firstName.trim().length==0) {
        		 isValid = false;
        		 errorMessage = "First name is missing";
        	 }
        	 else if(lastName.trim().length==0) {
        		 isValid = false;
        		 errorMessage = "Last name is missing";
        	 }
        	 else if(emailAddress.trim().length==0) {
        		 isValid = false;
        		 errorMessage = "Last name is missing";
        	 }
        	 
        	 if(!validEmailSyntax) {
        		 errorMessage = "Email address appears to be invalid";
        		 isValid = false;
        	 }
        	 
        	 if(!isValid && !preprocessCheck) {
        		 var span_invalidEmailAddress = document.getElementById("span_invalidEmailAddress");
        		 span_invalidEmailAddress.innerHTML = errorMessage;
        		 span_invalidEmailAddress.style.visibility = "visible";
        	 }
        	 
        	 console.log("emailDialogueValidation()()...exit.");
        	 return isValid;

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
         	
         	var nowDate = new Date();
         	lastTokenActionTime = nowDate.getTime();
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
                    buildStudySummaryMap(responseArray[1]);
               	    buildStudyMenu(responseArray[2]);
               	    getNetworkFolderNamesConfig();
      
               	    //buildNeuralNetworkDropdownList(responseArray[1]);
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
         
         function getNetworkFolderNamesConfig() {
        	 
          	console.log("getNetworkFolderNamesConfig()...invoked...");
          	
          	ajaxRequest_startTime = performance.now();

          	var ajaxRequest = getAjaxRequest();
           	var paramString = "&selectedStudy=" + selectedStudy;
           	paramString += "&selectedDataType=" + selectedDataType;
          	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getNetworkFolderNamesConfig"
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
                      //var responseArray = ajaxRequest.responseText.split("!@!");
                      var div_submitNotification = document.getElementById("div_submitNotification");
                 	  div_submitNotification.style.display = "none";
                      buildNetworkFolderNamesMap(ajaxRequest.responseText);
                      continueStartup();
                      //buildNetworkFolderNamesMap(responseArray[0]);
                      //processThresholdImagesResponse(responseArray[1]);
                 	  //buildNeuralNetworkDropdownList(responseArray[0]);
                  }
                  if (ajaxRequest.readyState==4 && ajaxRequest.status==503) {
                  	alert("The server is not responding.")
                  	return;
                  }
                  
          	}
           	ajaxRequest.send();
           	console.log("getNetworkFolderNamesConfig()...exit...");
        	 
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
         	    
         	var span_submitNotification = document.getElementById("span_submitNotification");
         	
         	if(selectedStudy != priorSelectedStudy) {
         		var radioSurface = document.getElementById("radio_surface");
         		radioSurface.checked = true;
         		selectedDataType = "surface";
         	}
         	
         	 priorSelectedStudy = selectedStudy;

         	 var div_download = document.getElementById("div_download");
         	 div_download.style.display = "none";
         	 
         	 readyToDisplayDownloadDiv = false;

           	
         	 if(selectedDataType == "volume") {
         		span_submitNotification.innerHTML = "Retrieving volume data...";
         	 }
         	 else {
         		span_submitNotification.innerHTML = "Retrieving surface data...";
         	 }
         	 
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
                	   //alert("images response");
                       if(ajaxRequest.responseText.includes("Unexpected Error")) {
                    	   //alert("images response error");

                       	var errorBeginIndex = ajaxRequest.responseText.indexOf(fatalErrorBeginMarker) + 19;
                    		var errorEndIndex = ajaxRequest.responseText.indexOf(fatalErrorEndMarker);
                    		var errorData = ajaxRequest.responseText.substring(errorBeginIndex, errorEndIndex);
                        	var errorArray = errorData.split("&");
                       	var msg1 = errorArray[0];
                       	var msg2 = errorArray[1];
                       	stackTraceData = errorArray[2];
                       	
                       	var incidentIdIndex = ajaxRequest.responseText.indexOf("INCIDENT_ID");
                       	var incidentId = ajaxRequest.responseText.substring(incidentIdIndex, incidentIdIndex+54);
                       	var headerElement = document.getElementById("stackTraceHeader");
                       	headerElement.innerHTML = incidentId;
                       	doErrorAlert(msg1, msg2, errorAlertOK);
                       	return;
                       }
                       if(selectedSubmenuAnchor.id.includes("combined")) {
                            processThresholdImagesResponse(ajaxRequest.responseText);
                       }
                       else {
                    	   priorSelectedDataType = selectedDataType;
                    	   var combinedDataArray = ajaxRequest.responseText.split(DELIMITER_NETWORK_MAP_DATA);
                    	   /* first, process the image data for the Network Probabilistic Map  */
                    	   processNetworkProbabilityMapData(combinedDataArray[0]);
                    	   /* now process all image files for the main image panel */
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
         
         
         function handleAddStudyResponse(responseText) {
             console.log("handleAddStudyResponse()...invoked, responseText=" + responseText);
             
                          
             if(responseText.includes("success")) {
            	 console.log("adding new study to studyMenuIDArray:" + newStudy.folderName);
            	 studyMenuIDArray.push(newStudy.studyFolder);
            	 buildMenuIdDropdownForRemoveStudy();
             	 buildMenuIdDropdownForUpdateStudy();

            	 console.log("studyMenuIDArray=" + studyMenuIDArray);
                 resetAddStudyForm();
            	 //getMenuData();
             }
            	 
        	 var button_removeStudy = document.getElementById("button_remove_study");
        	 button_removeStudy.disabled = false;
        	 
    	   	 var button_addNetworkEntry = document.getElementById("button_addTableRow");
    	   	 button_addNetworkEntry.disabled = false;
    	   	 
    	   	 var button_deleteNetworkEntry = document.getElementById("button_deleteTableRow");
    	   	 button_deleteNetworkEntry.disabled = false;
    	   	 
    	   	 var button_createStudy = document.getElementById("button_createStudy");
    	   	 button_createStudy.disabled = false;
    	   	 
    	   	 var div_dropZone = document.getElementById("div_dropZone");
    	   	 div_dropZone.style.display = "block";
    	  
             doAdminAlert(responseText);
             
             console.log("handleAddStudyResponse()...exit.");

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
             var div_downloadWrapper = document.getElementById("div_downloadWrapper");
             var div_resources = document.getElementById("div_resources");
             var div_midbAtlas = document.getElementById("div_midbAtlas");
             var div_contactUs = document.getElementById("div_contactUs");
             var div_admin = document.getElementById("div_admin");
             var div_addStudy = document.getElementById("div_addStudy");
             var div_mobileDownload = document.getElementById("div_mobileDownload");

             
             div_home.style.display = "none";
             div_overview.style.display = "none";
             div_download.style.display = "none";
             div_downloadWrapper.style.display = "none";
             div_resources.style.display = "none";
             div_midbAtlas.style.display = "none";
             div_contactUs.style.display = "none";
             div_admin.style.display = "none";
             div_mobileDownload.style.display = "none";
             div_mobileDownload.style.display = "none";

             
             switch(id) {
             case "tab_home":
            	 tab_home.checked = true;
            	 var header_umn = document.getElementById("mandatory-header-wrapper");
            	 header_umn.scrollIntoView({behavior: 'smooth', block: 'start'});
            	 div_home.style.display = "block";
            	 break;
             case "tab_overview":
                 tab_overview.checked = true;
                 div_overview.style.display = "block";
            	 //div_overview.scrollIntoView({behavior: 'smooth', block: 'center'});
                 //div_overview.scrollIntoView(false);
                 var divTop = div_overview.offsetTop;
                 window.scrollTo(0,divTop);
                 break;
             case "tab_download":
            	 tab_download.checked = true;
                 if(!mobileDeviceActive) {
  	               div_downloadWrapper.style.display = "block";
  	               if(readyToDisplayDownloadDiv) {
  	            	   div_download.style.display = "block";
  	               }
  	               //var button_downloadAll = document.getElementById("button_downloadAll");
  	               //button_downloadAll.scrollIntoView(false);
  	               var div_submenuTop = document.getElementById("div_submenu").offsetTop;
                   window.scrollTo(0, div_submenuTop);
                 }
                 else {
              	   div_mobileDownload.style.display = "block";
                 }
                 break;
             case "tab_resources":
               	 tab_resources.checked = true;
                 //div_overview.style.display = "none";
                 div_resources.style.display = "block";
               	 var rsTop = div_resources.offsetTop;
                 window.scrollTo(0, rsTop);
                 break;
             case "tab_midbAtlas":
            	 tab_midbAtlas.checked = true;
                 div_midbAtlas.style.display = "block";
                 var maTop = div_midbAtlas.offsetTop;
                 window.scrollTo(0, maTop);
                 break;
             case "tab_contactUs":
            	 tab_contactUs.checked = true;
            	 var div_tabs = document.getElementById("div_tabs");
            	 //div_tabs.scrollIntoView({behavior: 'smooth', block: 'start'});
                 div_contactUs.style.display = "block";
            	 var cuTop = div_contactUs.offsetTop;
                 window.scrollTo(0, cuTop);           
                 break;
             case "div_admin":
                 div_admin.style.display = "block";
                 div_addStudy.style.display = "block";
          	   	 var anchor_addStudy = document.getElementById("a_addStudy");
          	     anchor_addStudy.style.color = "#FFC300";
          	     var ul_studyMenu = document.getElementById("ul_studyMenu");
          	     ul_studyMenu.scrollIntoView({behavior: 'smooth', block: 'start'});
                 break;
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
         
         function notifyUploadComplete(event) {
        	 console.log("notifyUploadComplete()...invoked, e=" + event);
             div_uploadProgress.style.display = "none";
             div_unzipProgress.style.display = "block";
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
         
         
          /**
           * The incoming data consists of 2 arrays:
           * The first array is the base64 encoded images that appear in the main image panel
           * The second array is a list of target download files that are .nii files on the server
           * When the user selects a threshold image and chooses to download a file, then the
           * corresponding .nii file will have the same index in the target download file array as does
           * the base64 image has in its array.
           * In other words, if the image they choose has an array index of [3], then the target
           * .nii file will be stored in the target file array at index of [3] also.
           * 
           * The imageDataURLArray elements map to the targetDownloadFilePathsArray elements.
           * 
           */     
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
        	  var anImageFileAndPath = null;
        	  var priorPath = "";
        	  var anImageSrcURL = null;
        	  var imageSrcURLPrefix = "data:image/png;base64,";
        	  var aBase64String = null;
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
        			  var zipPathIndex = 0;
        			  if(selectedDataType=="surface") {
        				  zipPathIndex = anImageFileAndPath.indexOf("surface")+8; 
        			  }
        			  else if(selectedDataType=="volume") {
        				  zipPathIndex = anImageFileAndPath.indexOf("volume")+7; 
        			  }
        			  var downloadZIP_root = anImageFileAndPath.substring(0, zipPathIndex);
        			  downloadZIP_root += "zips/";
        			  var downloadZIP_fileName = selectedNeuralNetworkName + ".zip";
        			  downloadZIP_path = downloadZIP_root + downloadZIP_fileName;
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
        	 var tab_midbAtlas = document.getElementById("tab_midbAtlas");
        	 var tab_resources = document.getElementById("tab_resources");
        	 var tab_download = document.getElementById("tab_download");
        	 var tab_contact = document.getElementById("tab_contactUs");
        	 

        	 tab_home.checked = true;
        	 tab_overview.checked = false;
        	 //label_overview.style.color = "#333";
        	 tab_midbAtlas.checked = false;
        	 //label_midbAtlas.style.color = "#333";
        	 tab_resources.checked = false;
        	 //label_resources.style.color = "#333";
        	 tab_download.checked = false;
        	 //label_download.style.color = "#333";
        	 tab_contact.checked = false;
        	 //label_contact.style.color = "#333";
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
        	 
        	 console.log("trackInputNumberChange()...invoked.");
        	 var newValue = number_inputThresholdValueControl.value;
        	 
        	 if(newValue.startsWith(".")) {
        		 newValue = "0" + newValue;
        		 number_inputThresholdValueControl.value = newValue;
        	 }
        	 
        	 range_thresholdSlider.value = number_inputThresholdValueControl.value;
        	 //console.log("trackInputNumberChange()...new value =" + range_thresholdSlider.value); 
        	 trackThresholdValue(false);
        	 console.log("trackInputNumberChange()...exit.");
         }
         
         
         function trackThresholdValue(isFirstTrackingEvent) {
        	 console.log("trackThresholdValue()...invoked...");
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

        	 if(!adminLoginFocusPending) {
	        	 range_thresholdSlider.focus({preventScroll: true});
	        	 range_thresholdSlider.disabled = false; 
	        	 range_thresholdSlider.focus({preventScroll: false});
        	 }
	        	 
        	 /*
        	 if(autoScrollHelpPending) {
           	     //setTimeout(function(){ showSnackbar(autoScrollHelpMsg, 6000); }, 1000);
        		 div_snackbar.innerHTML = autoScrollHelpMsg;
           	     div_snackbar.className = "show_4";
           	     setTimeout(function(){ div_snackbar.className = div_snackbar.className.replace("show_4", ""); }, 4000);
           	     autoScrollHelpPending = false;
        	 }
        	 */
        	 console.log("trackThresholdValue()...exit.");
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
         
         function toggleEmailInfo(checkbox) {
        	 console.log("toggleEmailInfo()...invoked.");
        	 
        	 var div_emailInfo = document.getElementById("div_emailInfo");
        	 
        	 if(checkbox.checked) {
        		 div_emailInfo.style.display = "none";
        	 }
        	 else {
        		 div_emailInfo.style.display = "block";
        	 }
        	 console.log("toggleEmailInfo()...exit.");
         }
         
         function updateStudyEntry() {
        	 console.log("updateStudyEntry()...invoked.");
        	 
        	 var div_updateStudyDetails = document.getElementById("div_updateStudyDetails");
        	 div_updateStudyDetails.style.display = "none";

        	 //var div_updateStudyProgress = document.getElementById("div_updateStudyProgress");
    		 //div_updateStudyProgress.style.display = "block";
        	 
        	 updateStudy.div_progressUpload.style.display = "block";
        	 
    		 var fileSize = updateStudy.uploadFilesArray[0].size;
        	 uploadUpdateStudyFile(fileSize);
        	 
        	 console.log("updateStudyEntry()...exit.");

         }
         
                  
         function uploadStudyFile(zipFormData, fileName, fileSize) {

        	console.log("uploadStudyFile()...invoked.");
        	 
          	var ajaxRequest = getAjaxRequest();
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=uploadStudyFiles";
         	
         	var paramString = "&studyFolderName=" + newStudy.studyFolder;
         	paramString += "&menuEntry=" + newStudy.menuEntry;
         	paramString += "&availableDataTypes=" + newStudy.selectedDataTypes;
         	paramString += "&currentFileNumber=" + newStudy.currentFileNumber;
         	paramString += "&totalFileNumber=" + newStudy.totalFileNumber;
         	paramString += "&fileSize=" + fileSize;

         	
         	url += paramString;

         	var encodedUrl = encodeURI(url);
         	ajaxRequest.open('post', encodedUrl, true);
         	ajaxRequest.timeout = 600000*10;
         	
         	
     		newStudy.span_progress_0.style.display = "none";
     		newStudy.span_progress_1.style.display = "none";
   	
         	var remainder =  newStudy.totalFileNumber % newStudy.currentFileNumber;
         	console.log("remainder=" + remainder);
			//changes the upload message without flickering
         	if(remainder == 0) {
         		console.log("in remainder=0 code");
         		newStudy.span_progress_0.innerHTML = "Uploading File:  " + fileName + "...";
         		newStudy.span_progress_1.style.display = "none";
         		newStudy.span_progress_0.style.display = "block";
         	}
         	else {
         		console.log("in remainder=1 code");
         		newStudy.span_progress_1.innerHTML = "Uploading File:  " + fileName + "...";
         		newStudy.span_progress_0.style.display = "none";
         		newStudy.span_progress_1.style.display = "block";
         	}
         	
         	if(newStudy.currentFileNumber == 1) {
				div_uploadProgress.style.display = "block";
			}
       
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
                   	var incidentIdIndex = ajaxRequest.responseText.indexOf("INCIDENT_ID");
                   	var incidentId = ajaxRequest.responseText.substring(incidentIdIndex, incidentIdIndex+54);
                   	var headerElement = document.getElementById("stackTraceHeader");
                   	headerElement.innerHTML = incidentId;
                	doErrorAlert(msg1, msg2, errorAlertOK);
                	console.log("uploadMenuFiles()...onreadystatechange...error");
                	console.log("msg1=" + msg1);
                	console.log("msg2=" + msg2);
                	console.log(stackTraceData);
                	return;
                }
                if (ajaxRequest.readyState == 4 && ajaxRequest.status == 200) {
                	var lastFile = false;
                	if(newStudy.currentIndex == newStudy.totalFileNumber) {
                		lastFile = true;
                	}
                    if(lastFile) {
                    	div_uploadProgress.style.display = "none";
                    	div_unzipProgress.style.display = "none";
                    	handleAddStudyResponse(ajaxRequest.responseText);
                    }
                    else {
                    	manageFileUploads();
                    }
         	    }
                //handle nginx hiccup from server
                if (ajaxRequest.readyState == 4 && ajaxRequest.status == 404) {
                	div_uploadProgress.style.display = "none";
                	div_unzipProgress.style.display = "none";
                	var inferredResponse = "Study created, please refresh page to view new menu";
                    handleAddStudyResponse(inferredResponse);
         	    }

         	}
         	
         	ajaxRequest.upload.onprogress = function(e) {
         		
         		// https://stackoverflow.com/questions/32045093/xmlhttprequest-upload-addeventlistenerprogress-not-working
                //div_uploadProgress.style.display = "block";
				// if the file upload length is known, then show progress bar
				if (e.lengthComputable) {
					//uploadProgress.classList.remove("hide-me");
					// total number of bytes being uploaded
					progress_upload.setAttribute("max", e.total);
					// total number of bytes that have been uploaded
					progress_upload.setAttribute("value", e.loaded);
					var done = false;
					if(newStudy.currentIndex==newStudy.totalFileNumber) {
						done = true;
					}
					if((e.total == e.loaded) && done) {
		                div_uploadProgress.style.display = "none";
		                div_unzipProgress.style.display = "block";
					}
					//console.log(e.loaded);
				}

			};
			
			newStudy.currentFileNumber++;
			newStudy.currentIndex++;
          	ajaxRequest.send(zipFormData);
          	console.log("uploadStudyFile()...exit.");
         }
         
         
         function uploadUpdateStudyFile(fileSize) {


         	console.log("uploadUpdateStudyFile()...invoked.");
         	 
           	var ajaxRequest = getAjaxRequest();
          	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=uploadUpdateStudyFile";
          	
          	var paramString = "&studyFolderName=" + updateStudy.studyId;
          	paramString += "&updateAction=" + updateStudy.actionName;
          	paramString += "&fileSize=" + fileSize;

          	
          	url += paramString;

          	var encodedUrl = encodeURI(url);
          	ajaxRequest.open('post', encodedUrl, true);
          	ajaxRequest.timeout = 600000*10;
          	
          	
      		newStudy.span_progress_0.style.display = "none";
      		newStudy.span_progress_1.style.display = "none";
    	

          	if(updateStudy.currentFileNumber == 1) {
 				div_uploadProgress.style.display = "block";
 			}
        
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
                    	var incidentIdIndex = ajaxRequest.responseText.indexOf("INCIDENT_ID");
                    	var incidentId = ajaxRequest.responseText.substring(incidentIdIndex, incidentIdIndex+54);
                    	var headerElement = document.getElementById("stackTraceHeader");
                    	headerElement.innerHTML = incidentId;
                 	doErrorAlert(msg1, msg2, errorAlertOK);
                 	console.log("uploadMenuFiles()...onreadystatechange...error");
                 	console.log("msg1=" + msg1);
                 	console.log("msg2=" + msg2);
                 	console.log(stackTraceData);
                 	return;
                 }
                 if (ajaxRequest.readyState == 4 && ajaxRequest.status == 200) {
                 	 
                	 var lastFile = true; // there is only 1 file to upload in updateStudy
                 	
                     if(lastFile) {
                     	handleUpdateStudyResponse(ajaxRequest.responseText);
                     }
 
          	    }
                 //handle nginx hiccup from server
                 if (ajaxRequest.readyState == 4 && ajaxRequest.status == 404) {
                 	div_uploadProgress.style.display = "none";
                 	div_unzipProgress.style.display = "none";
                 	var inferredResponse = "Study created, please refresh page to view new menu";
                    handleUpdateStudyResponse(inferredResponse);
          	    }

          	}
          	
          	ajaxRequest.upload.onprogress = function(e) {
          		
          		console.log("progress update...received.");
          		// https://stackoverflow.com/questions/32045093/xmlhttprequest-upload-addeventlistenerprogress-not-working
                 //div_uploadProgress.style.display = "block";
 				// if the file upload length is known, then show progress bar
 				if (e.lengthComputable) {
 	          		console.log("progress update...e.lengthComputable=true");
 					//uploadProgress.classList.remove("hide-me");
 					// total number of bytes being uploaded
 	          		updateStudy.progress_updateUpload.setAttribute("max", e.total);
 					// total number of bytes that have been uploaded
 	          		updateStudy.progress_updateUpload.setAttribute("value", e.loaded);
 					var done = false;
 					if(updateStudy.currentIndex==updateStudy.totalFileNumber) {
 						done = true;
 					}
 					if((e.total == e.loaded) && done) {
 					   if(updateStudy.actionName == "addVolumeData" ||
 						  updateStudy.actionName == "addSurfaceData") {
 						   	updateStudy.div_progressUpload.style.display = "none";
 						   	updateStudy.div_unzipProgress.style.display = "block";
 					   }
 					}
 					//console.log(e.loaded);
 				}

 			};
 			
           	ajaxRequest.send(updateStudy.formData);
           	console.log("uploadStudyFile()...exit.");
	 
         }

         
         function validateEmail(email) {
             var re = /\S+@\S+\.\S+/;
             return re.test(email);
         }
         
  	   	function viewWebHitsMap(googleMapURL) {
		   console.log("viewWebHitsMap()...invoked");
		   console.log(googleMapURL);
		   window.open(googleMapURL, '_blank');
		   console.log("viewWebHitsMap()...exit");
	   }

            
         
