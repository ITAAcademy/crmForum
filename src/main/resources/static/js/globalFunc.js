function toArray(object) {
    return angular.isArray(object) ? object : Object.keys(object).map(function(key) {
        return object[key];
    });
}
MAX_UPLOAD_FILE_SIZE_BYTES = 100 * 1000 * 1024;
var MAXIMAL_MESSAGE_SIZE = 1000;
var daysName = {},
    hoursName = {},
    minutesName = {};
var dayName = {},
    hourName = {},
    minuteName = {};
var endName = {};
daysName['ua'] = 'днів';
dayName['ua'] = 'день';
daysName['en'] = 'day';
dayName['en'] = 'days';
daysName['ru'] = 'дней';
dayName['ru'] = 'день';

hoursName['ua'] = 'годин';
hourName['ua'] = 'годину';
hoursName['en'] = 'hour';
hourName['en'] = 'hours';
hoursName['ru'] = 'часов';
hourName['ru'] = 'час';

minutesName['ua'] = ' хвилин ';
minuteName['ua'] = ' хвилину ';
minutesName['en'] = ' minutes ';
minuteName['en'] = ' minute ';
minutesName['ru'] = ' минут ';
minuteName['ru'] = ' минуту ';

endName['ua'] = "тому";
endName['en'] = "ago";
endName['ru'] = "спустя";

var formatDateWithLast = function(date) {
    if (date == null || date == undefined || isNaN(date))
        return "";

    // need translate and move to global to config map
    var dateObj = new Date(date);

    if (dateObj == null || dateObj == undefined || isNaN(dateObj))
        return "";

    var delta = new Date().getTime() - dateObj.getTime();
    if (delta > 60000 * 59)
        return formatDate(date);
    else
    if (Math.round(delta / 60000) == 0)
        return null;

    var minutesStr = Math.round(delta / 60000);
    if (minutesStr > 1)
        return minutesStr + minutesName[globalConfig.lang] + endName[globalConfig.lang];
    else
        return minutesStr + minuteName[globalConfig.lang] + endName[globalConfig.lang];

}
var formatDate = function(date) {
    // need translate and move to global to config map
    var monthNames = {};
    monthNames['ua'] = [
        "Січеня", "Лютого", "Березеня ",
        "Квітня", "Травня ", "Червня ", "Липеня",
        "Серпня", "Вересеня", "Жовтеня",
        "Листопада", "Груденя"
    ];
    monthNames['en'] = [
        "January", "February", "March",
        "April", "May", "June", "July",
        "August", "September", "October",
        "November", "December"
    ];
    monthNames['ru'] = [
        "Января", "Февраля", "Марта",
        "Апреля", "Мая", "Июня", "Июля",
        "Августа", "Сентября", "Октября",
        "Ноября", "Декабря"
    ];
    var dateObj = new Date(date);
    var day = dateObj.getDate();
    var monthIndex = dateObj.getMonth();
    var year = dateObj.getFullYear();
    var minutes = dateObj.getMinutes();
    if (minutes < 10)
        minutes = '0' + minutes;

    return day + " " + monthNames[globalConfig.lang][monthIndex] + " " + dateObj.getHours() + ":" + minutes;
}

function getCurrentTime() {
    var currentdate = new Date();
    var h = currentdate.getHours();
    var m = currentdate.getMinutes();
    var s = currentdate.getSeconds();
    return h + ":" + m + ":" + s;
}

function formatDateToTime(date) {
    var h = date.getHours();
    var m = date.getMinutes();
    var s = date.getSeconds();
    return h + ":" + m + ":" + s;
}



function getPropertyByValue(obj, value) {
    for (var prop in obj) {
        if (obj.hasOwnProperty(prop)) {
            if (obj[prop] === value)
                return prop;
        }
    }
}

var curentDateInJavaFromat = function() {
    var currentdate = new Date();
    var day = currentdate.getDate();
    if (day < "10")
        day = "0" + day;

    var mouth = (currentdate.getMonth() + 1);
    if (mouth < "10")
        mouth = "0" + mouth;

    var datetime = currentdate.getFullYear() + "-" + mouth + "-" +
        day + " " + currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds() + ".0";
    //console.log("------------------ " + datetime)
    return datetime;
};

function getIdInArrayFromObjectsMap(roomNameMap, propertyName, valueToFind) {

    for (var item in roomNameMap)
        if (roomNameMap[item][propertyName] == valueToFind) return item;
    return undefined;
}

function getRoomById(rooms, id) {

    for (var i = 0; i < rooms.length; i++) {
        if (rooms[i].roomId == id) return rooms[i];
    }
    return undefined;
}

/*
 * FILE UPLOAD
 */
function uploadXhr(files, urlpath, successCallback, errorCallback, onProgress) {

    var xhr = getXmlHttp();

    //  обработчик для закачки
    xhr.upload.onprogress = function(event) {
        //console.log(event.loaded + ' / ' + event.total);
        onProgress(event, xhr.upload.loaded);
    }

    //  обработчики успеха и ошибки
    //  если status == 200, то это успех, иначе ошибка
    xhr.onload = xhr.onerror = function() {
        if (this.status == 200) {
            console.log("SUCCESS:" + xhr.responseText);
            successCallback(xhr.responseText);
        } else {
            console.log("error " + this.status);
            errorCallback(xhr);
        }
    };

    xhr.open("POST", urlpath);
    var boundary = String(Math.random()).slice(2);
    //  xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
    var formData = new FormData();

    for (var i = 0; i < files.length; i++) {
        formData.append("file" + i, files[i]);
    }
    xhr.send(formData);

}

function upload($http, files, urlpath) {
    var formData = new FormData();
    for (var i = 0; i < files.length; i++) {
        formData.append("file" + i, files[i]);
    }

    return $http.post(urlpath, formData, {
        transformRequest: function(data, headersGetterFunction) {
            return data;
        },
        headers: { 'Content-Type': undefined }
    }).error(function(data, status) {
        console.log("Error ... " + status);
    });
}


function getXmlHttp() {
    var xmlhttp;
    try {
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
        try {
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (E) {
            xmlhttp = false;
        }
    }
    if ((!xmlhttp || !xmlhttp.upload) && typeof XMLHttpRequest != 'undefined') {
        xmlhttp = new XMLHttpRequest();
    }
    return xmlhttp;
}

function replacer(str, offset, s) {
    return "{{call(" + str.substring(2, str.length - 4) + ")}}";
}

function call(str) {
    return "{{" + str + "}}";
}

/*function upload(file,urlpath) {

var xhr = new XMLHttpRequest();

// обработчик для закачки
xhr.upload.onprogress = function(event) {
  log(event.loaded + ' / ' + event.total);
}

// обработчики успеха и ошибки
// если status == 200, то это успех, иначе ошибка
xhr.onload = xhr.onerror = function() {
  if (this.status == 200) {
    log("success");
  } else {
    log("error " + this.status);
  }
};

xhr.open("POST", urlpath, true);
xhr.setRequestHeader('Content-Type', 'multipart/form-data')
xhr.send(file);

}*/

/*
 * CONST
 */
var Operations = Object.freeze({
    "send_message_to_all": "SEND_MESSAGE_TO_ALL",
    "send_message_to_user": "SEND_MESSAGE_TO_USER",
    "add_user_to_room": "ADD_USER_TO_ROOM",
    "add_room": "ADD_ROOM",
    "add_room_from_tenant": "ADD_ROOM_FROM_TENANT",
    "add_room_on_login": "ADD_ROOM_ON_LOGIN"
});

var serverPrefix = "/crmForum";
var DEFAULT_FILE_PREFIX_LENGTH = 15;

var substringMatcher = function(strs) {
    return function findMatches(q, cb) {
        var matches, substringRegex;

        // an array that will be populated with substring matches
        matches = [];

        // regex used to determine if a string contains the substring `q`
        substrRegex = new RegExp(q, 'i');

        // iterate through the pool of strings and for any string that
        // contains the substring `q`, add it to the `matches` array
        $.each(strs, function(i, str) {
            if (substrRegex.test(str)) {
                matches.push(str);
            }
        });
        cb(matches);
    };
};

function getKeyByValue(value, object) {
    for (var prop in object) {
        if (object.hasOwnProperty(prop)) {
            if (object[prop] === value)
                return prop;
        }
    }
}

var isDate = function(date) {
    return (date instanceof Date); //&& (!Number.isInteger(parseInt(date)) && ((new Date(date) !== "Invalid Date" && !isNaN(new Date(date)) )));
}

function getType(value) {
    if (value === true || value === false || value == 'true' || value == 'false')
        return "bool";

    if (Array.isArray(value))
        return "array";

    if (isDate(value))
        return "date";

    return "string";
}

function parseBoolean(value) {
    if (value == "true")
        return true;
    else
        return false;
}

function Color(val) {
    this.val = val;
}

var globalTimeOut;
jQuery.fn.calcTextSize = function() {
    var res;
    var cont = $('<div>' + this.text() + '</div>').css("display", "table")
        .css("z-index", "-1").css("position", "absolute")
        .css("font-family", this.css("font-family"))
        .css("font-size", this.css("font-size"))
        .css("font-weight", this.css("font-weight")).appendTo('body');
    res = cont.width();
    var size = 160 //parseInt(this.css("max-width"), 10);
    if (res > size)
        res = size;
    cont.remove();
    return res;
}

jQuery.fn.hasOverflown = function() {
    var res;
    var cont = $('<div>' + this.text() + '</div>').css("display", "table")
        .css("z-index", "-1").css("position", "absolute")
        .css("font-family", this.css("font-family"))
        .css("font-size", this.css("font-size"))
        .css("font-weight", this.css("font-weight")).appendTo('body');
    var size = 160 //parseInt(this.css("max-width"), 10);
    res = (cont.width() > size);
    var q = cont.text();
    var q1 = cont.width();
    var q2 = size;

    cont.remove();
    return res;
}

/*************
 * URL PARSE
 *************/
function getRequestParam(name) {
    if (name = (new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1]);
}

function getUrlQueryString() {
    return window.location.href.slice(window.location.href.indexOf('?') + 1);
}

function getUrlPath() {
    if(window.location.href.indexOf('?') == -1)
        return window.location.href;
    return window.location.href.slice(0, window.location.href.indexOf('?') - 1);
}

function urlPathContain(part) {
    if (getUrlPath().indexOf('/' + part + '/') == -1)
        return false;
    return true;
}
function urlPathValue(name) {
    var index = getUrlPath().indexOf('/' + name + '/');
    if (index == -1)
        return null;
    var res = getUrlPath().slice(index + 2 + name.length);
    if(res.indexOf("/") != -1)
        res = res.slice(0, res.indexOf("/") - 1);
    return res;
}

function getRequestVars() {
    var vars = [],
        hash;
    var hashes = getUrlQueryString().split('&');
    for (var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}
/*************
 * BREADSCRUMB
 *************/
function autoDisableToolTips($element) {
    /* var $c = $element.find('div')
         .clone()
         .css({ display: 'inline', 'max-width': 'auto', visibility: 'hidden' })
         .appendTo('body');
         var l = $c.width();
         var k = $element.find('div').width();
     if ($c.width() > $element.find('div').width()) {
         $element.addClass("tooltipped");
     } else
         $element.removeClass("tooltipped");

     $c.remove();*/

    if ($element.find('div').hasOverflown())
        $element.addClass("tooltipped");
    else
        $element.removeClass("tooltipped");
}
var menu_upate = function(event) {
    clearTimeout(globalTimeOut);
    globalTimeOut = setTimeout(function() {
        var tooltipped = $('.tooltipped');
        if (typeof tooltipped != 'undefined')
            tooltipped.tooltip('remove');
        var win = $(this); //this = window
        if (win.height() >= 820) { /* ... */ }
        if (win.width() >= 1280) { /* ... */ }
        ellipses = $(".breadcrumb-container")[0].children;
        //
        var sum = 80 + $(".tool-bar").width();
        var max = $(".breadcrumb-container").parent().width();
        var i = ellipses.length - 1;
        for (; i >= 0; i--) {
            var size = $(ellipses[i]).calcTextSize() + 25;
            if (size + sum > (max - 15)) {
                for (var j = 0; j <= i; j++) {
                    $(ellipses[j]).addClass("hide-me");
                }
                break;
            } else {
                $(ellipses[i]).removeClass("hide-me");
                autoDisableToolTips($(ellipses[i]));
                sum += size;
            }
        }
        $('.tooltipped').tooltip();
    }, 500)
}

var myHilitor;
$(window).on('resize', menu_upate);
$(document).ready(function() {
    var search_param = getRequestParam('searchvalue');
    if (search_param != undefined && search_param != null) {
        myHilitor = new Hilitor();
        myHilitor.apply(search_param);
    }
    menu_upate();

});

function onDivLinkClick(event, url) {
    var isLink = event.target.nodeName == "a" || event.target.nodeName == "A";
    if (!isLink) { //skip redirection to chatroom from block onclick event
        //if click occured in <a> element
        GoToUrl(url);
    }
}

function GoToUrl(url) {
    window.location.href = url;
}

function submitForm(formId, url, successCallback, failCallback) {
    var serialized = $(formId).serialize();
    $.ajax({
        url: url,
        type: 'post',
        data: serialized,
        success: successCallback,
        error: failCallback

    });
}


// First, checks if it isn't implemented yet.
if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) {
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}

//cleen text from HTML tag
function strip(html) {
    var tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || "";
}

function openDialog(event) {

    event.stopImmediatePropagation();
    event.preventDefault();
    var obj = $(event.currentTarget);

    var win = window.open(strip(obj.attr("href")), 'Dialog', 'width=600,height=400');
    win.focus()
    $(window).focus(function() {
        win.close();
    })
    return false;
}

function focusSearchInput()
{
    $("#search-input").focus();
}
