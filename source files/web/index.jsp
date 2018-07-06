<%--
  Created by IntelliJ IDEA.
  User: idan
  Date: 27/09/2017
  Time: 11:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ page import="constants.Constants" %>
    <!-- Standard Meta -->
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

    <!-- Site Properties -->
    <title>BattleShips</title>
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/reset.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/site.css">

    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/container.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/grid.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/header.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/image.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/menu.css">

    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/divider.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/segment.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/form.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/input.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/button.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/list.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/message.css">
    <link rel="stylesheet" type="text/css" href="lib/Semantic-UI-CSS-master/components/icon.css">

    <script src="lib/jquery-3.2.1.min.js"></script>
    <script src="lib/Semantic-UI-CSS-master/components/form.js"></script>
    <script src="lib/Semantic-UI-CSS-master/components/visibility.js"></script>
    <script src="lib/Semantic-UI-CSS-master/components/transition.js"></script>
    <script src="lib/Semantic-UI-CSS-master/components/dimmer.js"></script>

    <style type="text/css">
        body {
            background-color: #DADADA;
        }

        body > .grid {
            height: 100%;
        }

        .image {
            margin-top: -100px;
        }

        .column {
            max-width: 450px;
        }
    </style>
</head>
<body>

<div class="ui middle aligned center aligned grid">
    <div class="column">
        <h2 class="ui teal image header">
            <img src="img/Bicon.png" class="image">
            <div class="content">
                Log-in to game room
            </div>
        </h2>
        <form method="get" action="login" class="ui large form">
            <div class="ui stacked segment">
                <div class="field">
                    <div class="ui left icon input">
                        <i class="user icon"></i>
                        <input id="username" type="text" name="username" placeholder="User Name">
                    </div>
                </div>
                <button class="ui fluid large teal submit button" id="loginBtn" type="submit" onclick="login()">Login
                </button>
            </div>
            <div class="ui negative message" id="msg">
                <% Object errorMessage = request.getAttribute(Constants.USER_NAME_ERROR);%>
                <% if (errorMessage != null) {%>
                <%--<span class="label important"></span>--%>
                <strong>Username error: </strong><%=errorMessage%>
                <% } %>
            </div>
        </form>
    </div>
</div>

</body>
<script src="js/login.js"></script>

</html>
