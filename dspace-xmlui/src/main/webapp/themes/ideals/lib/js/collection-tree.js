$(function () {
    initialize_collection_tree()
});

function initialize_collection_tree() {
    var root = $("#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser");
    $("ul", root).first().addClass("mktree");
    $("a", root).each(function() {
        $(this).replaceAll($(this).closest('div.artifact-description'));
    });
    $("li", root).each(function () {
        var element = $(this);
        element.removeClass();
        element.prepend($("<span class='bullet'>&nbsp;</span>"))
        if ($("ul", element).length == 0) {
            element.addClass("liBullet");
            element.click(leafNodeOnClick);
        } else {
            element.addClass("liClosed");
            element.click(treeNodeOnClick);
        }
    })

}

function treeNodeOnClick(event) {
    if ($(event.target).is("a") || $(event.target).parent().is("a")) {
        return true;
    } else {
        $(this).toggleClass("liOpen liClosed");
        return false;
    }
}

function leafNodeOnClick(event) {
    if($(event.target).is("a") || $(event.target).parent().is("a")) {
        return true;
    } else {
        return false;
    }
}
