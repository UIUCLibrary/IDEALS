/**
 * Created by srobbins on 3/19/14.
 */
$(document).ready(function () {

        var defaultDateId = "#restriction-date";
        var defaultReasonId = "#restriction-reason";
        var formId = "#aspect_submission_StepTransformer_div_submit-edit-file";
        var groupsPrefix = "aspect_submission_StepTransformer_field_groups_";
        var openConst = "Open";
        var UofIOnlyConst  = "U of I Only";
        var limitedConst = "Limited";
        var embargoConst = "Embargoed";

        $(formId).submit(function(){
            continueSubmission = true;
            $('div.form-group.error').remove()
            defaultDateValid = validateDefaultDateField();
            defaultReasonValid = validateDefaultReasonField();
            continueSubmission = defaultDateValid && defaultReasonValid;

            return continueSubmission;

        });


        function activateErrorBorder(element)
        {
            element.css("border-color", "#C22121");
        }

        function activateValidBorder(element)
        {
            element.css("border-color", "#CCC");
        }




        $(defaultDateId).datepicker({dateFormat: 'yy-mm-dd',
            minDate: +1,
            changeYear: true,
            defaultDate: "+2y",
            showOn: "focus"
        });

        $(function () {
            $(defaultDateId).datepicker();
            initDefaultAccessSubmissionForm();
        });


        $('[id^='+groupsPrefix+']').change(function () {
            // Visible
            if ($('[id^='+groupsPrefix+']').val() === openConst) {
                disableDefaultFields();
            }
            // Embargoed
            else if ($('[id^='+groupsPrefix+']').val() !== openConst) {
                enableDefaultFields()
            }


        });

        function initDefaultAccessSubmissionForm() {
            if ($('[id^='+groupsPrefix+']').val() === openConst){
                disableDefaultFields();
            }

            else if ($('[id^='+groupsPrefix+']').val()===embargoConst){
                $(defaultDateId).attr("readonly", "readonly");
                $(defaultReasonId).attr("readonly", "readonly");
            }
            else{
                enableDefaultFields()
            }


        }

        function validateDefaultReasonField()
        {
            if (!defaultReasonIsOk())
            {
                activateErrorBorder($(defaultReasonId));
                $(defaultReasonId).addClass("error");
                addDefaultErrorMessage($(defaultReasonId))
                return false;
            }else
            {
                activateValidBorder($(defaultReasonId));
                $(defaultReasonId).removeClass("error");
                return true;
            }
        }

        function validateDefaultDateField()
        {
            if (!defaultDateIsOK())
            {
                activateErrorBorder($(defaultDateId));
                $(defaultDateId).addClass("error");
                addDefaultErrorMessage($(defaultDateId))
                return false;
            }else
            {
                activateValidBorder($(defaultDateId));
                $(defaultDateId).removeClass("error");
                return true;
            }
        }

        function enableDefaultFields() {
            $(defaultReasonId).removeAttr("disabled");
            $(defaultDateId).removeAttr("disabled");

        }

        function disableDefaultFields() {
            $(defaultReasonId).attr("disabled", "disabled");
            $(defaultReasonId).val("");
            $(defaultDateId).attr("disabled", "disabled");
            $(defaultDateId).val("");
        }

        function defaultReasonCanBeEmpty(){
            return ($('[id^='+groupsPrefix+']').val() === openConst);
        }

        function defaultDateCanBeEmpty(){
            return (($('[id^='+groupsPrefix+']').val() === openConst)||(
                ($('[id^='+groupsPrefix+']').val() === UofIOnlyConst))||
                ($('[id^='+groupsPrefix+']').val() === limitedConst));
        }

        function defaultDateIsValid(){
            var regexp = new RegExp('([0-9]{4})|(([0-9]{4})-([0-9]{2}))|(([0-9]{4})-([0-9]{2})-([0-9]{2}))');
            return $(defaultDateId).val().match(regexp);
        }

        function defaultDateIsEmpty(){
            return $(defaultDateId).val().trim() === '';
        }

        function defaultReasonIsEmpty(){
            return $(defaultReasonId).val().trim() === '';
        }

        function defaultDateIsOK(){
            if (defaultDateCanBeEmpty())
            {
                return defaultDateIsEmpty()||defaultDateIsValid();
            }
            return ((!defaultDateIsEmpty())&&defaulDateIsValid());
        }

        function defaultReasonIsOk(){
            if (!defaultReasonCanBeEmpty()){
                return !defaultReasonIsEmpty();
            }
            return true;
        }

        function addDefaultErrorMessage(element){
            if (element.attr('id').match(/date$/)){
                element.after('<div class="form-group error"><span class="error date" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* Date is required for full embargo and limited access</span></div>');
            }
            if (element.attr('id').match(/reason$/)){
                element.after('<div class="form-group error"><span class="error reason" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* A reason of is required for all access restrictions</span></div>');
            }

        }

    });
