/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
  // Register listener on search field
  jQuery('#search').on('input propertychange paste', function() {
    var query = $('#search').val().trim().toLowerCase();
    filterMenuList(query);
  });
});

function filterMenuList(query) {
  // Hide all the links from the menu
  $(".sidemenu li a").hide();
  // Find all the links in the menu, that are not menu groups
  $(".sidemenu li:not(.menu-group) a").filter(function (index) {
    // If the link text contains search query return `true`
    // and show all parents
    if ($(this).text().toLowerCase().indexOf(query) >= 0) {
      showParentLinks($(this));
      return true;
    }
    return false;
  }).css("display", "block");
};

function showParentLinks(link) {
  link.parents(".menu-group").each(function(index) {
    $(this).find(">a").css("display", "block");
  });
}
