$(document).ready(function () {

    $('[id^="aspect_submission_StepTransformer_field_restrict_until_date_"]').datepicker({dateFormat: 'yy-mm-dd',
        minDate: +1,
        changeYear: true,
        defaultDate: "+2y",
        showOn: "focus"
    });
    $(function () {
        $('[id^="aspect_submission_StepTransformer_field_restrict_until_date_"]').datepicker();
        initBitstreamRestrictionForm()
    });

    $('[id^="aspect_submission_StepTransformer_field_groups_"]').change(function () {
        processRowRestrictionSettings($(this));
    });


    function initBitstreamRestrictionForm() {
        $('[id^="aspect_submission_StepTransformer_field_groups_"]').each(function () {
            processRowRestrictionSettings($(this));
        })
    }

    function processRowRestrictionSettings(selection) {
        // Visible
        var idArray = selection.attr("id").split("_")
        var ordinal = idArray[idArray.length - 1]
        var dateId = "aspect_submission_StepTransformer_field_restrict_until_date_" + ordinal
        var reasonId = "aspect_submission_StepTransformer_field_reason_" + ordinal
        if (selection.val() === "Open") {
            $('[id=' + dateId + ']').attr("readonly", "readonly");
            $('[id=' + reasonId + ']').attr("readonly", "readonly");
            $('[id=' + dateId + ']').val("");
            $('[id=' + reasonId + ']').val("");

        }
        //Embargoed
        else if (selection.val() === 'Embargoed') {
            $('[id=' + dateId + ']').attr("readonly", "readonly");
            $('[id=' + reasonId + ']').attr("readonly", "readonly");
        }
        //Restricted
        else {
            $('[id=' + dateId + ']').removeAttr("readonly");
            $('[id=' + reasonId + ']').removeAttr("readonly");
        }

    }

    $("#aspect_submission_StepTransformer_div_submit-upload").submit(function () {
        continueSubmission = true;
        $('div.form-group.error').remove()
        $('[id^="aspect_submission_StepTransformer_field_groups_"]').each(function () {
            continueSubmit = validateRow($(this));

            if (!continueSubmit) {
                continueSubmission = false;
            }
        });
//       $("input.error").change(function () {
//           validateRow($(this))
//       });
        return continueSubmission;

    });

    function validateRow(selection) {
        $(this).children('div.form-group.error').remove();
        var idArray = selection.attr("id").split("_");
        var ordinal = idArray[idArray.length - 1];
        var dateId = "#aspect_submission_StepTransformer_field_restrict_until_date_" + ordinal;
        var reasonId = "#aspect_submission_StepTransformer_field_reason_" + ordinal;
        dateValid = validateDateField(selection, dateId)
        reasonValid = validateReasonField(selection, reasonId)
        if (dateValid && reasonValid) {
            return true;
        }
        return false;
    }

    function validateReasonField(selection, reasonId) {
        if (!reasonIsOk(selection, reasonId)) {
            activateErrorBorder($(reasonId));
            $(reasonId).addClass("error");
            addErrorMessage($(reasonId))
            return false;
        } else {
            activateValidBorder($(reasonId));
            $(reasonId).removeClass("error");
            return true;
        }
    }

    function validateDateField(selection, dateId) {
        if (!dateIsOK(selection, dateId)) {
            activateErrorBorder($(dateId));
            $(dateId).addClass("error");
            addErrorMessage($(dateId))
            return false;
        } else {
            activateValidBorder($(dateId));
            $(dateId).removeClass("error");

            return true;
        }
    }

    function addErrorMessage(element) {
        elementId = element.attr('id')
        if (elementId.match(/date_[0-9]+$/)) {
            element.parents('div.form-group').after('<div class="form-group error ' + getErrorClassString(elementId) + '"><span class="error" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* Date is required for full embargo and limited access</span></div>');
        }
        if (elementId.match(/reason_[0-9]+$/)) {
            element.parents('div.form-group').after('<div class="form-group error ' + getErrorClassString(elementId) + '"><span class="error" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* A reason is required for all access restrictions</span></div>');
        }

    }

    function activateErrorBorder(element) {
        element.css("border-color", "#C22121");
    }

    function activateValidBorder(element) {
        element.css("border-color", "#CCC");
    }

    function reasonCanBeEmpty(selection) {
        return (selection.val() === 'Open');
    }

    function dateCanBeEmpty(selection) {
        return (selection.val() === 'Open')
            || (selection.val() === 'U of I Only')
            || (selection.val() === 'Limited');
    }


    function dateIsValid(dateId) {
        var regexp = new RegExp('([0-9]{4})|(([0-9]{4})-([0-9]{2}))|(([0-9]{4})-([0-9]{2})-([0-9]{2}))');
        return $(dateId).val().match(regexp);
    }

    function dateIsEmpty(dateId) {
        return $(dateId).val().trim() === '';
    }

    function reasonIsEmpty(reasonId) {
        return $(reasonId).val().trim() === '';
    }

    function dateIsOK(selection, dateId) {
        if (dateCanBeEmpty(selection)) {
            return dateIsEmpty(dateId) || dateIsValid(dateId);
        }
        return ((!dateIsEmpty(dateId)) && dateIsValid(dateId));
    }

    function reasonIsOk(selection, reasonId) {
        if (!reasonCanBeEmpty(selection)) {
            return !reasonIsEmpty(reasonId);
        }
        return true;
    }

    function getErrorClassString(id) {
        idArray = id.split("_")
        return idArray[idArray.length - 2] + "_" + idArray[idArray.length - 1];
    }

});

//I'd be happy to do this on the Java side, but I can't figure out how to get UploadWithEmbargoStep.java to see
//a different value of T_Next
$(function () {
    var moreButton = $("#aspect_submission_StepTransformer_div_submit-upload input[name='submit_upload']");
    if (moreButton.length > 0) {
        var nextButton = $("#aspect_submission_StepTransformer_div_submit-upload input[name='submit_next']");
        //The form needs nextButton's val to be the original val when it submits, so arrange for this, then
        //change it to the text we actually want.
        var oldNextVal = nextButton.val();
        $('form').submit(function() {
            nextButton.val(oldNextVal);
            return true;
        });
        nextButton.val("Upload file & proceed >")
        //This is a bit roundabout, but the easiest way, I think, is to move the more button to the right relative
        //place and then move them all up into the div where the more button originally lived
        var moreParent = moreButton.parent();
        var nextParent = nextButton.parent();
        nextButton.before(moreButton);
        moreParent.append(nextParent.children());
    }
});
