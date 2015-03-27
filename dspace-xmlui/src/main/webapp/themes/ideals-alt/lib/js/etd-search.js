$(function() {initialize_etd_search()});

function initialize_etd_search() {
    var root = $("#aspect_artifactbrowser_CommunityViewer_div_community-search-browse");
    root.addClass("hidden");
    var hidden_message = "Advanced Search this community";
    var open_message = "Hide search form";
    var link = $("<a href='#'>" + hidden_message + "</a>")
    root.before(link);
    link.click(function() {
        root.toggleClass("hidden");
        link.text(root.hasClass("hidden") ? hidden_message : open_message);
    });
};
