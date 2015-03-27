/**
 * Created by srobbins on 2/24/14.
 */
$(document).ready(function () {

    BitstreamRestrictionForm.setupDatepicker();
    BitstreamRestrictionForm.setupForm();
    BitstreamRestrictionForm.handleGroups();
    DefaultRestrictionForm.setupDatepicker();
    DefaultRestrictionForm.setupForm();
    DefaultRestrictionForm.radioHandler();

    //Todo refactor
    DefaultRestrictionForm.cancel_clicked = false;
    $("#aspect_administrative_item_RestrictItemForm_field_submit_cancel").click(function()
    {
        DefaultRestrictionForm.cancel_clicked = true;
    });



    $(DefaultRestrictionForm.formId).submit(function(){
        continueSubmission = true;
        $('div.form-group.error').remove()
        defaultDateValid = DefaultRestrictionForm.validateDefaultDateField();
        defaultReasonValid = DefaultRestrictionForm.validateDefaultReasonField();
        continueSubmission = defaultDateValid && defaultReasonValid;
        $('[id^= "'+ BitstreamRestrictionForm.bitstreamGroupsPrefix + '"]').each(function (){
            continueSubmit = BitstreamRestrictionForm.validateRow($(this));

            if (!continueSubmit)
            {
                continueSubmission=false;
            }
        });
        //make sure submission goes forward if cancel button is pressed
        if (DefaultRestrictionForm.cancel_clicked===true)
        {
            continueSubmission=true;
        }
        return continueSubmission;

    });

});
