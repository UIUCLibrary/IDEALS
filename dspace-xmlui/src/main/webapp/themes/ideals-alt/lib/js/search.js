//Move where the actual query is shown for search, advanced search, filter search
$(function () {
    $('#aspect_artifactbrowser_AdvancedSearch_div_search-results').before(
        $('#aspect_artifactbrowser_AdvancedSearch_p_result-query'));
    $('#aspect_artifactbrowser_SimpleSearch_div_search-results').before(
            $('#aspect_artifactbrowser_SimpleSearch_p_result-query'));
    $('#edu_uiuc_dspace_filtersearch_FilterSearch_div_search-results').before(
        $('#edu_uiuc_dspace_filtersearch_FilterSearch_p_result-query'));
})