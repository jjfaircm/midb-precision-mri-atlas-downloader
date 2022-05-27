   
/* background colors working with white and #fff */

   /*
    * selectedSubmenu is the final link in a choice so it could be considered a sub-sub-menu
    * this nomenclature considers the name of the study (abcd, human connectome, etc.)
    * as the menu choice, while the choice of combined, integration, or single is
    * considered the subMenu choice.  
    */
   var menuHasBeenClicked = false;
   var lastSelectedMenu = null;
   //var selectedStudy = "abcd_template_matching";
   var selectedSubmenu = null;
   var selectedNeuralNetworkName = null;
   var selectElement = null;
   var selectedSubmenuAnchor = null;
   var firstTimeSelectingSingle = true;
   var priorSelectedDataType = "surface";
   var zipFormData = new FormData();
   var div_dropZone = null;
   var droppedFileRemovalPending = false;
   var networkFolderNamesMap = new Map();
   var menuCreationDisabled = false;
   var unmaskedText = "";
   var global_studyToRemove = null;
   var adminLoginFocusPending = false;
   var global_networkTypeId = "combined_clusters";
   var webHitsMapURL = null;
   var fileDownloadsMapUrl = null;
   var newStudy = {
		   studyFolder: null,
		   selectedDataTypes: null,
		   menuEntry: null,
		   uploadFileNamesArray: new Array(),
		   uploadFilesArray: new Array(),
		   ul_uploadFileList: null,
		   currentIndex: 0,
		   currentFileNumber: 1,
		   totalFileNumber: 0,
		   span_progress_0: null,
		   span_progress_1: null
       };
   
   var updateStudy = {
		   studyId: null,
		   actionName: null,
		   currentIndex: 1,
		   totalFileNumber: 1,
		   uploadFileNamesArray: new Array(),
		   uploadFilesArray: new Array(),
		   ul_uploadFileList: null,
   		   div_dropZone: null,
   		   div_progressUpload: null,
   		   div_unzipProgress: null,
   		   progress_updateUpload: null,
   		   formData: new FormData()
   };

   var selectedStudy = {
		studyId: "abcd_template_matching",
		selectedDataType: "surface",
		availableDataType: null
   };
   
   var lastAdminActionRequest = null;
   var targetMap = null;
   var numberMapDisplays = 0;


   function startupMenu() {
       console.log("startUpMenu()...invoked.");
              
       newStudy.span_progress_0 = document.getElementById("span_progress_0");;
       newStudy.span_progress_1 = document.getElementById("span_progress_1");;

       
       networkFolderNamesMap.set("Combined Networks", "combined_clusters");
       networkFolderNamesMap.set("Integration Zone", "overlapping");
       networkFolderNamesMap.set("Single Networks", "single");
       
       div_dropZone = document.getElementById("div_dropZone");
       zipFormData.id = "form_uploadZipFiles";
       updateStudy.formData.id ="form_uploadUpdateFiles";
       
       newStudy.ul_uploadFileList = document.getElementById("ul_zipList");
       
       updateStudy.div_dropZone = document.getElementById("div_dropZoneUpdateStudy");
       updateStudy.ul_uploadFileList = document.getElementById("ul_updateStudyFileList");
       updateStudy.div_progressUpload = document.getElementById("div_updateStudyProgress");
       updateStudy.progress_updateUpload = document.getElementById("progress_updateUpload");
       updateStudy.div_unzipProgress = document.getElementById("div_updateUnzipProgress");
       
       console.log("startUpMenu()...exit.");
   }

    function confirmDownloadAdminFile(fileName) {
		console.log("confirmDownloadAdminFile()...invoked");
		var div_confirmDownloadAdminBoxMessage = document.getElementById("div_confirmDownloadAdminBoxMessage");
		div_confirmDownloadAdminBoxMessage.innerHTML = "Download size is 1.5 GB...this will take a few minutes.<br>" +
		                                               "continue or cancel...?";
		var div_confirmAdminDownloadBox = document.getElementById("div_confirmAdminDownloadBox");
		div_confirmAdminDownloadBox.style.display = "block";
	    console.log("confirmDownloadAdminFile()...exit");
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

	function dismissConfirmDownloadAdminAlert() {
		console.log("dismissConfirmDownloadAdminAlert()...invoked");
		var div_confirmAdminDownloadBox = document.getElementById("div_confirmAdminDownloadBox");
		div_confirmAdminDownloadBox.style.display = "none";
	    console.log("dismissConfirmDownloadAdminAlert()...exit");
	}
    
    function dismissUpdatesAlert() {
    	console.log("dismissUpdatesAlert()...invoked");
    	
    	var div_updatesAlertBox = document.getElementById("updatesAlertBox");
    	div_updatesAlertBox.style.display = "none";
    	
    	console.log("dismissAdminAlert()...exit");
    }
    
    
    
    function doAdminAlert(responseText, isDownloadAlert) {
    	console.log("doAdminAlert()...invoked");
    	
    	var div_adminAlertBox = document.getElementById("adminAlertBox");
    	var div_adminAlertBoxMessage = document.getElementById("adminAlertBoxMessage");
    	div_adminAlertBoxMessage.innerHTML = responseText;
    	//div_adminAlertBox.style.display = "block";
    	
    	if(isDownloadAlert) {
    		div_adminAlertBox.classList.remove("admin_gradient");
    		div_adminAlertBox.style.backgroundColor = "#0F52BA";
    	}
    	else {
    		div_adminAlertBox.classList.add("admin_gradient");
    	}
    	
    	div_adminAlertBox.style.display = "block";

    	console.log("doAdminAlert()...exit");
    }

    function doUpdatesAlert(message) {

    	console.log("doUpdatesAlert()...invoked");
    	
    	var div_updatesAlertBox = document.getElementById("updatesAlertBox");
    	var span_updatesAlertSpanMessage = document.getElementById("updateAlertSpan");
    	span_updatesAlertSpanMessage.innerHTML = message;
    	div_updatesAlertBox.style.display = "block";
    	
    	//#644B26 #483d8b #0f52ba #536895 #000080 #191970.OK
    	// GRAY: #808080 #696969.ok #575757
    	console.log("doAdminAlert()...exit");
    
    }  
	  
	function enableScroll() {
	    window.onscroll = function() {};
	}
	
	function buildEmailAddressesTable(emailAddressesJSON) {

	   	 console.log("buildEmailAddressesTable()...invoked.");
		 var emailAddressesArray = JSON.parse(emailAddressesJSON);
		 var currentEmailAddress = null;
		 var tr = null;
		 var td = null;
		 var tbodyRef = document.getElementById("emailAddressesTable").getElementsByTagName('tbody')[0];
		 
		 while(tbodyRef.rows.length > 0) {
			 tbodyRef.deleteRow(0);
		 }
		 
		 for(var i=0; i<emailAddressesArray.length; i++) {
			 currentEmailAddress = emailAddressesArray[i];
			 tr = tbodyRef.insertRow();
			 tr.className = "admin";
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentEmailAddress.firstName;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentEmailAddress.lastName;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentEmailAddress.emailAddress;
			 tr.appendChild(td);
		 }
		 
		 var th_emailAddressesCount = document.getElementById("th_emailAddressesFooter");
		 th_emailAddressesCount.innerHTML = "Email Addresses Record Count: " + emailAddressesArray.length;
		 var div_dataBaseProgress = document.getElementById("db_progress");
		 div_dataBaseProgress.style.display = "none";
		 var div_emailAddressesView = document.getElementById("div_emailAddressesView");
		 div_emailAddressesView.style.display = "block";
	   	 console.log("buildEmailAddressesTable()...exit.");
	}
	
	function buildAdminAccessTable(adminAccessJSON) {
		
	   	 console.log("buildAdminAccessTable()...invoked.");
		 var adminAccessRecordArray = JSON.parse(adminAccessJSON);
		 var currentAdminAccessRecord = null;
		 var tr = null;
		 var td = null;
		 var tbodyRef = document.getElementById("adminAccessTable").getElementsByTagName('tbody')[0];
		 
		 while(tbodyRef.rows.length > 0) {
			 tbodyRef.deleteRow(0);
		 }

		 for(var i=0; i<adminAccessRecordArray.length; i++) {
			 currentAdminAccessRecord = adminAccessRecordArray[i];
			 
			 tr = tbodyRef.insertRow();
			 tr.className = "admin";
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.createDate;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.ipAddress;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.action;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.validPassword;
			 tr.appendChild(td);
			 
			 tr.appendChild(td);
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.city;
			 tr.appendChild(td);
			 
			 tr.appendChild(td);
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.state;
			 tr.appendChild(td);
			 
			 tr.appendChild(td);
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentAdminAccessRecord.country;
			 tr.appendChild(td);
			 
		 }
		 var th_adminAccessCount = document.getElementById("th_adminAccessFooter");
		 th_adminAccessCount.innerHTML = "Admin Access Record Count: " + adminAccessRecordArray.length;
		 var div_dataBaseProgress = document.getElementById("db_progress");
		 div_dataBaseProgress.style.display = "none";
		 var div_fileDownloadsView = document.getElementById("div_adminAccessView");
		 div_fileDownloadsView.style.display = "block";
	   	 console.log("buildAdminAccessTable()...exit.");	
	}
	
	function buildFileDownloadsTable(fileDownloadsJSON) {

	   	 console.log("buildFileDownloadsTable()...invoked.");
		 var fileDownloadsArray = JSON.parse(fileDownloadsJSON);
		 var currentFileDownloadRecord = null;
		 var tr = null;
		 var td = null;
		 var tbodyRef = document.getElementById("fileDownloadsTable").getElementsByTagName('tbody')[0];
		 
		 while(tbodyRef.rows.length > 0) {
			 tbodyRef.deleteRow(0);
		 }

		 for(var i=0; i<fileDownloadsArray.length; i++) {
			 currentFileDownloadRecord = fileDownloadsArray[i];
			 
			 tr = tbodyRef.insertRow();
			 tr.className = "admin";
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.createDate;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.fileName;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.study;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.ipAddress;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.emailAddress;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.city;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.state;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentFileDownloadRecord.country;
			 tr.appendChild(td);
		 }
		 
		 var th_fileDownloadsCount = document.getElementById("th_fileDownloadsFooter");
		 th_fileDownloadsCount.innerHTML = "File Downloads Record Count: " + fileDownloadsArray.length;
		 var div_dataBaseProgress = document.getElementById("db_progress");
		 div_dataBaseProgress.style.display = "none";
		 var div_fileDownloadsView = document.getElementById("div_fileDownloadsView");
		 div_fileDownloadsView.style.display = "block";
	   	 console.log("buildFileDownloadsTable()...exit.");
	}

	
	function buildWebHitsTable(webHitsJSON) {
	   	 console.log("buildWebHitsTable()...invoked.");
		 var webHitsArray = JSON.parse(webHitsJSON);
		 var currentWebHit = null;
		 var tr = null;
		 var td = null;
		 var tbodyRef = document.getElementById("webHitsTable").getElementsByTagName('tbody')[0];
		 
		 while(tbodyRef.rows.length > 0) {
			 tbodyRef.deleteRow(0);
		 }
		 
		 for(var i=0; i<webHitsArray.length; i++) {
			 currentWebHit = webHitsArray[i];
			
			 tr = tbodyRef.insertRow();
			 tr.className = "admin";
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 //td.innerHTML = i+1;
			 td.innerHTML = currentWebHit.hitCount;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentWebHit.createDate;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentWebHit.ipAddress;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentWebHit.city;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentWebHit.state;
			 tr.appendChild(td);
			 
			 td = document.createElement("td");
			 td.className = "admin";
			 td.innerHTML = currentWebHit.country;
			 tr.appendChild(td);
		 }
		 
		 var webHitsTable = document.getElementById("webHitsTable");
		 var th_webHitsCount = document.getElementById("th_webHitsFooter");
		 th_webHitsCount.innerHTML = "Web Hits Record Count: " + webHitsArray.length;
		 var div_dataBaseProgress = document.getElementById("db_progress");
		 div_dataBaseProgress.style.display = "none";
		 var div_webHitsView = document.getElementById("div_webHitsView");
		 div_webHitsView.style.display = "block";
		 //var width = window.getComputedStyle(document.querySelector('#webHitsTable')).width
		 //div_webHitsView.style.width = width + "100px";
	   	 console.log("buildWebHitsTable()...exit.");
	}
	
	function cancelUpdateURL() {
		console.log("cancelUpdateURL()...invoked.");
		var div_updateURL = document.getElementById("div_updateURLDialogue");
		div_updateURL.style.display = "none";
		var button_maps = document.getElementById("button_maps");
	 	button_maps.style.display = "inline-block";
		console.log("cancelUpdateURL()...exit.");
	}
	
	   function convertStudyNameToFolderId(studyNameTextInput) {
	       console.log("convertStudyNameToFolderId()...invoked.");

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
		   
	       console.log("convertStudyNameToFolderId()...exit.");
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
		   	 if(newStudy.uploadFileNamesArray.length<4){
		   		 if(!droppedFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("folders.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("surface.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("surface.zip file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("volume.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("volume.zip file must be added");
		   			 return;
		   		 }
		   	 }
	   	 }
	   	 
	   	 if(selectedDataTypes == "surface") {
		   	 if(newStudy.uploadFileNamesArray.length<3){
		   		 if(!newStudy.uploadFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("folders.txt")) {
		 	   		 button_createStudy.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("surface.zip")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("surface.zip file must be added");
		   			 return;
		   		 }
		   	 }
	   	 }
	   	 
	   	 if(selectedDataTypes == "volume") {
		   	 if(newStudy.uploadFileNamesArray.length<3){
		   		 if(!newStudy.uploadFileNamesArray.includes("summary.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("summary.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("folders.txt")) {
			   		 createButton.disabled = false;
		   			 doAdminAlert("folders.txt file must be added");
		   			 return;
		   		 }
		   		 else if(!newStudy.uploadFileNamesArray.includes("volume.zip")) {
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
	   	 
	   	 newStudy.studyFolder = studyFolderName;
	   	 newStudy.selectedDataTypes = selectedDataTypes;
	   	 newStudy.menuEntry = menuEntry;
	   	 newStudy.totalFileNumber = newStudy.uploadFileNamesArray.length;
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
 
        var div_thresholdImageWrapper = document.getElementById("thresholdImageWrapper");
        div_thresholdImageWrapper.style.display = "none";
        
        
        if(global_networkTypeId.includes("single")) {
        	var div_selectNeuralNetworkName = document.getElementById("div_selectNeuralNetworkName");
        	div_selectNeuralNetworkName.style.visibility = "visible";
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
   
   function handleAdminLoginRequest() {
          console.log("handleAdminLoginRequest()...invoked.");

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
      	
        console.log("handleAdminLoginRequest()...exit.");

   }
   
   function handleAdminValidationResponse(responseJSONString) {
	   console.log("handleAdminValidationResponse()...invoked.");
	   
	   var jsonResponseObject = JSON.parse(responseJSONString);
	   var responseText = jsonResponseObject.validationMessage;
	 	   
	   if(responseText.includes("true")) {
		   handleTabSelected("div_admin");
	   }
	   else if(responseText.includes("false") && responseText.includes("expired")) {
		   doAdminAlert("Access denied<br>Error 100");
		   return;
	   }
	   else if(responseText.includes("false") && responseText.includes("access_denied")) {
		   doAdminAlert("Access denied<br>Error 200");
		   return;
	   }
	   else if(responseText.includes("false") && !responseText.includes("expired")) {
		   handleAdminLoginRequest();
	   }
	   
	   webHitsMapURL = jsonResponseObject.webHitsMapURL; 
       fileDownloadsMapUrl = jsonResponseObject.downloadsMapURL;
	   console.log(webHitsMapURL);
       console.log(fileDownloadsMapUrl);
	   console.log("handleAdminValidationResponse()...exit");


   }
   
   function handleChunkUploadResponse(responseText) {
	   console.log("handleChunkUploadResponse()...invoked");
	   alert(responseText, alertOK);
	   console.log("handleChunkUploadResponse()...exit");

   }
   
   function handleStudyMenuClicked(anchor) {
	   console.log("handleStudyMenuClicked()...invoked, anchor.id=" + anchor.id);
	   
	   anchor.style.backgroundColor = "#7a0019";
	   anchor.style.color = "#FFC300";
	   
	   var div_removeStudyProgress = document.getElementById("div_removeStudyProgress");
	   div_removeStudyProgress.style.display = "none";

	   
	   var div_addStudy = document.getElementById("div_addStudy");
	   var div_removeStudy = document.getElementById("div_removeStudy");
	   var div_updateStudy = document.getElementById("div_updateStudy");
	   var div_databaseAccess = document.getElementById("div_databaseAccess");
	   var div_downloadSamples = document.getElementById("div_downloadSamples");
	   var div_updateMaps = document.getElementById("div_updateMaps");
	   var div_viewMaps = document.getElementById("div_viewMapsWrapper");

	   
	   div_addStudy.style.display = "none";
	   div_removeStudy.style.display = "none";
	   div_updateStudy.style.display = "none";
	   div_databaseAccess.style.display = "none";
	   div_downloadSamples.style.display = "none";
	   div_updateMaps.style.display = "none";
	   div_viewMaps.style.display = "none";

	   var anchor_addStudy = document.getElementById("a_addStudy");
	   var anchor_removeStudy = document.getElementById("a_removeStudy");
	   var anchor_updateStudy = document.getElementById("a_updateStudy");
	   var anchor_databaseAccess = document.getElementById("a_databaseAccess");
	   var anchor_downloadSamples = document.getElementById("a_downloadSamples");
	   var anchor_updateMaps = document.getElementById("a_updateMaps");
	   var anchor_viewMaps = document.getElementById("a_viewMaps");

	   
	   anchor_addStudy.style.color = "white";
	   anchor_removeStudy.style.color = "white";
	   anchor_updateStudy.style.color = "white";
	   anchor_databaseAccess.style.color = "white";
	   anchor_downloadSamples.style.color = "white";
	   anchor_updateMaps.style.color = "white";
	   anchor_viewMaps.style.color = "white";

	   
	   switch (anchor.id) {
	   case "a_addStudy":
		   div_addStudy.style.display = "block";
		   var ul_studyMenu = document.getElementById("ul_studyMenu");
		   ul_studyMenu.scrollIntoView({behavior: 'smooth', block: 'start'});
		   anchor_addStudy.style.color = "#FFC300";
		   break;
	   case "a_removeStudy":
		   div_removeStudy.style.display = "block";
		   anchor_removeStudy.style.color = "#FFC300";
		   break;
	   case "a_updateStudy":
		   div_updateStudy.style.display = "block";
		   anchor_updateStudy.style.color = "#FFC300";
		   break;
	   case "a_databaseAccess":
		   div_databaseAccess.style.display = "block";
		   anchor_databaseAccess.style.color = "#FFC300";
		   break;
	   case "a_downloadSamples":
		   div_downloadSamples.style.display = "block";
		   anchor_downloadSamples.style.color = "#FFC300";
		   break;
	   case "a_viewMaps":
		   //use this technique rather than changing the src attribute
		   //for the iframe tag. Otherwise, the zoom on the map will
		   //default to an incorrect value
           var radio_webHitsMap = document.getElementById("radio_webHitsMap");
           var radio_downloadsMap = document.getElementById("radio_downloadsMap");

           if(numberMapDisplays==0) {
	           radio_webHitsMap.checked = true;
			   handleMapSelected(radio_webHitsMap);
		       radio_downloadsMap.checked = false;
		   }
           else if(radio_webHitsMap.checked) {
			   handleMapSelected(radio_webHitsMap);
		   }
		   else {
				handleMapSelected(radio_downloadsMap);
		   }
		   div_viewMaps.style.display = "block";

		   var ul_studyMenu = document.getElementById("ul_studyMenu");
		   ul_studyMenu.scrollIntoView({behavior: 'smooth', block: 'start'});
		   anchor_viewMaps.style.color = "#FFC300";
		   numberMapDisplays++;
		   break;
	   case "a_updateMaps":
		   div_updateMaps.style.display = "block";
		   anchor_updateMaps.style.color = "#FFC300";
		   break;
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

   function handleMapSelected(radioButton) {
	    console.log("handleMapSelected()...invoked");
	    var newInnerHTML = template_iframeMap;

		if(radioButton.id=="radio_webHitsMap") {
			newInnerHTML = newInnerHTML.replace("url", webHitsMapURL);
		}
		else {
			newInnerHTML = newInnerHTML.replace("url", fileDownloadsMapUrl);
		}
	   var div_iframeMap = document.getElementById("div_iframeMap"); 
       div_iframeMap.style.display = "none";
	   div_iframeMap.innerHTML = newInnerHTML;
       div_iframeMap.style.display = "block";
       console.log("handleMapSelected()...exit");
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
       
       var select_MenuId = document.getElementById("select_menuId_removeStudy");
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
   
   function handleUpdateMapURLResponse(responseText) {
       console.log("handleUpdateMapURLResponse()()...invoked.");
       
       var div_mapProgress = document.getElementById("div_map_progress");
       div_mapProgress.style.display = "none";
              
       var jsonResponseObject = JSON.parse(responseText);
	  
	   webHitsMapURL = jsonResponseObject.webHitsMapURL;
	   fileDownloadsMapURL = jsonResponseObject.downloadsMapURL;
       
	   console.log(webHitsMapURL);
	   var alertMessage = jsonResponseObject.message;
	   
	   var button_maps = document.getElementById("button_maps");
	   button_maps.style.display = "inline-block";
      
       doAdminAlert(alertMessage);
       console.log("handleUpdateMapURLResponse()()...exit.");
   }
   
   function handleRemoveStudyResponse(responseText) {
        console.log("handleRemoveStudyResponse()...invoked.");
       
        const index = studyMenuIDArray.indexOf(global_studyToRemove);
        if (index > -1) {
        	studyMenuIDArray.splice(index, 1);
        }
        buildMenuIdDropdownForRemoveStudy();
        buildMenuIdDropdownForUpdateStudy();

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
	   	
   		select_MenuId = document.getElementById("select_menuId_removeStudy");
        studyToRemove = select_MenuId.options[select_MenuId.selectedIndex].value;
       
	   	var div_removeStudy = document.getElementById("div_removeStudy");
	   	div_removeStudy.style.display = "none";
	   	
	   	var div_removeStudyProgress = document.getElementById("div_removeStudyProgress");
	   	div_removeStudyProgress.style.display = "block";
	   	
	   	global_studyToRemove = studyToRemove;
	   	sendRemoveStudyRequest(studyToRemove);

        console.log("handleRemoveStudyRequest()...exit.");
   }
   
   function handleUpdateStudyResponse(responseText) {
	   	console.log("handleUpdateStudyResponse()...invoked.");
	   	
	   	updateStudy.div_unzipProgress.style.display = "none";
	   	updateStudy.div_progressUpload.style.display = "none";
	   	
	    resetUpdateStudyForm();
		doAdminAlert(responseText);
	   	console.log("handleUpdateStudyResponse()...exit.");
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
   
   function hideAddStudyHelp() {
	   console.log("hideAddStudyHelp()...invoked.");
	   var div_addStudyHelp = document.getElementById("div_addStudyHelp");
	   div_addStudyHelp.style.display = "none";
	   console.log("hideAddStudyHelp()...exit.");
   }
   
   function hideUpdateStudyHelp() {
	   console.log("hideUpdateStudyHelp()...invoked.");
	   var div_updateStudyHelp = document.getElementById("div_updateStudyHelp");
	   div_updateStudyHelp.style.display = "none";
	   console.log("hideUpdateStudyHelp()...exit.");
   }
   
   function initializeDragDropAddStudy() {
	  	 //alert("drag and drop init...");
	  	 console.log("initializeDragDropAddStudy()...invoked.");
	  	 
	  	newStudy.ul_uploadFileList.addEventListener("dblclick", function(e) {
	  		console.log("newStudy.ul_uploadFileList event handler");
	  		e.preventDefault();
	  		e.stopPropagation();
	  		return false;
	  	});
	  	 
	  	div_dropZone.addEventListener("dragenter", function() {
	  		 this.classList.add("active");
	  	});

	  	div_dropZone.addEventListener("dragleave", function() {
			  this.classList.remove("active");
			});

	  	div_dropZone.addEventListener("dragover", function(e) {
			    e.preventDefault();
			});
			
	  	div_dropZone.addEventListener("drop", function(e) {
				e.preventDefault();
				this.classList.remove("active");
				var fileName = null;
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
					
					if(newStudy.uploadFileNamesArray.includes(fileName)) {
						doAdminAlert("duplicate file name");
						return;
					}
					
					if(fileName != "surface.zip" && fileName != "volume.zip" 
					   && fileName != "summary.txt" && fileName != "folders.txt") {
						doAlert("file name must be surface.zip, volume.zip, summary.txt, or folders.txt");
						return;
					}

					console.log("fileName=" + fileName);
					newStudy.uploadFileNamesArray.push(fileName);
					//zipFormData.append(fileName, e.dataTransfer.files[x]);
					newStudy.uploadFilesArray.push(e.dataTransfer.files[x]);
					//fileNamesArray.push(fileName);
				}
				if(fileName.includes("surface.zip")){
					var innerHTML = newStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_surfaceZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					newStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
				if(fileName.includes("volume.zip")){
					var innerHTML = newStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_volumeZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					newStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
				if(fileName.includes("txt")){
					var innerHTML = newStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_textImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					var newInnerHTML = innerHTML + additionalHTML;
					newStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
											
			});
			
	 	    console.log("initializeDragDropAddStudy()...exit.");
	   
	   
   }
   
   function initializeDragDropUpdateStudy() {
	   

	  	 //alert("drag and drop init...");
	  	 console.log("initializeDragDropUpdateStudy()...invoked.");
	  	 
	  	updateStudy.ul_uploadFileList.addEventListener("dblclick", function(e) {
	  		console.log("updateStudy.ul_uploadFileList event handler");
	  		e.preventDefault();
	  		e.stopPropagation();
	  		return false;
	  	});
	  	 
	  	updateStudy.div_dropZone.addEventListener("dragenter", function() {
	  		 this.classList.add("active");
	  	});

	  	updateStudy.div_dropZone.addEventListener("dragleave", function() {
			  this.classList.remove("active");
			});

	  	updateStudy.div_dropZone.addEventListener("dragover", function(e) {
			    e.preventDefault();
			});
			
	  	updateStudy.div_dropZone.addEventListener("drop", function(e) {
				e.preventDefault();
				this.classList.remove("active");
				var fileName = null;
				var additionalHTML = null;
				
			   	 var select_updateStudyId = document.getElementById("select_menuId_updateStudy");
			   	 var studyId = select_updateStudyId.options[select_updateStudyId.selectedIndex].value;
			   	 updateStudy.studyId = studyId;
			   	 
			   	 var select_updateStudyAction = document.getElementById("select_action_updateStudy");
			   	 var selectedActionType = select_updateStudyAction.options[select_updateStudyAction.selectedIndex].value;
			   	 updateStudy.actionName = selectedActionType;

			   	 if(selectedActionType == "unselected" || studyId == "none selected" ) {
			   		 doAdminAlert("Please select a study and an action first");
			   		 return;
			   	 }
				
				for (var x=0; x < e.dataTransfer.files.length; x++) {
					fileName = e.dataTransfer.files[x].name;
										
					if(selectedActionType == "updateSummary") {
						if(!(fileName == "summary.txt")) {
							doAdminAlert("only summary.txt file allowed for update summary action");
							return;
						}
					}
					else if(selectedActionType === "addVolumeData") {
						if(!(fileName == "volume.zip")) {
							doAdminAlert("only volume.zip file allowed for add volume data action");
							return;
						}
					}
					else if(selectedActionType === "addSurfaceData") {
						if(!(fileName == "surface.zip")) {
							doAdminAlert("only surface.zip file allowed for add surface data action");
							return;
						}
					}
					
					
					if(updateStudy.uploadFileNamesArray.includes(fileName)) {
						doAdminAlert("duplicate file name");
						return;
					}
					
					if(fileName != "volume.zip" && fileName != "summary.txt" && fileName != "surface.zip") {
						doAdminAlert("file name must be volume.zip, surface.zip, or summary.txt");
						return;
					}
					
					console.log("fileName=" + fileName);
					updateStudy.uploadFileNamesArray.push(fileName);
					updateStudy.formData.append(fileName, e.dataTransfer.files[x]);
					updateStudy.uploadFilesArray.push(e.dataTransfer.files[x]);
				}
		
				if(fileName.includes("surface.zip")){
					var innerHTML = updateStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_surfaceZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(actionTypeReplacementMarker, "updateStudy");				
					var newInnerHTML = innerHTML + additionalHTML;
					updateStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
				if(fileName.includes("volume.zip")){
					var innerHTML = updateStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_volumeZipImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(actionTypeReplacementMarker, "updateStudy");				
					var newInnerHTML = innerHTML + additionalHTML;
					updateStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
				if(fileName.includes("summary.txt")){
					var innerHTML = updateStudy.ul_uploadFileList.innerHTML;
					additionalHTML = template_li_textImage;
					additionalHTML = additionalHTML.replace(fileNameReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(formKeyReplacementMarker, fileName);
					additionalHTML = additionalHTML.replace(actionTypeReplacementMarker, "updateStudy");
					var newInnerHTML = innerHTML + additionalHTML;
					updateStudy.ul_uploadFileList.innerHTML = newInnerHTML
				}
														
			});
			
	 	    console.log("initializeDragDropUpdateStudy()...exit.");
  
   }

   
   
 
   function loadStudyROIImageHeader() {
	   console.log("loadStudyROIImageHeader()...invoked");
	   var studyDisplayName = studyDisplayNameMap.get(selectedStudy.studyId);
	   var headerText = studyDisplayName + " PROBABILISTIC ROIS";
	   var headerElement = document.getElementById("roi_image_slides_header");
	   headerElement.innerHTML = headerText;
	   console.log("loadStudyROIImageHeader()...exit, innerHTML=" + headerElement.innerHTML);
   }
   
   function loadStudySummaryList() {
	    console.log("loadStudySummaryList()...invoked.");
	    
	    var summaryEntriesArray = studySummaryMap.get(selectedStudy.studyId);	    
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

           var study = element.getAttribute("data-study");
           selectedStudy.studyId= study; //ie: abcd_template_matching
           console.log("menuClicked()...invoked, study=" + study);
	      
	      if(lastSelectedMenu != null) {
		      if(lastSelectedMenu.id === element.id) {
		    	  if(priorSelectedDataType === selectedDataType) {
		    		  return;
		    	  }
		      }
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
       selectedStudy.availableDataType = surfaceVolumeType;
       var volumeDataAvailable = surfaceVolumeType.includes("volume");
       var surfaceDataAvailable = surfaceVolumeType.includes("surface");
       var radio_VolumeControl = radio_VolumeControl = document.getElementById("radio_volume"); 
       var label_volume = document.getElementById("label_volume");
       var radio_SurfaceControl = document.getElementById("radio_surface"); 
       var label_surface = document.getElementById("label_surface");
	   label_surface.style.backgroundColor = "#FFC300";

       if(priorSelectedDataType=="volume" && volumeDataAvailable) {
			selectedStudy.selectedDataType = "volume";
	   }
       else {
			selectedStudy.selectedDataType = "surface";
	   }

       if(priorSelectedDataType=="surface" && surfaceDataAvailable) {
			selectedStudy.selectedDataType = "surface";
	   }
       else {
			selectedStudy.selectedDataType = "volume";
	   }
       

       
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

       if(!surfaceDataAvailable) {
    	   radio_SurfaceControl.disabled = true;
           radio_SurfaceControl.checked = false;
		   radio_VolumeControl.checked = true;
    	   label_surface.innerHTML = "Surface Data - not yet available";
    	   label_surface.style.backgroundColor = "lightgrey";
    	   label_volume.style.backgroundColor = "#FFC300";    	   

       }
       else {
	       radio_VolumeControl.checked = false;
           radio_SurfaceControl.checked = true;
    	   radio_SurfaceControl.disabled = false;
    	   //label_volume.style.backgroundColor = "#F0EAD6";
    	   label_surface.innerHTML = "Surface Data";
    	   label_surface.style.backgroundColor = "#FFC300";    	   
       }
       
       console.log("networkTypeId=" + networkTypeId);
       
       loadStudySummaryList();
       loadStudyROIImageHeader();
       
       global_networkTypeId = "unselected";

       // the selectedNeuralNetworkName maps to a folder name on the server
       // if the networkTypeId is 'single' then we have to determine which
       // single network has been chosen, since single has a subset of
       // of options:  DMN, CO, Aud, etc.      

       if(networkTypeId.includes("single")) {
    	 global_networkTypeId = "single";
    	 buildNeuralNetworkDropdownList();
    	 if(selectedStudy.studyId != priorSelectedStudy) {
    		 firstTimeSelectingSingle = true;
    	 }
      	 var select_neuralNetworkName = document.getElementById("select_neuralNetworkName");
      	 
         //If this is the first time single networks has been selected for the currently
         //selected study, then we go with the default choice of 'DMN'  which is set when
         //the dropdown is first built in the buildNeuralNetworkDropdownList() function.
		 //Then we turn off the global indicator. When it is not the first time viewing Single
	     //network data, then it is because the user selected a new/different Single network
         //(not 'DMN') and therefore we need to see which option from the select dropdown was
         //selected.
		 if(firstTimeSelectingSingle) {
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

         function preProcessUpdateMapURL() {
  	 		
        	var div_urlDialogue = document.getElementById("div_updateURLDialogue");
        	div_urlDialogue.style.display = "none";
        	
        	var newURL = document.getElementById("newURLEntry").value;
        	newURL = newURL.replace("&", "!!!");
        	var beginText1 = "<ifram";
        	var beginText2 = "https";
        	
        	if(newURL.trim().length==0) {
        		doAdminAlert("The url field must not be blank");
            	div_urlDialogue.style.display = "block";
            	return;
        	}
        	else if(!newURL.startsWith(beginText1) && !newURL.startsWith(beginText2)) {
        		doAdminAlert("The url must start with " + "&lt;" + "iframe or https");
            	div_urlDialogue.style.display = "block";
        		return;
        	}

 			var div_mapProgress = document.getElementById("div_map_progress");
			div_mapProgress.style.display = "block";
        	        	
 	 		sendUpdateMapURLRequest(newURL.trim());
         }

 		function processMapsRequest() {
 		   	 console.log("processMapsRequest()...invoked.");

 		   	 var mapsDropdown = document.getElementById("mapsDropdown");
        	 var selection = mapsDropdown.options[mapsDropdown.selectedIndex].value
        	 console.log("selection=" + selection);
        	 
        	 
        	switch(selection) {
        	
     	 	case "updateWebHitsMapURL":
			    targetMap = "WEB_HITS_MAP";
     	 		var div_urlDialogue = document.getElementById("div_updateURLDialogue");
     	 		div_urlDialogue.style.display = "block";
     	 		document.getElementById("newURLEntry").focus();
     	 		var button_maps = document.getElementById("button_maps");
     	 		button_maps.style.display = "none";
     	 		break;
    	 	case "updateDownloadHitsMapURL":
			    targetMap = "FILE_DOWNLOADS_MAP";
     	 		var div_urlDialogue = document.getElementById("div_updateURLDialogue");
     	 		div_urlDialogue.style.display = "block";
     	 		document.getElementById("newURLEntry").focus();
     	 		var button_maps = document.getElementById("button_maps");
     	 		button_maps.style.display = "none";
     	 		break;
     	 	case "downloadWebHitsGeoLoc":
     	 		downloadAdminFile("/midb/web_hits_geoloc.csv");
     	 		break;
     	 	case "downloadFileDownloadHitsGeoLoc":
     	 		downloadAdminFile("/midb/file_downloads_geoloc.csv");
     	 		break;
     	 	case "resynchWebHits":
     	 		var div_mapProgress = document.getElementById("div_map_progress");
     	 		div_mapProgress.style.display = "block";
     	 		sendResynchWebHitsRequest();
     	 		break;
     	 	case "downloadCreateMapDoc":
     	 		downloadAdminFile("CreateNewMap.odt");
        	}
 		   	 
 		   	 console.log("processMapsRequest()...exit.");
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
	   
	   function manageFileUploads() {
		   console.log("manageFileUploads()...invoked");
		   zipFormData = new FormData();
		   var index = newStudy.currentIndex;
		   var currentFile = newStudy.uploadFilesArray[index];
		   var currentFileSize = currentFile.size;
		   var currentFileName = newStudy.uploadFileNamesArray[index];
		   zipFormData.append(currentFileName, currentFile);
		   
		   if(index==0) {
				var div_addStudyDetails = document.getElementById("div_addStudyDetails");
				div_addStudyDetails.style.display = "none";
			}
		   uploadAddStudyFile(zipFormData, currentFileName, currentFileSize);
		   console.log("manageFileUploads()...exit");
	   }
	   
		function preProcessAdminRequest(button, actionRequest) {
		   	 console.log("preProcessUpdateStudyRequest()...invoked.");
		   	 
			 if(actionRequest == "updateStudy") {
			   	 if(updateStudy.uploadFileNamesArray.length==0) {
			   		 doAdminAlert("You must drag/drop a file to upload.");
			   		 return;
			   	 }
			 }
			 if(actionRequest == "createStudy") {
			   	 if(newStudy.uploadFileNamesArray.length==0) {
			   		 doAdminAlert("You must drag/drop a file to upload.");
			   		 return;
			   	 }
			 }
		
		   	 button.disabled = true;
		   	 lastAdminActionRequest = actionRequest;
		   	 
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
	    	 
		   	 console.log("preProcessUpdateStudyRequest()...exit.");
		}

			   
	   function processDataModeChoice(element) {
	        console.log("processDataModeChoice()...invoked.");
            
	        var id = element.id;
	        var surfaceLabel = document.getElementById("label_surface");
	        var volumeLabel = document.getElementById("label_volume");
	        
	        if(id.includes("surface")) {
	        	surfaceLabel.style.background = "#FFC300";
	        	volumeLabel.style.background = "#f0efee";
	        	selectedStudy.selectedDataType = "surface";
	        }
	        else {
	        	volumeLabel.style.background = "#FFC300";
		        surfaceLabel.style.background = "#f0efee";
	        	selectedStudy.selectedDataType = "volume";
	        }
	        //preProcessGetThresholdImages();
	        //put a slight delay so user can see the radio button change
	        setTimeout(preProcessGetThresholdImages, 200);
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
		   		   		   
		   var isUpdateStudyAction = false;
		   var actionType = liElement.getAttribute("data-actionType");
		   
		   if(actionType == "updateStudy") {
			   isUpdateStudyAction = true;
		   }

		   event.stopPropagation();
		   if(droppedFileRemovalPending) {
			   console.log("removeDroppedFile()...aborting, array=" + droppedFileNamesArray);
			   return;
		   }
		   
		   var fileName = liElement.getAttribute("data-formKey");
		   var arrayIndex = 0;
		   
		   if(isUpdateStudyAction) {
			   arrayIndex = updateStudy.uploadFileNamesArray.indexOf(fileName);
		   }
		   else {
		       arrayIndex = newStudy.uploadFileNamesArray.indexOf(fileName);
		   }
		   
		   droppedFileRemovalPending = true;
		   
		   if(isUpdateStudyAction) {
			   updateStudy.uploadFileNamesArray.splice(arrayIndex, 1);
			   updateStudy.uploadFilesArray.splice(arrayIndex, 1);
		   }
		   else {
			   newStudy.uploadFileNamesArray.splice(arrayIndex, 1);
			   newStudy.uploadFilesArray.splice(arrayIndex, 1);
		   }
		   
		   var parent = liElement.parentElement;
		   parent.removeChild(liElement);
		   //setTimeout(removeChild, 500, parent, liElement);
		   
		   droppedFileRemovalPending = false;
		   console.log("removeDroppedFile()...exit.");
	   }
	   
	   function removeChild(parent, child) {
		   console.log("removeChild()...invoked.");
		   console.log("parentID=" + parent.id);
		   parent.removeChild(child);
		   droppedFileRemovalPending = false;
		   console.log("removeChild()...exit, array=" + droppedFileNamesArray);
	   }

        function resetAddStudyDropZone() {
        	console.log("resetAddStudyDropZone()...invoked.");
        	newStudy.ul_uploadFileList.innerHTML = "";
        	
        	newStudy.uploadFileNamesArray = new Array();
        	newStudy.uploadFilesArray = new Array();

        	console.log("resetAddStudyDropZone()...exit.");

         }

         function resetAddStudyForm() {
       	    console.log("resetAddStudyForm()...invoked.");
  			var table = document.getElementById("adminTable");
  			var rowCount = table.rows.length;
  			var networkDisplayNameRows = document.getElementsByClassName("networkDisplayNameRow");
 			var numberToDelete = networkDisplayNameRows.length - 1;
 			
 			while(numberToDelete > 0) {
 				table.deleteRow(rowCount-1);
 				rowCount = table.rows.length;
 				numberToDelete--;
         	}
            
   	   	 	var text_studyDisplayName = document.getElementById("input_studyDisplayName");
   	   	 	text_studyDisplayName.value = "";
	   	 
   	   	 	var text_studyFolderName = document.getElementById("input_studyFolderName");;
   	   	 	text_studyFolderName.value = "";

 		    var selectElement = document.getElementById("select_dataType");
		    selectElement.selectedIndex = 0; 
		    
		    selectElement = document.getElementById("select_networkType");
		    selectElement.selectedIndex = 0; 

		    var div_dropZone = document.getElementById("div_dropZone");
		    
		    resetAddStudyDropZone();
		    div_dropZone.style.display = "block";

			var div_addStudyDetails = document.getElementById("div_addStudyDetails");
			div_addStudyDetails.style.display = "block";
		    
       	    console.log("resetAddStudyForm()...exit.");
         }
         
	   
	   function resetUpdateStudyForm() {
		   
		   	console.log("resetUpdateStudyForm()...invoked.");
	   		updateStudy.ul_uploadFileList.innerHTML = "";
	   	   	
	   		updateStudy.uploadFileNamesArray = new Array();
	   		updateStudy.uploadFilesArray = new Array();
	   		
	   		updateStudy.formData = new FormData();

		   	var select_updateStudyId = document.getElementById("select_menuId_updateStudy");
		   	select_updateStudyId.selectedIndex = 0;
		   	 
		   	var select_updateStudyAction = document.getElementById("select_action_updateStudy");
		   	select_updateStudyAction.selectedIndex = 0;
		   	
		   	var button_updateStudy = document.getElementById("button_updateStudy");
		   	button_updateStudy.disabled = false;
		   	
	      	var div_updateStudyDetails = document.getElementById("div_updateStudyDetails");
	      	div_updateStudyDetails.style.display = "block";
	   	 
	      	var div_updateStudyProgress = document.getElementById("div_updateStudyProgress");
			div_updateStudyProgress.style.display = "none";
			
		   	console.log("resetUpdateStudyForm()...exit.");

	   }
	   
	   function sendGetAdminAccessRecordsJSON() {

	        console.log("sendGetAdminAccessRecordsJSON()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getAdminAccessRecords";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
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
			           	
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  }
	            	  else {
	            		  buildAdminAccessTable(ajaxRequest.responseText);
	            	  }
	   	       	  }
	       	}
	   
      	    ajaxRequest.send();
	        console.log("sendGetAdminAccessRecordsJSON()...exit.");	   
	   }

	   
	   function sendGetEmailAddressesJSON() {

	        console.log("sendGetEmailAddressesJSON()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getEmailAddresses";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
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
			           	
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  }
	            	  else {
	            		  buildEmailAddressesTable(ajaxRequest.responseText);
	            	  }
	   	       	  }
	       	}
	   
       	    ajaxRequest.send();
	        console.log("sendGetEmailAddressesJSON()...exit.");	   
	   }
	   
	   
	   function sendGetFileDownloadsJSON() {

		   console.log("sendGetFileDownloadsJSON()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getFileDownloadRecords";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
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
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  } 
	            	  else {
	            	  	buildFileDownloadsTable(ajaxRequest.responseText);
	            	  }
	   	       	  }
	       	}
	   
      	    ajaxRequest.send();
 		    console.log("sendGetFileDownloadsJSON()...exit.");
	   }

	   function getStorageStats(ssButton) {
		    console.log("getStorageStats()...invoked.");

			ssButton.disabled = true;
			var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getStorageStats";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
	       	ajaxRequest.onreadystatechange=function() {
	
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
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  }
	            	  else {
		            	  doAdminAlert(ajaxRequest.responseText);
						  var button_getServerStorage1 = document.getElementById("button_getServerStorage1");
						  button_getServerStorage1.disabled = false;
						  var button_getServerStorage2 = document.getElementById("button_getServerStorage2");
						  button_getServerStorage2.disabled = false;
	            	  }
	   	       	  }
	       	}
	   
        	ajaxRequest.send();
		    console.log("getStorageStats()...exit.");
	   }
	   
	   
	   function sendGetWebHitsRequest() {
	        console.log("sendGetWebHitsRequest()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getWebHits";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
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
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  }
	            	  else {
		            	  buildWebHitsTable(ajaxRequest.responseText);
	            	  }
	   	       	  }
	       	}
	   
        	ajaxRequest.send();
	        console.log("sendRemoveStudyRequest()...exit.");

	   }
	   
	   function sendGetWebHitsMapURLRequest() {

	        console.log("sendGetWebHitsMapURLRequest()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=getWebHitsMapURL";	
	       	var encodedUrl = encodeURI(url);
	       	ajaxRequest.open('get', encodedUrl, true);
	      
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
	            	  if(ajaxRequest.responseText.includes("Access denied")) {
	            		  var div_dataBaseProgress = document.getElementById("db_progress");
	            		  div_dataBaseProgress.style.display = "none";
	            		  doAdminAlert(ajaxRequest.responseText);
	            	  }
	            	  else {
	            		  webHitsMapURL = responseText;
	            		  var anchor_viewMaps = document.getElementById("a_viewMaps");
		            	  viewWebHitsMap(anchor_viewMaps);
	            	  }
	   	       	  }
	       	}
	   
	       	ajaxRequest.send();
	        console.log("sendGetWebHitsMapURLRequest()...exit.");
	   }
	   
	   function sendUpdateMapURLRequest(newURL) {

	        console.log("sendUpdateMapURLRequest()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var paramString = "&targetMap=" + targetMap;
	       	paramString += "&newURL=" + newURL;
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=updateMapURL"
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
			           	handleUpdateMapURLResponse(ajaxRequest.responseText);
	   	       }
	       	}
        	ajaxRequest.send();
	        console.log("sendUpdateMapURLRequest()...exit.");
		   
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
  
	   function sendResynchWebHitsRequest() {

	        console.log("sendResynchWebHitsRequest()...invoked.");

	       	var ajaxRequest = getAjaxRequest();
	       	var url = "/NetworkProbabilityDownloader/NPViewerDownloaderServlet?action=resynchWebHits";
	
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
	      	 			var div_mapProgress = document.getElementById("div_map_progress");
	      	 			div_mapProgress.style.display = "none"; 	
	      	 			doAdminAlert(ajaxRequest.responseText);
	              }
	       	}
	       	ajaxRequest.send();
	        console.log("sendResynchWebHitsRequest()...exit.");
	   }
	   
	   function showAddStudyHelp() {
		   console.log("showAddStudyHelp()...invoked.");
		   var div_addStudyHelp = document.getElementById("div_addStudyHelp");
		   div_addStudyHelp.style.display = "block";
		   doAdminAlert("Be sure to check server storage availability before adding a study.");
		   console.log("showAddStudyHelp()...exit.");
	   }
	   
	   function showUpdateStudyHelp() {
		   console.log("showUpdateStudyHelp()...invoked.");
		   var div_updateStudyHelp = document.getElementById("div_updateStudyHelp");
		   div_updateStudyHelp.style.display = "block";
		   doAdminAlert("Be sure to check server storage availability before adding surface or volume data.");
		   console.log("showUpdateStudyHelp()...exit.");
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
			           		if(lastAdminActionRequest == "createStudy") {
			           			var button_createStudy = document.getElementById("button_createStudy");
			           			createStudyEntry(button_createStudy);
			           		}
			           		else if(lastAdminActionRequest == "updateStudy") {
			           			updateStudyEntry();
			           		}
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
		   console.log("validateCreateStudyFormData()...invoked");
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
		   console.log("validateCreateStudyFormData()...exit");
		   return true;
		   
	   }

   function validateAddStudyDroppedFiles(select_DataTypeElement) {
		   console.log("validateAddStudyDroppedFiles()...invoked.");
		   
		   var selectedDataTypes = select_DataTypeElement.options[select_DataTypeElement.selectedIndex].value;
		   var arrayIndex = 0;
		   
		   console.log("before: array=" + newStudy.uploadFileNamesArray);
		   
		   if(selectedDataTypes == "surface") {
			   if(newStudy.uploadFileNamesArray.includes("volume.zip")) {
				   console.log("validateDroppedFiles()...removing volume.zip");
				   zipFormData.delete("volume.zip");
				   arrayIndex = newStudy.uploadFileNamesArray.indexOf("volume.zip");
				   newStudy.uploadFileNamesArray.splice(arrayIndex,1);
				   newStudy.uploadFilesArray.splice(arrayIndex,1);
				   var targetLI = document.getElementById("volume.zip");
				   newStudy.ul_uploadFileList.removeChild(targetLI);
			   }
		   }
		   else if(selectedDataTypes == "volume") {
			   if(newStudy.uploadFileNamesArray.includes("surface.zip")) {
				   console.log("validateDroppedFiles()...removing surface.zip");
				   zipFormData.delete("surface.zip");
				   arrayIndex = newStudy.uploadFileNamesArray.indexOf("surface.zip");
				   newStudy.uploadFileNamesArray.splice(arrayIndex,1);
				   newStudy.uploadFilesArray.splice(arrayIndex,1);
				   var targetLI = document.getElementById("surface.zip");
				   newStudy.ul_uploadFileList.removeChild(targetLI);
			   }
		   }
		   
		   console.log("after: array=" + newStudy.uploadFileNamesArray);
		   console.log("zipFormData keys follow...");
		   for (var key of zipFormData.keys()) {
			   console.log(key);
			}
		   console.log("validateAddStudyDroppedFiles()...exit.");
	   }

   function validateUpdateStudyDroppedFiles(select_ActionElement) {
		   console.log("validateUpdateStudyDroppedFiles()...invoked.");
		   
		   var selectedAction = select_ActionElement.options[select_ActionElement.selectedIndex].value;
		   var arrayIndex = 0;
           var namesToDelete = new Array();

		   if(selectedAction == "updateSummary") {
				namesToDelete.push("volume.zip");
				namesToDelete.push("surface.zip");
		   }
		   else if(selectedAction == "addSurfaceData") {
				namesToDelete.push("volume.zip");
				namesToDelete.push("summary.txt");
		   }
		   else if(selectedAction == "addVolumeData") {
				namesToDelete.push("surface.zip");
				namesToDelete.push("summary.txt");
		   }

           var currentName = null;
           var spliceIndex = -1;
		   var targetLI = null;

		   for(i=0; i<namesToDelete.length; i++) {
				currentName = namesToDelete[i];
				spliceIndex = updateStudy.uploadFileNamesArray.indexOf(currentName);
				if(spliceIndex >=0) {
				   console.log("removing:" + currentName);
				   updateStudy.uploadFileNamesArray.splice(arrayIndex,1);
				   updateStudy.uploadFilesArray.splice(arrayIndex,1);
	               updateStudy.formData.delete(currentName);
				   targetLI = document.getElementById(currentName);
		           updateStudy.ul_uploadFileList.removeChild(targetLI);
				}
		   }
		   
		   console.log("after: array=" + updateStudy.uploadFileNamesArray);
		   console.log("updateStudy.formData keys follow...");
		   for (var key of updateStudy.formData.keys()) {
			   console.log(key);
			}
		   console.log("validateUpdateStudyDroppedFiles()...exit.");
	   }

   