var submit_form_describe_organizer = {

    //Fields that allow simple inline actions
    js_simple_inline_fields: $(["dc_title", "dc_subject", "dc_creator", "dc_contributor",
        "dc_relation_ispartof", "dc_rights", "dc_description_sponsorship", "dc_contributor_committeeChair",
        "dc_contributor_advisor", "dc_contributor_committeeMember"]),

    //Fields that support semicolon delimited values - this has to be done both when the "Add more" button is
    //clicked and before the form is submitted
    js_semicolon_delimited_fields: $(["dc_subject", "dc_creator", "dc_contributor"]),

    //Change the original output form into one that is javascript enabled and reformatted.
    reorganize_form: function () {
        //Find ol containing form elements
        var field_list = $("#aspect_submission_StepTransformer_list_submit-describe ol");
        //Nothing to do if not found
        if (field_list.length == 0)
            return;
        //this.accordionize_form(field_list);
        this.move_help();
        for (var i = 0; i < this.js_simple_inline_fields.length; i++) {
            this.ensure_previous_value_div(this.js_simple_inline_fields[i]);
            this.enable_js_editing(this.js_simple_inline_fields[i]);
            this.enable_inline_actions(this.js_simple_inline_fields[i]);
            this.enable_inline_addition(this.js_simple_inline_fields[i]);
        }
        //"dc_identifier_value" has to be handled separately, but in a similar way
        this.ensure_identifier_previous_value_div();
        this.enable_identifier_js_editing();
        this.enable_identifier_inline_actions();
        this.enable_identifier_inline_addition();
        //static content
        this.split_page_at_degree_level();
        this.add_padding_to_non_repeatables();
        this.remove_breaks_from_year_and_day_field();
        //don't submit the form unless required fields are present
        this.check_form_before_submission(field_list.closest('form'));
        this.fix_date_label();
    },

    fix_date_label: function () {
        $('label[for="aspect_submission_StepTransformer_field_dc_date_issued"] span.required').text(' (Year Required)')
    },

    //Intercept form submission to check that required fields are filled in
    //Handle any values in semicolon separated fields
    check_form_before_submission: function (form) {
        form.submit(function (e) {
            var errors = [];
            var fields = form.serializeArray();
            var required_prefixes = {'dc_title': 'Title', 'dc_date_issued_year': 'Date of Publication',
                'dc_subject': 'Subject Keywords', 'dc_type': 'Type of Resource'};
            //Check for dc_title, dc_date_issued_year, dc_subject, dc_type. Must find at least one with that prefix
            //(because they may be in 'previous value' fields - see regexp for exactly how we sort that out
            // while avoiding dc_type_genre) and with non-blank trimmed value.
            for (var prefix in required_prefixes) {
                var valid = false;
                var regexp = new RegExp('^' + prefix + '(_\\d+)?$');
                for (var index = 0; index < fields.length; index++) {
                    var field = fields[index];
                    if (!(field['name']).match(regexp) || field['value'].trim() === '')
                        continue;
                    valid = true;
                    break;
                }
                var input = $("input[name='" + prefix + "']");
                var parent = input.closest('li');
                $('label.ds-form-label', parent).addClass('control-label');
                if (valid) {
                    parent.removeClass('has-error');
                } else {
                    parent.addClass('has-error');
                    errors.push(required_prefixes[prefix]);
                }
            }
            if (errors.length == 0) {
                submit_form_describe_organizer.handle_semicolon_fields();
                return true;
            } else {
                e.preventDefault();
                alert('Some required fields missing: ' + errors.join());
            }
        });
    },

    //take any values currently in semicolon delimited fields and separate them
    handle_semicolon_fields: function() {
        submit_form_describe_organizer.js_semicolon_delimited_fields.each(function(i, prefix) {
            submit_form_describe_organizer.handle_semicolon_field(prefix);
        });
    },

    split_page_at_degree_level: function() {
        $("#aspect_submission_StepTransformer_field_thesis_degree_name").parents("li").attr("id", "page_break");
        $("#aspect_submission_StepTransformer_field_thesis_degree_name").parents("li").prepend('<h2>Dissertation/Thesis Specific Fields</h2>')
    },

    add_padding_to_non_repeatables: function() {
        $('div.ds-form-content').each(function(i){
            if ($(this).find('*').has('ds-add-button').length==0){
                $(this).append('<div class="ds-previous-values"></div>')
            }
        });
    },

    remove_breaks_from_year_and_day_field: function() {
        $("#aspect_submission_StepTransformer_field_dc_date_issued_year").siblings("br").remove();
        $("#aspect_submission_StepTransformer_field_dc_date_issued_day").siblings("br").remove();

    },
    //separate values in semicolon delimited field with given prefix
    handle_semicolon_field: function (prefix) {
        var input_field = $("input[name='" + prefix + "']");
        submit_form_describe_organizer.inline_add(input_field, prefix);
    },

    //Make the form into an accordion for ease of use
    accordionize_form: function (field_list) {
        //Array of arrays - Dspace doesn't give very much to get a handle on the lis for the fields,
        //so this is just a list of accordion titles, the starting position of the lis, and the number of lis to take
        //We assume that the elements are already correctly ordered in config/input-form.xml
        var accordion_spec = [
            ["Required Fields", 0, 4],
            ["Additional Description", 4, 6],
            ["Publication Information", 10, 7]
        ];
        //Add structure for accordion
        var submit_buttons = field_list.children("li.last").detach();
        var list_items = field_list.children("li").detach();
        var accordion_div = $("<div id='submit_describe_accordion'></div>");
        for (var section = 0; section < accordion_spec.length; section++) {
            var spec = accordion_spec[section];
            var title = spec[0];
            var start = spec[1];
            var count = spec[2];
            var header = $("<h3></h3>");
            header.text(title);
            var group = $("<div></div>");
            accordion_div.append(header);
            accordion_div.append(group);
            for (var item = start; item < start + count; item++) {
                group.append(list_items[item]);
            }
        }
        field_list.prepend(accordion_div);
        field_list.append(submit_buttons);
        $('h3', accordion_div).prepend("<img src='/themes/ideals-alt/images/page_white_edit.png' alt='edit section'>")
        //autoHeight is for early versions of jQuery, heightStyle for later (1.10 and after)
        accordion_div.accordion({heightStyle: "content", icons: false, autoHeight: false});
    },

    //Move the help divs below their inputs
    move_help: function () {
        var root = $('#submit_describe_accordion');
        $('.composite-help', root).add($('.field-help', root)).each(function (i, element) {
            submit_form_describe_organizer.move_help_inner($(element));
        })
    },

    //Helper function for move_help - it's a little tricky to get it to move past only what it needs to,
    //and doing it recursively seems to be the easiest way.
    move_help_inner: function (help_element) {
        var next = help_element.next();
        if (next.is('label') || next.is('input') || next.is('fieldset') || next.is('textarea')) {
            help_element.detach();
            help_element.insertAfter(next);
            submit_form_describe_organizer.move_help_inner(help_element);
        }
    },

    //Make simple inline values directly editable
    enable_js_editing: function (prefix) {
        var input_field = $("input[name='" + prefix + "']");
        var parent_div = input_field.closest('div');
        var previous_values_div = $('div.ds-previous-values', parent_div);
        $('span', previous_values_div).each(function (i, element) {
            submit_form_describe_organizer.make_span_editable($(element), parent_div);
        });
    },

    enable_identifier_js_editing: function () {
        var input_field = $("input[name='dc_identifier_value']");
        var parent_div = input_field.closest('div');
        var previous_values_div = $('div.ds-previous-values', parent_div);
        $('span', previous_values_div).each(function (i, element) {
            submit_form_describe_organizer.make_identifier_span_editable($(element), parent_div);
        });
    },

    //Make simple inline values directly deletable, editable
    enable_inline_actions: function (prefix) {
        var input_field = $("input[name='" + prefix + "']");
        var parent_div = input_field.closest('div');
        var previous_values_div = $('div.ds-previous-values', parent_div);
        //add span with delete button, hooked up to delete function
        $('span.ds-interpreted-field', previous_values_div).each(function (i, element) {
            //remove existing delete button
            $(element).next("input").detach();
            submit_form_describe_organizer.action_buttons_for_span($(element));
        });
    },

    enable_identifier_inline_actions: function () {
        var input_field = $('input[name="dc_identifier_value"]');
        var parent_div = input_field.closest('div');
        var previous_values_div = $('div.ds-previous-values', parent_div);
        $('span.ds-interpreted-field', previous_values_div).each(function (i, element) {
            $(element).next('input').detach();
            submit_form_describe_organizer.action_buttons_for_identifier_span($(element));
        });
    },

    //Add img div with trash icon for deleting a value, pencil for editing a value. This is added after the
    //supplied span, which must already be in the DOM.
    action_buttons_for_span: function (span) {
        var delete_button = $("<img src='/themes/ideals-alt/images/trash.png' class='js-delete' alt='delete button'>");
        delete_button.click(function () {
            submit_form_describe_organizer.inline_delete(span);
        });
        span.after(delete_button);
        var edit_button = $("<img src='/themes/ideals-alt/images/pencil.png' class='js-edit' alt='delete button'>");
        edit_button.click(function () {
            span.click();
        });
        span.after(edit_button);
    },

    action_buttons_for_identifier_span: function (span) {
        var delete_button = $("<img src='/themes/ideals-alt/images/trash.png' class='js-delete' alt='delete button'>");
        delete_button.click(function () {
            submit_form_describe_organizer.identifier_inline_delete(span);
        });
        span.after(delete_button);
        var edit_button = $("<img src='/themes/ideals-alt/images/pencil.png' class='js-edit' alt='delete button'>");
        edit_button.click(function () {
            span.click();
        });
        span.after(edit_button);
    },

    //Callback to delete a simple inline field value and renumber remaining values
    inline_delete: function (previous_value_span) {
        var previous_values_div = previous_value_span.closest('div.ds-previous-values');
        previous_value_span.closest('div.previous-value').detach();
        this.renumber_hidden_fields(previous_values_div);
    },

    //Callback to delete a simple inline field value and renumber remaining values
    identifier_inline_delete: function (previous_value_span) {
        var previous_values_div = previous_value_span.closest('div.ds-previous-values');
        previous_value_span.closest('div.previous-value').detach();
        this.regenerate_hidden_identifier_fields(previous_values_div);
    },

    //Renumber simple inline hidden fields (so they always go from 1 to n)
    renumber_hidden_fields: function (previous_values_div) {
        var hidden = $('input[type="hidden"]', previous_values_div);
        for (var i = 0; i < hidden.length; i++) {
            $(hidden[i]).attr('name', $(hidden[i]).attr('name').replace(/\d+$/, i + 1));
        }
    },

    //Renumber simple inline hidden fields (so they always go from 1 to n)
    renumber_identifier_hidden_fields: function (previous_values_div) {
        var hidden_values = $('input[type="hidden"][name*="value"]', previous_values_div);
        var hidden_qualifiers = $('input[type="hidden"][name*="qualifier"]', previous_values_div);
        for (var i = 0; i < hidden_values.length; i++) {
            var label = 'dc_identifier' + "_" + (i + 1);
            $(hidden_values[i]).attr('name', label.replace('identifier', 'identifier_value'));
            $(hidden_qualifiers[i]).attr('name', label.replace('identifier', 'identifier_qualifier'))
        }
    },


    //Ensure that there is a previous value div for a simple inline field
    ensure_previous_value_div: function (prefix) {
        this.ensure_previous_value_div_inner($("input[name='" + prefix + "']"))
    },

    //Ensure that there is a previous value div for the identifier field
    ensure_identifier_previous_value_div: function () {
        this.ensure_previous_value_div_inner($("input[name='dc_identifier_value']"));
    },

    //Insert the previous values div at the end of the first div up from input_field
    ensure_previous_value_div_inner: function (input_field) {
        var parent_div = input_field.closest("div");
        var previous_values_div = $('div.ds-previous-values', parent_div);
        if (previous_values_div.length == 0) {
            parent_div.append($("<div class='ds-previous-values'></div>"))
        }
    },

    //Make the add procedure work locally for simple inline fields instead of going back to the server
    enable_inline_addition: function (prefix) {
        var input_field = $("input[name='" + prefix + "']");
        var add_button = input_field.siblings('input');
        add_button.click(function (e) {
            e.preventDefault();
            submit_form_describe_organizer.inline_add(input_field, prefix);
            return false;
        });
        input_field.keydown(function (e) {
            if (e.which == 13) {
                e.preventDefault();
                return false;
            }
        }).keyup(function (e) {
                if (e.which == 13) {
                    submit_form_describe_organizer.inline_add(input_field, prefix);
                    return false;
                }
            });
    },

    enable_identifier_inline_addition: function () {
        var input_field = $('input[name="dc_identifier_value"]');
        var input_select = $('select[name="dc_identifier_qualifier"]');
        var add_button = $('input[name="submit_dc_identifier_add"]');
        add_button.click(function (e) {
            e.preventDefault();
            submit_form_describe_organizer.identifier_inline_add(input_field, input_select);
            return false;
        })
    },

    //Callback to actually add a simple inline field value
    //This has to construct a number of elements to be added to the form
    inline_add: function (input_field, prefix) {
        //If input field support semicolon delimiters then split and add for each value
        //If not, then just add the single value of the input field
        if ($.inArray(prefix, submit_form_describe_organizer.js_semicolon_delimited_fields) > -1) {
            var values = input_field.val().split(";");
            values.map(function (value) {
                submit_form_describe_organizer.internal_inline_add(input_field, value);
            });
        } else {
            submit_form_describe_organizer.internal_inline_add(input_field, input_field.val());
        }
        input_field.val('');
    },

    //Internal function - do the actual adding of inline value
    //This constructs a number of elements to add to the form
    internal_inline_add: function (input_field, new_value) {
        if (new_value.trim() === "")
            return;
        var prefix = input_field.attr('name');
        var previous_values_div = input_field.siblings('.ds-previous-values');
        var value_count = $('input[type="hidden"]', previous_values_div).length + 1;
        var new_value_div = $("<div class='previous-value'></div>");
        previous_values_div.append(new_value_div);
        var hidden_element = $("<input type='hidden'>").attr({'name': prefix + '_' + value_count, 'value': new_value});
        new_value_div.prepend(hidden_element);
        var span = $('<span class="ds-interpreted-field"></span>').text(new_value.trim());
        new_value_div.append(span);
        this.make_span_editable(span, previous_values_div.parent());
        var delete_button = submit_form_describe_organizer.action_buttons_for_span(span);
    },

    identifier_inline_add: function (input_field, input_select) {
        var new_value = input_field.val();
        var new_type = $('option:selected', input_select).val();
        var new_label = new_type + ':' + new_value;
        if (new_value == "")
            return;
        var previous_values_div = $('.ds-previous-values', input_field.closest('div'));
        var value_count = $('div.previous-value', previous_values_div).length + 1;
        var new_value_div = $('<div class="previous-value"></div>');
        previous_values_div.append(new_value_div);
        var span = $('<span class="ds-interpreted-field"></span>').text(new_label);
        new_value_div.append(span);
        this.make_identifier_span_editable(span, previous_values_div.closest('div'));
        var delete_button = this.action_buttons_for_identifier_span(span);
        this.regenerate_hidden_identifier_fields(previous_values_div);
        input_field.val('');
    },

    //Make a span editable via jeditable library for simple inline fields. Includes callback to update value.
    make_span_editable: function (span, parent_div) {
        span.editable(function (value, settings) {
            var trimmed_value = value.trim();
            if (trimmed_value == '')
                return span.text();
            span.text(trimmed_value);
            var name = span.prev("input").val(trimmed_value);
            return trimmed_value;
        }, submit_form_describe_organizer.editable_field_options);
    },

    make_identifier_span_editable: function (span, parent_div) {
        span.editable(function (value, settings) {
            var trimmed_values = submit_form_describe_organizer.parse_identifier_value(value);
            var name = span.prev("input").val();
            if (trimmed_values == false) {
                alert(value + ' is an invalid identifier value. Please correct or cancel.');
                return submit_form_describe_organizer.old_identifier_value(span);
            }
            span.text(trimmed_values['label']);
            submit_form_describe_organizer.regenerate_hidden_identifier_fields(span.closest('div.ds-previous-values'));
            return trimmed_values['label'];
        }, submit_form_describe_organizer.editable_field_options);
    },

    regenerate_hidden_identifier_fields: function (previous_values_div) {
        $('input[type="hidden"]', previous_values_div).detach();
        $('div.previous-value span', previous_values_div).each(function (i, element) {
            var values = submit_form_describe_organizer.parse_identifier_value($(element).text());
            previous_values_div.append($("<input type='hidden'>").val(values['type']).attr('name', 'dc_identifier_qualifier_' + (i + 1)));
            previous_values_div.append($("<input type='hidden'>").val(values['value']).attr('name', 'dc_identifier_value_' + (i + 1)));
        });
    },

    old_identifier_value: function (span) {
        var previous_values_div = span.closest('div.ds-previous-values');
        var parent_div = span.closest('div.previous-value');
        var index = $('div.previous-value', parent_div).index(previous_values_div);
        var type = $("input[type='hidden'][name*='qualifier']", previous_values_div).get(index).val();
        var value = $("input[type='hidden'][name*='value']", previous_values_div).get(index).val();
        return type + ':' + value;
    },

    //try to parse an identifier value. This should have form id_type:value, where id_type is
    //a valid, non-blank, identifier type as represented in the select and value is non blank
    //If we can parse into two non blank values then return an object {type: 'x', value: 'y', label: 'x:y'}, else return false.
    parse_identifier_value: function (string) {
        var split_index = string.indexOf(':');
        if (split_index == -1)
            return false;
        var type = string.substr(0, split_index).trim();
        var value = string.substr(split_index + 1).trim();
        var option_selector = 'select[name="dc_identifier_qualifier"] option[value="' + type + '"]';
        if (value == '' || $(option_selector).length == 0)
            return false;
        return {type: type, value: value, label: type + ':' + value};
    },


    //Options to pass to jeditable
    editable_field_options: {
        type: 'text',
        cssclass: 'inline-edit',
        submit: 'Ok',
        tooltip: 'Click to edit...',
        width: '500px',
        onblur: 'cancel',
        cancel: 'Cancel'
    }

};

//After page loads do the reorganization
$(function () {
    submit_form_describe_organizer.reorganize_form();
});
