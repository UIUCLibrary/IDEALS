$(function () {
    submissions_datatable('#aspect_submission_Submissions_table_completed-submissions', false);
    submissions_datatable('#aspect_submission_Submissions_table_unfinished-submissions', true);
    submissions_datatable('#aspect_workflow_Submissions_table_submissions-inprogress', false);
})

function submissions_datatable(id, make_footer) {
    //Preprocess to get table into form that datatables expects
    var table = $(id);
    var table_head = $('<thead></thead>');
    var table_body = $('tbody', table);
    table.prepend(table_head);
    table_head.prepend($('tr', table_body).first());
    //At least one of these tables has a button that needs to be preserved in the last row
    if (make_footer) {
        var table_foot = $('<tfoot></tfoot>')
        table_foot.prepend($('tr', table_body).last());
        table.append(table_foot);
    }
    //Make into datatable
    table.dataTable();
}

