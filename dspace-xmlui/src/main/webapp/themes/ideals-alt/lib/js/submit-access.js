/**
 * Created by srobbins on 12/20/13.
 */
$(document).ready(function () {

    $("#aspect_submission_StepTransformer_field_embargo_until_date").datepicker({dateFormat: 'yy-mm-dd',
        minDate: +1,
        changeYear: true,
        defaultDate: "+2y",
        showOn: "focus"
    });

    $(function () {
        $("#aspect_submission_StepTransformer_field_embargo_until_date").datepicker();
        initAccessSubmissionForm();
    });


    $('input[name|="ideals_radios"]').change(function () {
        // Visible
        if ($('input[name|="ideals_radios"]:checked').val() === 'Open') {
            disableFields();
        }
        // Embargoed
        else if ($('input[name|="ideals_radios"]:checked').val() !== 'Open') {
            enableFields()
        }
        if  ($('input[name|="ideals_radios"]:checked').val() === 'Limited'){
            $('input[name|="full_embargo"]').removeAttr("disabled");
            $('#limited_checkbox label').removeClass("ui-state-disabled");

        }
        else{
            $('input[name|="full_embargo"]').prop('checked', false);
            $('input[name|="full_embargo"]').attr("disabled", "disabled");
            $('#limited_checkbox label').addClass('ui-state-disabled');
        }


    });

    $('#aspect_submission_StepTransformer_div_submit-restrict').submit(function (){
        $('span.error').remove();

        dateValid = validateDateField();
        reasonValid = validateReasonField();
        //return false prevents submission
        return dateValid && reasonValid;
    });

    function initAccessSubmissionForm() {
        if ($('input[name|="ideals_radios"]').length >0){
            if ($('input[name|="ideals_radios"]:checked').val() === 'Open'){
                disableFields();
            }
            // Embargoed
            else if ($('input[name|="ideals_radios"]:checked').val() !== 'Open'){
                enableFields()
            }
            if  ($('input[name|="ideals_radios"]:checked').val() === 'Limited'){
                $('input[name|="full_embargo"]').removeAttr("disabled")
                $('#limited_checkbox label').removeClass("ui-state-disabled");
            }
            else{
                $('input[name|="full_embargo"]').attr("disabled", true);
                $('#limited_checkbox label').addClass('ui-state-disabled');
            }

        }
    }

    function validateReasonField()
    {
        if (!reasonIsOk())
        {
            activateErrorBorder($("#aspect_submission_StepTransformer_field_reason"));
            $("#aspect_submission_StepTransformer_field_reason").addClass("error");
            addErrorMessage($("#aspect_submission_StepTransformer_field_reason"))
            return false;
        }else
        {
            activateValidBorder($("#aspect_submission_StepTransformer_field_reason"));
            $("#aspect_submission_StepTransformer_field_reason").removeClass("error");
            $('span.error.reason').remove();
            return true;
        }
    }

    function validateDateField()
    {
        if (!dateIsOK())
        {
            activateErrorBorder($("#aspect_submission_StepTransformer_field_embargo_until_date"));
            $("#aspect_submission_StepTransformer_field_embargo_until_date").addClass("error");
            addErrorMessage($("#aspect_submission_StepTransformer_field_embargo_until_date"))
            return false;
        }else
        {
            activateValidBorder($("#aspect_submission_StepTransformer_field_embargo_until_date"));
            $("#aspect_submission_StepTransformer_field_embargo_until_date").removeClass("error");
            $('span.error.date').remove();

            return true;
        }
    }

    function addErrorMessage(element){
        if (element.attr('id').match(/date$/)){
            element.after('<span class="error date" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* Date is required for full embargo and limited access</span>');
        }
        if (element.attr('id').match(/reason$/)){
            element.after('<span class="error reason" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* A reason is required for all access restrictions</span>');
        }

    }
    function activateErrorBorder(element)
    {
        element.css("border-color", "#C22121");
    }

    function activateValidBorder(element)
    {
        element.css("border-color", "#CCC");
    }

    function enableFields() {
        $("#aspect_submission_StepTransformer_field_reason").removeAttr("disabled");
        $("#aspect_submission_StepTransformer_field_embargo_until_date").removeAttr("disabled");

    }

    function disableFields() {
        $("#aspect_submission_StepTransformer_field_reason").attr("disabled", "disabled");
        $("#aspect_submission_StepTransformer_field_reason").val("");
        $("#aspect_submission_StepTransformer_field_embargo_until_date").attr("disabled", "disabled");
        $("#aspect_submission_StepTransformer_field_embargo_until_date").val("");
    }

    function reasonCanBeEmpty(){
        return ($('input[name|="ideals_radios"]:checked').val() === 'Open');
    }

    function dateCanBeEmpty(){
        return !$('input[name|="full_embargo"]')[0].checked;
    }

    function dateIsValid(){
        var regexp = new RegExp('([0-9]{4})|(([0-9]{4})-([0-9]{2}))|(([0-9]{4})-([0-9]{2})-([0-9]{2}))');
        return $("#aspect_submission_StepTransformer_field_embargo_until_date").val().match(regexp);
    }

    function dateIsEmpty(){
        return $("#aspect_submission_StepTransformer_field_embargo_until_date").val().trim() === '';
    }

    function reasonIsEmpty(){
        return $("#aspect_submission_StepTransformer_field_reason").val().trim() === '';
    }

    function dateIsOK(){
        if (dateCanBeEmpty())
        {
            return dateIsEmpty()||dateIsValid();
        }
        return ((!dateIsEmpty())&&dateIsValid());
    }

    function reasonIsOk(){
        if (!reasonCanBeEmpty()){
            return !reasonIsEmpty();
        }
        return true;
    }

});




