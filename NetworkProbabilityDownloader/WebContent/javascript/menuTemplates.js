
// <li class="submenu" id="li_ABCD"><a class="submenu" id="a_ABCD"  onmouseout="mouseOut(this)" onmouseover="showSubSubMenu(this)">ABCD - Template Matching</a>
var template_li_submenu = "<li class=\x22submenu\x22 id=\x22li_${id}\x22><a class=\x22submenu\x22 id=\x22a_${id}\x22  onmouseleave=\x22mouseOut(this)\x22 onmouseover=\x22showSubSubMenu(this)\x22>${displayName}</a>";
var idReplacementMarker = "${id}";


var template_iframeMap = "<iframe id=\"iframe_webHitsMap\" src=\"url\" width=\"100%\" height=\"680\"></iframe>";


var displayNameReplacementMarker = "${displayName}";
var studyReplacementMarker = "${studyName}";
var studyDisplayReplacementMarker = "${studyDisplayName}";
var studyFolderReplacementMarker = "${studyFolderName}";
var dataTypesReplacementMarker = "${available_dataTypes}";
var networkDisplayNameReplacementMarker = "${networkDisplayName}";
var networkFolderNameReplacementMarker = "${networkFolderName}";
var fileNameReplacementMarker = "${fileName}";
var menuIdReplacementMarker = "${menuId}";
var surfaceVolumeTypeReplacementMarker = "${surface_volume}";
var summaryEntryReplacementMarker = "${summaryEntry}";
var formKeyReplacementMarker = "${formKey}";
var actionTypeReplacementMarker = "${actionType}";




var networkIdReplacementMarker = "${networkId}";

// <ul class="subSubMenu" id="ul_ABCD">
var template_ul_subSubMenu = "<ul class=\x22subSubMenu\x22 id=\x22ul_${id}\x22>";

// <li class="subSubMenu" id="li_ABCD_Combined"><a class="subSubMenu" id="a_ABCD_Combined" data-study="abcd" href="#"  onmouseover="showSubSubMenu(this)" 
//                                                                            onmouseout="mouseOut(this)" onclick="menuClicked(this, false, true )">Combined Networks</a></li> 
                 
var template_li_subSubMenu = "<li class=\x22subSubMenu\x22 id=\x22li_${id}\x22><a class=\x22subSubMenu\x22 id=\x22a_${id}\x22 data-study=\x22${studyName}\x22 onmouseover=\x22showSubSubMenu(this)\x22" 
//var template_li_subSubMenu = "<li class=\x22subSubMenu\x22 id=\x22li_${id}\x22><a class=\x22subSubMenu\x22 id=\x22a_${id}\x22 data-study=\x22${studyName}\x22 " 
                                                                           + " onmouseout=\x22mouseOut(this)\x22 onclick=\x22menuClicked(this, false, true)\x22 "
                                                                           + " data-studyDisplayName=\x22${studyDisplayName}\x22 "
                                                                           + "data-surfaceVolumeType=\x22${surface_volume}\x22 data-networkId=\x22${networkId}\x22>${displayName}"
                                                                           + "</a></li>";

var tag_endLI = "</li>";
var tag_endUL = "</ul>";
var newLine = "\n";

var template_beginMenuEntry = "MENU ENTRY (ID=${menuId})" + newLine;
var template_studyNameEntry = "${studyDisplayName} (${studyFolderName}) (${available_dataTypes})" + newLine;
var template_networkEntry = "${networkDisplayName} (${networkFolderName})" + newLine;
var template_endMenuEntry = "END MENU ENTRY" + newLine + newLine;


var template_li_surfaceZipImage =   "<li class=\x22zipList\x22 onclick=\x22removeDroppedFile(this)\x22 data-actionType=\x22${actionType}\x22 data-formKey=\x22${formKey}\x22 id=\x22surface.zip\x22>" + newLine
                  + "<figure style=\x22text-align:center;\x22 class=\x22zipFigure\x22>" + newLine
                  + "<img src=\x22/NetworkProbabilityDownloader/images/zip_vice.jpg\x22 height=\x2265\x22 width=\x2265\x22>" + newLine
                  + "<figcaption style=\x22text-align:center\x22 class=\x22zipList\x22>${fileName}</figcaption>" + newLine
                  + "</figure>" + newLine
                  + "</li>";

var template_li_volumeZipImage =   "<li class=\x22zipList\x22 onclick=\x22removeDroppedFile(this)\x22 data-actionType=\x22${actionType}\x22 data-formKey=\x22${formKey}\x22 id=\x22volume.zip\x22>" + newLine
+ "<figure style=\x22text-align:center;\x22 class=\x22zipFigure\x22>" + newLine
+ "<img src=\x22/NetworkProbabilityDownloader/images/zip_vice.jpg\x22 height=\x2265\x22 width=\x2265\x22>" + newLine
+ "<figcaption class=\x22zipList\x22>${fileName}</figcaption>" + newLine
+ "</figure>" + newLine
+ "</li>";

var template_li_textImage =   "<li class=\x22zipList\x22 onclick=\x22removeDroppedFile(this)\x22 data-actionType=\x22${actionType}\x22 data-formKey=\x22${formKey}\x22 id=\x22summary.txt\x22>" + newLine
+ "<figure class=\x22zipFigure\x22>" + newLine
+ "<img src=\x22/NetworkProbabilityDownloader/images/txt-file.png\x22 height=\x2265\x22 width=\x2265\x22>" + newLine
+ "<figcaption class=\x22zipList\x22>${fileName}</figcaption>" + newLine
+ "</figure>" + newLine
+ "</li>";


var template_summary_li = "<li class=\x22li_atlasSummary\x22> ${summaryEntry}</li>";  

 