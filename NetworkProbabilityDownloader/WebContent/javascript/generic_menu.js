   
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

   function startupMenu() {
       console.log("startUpMenu()...invoked.");

       var anchor_ABCD_combined = document.getElementById("a_ABCD_Combined");
       menuClicked(anchor_ABCD_combined, true, true);
 
       //hideDataTypeSelection();
       
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
	  
	function enableScroll() {
	    window.onscroll = function() {};
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

   
   function menuClicked(element, startupTrigger, actionRequired) {
	      console.log("menuClicked()...invoked, id=" + element.id);
              var study = element.getAttribute("data-study");
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
       var networkTypeId = parent.id;
       //alert("clicked:id=" + parent.id);
       //hideMenu(); 
       //grandParent.style.display = "inline";  
       //selectedSubmenu = parent;   
       //parent.style.display = "inline";
       
       console.log("networkTypeId=" + networkTypeId);
       
       
       if(networkTypeId.includes("Single")) {
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
       else if(networkTypeId.includes("li_ABCD_Combined")) {
      	 selectedNeuralNetworkName = "combined_clusters";
      	 preProcessGetThresholdImages();
       }
       else if(networkTypeId.includes("li_ABCD_Overlapping")) {
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




   