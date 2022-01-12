   
/* background colors working with white and #fff */

   /*
    * selectedSubmenu is the final link in a choice so it could be considered a sub-sub-menu
    * this nomenclature considers the name of the study (abcd, human connectome, etc.)
    * as the menu choice, while the choice of combined, integration, or single is
    * considered the subMenu choice.  
    */
   var menuHasBeenClicked = false;
   var lastSelectedMenu = null;
   var selectedStudy = "abcd_template_matching";
   var selectedSubmenu = null;
   var selectedNeuralNetworkName = null;
   var selectElement = null;
   var selectedSubmenuAnchor = null;
   var firstTimeSelectingSingle = true;
   var selectedDataType = "surface";
   var priorSelectedDataType = "surface";
   var zipFormData = new FormData();
   var div_dropZone = null;
   var droppedFileRemovalPending = false;
   var networkFolderNamesMap = new Map();
   var ul_zipList = null;
   var droppedFileNamesArray = new Array();
   var uploadFilesArray = new Array();
   var menuCreationDisabled = false;
   var unmaskedText = "";
   var global_studyToRemove = null;
   var adminLoginFocusPending = false;
   var global_networkTypeId = "combined_clusters";
   var newStudy = {
		   studyFolder: null,
		   selectedDataTypes: null,
		   menuEntry: null,
		   currentIndex: 0,
		   currentFileNumber: 1,
		   totalFileNumber: 0,
		   span_progress_0: null,
		   span_progress_1: null
   
       }


   function startupMenu() {
       console.log("startUpMenu()...invoked.");
              
       newStudy.span_progress_0 = document.getElementById("span_progress_0");;
       newStudy.span_progress_1 = document.getElementById("span_progress_1");;

       
       networkFolderNamesMap.set("Combined Networks", "combined_clusters");
       networkFolderNamesMap.set("Integration Zone", "overlapping");
       networkFolderNamesMap.set("Single Networks", "single");
       
       div_dropZone = document.getElementById("div_dropZone");
       zipFormData.id = "form_uploadZipFiles";
       
       ul_zipList = document.getElementById("ul_zipList");
   
       
       console.log("startUpMenu()...exit.");
   }
   
   
   function disableScroll() {
	    // Get the current page scroll position
	    scrollTop = window.pageYOffset || document.documentElement.scrollTop;
	    scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
	  
	        // if any scroll is attempted, set this to the previous value
	        window.onscroll = function() {
	            window.scrollTo(scrollLeft, scrollTop);
	        };
	}
   
    function dismissAdminAlert() {
    	console.log("dismissAdminAlert()...invoked");
    	
    	var div_adminAlertBox = document.getElementById("adminAlertBox");
    	div_adminAlertBox.style.display = "none";
    	
    	console.log("dismissAdminAlert()...exit");
    }
    
    function doAdminAlert(responseText, isDownloadAlert) {
    	console.log("doAdminAlert()...invoked");
    	
    	var div_adminAlertBox = document.getElementById("adminAlertBox");
    	var div_adminAlertBoxMessage = document.getElementById("adminAlertBoxMessage");
    	div_adminAlertBoxMessage.innerHTML = responseText;
    	div_adminAlertBox.style.display = "block";
    	
    	if(isDownloadAlert) {
    		console.log("changing color to blue");
    		div_adminAlertBox.style.backgroundColor = "#0F52BA";
    	}
    	
    	console.log("doAdminAlert()...exit");
    }
	  
	function enableScroll() {
	    window.onscroll = function() {};
	}
	

	function createStudyEntry(createButton) {
	   	 		
	   	 console.log("createStudyEntry()...invoked.");
	   	 	   	 	   	 
	   	 var select_DataTypeElement = document.getElementById("select_dataType");
	   	 var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;

	   	 if(selectedDataTypes == "unselected") {
	   		 createButton.disabled = false;
	   		 doAdminAlert("Please select Available Data Type");
	   		 return;
	   	 }
	   	 
	   	 if(selectedDataTypes == "surface_volume") {
		   	 if(droppedFileNamesArray.length<4){
		   		 if(!droppedFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("folders.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("surface.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("surface.zip file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("volume.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("volume.zip file must be added");
		   			 return;
		   		 }
		   	 }
	   	 }
	   	 
	   	 if(selectedDataTypes == "surface") {
		   	 if(droppedFileNamesArray.length<3){
		   		 if(!droppedFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("folders.txt")) {
		 	   		 button_createStudy.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("surface.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("surface.zip file must be added");
		   			 return;
		   		 }
		   	 }
	   	 }
	   	 
	   	 if(selectedDataTypes == "volume") {
		   	 if(droppedFileNamesArray.length<3){
		   		 if(!droppedFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("folders.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!droppedFileNamesArray.includes("volume.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("volume.zip file must be added");
		   			 return;
		   		 }
		   	 }
	   	 }

	   	 
	   	 var isValidFormData = validateCreateStudyFormData();
	   	 if(!isValidFormData) {
	   		 createButton.disabled = false;
	   		 return;
	   	 }
	   	 
	   	 //disable the remove entry button to prevent someone from trying this
	   	 //simultaneously
	   	 var removeButton = document.getElementById("button_remove_study");
	   	 removeButton.disabled = true;
	   	 
	   	 createButton.disabled = true;
	   	 div_dropZone.style.display = "none"
	   		 
	   	 var button_addNetworkEntry = document.getElementById("button_addTableRow");
	   	 button_addNetworkEntry.disabled = true;
	   	 
	   	 var button_deleteNetworkEntry = document.getElementById("button_deleteTableRow");
	   	 button_deleteNetworkEntry.disabled = true;
	   	 
	   	 if(menuCreationDisabled) {
	   		 return;
	   	 }
	   	 
	   	 var text_studyDisplayName = document.getElementById("input_studyDisplayName");
	   	 var studyDisplayName = text_studyDisplayName.value;
	   	 
	   	 var text_studyFolderName = document.getElementById("input_studyFolderName");;
	   	 var studyFolderName = text_studyFolderName.value;
	   	 var menuEntry = template_beginMenuEntry;
	     menuEntry = menuEntry.replace(menuIdReplacementMarker, studyFolderName);
	   	    	 
	   	 var studyEntry = template_studyNameEntry;
	   	 studyEntry = studyEntry.replace(studyDisplayReplacementMarker, studyDisplayName);
	   	 studyEntry = studyEntry.replace(studyFolderReplacementMarker, studyFolderName);
	   	 studyEntry = studyEntry.replace(dataTypesReplacementMarker, selectedDataTypes);
	   	 
	   	 menuEntry += studyEntry;
	    	 
	   	 var networkTypes = document.getElementsByClassName("select_networkType");
	   	 var select_NetworkTypeElement = null;
	   	 var networkTypesArray = new Array();
	   	 var aSelectedNetworkType = null;
	   	 var networkEntry = null;
	   	 var networkFolderName = null;
	   	 
	   	 for(var i=0; i<networkTypes.length; i++) {
	   		 select_NetworkTypeElement = networkTypes[i];
	   		 aSelectedNetworkType = select_NetworkTypeElement.options[select_NetworkTypeElement.selectedIndex].value;
	   		 //networkTypesArray.push(aSelectedNetworkType);
	   		 networkEntry = template_networkEntry;
	   		 networkEntry = networkEntry.replace(networkDisplayNameReplacementMarker, aSelectedNetworkType);
	   		 networkFolderName = networkFolderNamesMap.get(aSelectedNetworkType);
	   		 networkEntry = networkEntry.replace(networkFolderNameReplacementMarker, networkFolderName);
	       	 menuEntry += networkEntry;
	   	 }
	   	 menuEntry += template_endMenuEntry;

	   	 console.log(menuEntry); 
	   	 
	   	 /*
	   	 for(var i=0; i<filesArray.length; i++) {
	   		 sliceFile(filesArray[i]);
	   	 }
	   	 //uploadFileChunks();
	   	 //processFileChunks();
	   	 */
	   	 
	   	 //var currentFile = null;
	   	 //var currentFileName = null;
	   	 //var currentFileNumber = 1;
	   	 //var totalFileNumber = droppedFileNamesArray.length;
	   	 
	   	 //uploadMenuFiles(studyFolderName, selectedDataTypes, menuEntry);
	   	 /*
	   	newStudy = {
	 		   studyFolder: null,
	 		   selectedDataTypes: null,
	 		   menuEntry: null,
	 		   currentFileNumber: 0,
	 		   totalFileNumber: 0
	        }
         */
	   	 
	   	 newStudy.studyFolder = studyFolderName;
	   	 newStudy.selectedDataTypes = selectedDataTypes;
	   	 newStudy.menuEntry = menuEntry;
	   	 newStudy.totalFileNumber = droppedFileNamesArray.length;
	   	 newStudy.currentIndex = 0;
	   	 newStudy.currentFileNumber = 1;
	   	 
	   	 manageFileUploads();
	   	 
	   	 console.log("createStudyEntry()...exit.");
		
	}
	   

   function displaySelectedMenu() {
        console.log("displaySelectedMenu()...invoked.");
        hideSubmenu_All();
        hideMenuAll();
        var parent = lastSelectedMenu.parentElement;
        var grandParent = parent.parentElement;
        var greatGrandParent = grandParent.parentElement;
        var greatGreatGrandParent = greatGrandParent.parentElement;
        console.log("greatGrandParent.id=" + greatGrandParent.id);
        console.log("grandparent.id=" + grandParent.id);
        console.log("parent.id=" + parent.id);
     
        var targetAnchor = greatGrandParent.getElementsByTagName("a")[0];
        targetAnchor.style.background = "#FFC300";
        targetAnchor.style.color = "black";

        greatGreatGrandParent.style.display = "block";
        greatGrandParent.style.display = "block";
        grandParent.style.display = "block";  
        selectedSubmenu = parent;   
        parent.style.display = "block";
        
        //jjf
        //var thresholdImagePanel = document.getElementById("img_threshold");
        //thresholdImagePanel.style.visibility = "visible";
        //thresholdImagePanel.style.opacity = "1.0";
        
        //var header = document.getElementById("roi_image_slides_header");
        //header.style.visibility = "visible";
        //header.style.opacity = "1.0";
        
        //var div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        //div_selectNeuralNetworkName.style.visibility = "visible";
        //div_selectNeuralNetworkName.style.opacity = "1.0";

        var div_thresholdImageWrapper = document.getElementById("thresholdImageWrapper");
        div_thresholdImageWrapper.style.display = "none";
        
        
        if(global_networkTypeId.includes("single")) {
        	var div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        	div_selectNeuralNetworkName.style.visibility = "visible";
        	/*
        	var div_select_neuralNetworkNameWrapper = document.getElementById("select_neuralNetworkNameWrapper");
        	div_select_neuralNetworkNameWrapper.style.display = block;
        	*/
        }
  	   

        
	    console.log("displaySelectedMenu()...exit.");
   }

   function resetMenuColors() {
        console.log("resetMenuColors()...invoked.");
        var elementArray = document.querySelectorAll("a.submenu");
        var currentElement = null;

        for(var i=0; i<elementArray.length; i++) {
            currentElement = elementArray[i];
            currentElement.style.color = "#777";
            currentElement.style.background = "#fbf7f5";
        } 
        
	selectedSubmenuAnchor.style.color = "#777";
	selectedSubmenuAnchor.style.background = "#fbf7f5"; 
	selectedSubmenu.style.background = "#fbf7f5";
        console.log("resetMenuColors()...exit.");
   }
   
  	function handleAdminPasswordEnterKey(event) {
    	//console.log("handleSSHPasswordEnterKey(), keyCode=" + event.keyCode);
    	if (event.keyCode === 13) {
			document.getElementById("adminOKButton").click();
		}
    }
   
   function handleAdminLogin() {
  	 
  	 //  https://stackoverflow.com/questions/7616461/generate-a-hash-from-string-in-javascript
       console.log("handleAdminLogin()...invoked.");
       var promptAdminPasswordBox = document.getElementById("div_promptAdminPasswordBox");
   	   promptAdminPasswordBox.style.display = "none";
   	   
   	   adminLoginFocusPending = false;
   	
   	   var adminPasswordMessageDiv = document.getElementById("adminPasswordMessage");
   	   adminPasswordMessageDiv.style.display = "none";
   	   
   	   validateAdminAccess();
   	    	 
   	   console.log("handleAdminLogin()...exit.");
   }
   
   function handleAdminRequest() {
          console.log("handleAdminRequest()...invoked.");

          //var div_admin = document.getElementById("div_promptAdmin");
          //div_admin.style.display = "block";
          
  	    var msg = "Enter the admin password";
  	   	var promptAdminPasswordBox = document.getElementById("div_promptAdminPasswordBox");
      	promptAdminPasswordBox.style.display = "block";
      	
      	var adminPasswordMessageDiv = document.getElementById("adminPasswordMessage");
      	adminPasswordMessageDiv.innerHTML = msg;
      	adminPasswordMessageDiv.style.display = "block";
      	
      	adminLoginFocusPending = true;
  	
      	var passwordInputField = document.getElementById("pwd_adminPasswordPrompt");
      	unmaskedText = "";
      	passwordInputField.value = "";
      	passwordInputField.focus();
      	
        console.log("handleAdminRequest()...exit.");

   }
   
   function handleAdminValidationResponse(responseText) {
	   console.log("handleAdminValidationResponse()...invoked, valid=" + responseText);
	   
	   if(responseText.includes("true")) {
		   handleTabSelected("div_admin");
	   }
	   else if(responseText.includes("false") && responseText.includes("expired")) {
		   return;
	   }
	   else if(responseText.includes("false") && responseText.includes("access_denied")) {
		   doAdminAlert("Access denied");
		   return;
	   }
	   else if(responseText.includes("false") && !responseText.includes("expired")) {
		   handleAdminRequest();
	   }
	   console.log("handleAdminValidationResponse()...exit");


   }
   
   function handleChunkUploadResponse(responseText) {
	   console.log("handleChunkUploadResponse()...invoked");
	   alert(responseText, alertOK);
	   console.log("handleChunkUploadResponse()...exit");

   }
   
   function handleStudyMenuClicked(anchor) {
	   console.log("handleStudyMenuClicked()...invoked");
	   
	   anchor.style.backgroundColor = "#7a0019";
	   anchor.style.color = "#FFC300";
	   
	   	var div_removeStudyProgress = document.getElementById("div_removeStudyProgress");
	   	div_removeStudyProgress.style.display = "none";

	   
	   var div_addStudy = document.getElementById("div_addStudy");
	   var div_removeStudy = document.getElementById("div_removeStudy");
	   var div_updateStudy = document.getElementById("div_updateStudy");
	   var div_downloadSamples = document.getElementById("div_downloadSamples");


	   
	   var anchor_addStudy = document.getElementById("a_addStudy");
	   var anchor_removeStudy = document.getElementById("a_removeStudy");
	   var anchor_updateStudy = document.getElementById("a_updateStudy");
	   var anchor_downloadSamples = document.getElementById("a_downloadSamples");

	   
	   if(anchor.id=="a_addStudy") {
		   div_addStudy.style.display = "block";
		   div_removeStudy.style.display = "none";
		   div_updateStudy.style.display = "none";
		   div_downloadSamples.style.display = "none";


		   anchor_removeStudy.style.color = "white";
		   anchor_updateStudy.style.color = "white";
		   anchor_downloadSamples.style.color = "white";


	   }
	   
	   else if(anchor.id=="a_removeStudy") {
		   div_removeStudy.style.display = "block";
		   div_addStudy.style.display = "none";
		   div_updateStudy.style.display = "none";
		   div_downloadSamples.style.display = "none";

		   anchor_addStudy.style.color = "white";
		   anchor_updateStudy.style.color = "white";
		   anchor_downloadSamples.style.color = "white";
	   }
	   else if(anchor.id=="a_updateStudy") {
		   div_updateStudy.style.display = "block";
		   div_addStudy.style.display = "none";
		   div_removeStudy.style.display = "none";
		   div_downloadSamples.style.display = "none";

		   anchor_addStudy.style.color = "white";
		   anchor_removeStudy.style.color = "white";
		   anchor_downloadSamples.style.color = "white";
	   }
	   else if(anchor.id=="a_downloadSamples") {
		   div_downloadSamples.style.display = "block";
		   div_updateStudy.style.display = "none";
		   div_addStudy.style.display = "none";
		   div_removeStudy.style.display = "none";

		   anchor_addStudy.style.color = "white";
		   anchor_removeStudy.style.color = "white";
		   anchor_updateStudy.style.color = "white";
	   }
	   
	   console.log("handleStudyMenuClicked()...exit");
   }
   
   function hideMenuAll() {

       console.log("hideMenuAll()...invoked");
       var elementArray = document.querySelectorAll("ul.submenu");
       var currentE = null;

       for(var i=0; i<elementArray.length; i++) {
           currentE = elementArray[i];
           currentE.style.display = "none";
       }

       var elementArray2 = document.querySelectorAll("li.submenu");


       for(var j=0; j<elementArray2.length; j++) {
           currentE = elementArray2[j];
           currentE.style.display = "none";
       }


       console.log("hideMenuAll()...exit");

   }
   
   function handleRemoveStudyConfirmation() {

       console.log("handleRemoveStudyConfirmation()...invoked.");
       
       var nowDate = new Date();
  	   var currentTimeSec = nowDate.getTime()/1000;
	   var lastTokenActionTimeSec = lastTokenActionTime/1000;

	   var timeDifference = currentTimeSec - lastTokenActionTimeSec;
	
	   if(timeDifference > 1800) {
		  var message = "Session timeout<br>Please refresh browser page";
		  doAdminAlert(message);
		  return;
	   }
       
       var select_MenuId = document.getElementById("select_menuId");
       var studyToRemove = select_MenuId.options[select_MenuId.selectedIndex].value;
       
       if(studyToRemove.includes("abcd_template_matching")) {
       	doAdminAlert("Sorry " + studyToRemove + " is not eligible for removal");
       	return;
       }
       else if(studyToRemove.includes("none")) {
       	doAdminAlert("Please choose a study id");
       	return;
       }
     
	    var msg = "This action will remove the menu<br>entry and delete all study data";
	   	var div_confirm = document.getElementById("div_confirm");
	   	div_confirm.style.display = "block";
   	
	   	var div_removeStudyConfirmMessage = document.getElementById("removeStudyConfirmMessage");
	   	div_removeStudyConfirmMessage.innerHTML = msg;
	   	div_removeStudyConfirmMessage.style.display = "block";
	   	
        console.log("handleRemoveStudyConfirmation()...exit.");
   }
   
   function handleRemoveStudyResponse(responseText) {
        console.log("handleRemoveStudyResponse()...invoked.");
       
        const index = studyMenuIDArray.indexOf(global_studyToRemove);
        if (index > -1) {
        	studyMenuIDArray.splice(index, 1);
        }
        buildMenuIdDropdown();

	   	var div_removeStudy = document.getElementById("div_removeStudy");
	   	div_removeStudy.style.display = "block";
	   	
	   	var div_removeStudyProgress = document.getElementById("div_removeStudyProgress");
	   	div_removeStudyProgress.style.display = "none";

	   	doAdminAlert(responseText);
       
        console.log("handleRemoveStudyResponse()...exit.");

   }
   
   function handleRemoveStudyRequest(shouldContinue) {
        console.log("handleRemoveStudyRequest()...invoked, shouldContinue=" + shouldContinue);
	   	var div_confirm = document.getElementById("div_confirm");
	   	div_confirm.style.display = "none";
	   	
	   	var studyToRemove = null;
	   	var select_MenuId = null;
	   	
	   	if(!shouldContinue) {
	   		return;
	   	}
	   	
   		select_MenuId = document.getElementById("select_menuId");
        studyToRemove = select_MenuId.options[select_MenuId.selectedIndex].value;
       
	   	var div_removeStudy = document.getElementById("div_removeStudy");
	   	div_removeStudy.style.display = "none";
	   	
	   	var div_removeStudyProgress = document.getElementById("div_removeStudyProgress");
	   	div_removeStudyProgress.style.display = "block";
	   	
	   	global_studyToRemove = studyToRemove;
	   	sendRemoveStudyRequest(studyToRemove);

        console.log("handleRemoveStudyRequest()...exit.");
   }
   


   function hideSubmenu_All() {
       console.log("hideSubmenu_All()...invoked");
       var elementArray = document.querySelectorAll("ul.subSubMenu");
       var currentE = null;

       for(var i=0; i<elementArray.length; i++) {
           currentE = elementArray[i];
           currentE.style.display = "none";
       }

       var elementArray2 = document.querySelectorAll("li.subSubMenu");


       for(var j=0; j<elementArray2.length; j++) {
           currentE = elementArray2[j];
           currentE.style.display = "none";
       }


       console.log("hideSubmenu_All()...exit");
   }
   
   function initializeDragDrop() {
	  	 //alert("drag and drop init...");
	  	 console.log("initializeDragDrop()...invoked.");
	  	 
	  	ul_zipList.addEventListener("dblclick", function(e) {
	  		console.log("ul_zipList event handler");
	  		event.preventDefault();
	  		event.stopPropagation();
	  		return false;
	  	});
	  	 
	  	div_dropZone.addEventListener("dragenter", function(e) {
	  		 this.classList.add("active");
	  	});

	  	div_dropZone.addEventListener("dragleave", function(e) {
			  this.classList.remove("active");
			});

	  	div_dropZone.addEventListener("dragover", function(e) {
			    e.preventDefault();
			});
			
	  	div_dropZone.addEventListener("drop", function(e) {
				e.preventDefault();
				this.classList.remove("active");
				var fileName = null;
				var keyName = null;
				var additionalHTML = null;
				
			   	 var select_DataTypeElement = document.getElementById("select_dataType");
			   	 var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;

			   	 if(selectedDataTypes == "unselected") {
			   		 doAdminAlert("Please select Available Data Type first");
			   		 return;
			   	 }
				
				for (var x=0; x < e.dataTransfer.files.length; x++) {
					fileName = e.dataTransfer.files[x].name;
					
					if(selectedDataTypes == "volume") {
						if(fileName.includes("surface.zip")) {
							doAdminAlert("surface.zip not allowed for Available Data Type of volume");
							return;
						}
					}
					else if(selectedDataTypes == "surface") {
						if(fileName.includes("volume.zip")) {
							doAdminAlert("volume.zip not allowed for Available Data Type of surface");
							return;
						}
					}
					
					if(droppedFileNamesArray.includes(fileName)) {
						doAdminAlert("duplicate file name");
						return;
					}
					
					if(fileName != "surface.zip" && fileName != "volume.zip" 
					   && fileName != "summary.txt" && fileName != "folders.txt") {
						doAlert("file name must be surface.zip, volume.zip, summary.txt, or folders.txt");
						return;
					}

					/*
					if(fileName.includes("surface")) {
						keyName = "surfaceFile";
					}
					else if(fileName.includes("volume")) {
						keyName = "volumeFile";
					}
					else if(fileName.includes("summary")) {
						keyName = "summaryFile";
					}
					*/
					
					console.log("fileName=" + fileName);
					droppedFileNamesArray.push(fileName);
					//zipFormData.append(fileName, e.dataTransfer.files[x]);
					uploadFilesArray.push(e.dataTransfer.files[x]);
					//fileNamesArray.push(fileName);
				}
				if(fileName.includes("surface.zip")){
					var innerHTML = ul_zipList.innerHTML;
					additionalHTML = template_li_surfaceZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					ul_zipList.innerHTML = newInnerHTML
				}
				if(fileName.includes("volume.zip")){
					var innerHTML = ul_zipList.innerHTML;
					additionalHTML = template_li_volumeZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					ul_zipList.innerHTML = newInnerHTML
				}
				if(fileName.includes("txt")){
					var innerHTML = ul_zipList.innerHTML;
					additionalHTML = template_li_textImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					ul_zipList.innerHTML = newInnerHTML
				}
											
			});
			
	 	    console.log("initializeDragDrop()...exit.");
	   
	   
   }
   
   
   function initializeDragDropOld() {
  	 //alert("drag and drop init...");
  	 console.log("initializeDragDrop()...invoked.");
  	 
  	ul_zipList.addEventListener("dblclick", function(e) {
  		console.log("ul_zipList event handler");
  		event.preventDefault();
  		event.stopPropagation();
  		return false;
  	});
  	 
  	div_dropZone.addEventListener("dragenter", function(e) {
  		 this.classList.add("active");
  	});

  	div_dropZone.addEventListener("dragleave", function(e) {
		  this.classList.remove("active");
		});

  	div_dropZone.addEventListener("dragover", function(e) {
		    e.preventDefault();
		});
		
  	div_dropZone.addEventListener("drop", function(e) {
			e.preventDefault();
			this.classList.remove("active");
			var fileName = null;
			var keyName = null;
			var additionalHTML = null;
			
		   	 var select_DataTypeElement = document.getElementById("select_dataType");
		   	 var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;

		   	 if(selectedDataTypes == "unselected") {
		   		 doAdminAlert("Please select Available Data Type first");
		   		 return;
		   	 }
			
			for (var x=0; x < e.dataTransfer.files.length; x++) {
				fileName = e.dataTransfer.files[x].name;
				
				if(selectedDataTypes == "volume") {
					if(fileName.includes("surface.zip")) {
						doAdminAlert("surface.zip not allowed for Available Data Type of volume");
						return;
					}
				}
				else if(selectedDataTypes == "surface") {
					if(fileName.includes("volume.zip")) {
						doAdminAlert("volume.zip not allowed for Available Data Type of surface");
						return;
					}
				}
				
				if(droppedFileNamesArray.includes(fileName)) {
					doAdminAlert("duplicate file name");
					return;
				}
				
				if(fileName != "surface.zip" && fileName != "volume.zip" && fileName != "summary.txt") {
					doAlert("file name must be surface.zip, volume.zip, or summary.txt");
					return;
				}

				/*
				if(fileName.includes("surface")) {
					keyName = "surfaceFile";
				}
				else if(fileName.includes("volume")) {
					keyName = "volumeFile";
				}
				else if(fileName.includes("summary")) {
					keyName = "summaryFile";
				}
				*/
				
				console.log("fileName=" + fileName);
				droppedFileNamesArray.push(fileName);
				zipFormData.append(fileName, e.dataTransfer.files[x]);
				//filesArray.push(e.dataTransfer.files[x]);
				//fileNamesArray.push(fileName);
			}
			if(fileName.includes("surface.zip")){
				var innerHTML = ul_zipList.innerHTML;
				additionalHTML = template_li_surfaceZipImage;
				additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
				additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
				var newInnerHTML = innerHTML + additionalHTML;
				ul_zipList.innerHTML = newInnerHTML
			}
			if(fileName.includes("volume.zip")){
				var innerHTML = ul_zipList.innerHTML;
				additionalHTML = template_li_volumeZipImage;
				additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
				additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
				var newInnerHTML = innerHTML + additionalHTML;
				ul_zipList.innerHTML = newInnerHTML
			}
			if(fileName.includes("txt")){
				var innerHTML = ul_zipList.innerHTML;
				additionalHTML = template_li_textImage;
				additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
				additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
				var newInnerHTML = innerHTML + additionalHTML;
				ul_zipList.innerHTML = newInnerHTML
			}
										
		});
		
 	    console.log("initializeDragDrop()...exit.");
   }
   
   function loadStudyROIImageHeader() {
	   console.log("loadStudyROIImageHeader()...invoked");
	   var studyDisplayName = studyDisplayNameMap.get(selectedStudy);
	   var headerText = studyDisplayName + " PROBABILISTIC ROIS";
	   var headerElement = document.getElementById("roi_image_slides_header");
	   headerElement.innerHTML = headerText;
	   console.log("loadStudyROIImageHeader()...exit, innerHTML=" + headerElement.innerHTML);
   }
   
   function loadStudySummaryList() {
	    console.log("loadStudySummaryList()...invoked.");
	    
	    var summaryEntriesArray = studySummaryMap.get(selectedStudy);	    
	    var ul_summary = document.getElementById("ul_atlasSummary");
	    var ul_innerHTML = "";
	    var currentLI = null;
	    
	    for(i=0; i<summaryEntriesArray.length; i++) {
	    	currentLI = template_summary_li.replace(summaryEntryReplacementMarker,summaryEntriesArray[i]);
	    	ul_innerHTML += currentLI;
	    }
	    ul_summary.innerHTML = ul_innerHTML;
	    console.log("loadStudySummaryList()...exit.");
   }
   
   function maskInput(inputElement) {
	   var inputText = inputElement.value;
	   var addSlash = false;
	   if(inputText.endsWith("_")) {
		   addSlash = true;
	   }
	   var tempText = inputText.replace(/\*/g,"");
	   if(tempText == "")
	   {
	     unmaskedText = unmaskedText.substr(0, unmaskedText.length - 1);
	   }
	   else {
		   unmaskedText += inputText.replace(/\*/g,"");
		   //unmaskedText += inputText.replace(/./g, '*');
		   
	   }
	   inputElement.value = "";
	   for (var i=0;i<inputText.length;i++)
	   {
	     inputElement.value += "*";
	   }
	   if(addSlash) {
		   unmaskedText += "_";
	   }
	   console.log(unmaskedText);
	 }

   
   function menuClicked(element, startupTrigger, actionRequired) {
	          console.log("menuClicked()...invoked, id=" + element.id);
	          console.log("menuClicked()...invoked, actionRequired=" + actionRequired);
	          /*
	          var surfaceVolumeType = element.getAttribute("data-surfaceVolumeType");
	          var studyDisplayName = element.getAttribute("data-studyDisplayName");

	          if(selectedDataType=="volume") {
	        	  if(surfaceVolumeType=="surface") {
	        		  //console.log(studyName);
	        		  doAlert("volume data not available for " + studyDisplayName);
	        		  return;
	        	  }
	          }
	      	  */
              var study = element.getAttribute("data-study");
              selectedStudy = study; //ie: abcd_template_matching
              console.log("menuClicked()...invoked, study=" + study);
	      
	      if(lastSelectedMenu != null) {
		      if(lastSelectedMenu.id === element.id) {
		    	  if(priorSelectedDataType === selectedDataType) {
		    		  return;
		    	  }
		      }
	      }
              
	      
              if(element.id.includes("uman")) {
            	  doAlert("Sorry, the Human Connectome study is not available yet", alertOK);
            	  return;
                  //selectedStudy = "human";
	      }
             
	      if(menuHasBeenClicked) { //menu has been previously clicked
                  resetMenuColors();
	      }

	      menuHasBeenClicked = true;
	      lastSelectedMenu = element;
	      selectedSubmenu = element;
	      selectedSubmenuAnchor = element;
	      console.log("selectedSubmenu.id=" + selectedSubmenu.id);
	      var parent = element.parentElement;
	      var grandParent = parent.parentElement;
	      var greatGrandParent = grandParent.parentElement;
	      var greatGreatGrandParent = greatGrandParent.parentElement;
	      console.log("greatGrandParent.id=" + greatGrandParent.id);
	      console.log("grandparent.id=" + grandParent.id);
	      console.log("parent.id=" + parent.id);

	      //alert("clicked:id=" + parent.id);

	      hideSubmenu_All(); 
              hideMenuAll();
	      greatGreatGrandParent.style.display = "block";
	      greatGrandParent.style.display = "block";
	      grandParent.style.display = "block";  
	      selectedSubmenu = parent;   
	      parent.style.display = "block";

	      selectedSubmenuAnchor.style.background = "#FFC300";
	      selectedSubmenuAnchor.style.color = "black";

              displaySelectedMenu();

	      if(actionRequired) {
	    	  menuClicked_Action(element, startupTrigger);
	      }
	      
	      console.log("menuClicked()...exit.");
	   }
   
   function menuClicked_Action(element, startupTrigger) {

       console.log("menuClickedAction()...invoked.");
       
       var networkTypeId = element.getAttribute("data-networkId");
       //alert("clicked:id=" + parent.id);
       //hideMenu(); 
       //grandParent.style.display = "inline";  
       //selectedSubmenu = parent;   
       //parent.style.display = "inline";
       
       var surfaceVolumeType = selectedSubmenuAnchor.getAttribute("data-surfaceVolumeType");
       var volumeDataAvailable = surfaceVolumeType.includes("volume");
       var radio_VolumeControl = radio_VolumeControl = document.getElementById("radio_volume"); 
       var label_volume = document.getElementById("label_volume");
       var radio_SurfaceControl = document.getElementById("radio_surface"); 
       var label_surface = document.getElementById("label_surface");
	   label_surface.style.backgroundColor = "#FFC300";

       
       if(!volumeDataAvailable) {
    	   radio_VolumeControl.disabled = true;
    	   label_volume.innerHTML = "Volume Data - not yet available";
    	   label_volume.style.backgroundColor = "lightgrey";
       }
       else {
    	   radio_VolumeControl.disabled = false;
    	   //label_volume.style.backgroundColor = "#F0EAD6";
    	   label_volume.innerHTML = "Volume Data";
    	   label_volume.style.backgroundColor = "#f0efee";    	   
       }
       
       console.log("networkTypeId=" + networkTypeId);
       
       loadStudySummaryList();
       loadStudyROIImageHeader();
       
       global_networkTypeId = "unselected";

      
       if(networkTypeId.includes("single")) {
    	 global_networkTypeId = "single";
      	 /*
      	 var div_thresholdImagePanel = document.getElementById("div_thresholdImage");
      	 div_thresholdImagePanel.style.display = "none";
      	 var div_selectNeuralNetwork = document.getElementById("div_selectNeuralNetworkName");
      	 div_selectNeuralNetwork.style.display = "block";
      	 */
    	 buildNeuralNetworkDropdownList();
    	 if(selectedStudy != priorSelectedStudy) {
    		 firstTimeSelectingSingle = true;
    	 }
      	 var select_neuralNetworkName = document.getElementById("select_neuralNetworkName");
      	 if(firstTimeSelectingSingle) {
      		 //select_neuralNetworkName.selectedIndex = 3;
      		 //selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value;
      		 firstTimeSelectingSingle = false;
      	 }
      	 else {
               selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value;
      	 }
      	 preProcessGetThresholdImages();
       }
       else if(startupTrigger) {
        	 selectedNeuralNetworkName = "combined_clusters";
        	 preProcessGetThresholdImages();

      	 //getNeuralNetworkNames();
    	   //getNetworkFolderNamesConfig();
       }
       else if(networkTypeId.includes("combined_clusters")) {
      	 selectedNeuralNetworkName = "combined_clusters";
      	 preProcessGetThresholdImages();
      	 global_networkTypeId = "combined_clusters";
       }
       else if(networkTypeId.includes("overlapping")) {
      	 selectedNeuralNetworkName = "overlapping_networks";
      	 preProcessGetThresholdImages();
      	 global_networkTypeId = "overlapping_networks";
       }

       
       console.log("menuClickedAction()...exit.");
   
   }

  function mouseOut(element) {
	      console.log("mouseOut()...invoked, element.id=" + element.id);
             
	      //console.log("mouseOut()...invoked, menuHasBeenClicked=" + menuHasBeenClicked);

	      var id = element.id;
              var className = element.className;
	      var selectedId = null;
	    
              if(className.includes("subSubMenu")) {
                 var targetParent = element.parentElement.parentElement.parentElement;
                 var targetAnchor = targetParent.getElementsByTagName("a")[0];
                 //alert("targetAnchor=" + targetAnchor.id);
	         //var targetE = document.getElementById("a_ABCD");
	         targetAnchor.style.background = "#fbf7f5";
	         targetAnchor.style.color = "#777";
	      }

	      displaySelectedMenu();
	        
	      console.log("mouseOut()...exit, id=" + element.id);
	   }

      
           function showSubSubMenu(element) {
               console.log("showSubSubMenu()...invoked, id=" + element.id);
               var className = element.className;
               var targetUL = null;

               hideSubmenu_All();
               showSubmenuLevel_1();
             
               if(className == "submenu") {
                  targetUL = element.parentElement.getElementsByTagName("ul")[0];
                  targetUL.style.display = "block";

                  var subSubLIArray = targetUL.getElementsByTagName("li");
                  var currentLI = null;

                  for(var i=0; i<subSubLIArray.length; i++) {
                      currentLI = subSubLIArray[i];
                      currentLI.style.display = "inline";
                  }
               }
               else if(className == "subSubMenu") {
                  var parentUL = element.parentElement.parentElement;
                  console.log("parentUL.id=" + parentUL.id);

                  var targetLI = element.parentElement.parentElement.parentElement;
                  var targetAnchor = targetLI.getElementsByTagName("a")[0];
                  targetAnchor.style.background = "#3d85c6";
		          targetAnchor.style.color = "white";
                  
                  //hideSubmenu_All();

                  showSubmenuLevel_1();
                  var subSubLIArray2 = parentUL.getElementsByTagName("li");
                  var currentLI = null;
                  //console.log("subSubLIArray2.length=" + subSubLIArray2.length);

                  for(var i=0; i<subSubLIArray2.length; i++) {
                      currentLI = subSubLIArray2[i];
                      currentLI.style.display = "inline";
                  }
                  parentUL.style.display = "block";
              
               }
               console.log("showSubSubMenu()...exit, id=" + element.id);
           }
	      
   
	   function showSubmenuLevel_1() {
	        console.log("showSubmenuLevel_1()...invoked.");
	        
	        // jjf
	        // https://stackoverflow.com/questions/9040768/getting-coordinates-of-objects-in-js
	        //var thresholdImagePanel = document.getElementById("img_threshold");
	        //thresholdImagePanel.style.visibility = "hidden";
	        //thresholdImagePanel.style.opacity = "0.1";
	        
	        //var header = document.getElementById("roi_image_slides_header");
	        //header.style.visibility = "hidden";
	        //header.style.opacity = "0.1";
	        
	        //var div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
	        //div_selectNeuralNetworkName.style.visibility = "hidden";
	        //div_selectNeuralNetworkName.style.opacity = "0.1";

	        var div_thresholdImageWrapper = document.getElementById("thresholdImageWrapper");
	        div_thresholdImageWrapper.style.display = "block";
	        
	        if(global_networkTypeId.includes("single")) {
	           	var div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
	        	div_selectNeuralNetworkName.style.visibility = "hidden";
	        }
	        

	        hideSubmenu_All();

                var elementArray = document.querySelectorAll("li.submenu");
                var currentElement = null;

                for(var i=0; i<elementArray.length; i++) {
                    currentElement = elementArray[i];
                    currentElement.style.display = "block";
                }

	        console.log("showSubmenuLevel_1()...exit.");
	   }
	   
	   function manageFileUploads(totalCount, currentCount) {
		   	 
		   	 /*
		   	 for(var i=0; i<droppedFileNamesArray.length; i++) {
		   		 currentFileName = droppedFileNamesArray[i];
		   		 currentFile = uploadFilesArray[i];
		   		 currentFileNumber += i;
		   		 zipFormData = new FormData();
		   		 zipFormData.append(currentFileName, currentFile);
		   		 uploadMenuFiles(studyFolderName, selectedDataTypes, menuEntry, currentFileNumber, totalFileNumber);
		   	 }
			 */
		   
           /*
		   newStudy = {
				   studyFolder: null,
				   selectedDataTypes: null,
				   menuEntry: null,
				   currentFileNumber: 0,
				   totalFileNumber: 0
		       }
		   */
		   
		   zipFormData = new FormData();
		   var index = newStudy.currentIndex;
		   var currentFile = uploadFilesArray[index];
		   var currentFileSize = currentFile.size;
		   var currentFileName = droppedFileNamesArray[index];
		   zipFormData.append(currentFileName, currentFile);
		   uploadStudyFile(zipFormData, currentFileName, currentFileSize);
		   
	   }
	   
		function preProcessCreateStudyEntry(button_createStudy) {
		   	 console.log("preProcessCreateStudyEntry()...invoked.");
		   	 
		   	 button_createStudy.disabled = true;
	     	 var nowDate = new Date();
	    	 var currentTimeSec = nowDate.getTime()/1000;
	    	 var lastTokenActionTimeSec = lastTokenActionTime/1000;
	  
	    	 var timeDifference = currentTimeSec - lastTokenActionTimeSec;
	    	
	    	 if(timeDifference > 1800) {
	    		var message = "Session timeout<br>Please refresh browser page";
	    		doAdminAlert(message);
	    		return;
	    	 }
	    	 
	    	 validateAdminAccessStatus();
	    	 
		   	 console.log("preProcessCreateStudyEntry()...exit.");
		}
		
	   
	   
	   function processDataModeChoice(element) {
	        console.log("processDataModeChoice()...invoked.");
            
	        var id = element.id;
	        var surfaceLabel = document.getElementById("label_surface");
	        var volumeLabel = document.getElementById("label_volume");
	        
	        if(id.includes("surface")) {
	        	surfaceLabel.style.background = "#FFC300";
	        	volumeLabel.style.background = "#f0efee";
	        	selectedDataType = "surface";
	        }
	        else {
	        	volumeLabel.style.background = "#FFC300";
		        surfaceLabel.style.background = "#f0efee";
	        	selectedDataType = "volume";
	        }
	        preProcessGetThresholdImages();
	        console.log("processDataModeChoice()...eit.");
	   }
	   
	   function processFileChunks() {
	       console.log("processFileChunks()...invoked.");
		   var chunkForm = null;
		   var currentChunk = null;
		   var currentChunkName = null;
		   
    	   for(var i=0; i<chunkArray.length; i++) {
    		   if(i==1) {
    			   return; // jjf_todo
    		   }
    		   currentChunk = chunkArray[i];
    		   currentChunkName = chunkNamesArray[i];
    		   chunkForm = new FormData();
    		   chunkForm.id = "form_" + currentChunkName;
    		   
     		   chunkForm.append('file', currentChunk, currentChunkName);
    		   uploadFileChunk(chunkForm, i);
    	   }
	       console.log("processFileChunks()...exit.");

       }
	   
	   function removeDroppedFile(liElement) {
		   console.log("removeDroppedFile()...invoked, event=" + event);
		   		   
		   console.log("before array=" + droppedFileNamesArray);

		   event.stopPropagation();
		   if(droppedFileRemovalPending) {
			   console.log("removeDroppedFile()...aborting, array=" + droppedFileNamesArray);
			   return;
		   }
		   var fileName = liElement.getAttribute("data-formKey");
		   var index = droppedFileNamesArray.indexOf(fileName);
		   
		   droppedFileRemovalPending = true;
		   
		   droppedFileNamesArray.splice(index, 1);
		   uploadFilesArray.splice(index, 1);
		   //zipFormData.delete(fileName);
		   var parent = liElement.parentElement;
		   parent.removeChild(liElement);
		   //setTimeout(removeChild, 500, parent, liElement);
		   console.log("after array=" + droppedFileNamesArray);

		   
		   console.log("keys follow:");
		   for (var key of zipFormData.keys()) {
			   console.log(key);
			}
		   droppedFileRemovalPending = false;
		   console.log("array=" + droppedFileNamesArray);
		   console.log("removeDroppedFile()...exit.");
	   }
	   
	   function removeChild(parent, child) {
		   console.log("removeChild()...invoked.");
		   console.log("parentID=" + parent.id);
		   parent.removeChild(child);
		   droppedFileRemovalPending = false;
		   console.log("removeChild()...exit, array=" + droppedFileNamesArray);
	   }
	   
	   function sendRemoveStudyRequest(study_folder) {
	        console.log("sendRemoveStudyRequest()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var paramString = "&studyFolder=" + study_folder;
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=removeStudy"
	       		    + paramString;
	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	       	//ajaxRequest.setRequestHeader("enctype","multipart/form-data");
	       	
	       	ajaxRequest.onreadystatechange=function() {
	
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
	              	console.log("uploadMenuFiles()...onreadystatechange...error");
	              	console.log("msg1=" + msg1);
	              	console.log("msg2=" + msg2);
	              	console.log(stackTraceData);
	              	doErrorAlert(msg1, msg2, errorAlertOK);

	              	return;
	              }
	              if (ajaxRequest.readyState == 4 && ajaxRequest.status == 200) {
	      	            console.log("sendRemoveStudyRequest()...ajaxRequest.responseText=" + ajaxRequest.responseText);
			           	handleRemoveStudyResponse(ajaxRequest.responseText);
	   	       }
	       	}
	        	ajaxRequest.send();
		        console.log("sendRemoveStudyRequest()...exit.");
		   
	   }
  
	   
       function sliceFile(file) {
       		var chunkSize = 1024*1024*100;
    	    var numberOfChunks = Math.ceil(file.size/chunkSize);
    	    var chunk = null;
            var chunkCount = 0;
            var chunkName = null;
            var fileName = file.name;
            var offset = 0;
    	    
    	    for(var i=0; i<numberOfChunks; i++) {
    	    	offset = i*chunkSize;
    	    	chunk = file.slice(offset, chunkSize);
    	    	chunkName = fileName + "_" + "chunk" + "_" + i;
    	    	chunkArray.push(chunk);
    	    	chunkNamesArray.push(chunkName);
    	    	//chunkEnd += chunk.size;
    	    }
    	    
    	    for(var j=0; j<chunkArray.length; j++) {
    	    	console.log(chunkNamesArray[j]);
    	    }
        }
       
	   
	   function updateStudyFolderName(studyNameTextInput) {
	       console.log("updateStudyFolderName()...invoked.");

		   var studyName = studyNameTextInput.value;
		   var studyFolderName = studyName.replaceAll("  ", " ");
		   studyFolderName = studyFolderName.trim();
		   studyFolderName = studyFolderName.replaceAll(" - ", "_");
		   studyFolderName = studyFolderName.replaceAll(" ", "_");
		   studyFolderName = studyFolderName.replaceAll("__", "_");
		   studyFolderName = studyFolderName.trim();
		   
		   var textInput_studyFolderName = document.getElementById("input_studyFolderName");
		   textInput_studyFolderName.readonly = false;
		   textInput_studyFolderName.value = studyFolderName.toLowerCase();
		   textInput_studyFolderName.readonly = true;
		   
	       console.log("updateStudyFolderName()...exit.");

	   }
	   
	   function uploadFileChunk(chunkForm, indexNumber) {
	        console.log("uploadChunk()...invoked.");

         	var ajaxRequest = getAjaxRequest();
         	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=uploadZipChunks&indexNumber=" + indexNumber;

         	var encodedUrl = encodeURI(url);
         	ajaxRequest.open('post', encodedUrl, true);
         	//ajaxRequest.setRequestHeader("enctype","multipart/form-data");

         	
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
                    //handleFileUploadResponse(ajaxRequest.responseText);
                	handleChunkUploadResponse(ajaxRequest.responseText);
         	    }

                //handleFileUploadResponse(ajaxRequest.responseText);
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
 
          	ajaxRequest.send(chunkForm);
	        console.log("uploadChunk()...exit.");
	   }
	   
	   function validateAdminAccess() {

	        console.log("validateAdminAccess()...invoked.");

        	var ajaxRequest = getAjaxRequest();
        	var paramString = "&token=" + token;
        	paramString += "&mriVersion=" + unmaskedText;
        	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=validateAdminAccess"
        		    + paramString;

        	var encodedUrl = encodeURI(url);
        	ajaxRequest.open('get', encodedUrl, true);
        	//ajaxRequest.setRequestHeader("enctype","multipart/form-data");
        	
        	ajaxRequest.onreadystatechange=function() {
 
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
       	            console.log("validateAdminAccess()...ajaxRequest.responseText=" + ajaxRequest.responseText);
		           	handleAdminValidationResponse(ajaxRequest.responseText);
    	       }
        	}
        	
         	ajaxRequest.send();
	        console.log("validateAdminAccess()...exit.");
	   }
	   
	   function validateAdminAccessStatus() {
		    console.log("validateAdminAccessStatus()...invoked.");
	
	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=validateAdminAccessStatus";
	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	       	//ajaxRequest.setRequestHeader("enctype","multipart/form-data");
	       	
	       	ajaxRequest.onreadystatechange=function() {
	
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
	      	            console.log("validateAdminAccessStatus()...ajaxRequest.responseText=" + ajaxRequest.responseText);
			           	if(ajaxRequest.responseText.includes("true")) {
			           		var button_createStudy = document.getElementById("button_createStudy");
			           		createStudyEntry(button_createStudy);
			           		return;
			           	}
			           	else {
			           		doAdminAlert("Admin access not validated");
			           		return;
			           	}
	   	       }
	       	}
	       	
	        ajaxRequest.send();
			console.log("validateAdminAccessStatus()...exit.");
	   }
	   
	   function validateCreateStudyFormData() {
		   
		   var selectElement = document.getElementById("select_dataType");
		   var selectedDataType = selectElement.options[selectElement.selectedIndex].value; 
		   
		   var input_studyDisplayName = document.getElementById("input_studyDisplayName");
		   var studyName = input_studyDisplayName.value;
		   
		   if(studyName.length==null || studyName.length==0) {
			   doAdminAlert("Please enter a value for Displayed Study Name");
			   return false;
		   }
		   
		   var text_studyFolderName = document.getElementById("input_studyFolderName");;
		   var studyFolderName = text_studyFolderName.value;
		   
		   if(studyMenuIDArray.includes(studyFolderName)) {
			   doAdminAlert("Duplicate Study name");
			   return false;
		   }
		   
		   if(selectedDataType == "unselected") {
			   doAdminAlert("Please select valid Available Data Type");
			   return false;
		   }
		   
		   var select_networkTypeArray = document.getElementsByClassName("select_networkType");
		   var networkTypeSelectElement = null;
		   var selectedNetworkType = null;
		   var selectedNetworkTypeArray = new Array();
		   
		   for(var i=0; i<select_networkTypeArray.length; i++) {
			   networkTypeSelectElement = select_networkTypeArray[i];
			   selectedNetworkType = networkTypeSelectElement.options[networkTypeSelectElement.selectedIndex].value;
			   if(selectedNetworkType == "unselected") {
				   doAdminAlert("One or more Network Display Name entries is unselected");
				   return false;
			   }
			   if(selectedNetworkTypeArray.includes(selectedNetworkType)) {
				   doAdminAlert("Duplicate Network Display Name");
				   return false;
			   }
			   selectedNetworkTypeArray.push(selectedNetworkType);
		   }
		   return true;
		   
	   }
	   
	   function validateDroppedFiles(select_DataTypeElement) {
		   console.log("validateDroppedFiles()...invoked.");
		   
		   var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;
		   var arrayIndex = 0;
		   var ul_zipList = document.getElementById("ul_zipList");
		   
		   console.log("before: array=" + droppedFileNamesArray);
		   
		   if(selectedDataTypes == "surface") {
			   if(droppedFileNamesArray.includes("volume.zip")) {
				   console.log("validateDroppedFiles()...removing volume.zip");
				   zipFormData.delete("volume.zip");
				   arrayIndex = droppedFileNamesArray.indexOf("volume.zip");
				   droppedFileNamesArray.splice(arrayIndex,1);
				   var targetLI = document.getElementById("volume.zip");
				   ul_zipList.removeChild(targetLI);
			   }
		   }
		   else if(selectedDataTypes == "volume") {
			   if(droppedFileNamesArray.includes("surface.zip")) {
				   console.log("validateDroppedFiles()...removing surface.zip");
				   zipFormData.delete("surface.zip");
				   arrayIndex = droppedFileNamesArray.indexOf("surface.zip");
				   droppedFileNamesArray.splice(arrayIndex,1);
				   var targetLI = document.getElementById("surface.zip");
				   ul_zipList.removeChild(targetLI);
			   }
		   }
		   
		   console.log("after: array=" + droppedFileNamesArray);
		   console.log("zipFormData keys follow...");
		   for (var key of zipFormData.keys()) {
			   console.log(key);
			}

		   
		   console.log("validateDroppedFiles()...invoked.");

	   }
	   
   