   
/* background colors working with white and #fff */

   /*
    * selectedSubmenu is the final link in a choice so it could be considered a sub-sub-menu
    * this nomenclature considers the name of the study (abcd, human connectome, etc.)
    * as the menu choice, while the choice of combined, integration, or single is
    * considered the subMenu choice.  
    */
   var menuHasBeenClicked = false;
   var lastSelectedMenu = null;
   var selectedStudy = "abcd";
   var selectedSubmenu = null;
   var selectedNeuralNetworkName = null;
   var selectElement = null;
   var selectedSubmenuAnchor = null;
   var firstTimeSelectingSingle = true;
   var selectedDataType = "surface";
   var priorSelectedDataType = "surface";
   var zipFormData = new FormData();
   var div_dropZone = null;
   var networkFolderNamesMap = new Map();
   var ul_zipList = null;
   var droppedFilesArray = new Array();
   var menuCreationDisabled = false;
   var unmaskedText = "";
   var global_studyToRemove = null;
   
   /*
   var filesArray = new Array();
   var fileNamesArray = new Array();
   var chunkArray = new Array();
   var chunkNamesArray = new Array();
   */


   function startupMenu() {
       console.log("startUpMenu()...invoked.");
       
       //hideDataTypeSelection();
       
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
    
    function doAdminAlert(responseText) {
    	console.log("dismissAdminAlert()...invoked");
    	
    	var div_adminAlertBox = document.getElementById("adminAlertBox");
    	var div_adminAlertBoxMessage = document.getElementById("adminAlertBoxMessage");
    	div_adminAlertBoxMessage.innerHTML = responseText;
    	div_adminAlertBox.style.display = "block";
    	
    	console.log("dismissAdminAlert()...exit");
    }
	  
	function enableScroll() {
	    window.onscroll = function() {};
	}
	
    function createStudyEntry(createButton) {
   	 
   	 // https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/Java-File-Upload-Servlet-Ajax-Example
   	 // https://www.codejava.net/java-ee/servlet/eclipse-file-upload-servlet-with-apache-common-file-upload
   	 
   	 console.log("createMenuEntry()...invoked.");
   	 
   	 if(droppedFilesArray.length==0){
   		 doAdminAlert("surface.zip and/or volume.zip must be added");
   		 return;
   	 }
   	 
   	 var isValidFormData = validateCreateStudyFormData();
   	 if(!isValidFormData) {
   		 return;
   	 }
   	 
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
   	 
   	 var select_DataTypeElement = document.getElementById("select_dataType");
   	 var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;
   	 
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
   	 uploadMenuFiles(studyFolderName, selectedDataTypes, menuEntry);

   	 console.log("createMenuEntry()...exit.");
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
   
   function handleAdminLogin() {
  	 
  	 //  https://stackoverflow.com/questions/7616461/generate-a-hash-from-string-in-javascript
       console.log("handleAdminLogin()...invoked.");
       var promptAdminPasswordBox = document.getElementById("div_promptAdminPasswordBox");
   	   promptAdminPasswordBox.style.display = "none";
   	
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
      	
  	
      	var passwordInputField = document.getElementById("pwd_adminPasswordPrompt");
      	passwordInputField.value = "";
      	passwordInputField.focus();
      	
        console.log("handleAdminRequest()...exit.");

   }
   
   function handleAdminValidationResponse(responseText) {
	   console.log("handleAdminValidationResponse()...invoked, valid=" + responseText);
	   
	   if(responseText.includes("true")) {
		   handleTabSelected("div_admin");
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
	   
	   var anchor_addStudy = document.getElementById("a_addStudy");
	   var anchor_removeStudy = document.getElementById("a_removeStudy");

	   
	   if(anchor.id=="a_addStudy") {
		   div_addStudy.style.display = "block";
		   div_removeStudy.style.display = "none";
		   anchor_removeStudy.style.color = "white";
	   }
	   
	   else if(anchor.id=="a_removeStudy") {
		   div_removeStudy.style.display = "block";
		   div_addStudy.style.display = "none";
		   anchor_addStudy.style.color = "white";
	   }
	   console.log("handleStudyMenuClicked()...exit");
   }
   
   function hideDataTypeSelection() {
           console.log("hideDataTypeSelection()...invoked.");
	   var labelDataType = document.getElementById("label_dataType");
	   labelDataType.style.display = "none";
	   var labelSurface = document.getElementById("label_surface");
	   labelSurface.style.display = "none";
	   var radioSurface = document.getElementById("radio_surface");
	   radioSurface.style.display = "none";
	   var labelVolume = document.getElementById("label_volume");
	   labelVolume.style.display = "none";
	   var radioVolume = document.getElementById("radio_volume");
	   radioVolume.style.display = "none";
           console.log("hideDataTypeSelection()...exit.");
  
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
       
       var select_MenuId = document.getElementById("select_menuId");
       var studyToRemove = select_MenuId.options[select_MenuId.selectedIndex].value;
       
       if(studyToRemove.includes("abcd")) {
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
			
			
			
			for (var x=0; x < e.dataTransfer.files.length; x++) {
				
				fileName = e.dataTransfer.files[x].name;
				
				if(droppedFilesArray.includes(fileName)) {
					doAlert("duplicate file name", alertOK);
					return;
				}
				
				if(fileName != "surface.zip" && fileName != "volume.zip") {
					doAlert("file name must be surface.zip or volume.zip");
					return;
				}

				if(fileName.includes("surface")) {
					keyName = "surfaceFile";
				}
				else if(fileName.includes("volume")) {
					keyName = "volumeFile";
				}
				
				console.log("fileName=" + fileName);
				droppedFilesArray.push(fileName);
				zipFormData.append(keyName, e.dataTransfer.files[x]);
				//filesArray.push(e.dataTransfer.files[x]);
				//fileNamesArray.push(fileName);
			}
			
			var innerHTML = ul_zipList.innerHTML;
			var additionalHTML = template_li_zipImage;
			additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
			var newInnerHTML = innerHTML + additionalHTML;
			ul_zipList.innerHTML = newInnerHTML;
									
		});
		
 	    console.log("initializeDragDrop()...exit.");
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
              var study = element.getAttribute("data-study");
              selectedStudy = study;
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
       
      
       var parent = element.parentElement;
       var grandParent = parent.parentElement;
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
       
       if(!volumeDataAvailable) {
    	   radio_VolumeControl.disabled = true;
    	   label_volume.style.backgroundColor = "lightgrey";
       }
       else {
    	   radio_VolumeControl.disabled = false;
    	   label_volume.style.backgroundColor = "#F0EAD6";
       }
       
       console.log("networkTypeId=" + networkTypeId);
       
       
       if(networkTypeId.includes("single")) {
      	 /*
      	 var div_thresholdImagePanel = document.getElementById("div_thresholdImage");
      	 div_thresholdImagePanel.style.display = "none";
      	 var div_selectNeuralNetwork = document.getElementById("div_selectNeuralNetworkName");
      	 div_selectNeuralNetwork.style.display = "block";
      	 */
      	 var select_neuralNetworkName = document.getElementById("select_neuralNetworkName");
      	 if(firstTimeSelectingSingle) {
      		 select_neuralNetworkName.selectedIndex = 3;
      		 selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value;
      		 firstTimeSelectingSingle = false;
      	 }
      	 else {
               selectedNeuralNetworkName = selectElement.options[selectElement.selectedIndex].value;
      	 }
      	 preProcessGetThresholdImages();
       }
       else if(startupTrigger) {
      	 getNeuralNetworkNames();
       }
       else if(networkTypeId.includes("combined_clusters")) {
      	 selectedNeuralNetworkName = "combined_clusters";
      	 preProcessGetThresholdImages();
       }
       else if(networkTypeId.includes("overlapping")) {
      	 selectedNeuralNetworkName = "overlapping";
      	 preProcessGetThresholdImages();
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

	        hideSubmenu_All();

                var elementArray = document.querySelectorAll("li.submenu");
                var currentElement = null;

                for(var i=0; i<elementArray.length; i++) {
                    currentElement = elementArray[i];
                    currentElement.style.display = "block";
                }

	        console.log("showSubmenuLevel_1()...exit.");
	   }
	   
	   function processDataModeChoice(element) {
	        console.log("processDataModeChoice()...invoked.");
            
	        var id = element.id;
	        var surfaceLabel = document.getElementById("label_surface");
	        var volumeLabel = document.getElementById("label_volume");
	        
	        if(id.includes("surface")) {
	        	surfaceLabel.style.background = "#FFC300";
	        	volumeLabel.style.background = "#F0EAD6";
	        	selectedDataType = "surface";
	        }
	        else {
	        	volumeLabel.style.background = "#FFC300";
		        surfaceLabel.style.background = "#F0EAD6";
	        	selectedDataType = "volume";
	        }
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
	   
	   function validateCreateStudyFormData() {
		   
		   var selectElement = document.getElementById("select_dataType");
		   var selectedDataType = selectElement.options[selectElement.selectedIndex].value; 
		   
		   if(selectedDataType == "unselected") {
			   doAdminAlert("Please select valid Available Data Type");
			   return false;
		   }
		   
		   var select_networkTypeArray = document.getElementsByClassName("select_networkType");
		   var networkTypeSelectElement = null;
		   var selectedNetworkType = null;
		   
		   for(var i=0; i<select_networkTypeArray.length; i++) {
			   networkTypeSelectElement = select_networkTypeArray[i];
			   selectedNetworkType = networkTypeSelectElement.options[networkTypeSelectElement.selectedIndex].value;
			   if(selectedNetworkType == "unselected") {
				   doAdminAlert("One or more Network Display Name entries is unselected");
				   return false;
			   }
		   }
		   return true;
		   
	   }
	   
   