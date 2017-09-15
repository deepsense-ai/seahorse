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
