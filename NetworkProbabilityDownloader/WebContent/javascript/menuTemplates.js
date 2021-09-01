
// <li class="submenu" id="li_ABCD"><a class="submenu" id="a_ABCD"  onmouseout="mouseOut(this)" onmouseover="showSubSubMenu(this)">ABCD - Template Matching</a>
var template_li_submenu = "<li class=\x22submenu\x22 id=\x22li_${id}\x22><a class=\x22submenu\x22 id=\x22a_${id}\x22  onmouseout=\x22mouseOut(this)\x22 onmouseover=\x22showSubSubMenu(this)\x22>${displayName}</a>";
var idReplacementMarker = "${id}";
var displayNameReplacementMarker = "${displayName}";
var studyReplacementMarker = "${studyName}";
var studyDisplayReplacementMarker = "${studyDisplayName}";
var studyFolderReplacementMarker = "${studyFolderName}";
var dataTypesReplacementMarker = "${available_dataTypes}";
var networkDisplayNameReplacementMarker = "${networkDisplayName}";
var networkFolderNameReplacementMarker = "${networkFolderName}";
var fileNameReplacementMarker = "${fileName}";
var menuIdReplacementMarker = "${menuId}";
var surfaceVolumeTypeReplacementMarker = "{surface_volume}";


var networkIdReplacementMarker = "${networkId}";

// <ul class="subSubMenu" id="ul_ABCD">
var template_ul_subSubMenu = "<ul class=\x22subSubMenu\x22 id=\x22ul_${id}\x22>";

// <li class="subSubMenu" id="li_ABCD_Combined"><a class="subSubMenu" id="a_ABCD_Combined" data-study="abcd" href="#"  onmouseover="showSubSubMenu(this)" 
//                                                                            onmouseout="mouseOut(this)" onclick="menuClicked(this, false, true )">Combined Networks</a></li> 
                 
var template_li_subSubMenu = "<li class=\x22subSubMenu\x22><a class=\x22subSubMenu\x22 id=\x22${id}\x22 data-study=\x22${studyName}\x22 onmouseover=\x22showSubSubMenu(this)\x22" 
                                                                           + " onmouseout=\x22mouseOut(this)\x22 onclick=\x22menuClicked(this, false, true)\x22 data-surfaceVolumeType=\x22${surface_volume}\x22 data-networkId=\x22${networkId}\x22>${displayName}</a></li>";

var tag_endLI = "</li>";
var tag_endUL = "</ul>";
var newLine = "\n";

var template_beginMenuEntry = "MENU ENTRY (ID=${menuId})" + newLine;
var template_studyNameEntry = "${studyDisplayName} (${studyFolderName}) (${available_dataTypes})" + newLine;
var template_networkEntry = "${networkDisplayName} (${networkFolderName})" + newLine;
var template_endMenuEntry = "END MENU ENTRY" + newLine + newLine;


var template_li_zipImage =   "<li class=\x22zipList\x22>" + newLine
                  + "<figure class=\x22zipFigure\x22>" + newLine
                  + "<img src=\x22/NetworkProbabilityDownloader/images/zip_vice.jpg\x22 height=\x2265\x22 width=\x2265\x22>" + newLine
                  + "<figcaption class=\x22zipList\x22>${fileName}</figcaption>" + newLine
                  + "</figure>" + newLine
                  + "</li>";
 