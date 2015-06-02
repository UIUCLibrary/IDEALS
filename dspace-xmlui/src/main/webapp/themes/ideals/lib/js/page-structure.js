//Clear default text of empty text areas on focus
function tFocus(element) {
    if (element.value == page_structure_js_data['default_textarea_value']) {
        element.value = '';
    }
}
//Clear default text of empty text areas on submit
function tSubmit(form) {
    var defaultedElements = document.getElementsByTagName("textarea");
    for (var i = 0; i != defaultedElements.length; i++) {
        if (defaultedElements[i].value === page_structure_js_data['default_textarea_value']) {
            defaultedElements[i].value = '';
        }
    }
}
//Disable pressing 'enter' key to submit a form (otherwise pressing 'enter' causes a submission to start over)
function disableEnterKey(e) {
    var key;

    if (window.event)
        key = window.event.keyCode;     //Internet Explorer
    else
        key = e.which;     //Firefox and Netscape

    if (key == 13)  //if "Enter" pressed, then disable!
        return false;
    else
        return true;
}

function FnArray() {
    this.funcs = new Array;
}

FnArray.prototype.add = function (f) {
    if (typeof f != "function") {
        f = new Function(f);
    }
    this.funcs[this.funcs.length] = f;
};

FnArray.prototype.execute = function () {
    for (var i = 0; i < this.funcs.length; i++) {
        this.funcs[i]();
    }
};

var runAfterJSImports = new FnArray();

$(function () {
    $('[data-toggle="tooltip"]').tooltip()
})
