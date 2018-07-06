const roomsURL = "rooms";
let userName = "";
let selectedRoomDetails = [];
let refreshRate = 1000; //miliseconds

function refreshUsersList(users) {
    let trString;
    //clear all current users
    $("#usersList").empty();
    $.each(users || [], function (index, element) {
        if (element === userName) {
            trString = '<tr style="background-color: #99eb94">';
        }
        else {
            trString = '<tr>';
        }
        $(trString +
            '<td>' + '<i class="user icon"></i>' + element + '</td>' +
            '</tr>').appendTo($("#usersList"));
    });
}


function ajaxUsersList() {
    $.ajax({
        data: {requestType: "userList"},
        url: roomsURL,
        success: function (users) {
            refreshUsersList(users);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showRoomMsg('Timeout, No connection');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showRoomMsg('Lost connection with server');
            }
        },
        timeout: 10000
    });
}


function refreshRoomsList(rooms) {
    $("#roomsList").empty();
    let trStr;
    let classType;
    let style;
    $.each(rooms || [], function (index, element) {
        if (selectedRoomDetails[0] === element.roomName) {
            classType = 'active';
        }
        else {
            classType = '';
        }

        if (element.numofPlayerInRoom == 2) {
            style = 'style="background-color: #a3ffba"';
        }
        else {
            style = '';
        }

        trStr = '<tr class="' + classType + '"  onclick="getdata(this)" ' + style + '>';

        $(trStr +
            '<td>' + element.roomName + '</td>' +
            '<td>' + element.createdBy + '</td>' +
            '<td>' + element.boardSize + '</td>' +
            '<td>' + element.gameType + '</td>' +
            '<td>' + element.numofPlayerInRoom + '</td>' +
            '</tr>').appendTo($("#roomsList"));
    });
}

function ajaxGetMyUserName() {
    $.ajax({
        data: {requestType: "userName"},
        url: roomsURL,
        success: function (name) {
            userName = name;
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showRoomMsg('Timeout, No connection');
            }
            else if (XMLHttpRequest.readyState == 0) {
                showRoomMsg('Lost connection with server');
            }
        }
        ,
        timeout: 10000
    })
    ;
}

function ajaxRoomsList() {
    $.ajax({
        data: {requestType: "roomList"},
        url: roomsURL,
        success: function (rooms) {
            refreshRoomsList(rooms);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showRoomMsg('Timeout, No connection');
            }
            else if (XMLHttpRequest.readyState == 0) {
                showRoomMsg('Lost connection with server');
            }
        },
        timeout: 10000
    });
}

function refreshPageData() {
    ajaxUsersList();
    ajaxRoomsList();
}

$("#roomsList").hover(function () {
    $(this).css('cursor', 'pointer');
}, function () {
    $(this).css('cursor', 'auto');
});

//activate the timer calls after the page is loaded
$(document).ready(function () {
    setInterval(refreshPageData, refreshRate);
    ajaxGetMyUserName();
});

function logout() {
    $.ajax({
        data: {
            requestType: "logout",
            userName: userName,
        },
        url: roomsURL,
        success: function (url) {
            let item;
            item = window.localStorage.getItem('logged_in');
            if (item != null) {
                item = !item;
            }
            else {
                item = true;
            }
            window.localStorage.setItem('logged_in', item);
            window.location = url;
        },
        error: function () {
            console.log("bad thing happend");
        }
    });
}


function getdata(element) {
    selectedRoomDetails = [];

    if ($(element).attr('class') === 'active') {
        $(element).removeClass('active');
    }
    else {
        let rows = document.getElementById("roomTable").rows;
        let x = rows.length;
        for (let i = 0; i < x; i++) {
            $(rows[i]).removeClass('active');
        }
        $(element).find('td').each(function () {
            selectedRoomDetails.push($(this).text());
        });
        $(element).addClass('active');
    }
}

function showAddRoomModel() {
    $('.ui.modal.createRoom').modal('setting', 'transition', 'fade').modal('setting', 'closable', false).modal('show').modal('refresh');
    $('#gameName').val("");
}

function addRoom() {
    if (document.getElementById("file").files[0]) {  //if a file was chosen
        hideErrMsg();

        let formData = new FormData();
        formData.append("XMLFile", document.getElementById("file").files[0]);
        formData.append("requestType", "fileUpload");
        formData.append("userName", userName);
        formData.append("roomName", $('#gameName').val());
        formData.append("fileName", $('#file').val());

        let xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            let msgClassName;
            if (xhr.readyState === xhr.DONE) {
                {
                    if ("Room added successfully" === xhr.responseText.trim()) {   //if there's something, it's an error
                        msgClassName = 'ui success  message';
                    }
                    else {
                        msgClassName = 'ui negative message';
                    }
                    $('#errMsg').attr('class', msgClassName).transition('bounce in').text(xhr.responseText);
                }
            }
        };

        xhr.open("POST", roomsURL, true);
        xhr.send(formData);
    }
    else {
        $('#errMsg').transition('bounce in').text("Please choose a file");

    }
}


// click button to enter a room
$(document).on("click", "#enterRoom", function (e) {
    if (selectedRoomDetails[0] != null) {
        $.ajax({
            data: {
                userName: userName,
                requestType: "enterRoom",
                roomName: selectedRoomDetails[0],
            },
            url: roomsURL,
            success: function (responseJson) {  //change to board page
                if (typeof responseJson.redirect !== "undefined") {
                    document.location.href = responseJson.redirect;
                }
                else if (typeof responseJson.error !== "undefined") {
                    showRoomMsg(responseJson.error);
                }
            }
        });
    }
});


// form in start modal, input user name, and check invalid parameters.
$('.ui.form.createRoom').form({
    on: 'blur',
    inline: true,
    onValid: function () {
        $('#addbtn').removeClass('disabled');
    },
    onInvalid: function () {
        $('#addbtn').addClass('disabled');
    },
    fields: {
        name: {
            identifier: 'name',
            rules: [
                {
                    type: 'regExp[^[a-zA-Z\u0590-\u05FF ]+$]',
                    prompt: "Please enter legal game name.",
                },
            ],
        },
    },
});

function hideErrMsg() {
    $('#errMsg').attr('class', 'ui negative message').transition('hide');
}

function deleteRoom() {
    if (selectedRoomDetails[0] != null) {
        $.ajax({
            data: {
                requestType: "deleteRoom",
                userName: userName,
                roomName: selectedRoomDetails[0],
            },
            url: roomsURL,
            success: function (res) {
                if (res[0]) {
                    selectedRoomDetails = [];
                }
                showRoomMsg(res[1]);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (textStatus == "timeout") {
                    showRoomMsg("Timeout", "No connection");
                }
                else if (XMLHttpRequest.readyState === 0) {
                    showRoomMsg("Lost connection with server");
                }
            },
            timeout: 10000
        });
    }
}

function showRoomMsg(msg) {
    $('#roomMsg').transition('show').transition('bounce in')
        .text(msg);

    setTimeout(function () {
        $('#roomMsg').transition('fade');
    }, 3000);
}

hideErrMsg();

function storageChange(event) {
    if (event.key === 'logged_in') {
        logout();
    }
}

window.addEventListener('storage', storageChange, false);
