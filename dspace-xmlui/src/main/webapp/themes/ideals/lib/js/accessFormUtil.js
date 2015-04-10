/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
$(function() {

    $("#aspect_submission_StepTransformer_field_embargo_until_date").datepicker({dateFormat: 'yy-mm-dd',
        minDate: +1,
        changeYear: true,
        defaultDate: "+2y",
        showOn: "focus"
//        onClose: function () {
//            $("#aspect_submission_StepTransformer_field_embargo_until_date").trigger('focusout')
//        }
    });
//    $("#aspect_submission_StepTransformer_field_embargo_until_date").datepicker({dateFormat: 'yy-mm-dd'});
//
//    $( "#aspect_submission_StepTransformer_field_embargo_until_date").datepicker();
//    $("#aspect_submission_StepTransformer_field_embargo_until_date").
    $(function() {
        $( "#aspect_submission_StepTransformer_field_embargo_until_date").datepicker();
        initAccessSubmissionForm();
    });


    $('input[name|="ideals_radios"]').change(function(){
        // Visible
        if ($('input[name|="ideals_radios"]:checked').val() === 'Open Access'){
            disableFields();
        }
        // Embargoed
        else if ($('input[name|="ideals_radios"]:checked').val() != 'Open Access'){
            enableFields()
        }

    });


    function initAccessSubmissionForm() {
        if ($('input[name|="ideals_radios"]').length >0){
            if ($('input[name|="ideals_radios"]:checked').val() == 'Open Access'){
                disableFields();
            }
            // Embargoed
            else if ($('input[name|="ideals_radios"]:checked').val() != 'Open Access'){
                enableFields()
            }
        }
    }

    function enableFields() {
        $("#aspect_submission_StepTransformer_field_reason").removeAttr("disabled");
        $("#aspect_submission_StepTransformer_field_embargo_until_date").removeAttr("disabled");

    }

    function disableFields() {
        $("#aspect_submission_StepTransformer_field_reason").attr("disabled", "disabled");
        $("#aspect_submission_StepTransformer_field_embargo_until_date").attr("disabled", "disabled");
    }
});
