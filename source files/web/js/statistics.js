// Constants:
const SHIPSIGN = 'O';
const EMPTY = ' ';
const HITINGSIGN = 'X';
const MISSSIGN = '-';
const MINESIGN = '*';
const HITMINE = '@';
const roomsURL = 'rooms';
const gameURL = 'game';

// Global vars:
let roomName;
let userName;

$(document).ready(function () {
    roomName = CookieUtil.GetCookie('roomName');
    userName = CookieUtil.GetCookie('userName');
    ajaxGetStatisticsGame();
});

function updateGlobalStatistics(statistic) {
    $('#roomName').text(roomName);
    $('#totalTurns').text(statistic[0]);
    $('#gameType').text(statistic[1]);
    updateTable('#winPShips', statistic[2]);
    updateTable('#losePShips', statistic[3]);
    $('#time').text(statistic[4]);
}

function updateWinPlayerStatistics(statistic) {
    $('#winPName').text(statistic[0]);
    $('#winPScore').text(statistic[1]);
    $('#winPHits').text(statistic[2]);
    $('#winPMiss').text(statistic[3]);
    $('#winPAvgAttack').text(statistic[4]);

    updateBoard('#winPShipsBoard', statistic[5].board);
    updateBoard('#winPHitsBoard', statistic[6].board);
}

function updateLoosePlayerStatistics(statistic) {
    $('#losePName').text(statistic[0]);
    $('#losePScore').text(statistic[1]);
    $('#losePHits').text(statistic[2]);
    $('#losePMiss').text(statistic[3]);
    $('#losePAvgAttack').text(statistic[4]);

    updateBoard('#losePShipsBoard', statistic[5].board);
    updateBoard('#losePHitsBoard', statistic[6].board);
}

function ajaxGetStatisticsGame() {
    $.ajax({
        data: {
            requestType: "statistics",
            roomName: roomName,
        },
        url: gameURL,
        success: function (statistics) {
            updateGlobalStatistics(statistics[2]);
            updateWinPlayerStatistics(statistics[0]);
            updateLoosePlayerStatistics(statistics[1]);
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

function updateTable(tableName, arr) {
    let row;
    $(tableName).empty();
    if (arr != null) {
        for (let i = 0; i < arr.length; i++) {
            row = '<tr class="center aligned">' +
                '<td>' + arr[i].length + '</td>' +
                '<td>' + arr[i].type + '</td>';
            $(row).appendTo($(tableName));
        }
    }
}

function updateBoard(boardType, board) {
    let row;
    let simpleFinish = '></div></td>';
    let begin;
    let ch;

    $(boardType).empty();
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
            row += (begin + simpleFinish);
        }
        row += '</tr>';
        $(row).appendTo($(boardType));
    }

}

function leaveStatsticsPage() {
    $.ajax({
        data: {
            requestType: "returnToRooms",
        },
        url: gameURL,
        success: function (responseJson) {
            if (typeof responseJson.redirect !== "undefined") {
                document.location.href = responseJson.redirect;
            }
            else if (typeof responseJson.error !== "undefined") {
                console.log(responseJson.error);
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
    if(event.key === 'logged_in') {
        logout();
    }
}

window.addEventListener('storage', storageChange, false);
