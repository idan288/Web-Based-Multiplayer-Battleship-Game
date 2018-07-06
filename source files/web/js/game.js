// constants:
const SHIPSIGN = 'O';
const EMPTY = ' ';
const HITINGSIGN = 'X';
const MISSSIGN = '-';
const MINESIGN = '*';
const HITMINE = '@';
const gameURL = 'game';
const roomsURL = 'rooms';

// Global vars
let sec = 0;
let mineAmount = 0;
let roomName;
let refreshRate = 1000;
let checkGameStartInterval;
let checkOppPlayerLeaveInterval;
let userName;
let oppName;
let firstTime = true;
let isMyTurnNow = false;
let inDisabledMode = false;
let turnSec = 0;
let avgTime = 0;
let numMiss = 0;
let numHits = 0;

function pad(val) {
    return val > 9 ? val : "0" + val;
}

let timer = setInterval(function () {
    document.getElementById("seconds").innerHTML = pad(++sec % 60);
    document.getElementById("minutes").innerHTML = pad(parseInt((sec / 60) % 60, 10));
    document.getElementById("hours").innerHTML = pad(parseInt(sec / 3600, 10));
}, refreshRate);

function allowDrop(ev, row, col, element) {
    ev.preventDefault();
    $.ajax({
        data: {
            requestType: "minePlace",
            roomName: roomName,
            userName: userName,
            row: row,
            col: col,
        },
        url: gameURL,
        success: function (isMinePlaceLegal) {
            if (!isMinePlaceLegal) {
                $(element).attr('ondrop', '');
                $(element).attr('ondragover', '');
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function drag(ev) {
    ev.dataTransfer.setData("text", ev.target.id);
}

function drop(ev, row, col, element) {
    if (mineAmount > 0) {
        ev.preventDefault();
        let data = ev.dataTransfer.getData("text");
        ev.target.appendChild(document.getElementById(data));

        $.ajax({
            data: {
                requestType: "mineMove",
                roomName: roomName,
                userName: userName,
                row: row,
                col: col,
            },
            url: gameURL,
            success: function (allGood) {
                if (allGood) {
                    mineAmount--;
                    $('#mine').html('<i id="1" class="bomb icon"  draggable="true" ondragstart="drag(event)"></i> Mines amount: ' + mineAmount);
                    if (mineAmount == 0) {
                        $('#mine').html('<i id="1" class="bomb icon"></i> Mines amount: ' + mineAmount);
                    }
                } else {
                    $(element).attr('class', 'boardCell empty');
                    $(element).attr('id', row.toString() + col.toString());
                    $(element).attr('ondrop', 'drop(event,' + row + ',' + col + ',' + 'this)');
                    $(element).attr('ondragover', 'allowDrop(event,' + row + ',' + col + ',' + 'this)');
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (textStatus == "timeout") {
                    showHideMSG('timeout');
                }
                else if (XMLHttpRequest.readyState === 0) {
                    showHideMSG("lostConnection");
                }
            }
            ,
            timeout: 10000
        });
    }
}


//activate the timer calls after the page is loaded
$(document).ready(function () {
    //The users list is refreshed automatically
    roomName = CookieUtil.GetCookie('roomName');
    userName = CookieUtil.GetCookie('userName');
    $('#gameType').text(CookieUtil.GetCookie('gameType'));
    checkGameStartInterval = setInterval(checkIfGameStarted, refreshRate);
});


function checkIfGameStarted() {
    $.ajax({
        data: {
            requestType: "checkGameStart",
            roomName: roomName,
            userName: userName,
        },
        url: gameURL,
        success: function (response) {
            updatePlayerList(response[1]);

            if (response[0]) { //if started
                showHideMSG("gameIsStart");
                clearInterval(checkGameStartInterval);
                ajaxupdatePlayerGameDetails();
                ajaxGetBoard('shipsBoard');
                ajaxGetBoard('hitsBoard');
                turnSec = parseInt(sec);
                oppName = response[3];
                if (response[2]) {
                    setTimeout(function () {
                        showHideMSG('myTurn');
                    }, 1000);
                }
                checkOppPlayerLeaveInterval = setInterval(checkGameStatusAndUpdateBoards, refreshRate);
            }
            else {
                showHideMSG('gameNotStartYet');
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}


function dissableElemetsOnTurn() {
    disableElements('hitsBoard');
    disableElements('shipsBoard');
    if (mineAmount > 0) {
        $('#mine').html('<i id="1" class="bomb icon"></i> Mines amount: ' + mineAmount);
    }
    $('#leaveBtn').attr('onclick', '');
    inDisabledMode = true;
}

function disableElements(type) {
    let board = document.getElementById(type).getElementsByClassName('boardCell');

    for (let i = 0; i < board.length; i++) {
        $(board[i]).attr('ondrop', '');
        $(board[i]).attr('ondragover', '');
        $(board[i]).attr('onclick', '');
    }
}


function enableElements() {
    ajaxGetBoard('shipsBoard');
    ajaxGetBoard('hitsBoard');
    if (mineAmount > 0) {
        $('#mine').html('<i id="1" class="bomb icon"  draggable="true" ondragstart="drag(event)"></i> Mines amount: ' + mineAmount);
    }
    $('#leaveBtn').attr('onclick', 'leaveGame()');
    inDisabledMode = false;
}

const msg = {
    gameNotStartYet: "Please wait to other player join the room.",
    myTurn: "Your turn now make your move!",
    oppTurn: " Make is move now please wait.",
    gameIsStart: "Game started.",
    miss: "Miss!!",
    hit: "Hit!!",
    win: "So So Excitement!! You are the Winner!",
    lose: "You lose the game!",
    leave: " leave the game!",
    lostConnection: "Lost connection with server",
    timeout: "Timeout No connection",
};

function showHideMSG(type, content) {
    let curMsg;
    curMsg = msg[type];
    if (type === 'oppTurn' || type === 'leave') {
        curMsg = oppName + curMsg;
    }
    else if (type === 'error') {
        curMsg = content;
    }
    $('#msgText').text(curMsg);
}

function ajaxGetBoard(boardType) {
    $.ajax({
        data: {
            requestType: "getBoard",
            roomName: roomName,
            userName: userName,
            boardType: boardType,
        },
        url: gameURL,
        success: function (board) {
            updateBoard(board, boardType);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function updateBoard(board, boardType) {
    let row;
    let finishOfShipsBoard;
    let finishOfHitBoard;
    let simpleFinish = '></div></td>';
    let begin;
    let id;
    boardType = '#' + boardType;
    $(boardType).empty();
    let ch;
    for (let i = 0; i < board.length; i++) {
        row = '<tr>';
        for (let j = 0; j < board.length; j++) {
            ch = board[i][j];
            begin = '<td><div class="boardCell ';
            switch (ch) {
                case EMPTY:
                    begin += 'empty" ';
                    break;
                case SHIPSIGN:
                    begin += 'ship" ';
                    break;
                case MISSSIGN:
                    begin += 'miss" ';
                    break;
                case HITINGSIGN:
                    begin += 'hit" ';
                    break;
                case MINESIGN:
                    begin += 'mine" ';
                    break;
                case HITMINE:
                    begin += 'hitMine" ';
                    break;
                default:
            }
            if (ch == EMPTY) {
                if (boardType == '#shipsBoard') {
                    id = i.toString() + j.toString();
                    finishOfShipsBoard = 'ondrop="drop(event,' + i + ',' + j + ',this)" ondragover="allowDrop(event,' + i + ',' + j + ',this)" ' + 'id="' + id + '"></div></td>';
                    row += ( begin + finishOfShipsBoard);
                }
                else {
                    finishOfHitBoard = 'onclick="clickOnHitsCellBoard(' + i + ',' + j + ',' + 'this' + ')"></div></td>';
                    row += (begin + finishOfHitBoard);
                }
            }
            else {
                row += (begin + simpleFinish)
            }

        }
        row += '</tr>';
        $(row).appendTo($(boardType));
    }
}

function getAvg() {
    let avgInt = parseInt(avgTime);
    let misAndHitsInt = parseInt(numMiss) + parseInt(numHits);
    if (avgInt !== 0 && (misAndHitsInt) !== 0) {
        return parseInt(avgInt / misAndHitsInt);
    }
    return 0;
}

function updateAvgSec() {
    console.log("sec = " + sec);
    console.log("turnSec = " + turnSec);
    avgTime += (sec - turnSec);
    console.log("avg = " + avgTime);
}

function getAvgStr() {
    let time = getAvg();
    let hours = pad(parseInt(time / 3600));
    let minutes = pad(parseInt((time / 60) % 60));
    let seconds = pad(parseInt(time % 60));

    return hours + ':' + minutes + ':' + seconds;
}

function clickOnHitsCellBoard(row, col, element) {
    updateAvgSec();
    $.ajax({
        data: {
            requestType: "clickOnBoard",
            roomName: roomName,
            userName: userName,
            row: row,
            col: col,
        },
        url: gameURL,
        success: function (result) {
            let hitResClass;

            switch (result) {
                case "hit":
                    hitResClass = 'hit';
                    turnSec = parseInt(sec);
                    $('#msg').transition('jiggle');
                    $('#msg').css('background-image', "url('img/hit.gif')");
                    showHideMSG("hit");
                    setTimeout(function () {
                        $('#msg').css('background-image', "");
                        showHideMSG("myTurn");
                    }, 2000);
                    break;
                case "miss":
                    hitResClass = 'miss';
                    itsFirstTime = true;
                    dissableElemetsOnTurn();
                    $('#msg').transition('jiggle');
                    showHideMSG("miss");
                    setTimeout(function () {
                        showHideMSG("oppTurn");
                    }, 2000);
                    isMyTurnNow = false;
                    break;
                case "hitMine":
                    hitResClass = 'hitMine';
                    break;
                case "win":
                    hitResClass = 'hit';
                    userWinActions();
                    break;
            }
            $(element).attr('class', 'boardCell ' + hitResClass).attr('onclick', '');
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function ajaxGameFinish() {
    $.ajax({
        data: {
            requestType: "gameFinish",
            roomName: roomName,
            userName: userName,
            avgTime: avgTime,
        },
        url: gameURL,
        success: function (responseJson) {
            if (typeof responseJson.redirect !== "undefined") {
                document.location.href = responseJson.redirect;
            }
            else if (typeof responseJson.error !== "undefined") {
                showHideMSG('error', responseJson.error);
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}


function updateShipTable(table, ships) {
    let row;
    $(table).empty();
    if (ships != null) {

        for (let i = 0; i < ships.length; i++) {
            row = '<tr class="center aligned">' +
                '<td>' + ships[i].length + '</td>' +
                '<td>' + ships[i].type + '</td>';
            $(row).appendTo($(table));
        }
    }
}

function ajaxGetShips() {
    $.ajax({
        data: {
            requestType: "getShips",
            roomName: roomName,
            userName: userName,
        },
        url: gameURL,
        success: function (ships) {
            updateShipTable('#myShips', ships[0]);
            updateShipTable('#oppShips', ships[1]);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function checkGameStatusAndUpdateBoards() {
    ajaxupdatePlayerGameDetails();

    if (!isMyTurnNow) {
        ajaxGetBoard('shipsBoard');
    }

    ajaxGetPlayersList();
    ajaxGetGameStatus();
    ajaxGetShips();
}

function ajaxGetGameStatus() {
    $.ajax({
        data: {
            requestType: "gameStatus",
            roomName: roomName,
            userName: userName,
        },
        url: gameURL,
        success: function (res) {
            switch (res) {
                case 'oppTurn':
                    isMyTurnNow = false;
                    if (!inDisabledMode) {
                        dissableElemetsOnTurn();
                        showHideMSG("oppTurn");
                    }
                    break;
                case 'leave':
                    clearInterval(checkOppPlayerLeaveInterval);
                    $('#msg').transition('jiggle');
                    showHideMSG('leave');
                    setTimeout(function () {
                        userWinActions();
                    }, 2000);
                    break;
                case 'win': //the status is win, and this is not me => in lose.
                    clearInterval(checkOppPlayerLeaveInterval);
                    userLoseActions();
                    break;
                case 'myTurn':
                    isMyTurnNow = true;
                    if (inDisabledMode) {
                        turnSec = parseInt(sec);
                        enableElements();
                        showHideMSG('myTurn');
                    }
                    break;
            }

        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus == "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function userLoseActions() {
    clearInterval(checkOppPlayerLeaveInterval);
    $('#msg').transition('jiggle');
    // $('#msg').css('background-image', "url('img/win.gif')");
    showHideMSG("lose");
    setTimeout(function () {
        ajaxGameFinish();
    }, 2000);
}

function userWinActions() {
    clearInterval(checkOppPlayerLeaveInterval);
    $('#msg').css('color', 'white');
    $('#msg').transition('jiggle');

    $('#msg').css('background-image', "url('img/win.gif')");
    showHideMSG("win");
    setTimeout(function () {
        ajaxGameFinish();
    }, 2000);
}

function ajaxGetPlayersList() {
    $.ajax({
        data: {
            requestType: "getPlayersList",
            roomName: roomName,
        },
        url: gameURL,
        success: function (playerList) {
            updatePlayerList(playerList);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });

}

function updatePlayerGameDetails(details) {
    $('#roomName').text(roomName);
    if (firstTime) {
        mineAmount = details[0];
    }
    $('#mineAmount').text('Mines amount: ' + mineAmount);
    $('#totalTurns').text(details[1]);
    numMiss = details[2];
    $('#miss').text(details[2]);
    numHits = details[3];
    $('#hits').text(details[3]);
    $('#avgAttack').text(getAvgStr());
    if (firstTime) {
        firstTime = false;
        if (details[5] == 0) {
            setTimeout(function () {
                sec = 0;
                turnSec = 0;
            }, 900);
        }
        else {
            sec = 0;
            turnSec = 0;
        }
    }
}

function ajaxupdatePlayerGameDetails() {
    $.ajax({
        data: {
            requestType: "playerGameDetails",
            roomName: roomName,
            userName: userName,
        },
        url: gameURL,
        success: function (details) {
            updatePlayerGameDetails(details);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (textStatus === "timeout") {
                showHideMSG('timeout');
            }
            else if (XMLHttpRequest.readyState === 0) {
                showHideMSG("lostConnection");
            }
        },
        timeout: 10000
    });
}

function updatePlayerList(playersList) {

    $('#playerLists').empty();
    if (playersList != null) {
        for (let i = 0; i < playersList.length; i++) {
            if (playersList[i].name == userName) {
                trString = '<tr class="center aligned" style="background-color: #99eb94">';
            }
            else {
                trString = '<tr class="center aligned">';
            }

            $(trString + '<td>' + playersList[i].name + '</td>' +
                '<td>' + playersList[i].score + '</td>' + '</tr>'
            ).appendTo($('#playerLists'));
        }
    }
}

function leaveGame(wantToLogOut) {
    $.ajax({
        data: {
            requestType: "leave",
            userName: userName,
            roomName: roomName,
            avgTime: avgTime,
        },
        url: gameURL,
        success: function (responseJson) {
            if (wantToLogOut === true) {
                logout();
            }
            else if (typeof responseJson.redirect !== "undefined") {
                document.location.href = responseJson.redirect;
            }
            else if (typeof responseJson.error !== "undefined") {
                showHideMSG('error', responseJson.error);
            }
        },
        error: function () {
            console.log("bad thing happend");
        }
    });
}


function logout() {
    $.ajax({
        data: {
            requestType: "logout",
            userName: userName,
        },
        url: roomsURL,
        success: function (url) {
            window.location = url;
        },
        error: function () {
            console.log("bad thing happend");
        }
    });
}

function storageChange(event) {
    if (event.key === 'logged_in') {
        leaveGame(true);
    }
}

window.addEventListener('storage', storageChange, false);
