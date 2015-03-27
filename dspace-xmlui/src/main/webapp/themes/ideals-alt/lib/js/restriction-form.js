/**
 * Created by srobbins on 4/7/14.
 */
var Constants =
    {openConst: "Open",
    UofIOnlyConst: "U of I Only",
    limitedConst: "Limited",
    embargoConst: "Embargoed"}

var DefaultRestrictionForm = {
    defaultDateId: "#aspect_administrative_item_RestrictItemForm_field_embargo_until_date",
    defaultReasonId: "#aspect_administrative_item_RestrictItemForm_field_reason",
    formId: "#aspect_administrative_item_RestrictItemForm_div_restrict-item",




    consoleTest: function() {console.log("this is in the object")},
    setupDatepicker: function(){
    defaultDateId = this.defaultDateId;
    $(defaultDateId).datepicker({dateFormat: 'yy-mm-dd',
        minDate: +1,
        changeYear: true,
        defaultDate: "+2y",
        showOn: "focus"
    })},

    setupForm: function () {
        $(this.defaultDateId).datepicker();
        DefaultRestrictionForm.initDefaultAccessSubmissionForm()
    },


    radioHandler: function () {
        $('input[name|="ideals_radios"]').change(function () {
        // Visible
        if ($('input[name|="ideals_radios"]:checked').val() === Constants.openConst) {
            DefaultRestrictionForm.disableDefaultFields();
        }
        // Embargoed
        else if ($('input[name|="ideals_radios"]:checked').val() !== Constants.openConst) {
            DefaultRestrictionForm.enableDefaultFields();
        }
        if  ($('input[name|="ideals_radios"]:checked').val() === Constants.limitedConst){
            $('input[name|="full_embargo"]').removeAttr("disabled");
            $('#limited_checkbox label').removeClass("ui-state-disabled");

        }
        else{
            $('input[name|="full_embargo"]').prop('checked', false);
            $('input[name|="full_embargo"]').attr("disabled", "disabled");
            $('#limited_checkbox label').addClass('ui-state-disabled');
        }

    })},

    submissionHandler: function() {
    formId = this.formId;
    $(formId).submit(function(){
        continueSubmission = true;
        $('div.form-group.error').remove()
        defaultDateValid = DefaultRestrictionForm.validateDefaultDateField();
        defaultReasonValid = DefaultRestrictionForm.validateDefaultReasonField();
        continueSubmission = defaultDateValid && defaultReasonValid;
        return continueSubmission;

    })},

    initDefaultAccessSubmissionForm: function(){
        if ($('input[name|="ideals_radios"]').length >0){
            if ($('input[name|="ideals_radios"]:checked').val() === Constants.openConst){
                DefaultRestrictionForm.disableDefaultFields();
            }
            // Embargoed
            else if ($('input[name|="ideals_radios"]:checked').val() !== Constants.openConst){
                DefaultRestrictionForm.enableDefaultFields()
            }

        }
    },

    validateDefaultReasonField: function()
    {
        if (!DefaultRestrictionForm.defaultReasonIsOk())
        {
            DefaultRestrictionForm.activateErrorBorder($(this.defaultReasonId));
            $(this.defaultReasonId).addClass("error");
            DefaultRestrictionForm.addDefaultErrorMessage($(this.defaultReasonId))
            return false;
        }
        else
        {
            DefaultRestrictionForm.activateValidBorder($(this.defaultReasonId));
            $(this.defaultReasonId).removeClass("error");
            return true;
        }
    },

    validateDefaultDateField: function()
    {
        if (!DefaultRestrictionForm.defaultDateIsOK())
        {
            console.log("date not ok")
            DefaultRestrictionForm.activateErrorBorder($(this.defaultDateId));
            $(this.defaultDateId).addClass("error");
            DefaultRestrictionForm.addDefaultErrorMessage($(this.defaultDateId))
            return false;
        }else
        {
            DefaultRestrictionForm.activateValidBorder($(this.defaultDateId));
            $(this.defaultDateId).removeClass("error");
            return true;
        }
    },

    enableDefaultFields: function() {
        $(this.defaultReasonId).removeAttr("disabled");
        $(this.defaultDateId).removeAttr("disabled");

    },

    disableDefaultFields: function() {
        $(this.defaultReasonId).attr("disabled", "disabled");
        $(this.defaultReasonId).val("");
        $(this.defaultDateId).attr("disabled", "disabled");
        $(this.defaultDateId).val("");
    },

    defaultReasonCanBeEmpty: function(){
        return ($('input[name|="ideals_radios"]:checked').val() === Constants.openConst);
    },

    defaultDateCanBeEmpty: function(){
        return !$('input[name|="full_embargo"]')[0].checked;
    },

    defaultDateIsValid: function(){
        var regexp = new RegExp('([0-9]{4})|(([0-9]{4})-([0-9]{2}))|(([0-9]{4})-([0-9]{2})-([0-9]{2}))');
        return $(this.defaultDateId).val().match(regexp);
    },

    defaultDateIsEmpty: function(){
        return $(this.defaultDateId).val().trim() === '';
    },

    defaultReasonIsEmpty: function(){
        return $(this.defaultReasonId).val().trim() === '';
    },

    defaultDateIsOK: function(){
        if (DefaultRestrictionForm.defaultDateCanBeEmpty())
        {
            return DefaultRestrictionForm.defaultDateIsEmpty()||DefaultRestrictionForm.defaultDateIsValid();
        }
        return ((!DefaultRestrictionForm.defaultDateIsEmpty())&&DefaultRestrictionForm.defaultDateIsValid());
    },

    defaultReasonIsOk: function(){
        if (!DefaultRestrictionForm.defaultReasonCanBeEmpty()){
            return !DefaultRestrictionForm.defaultReasonIsEmpty();
        }
        return true;
    },

    activateErrorBorder: function (element) {
        element.css("border-color", "#C22121");
    },

    activateValidBorder: function (element) {
        element.css("border-color", "#CCC");
    },

    addDefaultErrorMessage: function(element){
        if (element.attr('id').match(/date$/)){
            element.after('<div class="form-group error"><span class="error date" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* Date is required for full embargo and limited access</span></div>');
        }
        if (element.attr('id').match(/reason$/)){
            element.after('<div class="form-group error"><span class="error reason" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* A reason is required for all access restrictions</span></div>');
        }

    }
}
var BitstreamRestrictionForm = {
    bitstreamDatePrefix: "aspect_administrative_item_RestrictItemForm_field_restrict_until_date_",
    bitstreamReasonPrefix: "aspect_administrative_item_RestrictItemForm_field_reason_",
    formId: "#aspect_administrative_item_RestrictItemForm_div_restrict-item",
    bitstreamGroupsPrefix: "aspect_administrative_item_RestrictItemForm_field_groups_",
    setupDatepicker: function() {
        bitstreamDatePrefix = this.bitstreamDatePrefix;
        $('[id^=' + bitstreamDatePrefix + ']').datepicker({dateFormat: 'yy-mm-dd',
        minDate: +1,
        changeYear: true,
        defaultDate: "+2y",
        showOn: "focus"
    })},
    setupForm: function () {
        $('[id^="' + this.bitstreamDatePrefix + '"]').datepicker();
        BitstreamRestrictionForm.initBitstreamRestrictionForm();
    },

    handleGroups: function () {$('[id^="' + this.bitstreamGroupsPrefix + '"]').change(function () {
        BitstreamRestrictionForm.processRowRestrictionSettings($(this));
    })},


    initBitstreamRestrictionForm: function () {
        $('[id^="' + this.bitstreamGroupsPrefix + '"]').each(function () {
            BitstreamRestrictionForm.processRowRestrictionSettings($(this));
        })
    },

    processRowRestrictionSettings: function (selection) {
        // Visible
        var idArray = selection.attr("id").split("_")
        var ordinal = idArray[idArray.length - 1]
        var dateId = this.bitstreamDatePrefix + ordinal
        var reasonId = this.bitstreamReasonPrefix + ordinal
        if (selection.val() === Constants.openConst) {
            $('[id=' + dateId + ']').attr("disabled", "disabled");
            $('[id=' + reasonId + ']').attr("disabled", "disabled");
            $('[id=' + dateId + ']').val("");
            $('[id=' + reasonId + ']').val("");

        }
        //Embargoed
        else if (selection.val() === Constants.embargoConst) {
            $('[id=' + dateId + ']').attr("readonly", "readonly");
            $('[id=' + reasonId + ']').attr("readonly", "readonly");
        }
        //Restricted
        else {
            $('[id=' + dateId + ']').removeAttr("disabled");
            $('[id=' + dateId + ']').removeAttr("readonly");
            $('[id=' + reasonId + ']').removeAttr("disabled");
            $('[id=' + reasonId + ']').removeAttr("readonly");
        }

    },

    submissionHandler: function()
    {   formId = this.formId;
        bitstreamGroupsPrefix = this.bitstreamGroupsPrefix;
        $(formId).submit(function () {
            continueSubmission = true;
            $('div.form-group.error').remove();
            $('[id^= "' + bitstreamGroupsPrefix + '"]').each(function () {
                continueSubmit = BitstreamRestrictionForm.validateRow($(this));

                if (!continueSubmit) {
                    continueSubmission = false;
                }
            });
            return continueSubmission;

        })},

    validateRow: function (selection) {
        selection.children('div.form-group.error').remove();
        var idArray = selection.attr("id").split("_");
        var ordinal = idArray[idArray.length - 1];
        var dateId = "#" + this.bitstreamDatePrefix + ordinal;
        var reasonId = "#" + this.bitstreamReasonPrefix + ordinal;
        dateValid = BitstreamRestrictionForm.validateDateField(selection, dateId)
        reasonValid = BitstreamRestrictionForm.validateReasonField(selection, reasonId)
        if (dateValid && reasonValid) {
            return true;
        }
        return false;
    },

    validateReasonField: function (selection, reasonId) {
        if (!BitstreamRestrictionForm.reasonIsOk(selection, reasonId)) {
            BitstreamRestrictionForm.activateErrorBorder($(reasonId));
            $(reasonId).addClass("error");
            BitstreamRestrictionForm.addErrorMessage($(reasonId))
            return false;
        } else {
            BitstreamRestrictionForm.activateValidBorder($(reasonId));
            $(reasonId).removeClass("error");
            return true;
        }
    },

    validateDateField: function (selection, dateId) {
        if (!BitstreamRestrictionForm.dateIsOK(selection, dateId)) {
            BitstreamRestrictionForm.activateErrorBorder($(dateId));
            $(dateId).addClass("error");
            BitstreamRestrictionForm.addErrorMessage($(dateId))
            return false;
        } else {
            BitstreamRestrictionForm.activateValidBorder($(dateId));
            $(dateId).removeClass("error");

            return true;
        }
    },

    addErrorMessage: function (element) {
        elementId = element.attr('id')
        if (elementId.match(/date_[0-9]+$/)) {
            element.parents('div.form-group').after('<div class="form-group error ' + BitstreamRestrictionForm.getErrorClassString(elementId) + '"><span class="error" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* Date is required for full embargo and limited access</span></div>');
        }
        if (elementId.match(/reason_[0-9]+$/)) {
            element.parents('div.form-group').after('<div class="form-group error ' + BitstreamRestrictionForm.getErrorClassString(elementId) + '"><span class="error" xmlns:mets="http://www.loc.gov/METS/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:mods="http://www.loc.gov/mods/v3">* A reason is required for all access restrictions</span></div>');
        }

    },

    activateErrorBorder: function (element) {
        element.css("border-color", "#C22121");
    },

    activateValidBorder: function (element) {
        element.css("border-color", "#CCC");
    },

    reasonCanBeEmpty: function (selection) {
        return (selection.val() === Constants.openConst );
    },

    dateCanBeEmpty: function (selection) {
        return (selection.val() === Constants.openConst)
            || (selection.val() === Constants.UofIOnlyConst)
            || (selection.val() === Constants.limitedConst);
    },


    dateIsValid: function (dateId) {
        var regexp = new RegExp('([0-9]{4})|(([0-9]{4})-([0-9]{2}))|(([0-9]{4})-([0-9]{2})-([0-9]{2}))');
        return $(dateId).val().match(regexp);
    },

    dateIsEmpty: function (dateId) {
        return $(dateId).val().trim() === '';
    },

    reasonIsEmpty: function (reasonId) {
        return $(reasonId).val().trim() === '';
    },

    dateIsOK: function (selection, dateId) {
        if (BitstreamRestrictionForm.dateCanBeEmpty(selection)) {
            return BitstreamRestrictionForm.dateIsEmpty(dateId) || BitstreamRestrictionForm.dateIsValid(dateId);
        }
        return ((!BitstreamRestrictionForm.dateIsEmpty(dateId)) && BitstreamRestrictionForm.dateIsValid(dateId));
    },
    reasonIsOk: function (selection, reasonId) {
        if (!BitstreamRestrictionForm.reasonCanBeEmpty(selection)) {
            return !BitstreamRestrictionForm.reasonIsEmpty(reasonId);
        }
        return true;
    },

    getErrorClassString: function (id) {
        idArray = id.split("_")
        return idArray[idArray.length - 2] + "_" + idArray[idArray.length - 1];

    }
};
