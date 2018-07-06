let isValid = false;
let loginURL = 'login';


$('.ui.form').form({
    on: 'blur',
    onValid: function () {

        isValid = true;
    },
    onInvalid: function () {
        isValid = false;
    },
    fields: {
        username: {
            identifier: 'username',
            rules: [
                {
                    type: 'empty',
                    prompt: '{name} must have a value',
                }
            ]
        }
    }
});

$('#username').focus();

function login() {
    $('#msg').attr('class', 'ui error message');
    $('.ui.form').form({
        on: 'blur',
        onValid: function () {
            isValid = true;
        },
        onInvalid: function () {
            isValid = false;
        },
        fields: {
            username: {
                identifier: 'username',
                rules: [
                    {
                        type: 'empty',
                        prompt: '{name} must have a value',
                    }
                ]
            }
        }
    });
}

function ajaxGetUserName() {
    $.ajax({
        data: {
            getUserName: "getUserName",
        },
        url: loginURL,
        success: function (res) {
            if (res != null) {
                document.location.href = res;
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                console.log("Timeout", "No connection", true);
            }
            else if (XMLHttpRequest.readyState === 0) {
                console.log("Lost connection with server");
            }
        },
        timeout: 10000
    });
}

$(document).ready(function () {
    ajaxGetUserName();
});

localStorage.clear();