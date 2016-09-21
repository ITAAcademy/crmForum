function checkSubmitAvailable() {
    var submitButton = $('#addTopicButton');
    if (CKEDITOR.instances.ckeditor.getData() === '' || $('#topic_name').val() === '')
        submitButton.hide();
    else submitButton.show();
}

function topicAdditionSuccess(topicId) {
    Materialize.toast('Тему успішно додано', 4000, 'green-toast') // 4000 is the duration of the toast
    GoToUrl(serverPrefix + '/view/topic/' + topicId);
}

function topicAdditionFail() {
    Materialize.toast('Не вдалось додати тему', 4000, 'red-toast') // 4000 is the duration of the toast
}

function topicAdditionSuccess(topicId) {
    Materialize.toast('Категорію успішно додано', 4000, 'green-toast') // 4000 is the duration of the toast
    GoToUrl(serverPrefix + '/view/topic/' + topicId);
}

function topicAdditionFail() {
    Materialize.toast('Не вдалось додати категорію', 4000, 'red-toast') // 4000 is the duration of the toast
}

function addTopic(event, url) {
    event.preventDefault();
    for (instance in CKEDITOR.instances)
        CKEDITOR.instances[instance].updateElement();
    submitForm('#addTopicForm', url, topicAdditionSuccess, topicAdditionFail);
}

function addCategory(event, url) {
    event.preventDefault();
    for (instance in CKEDITOR.instances)
        CKEDITOR.instances[instance].updateElement();
    submitForm('#addCategoryForm', url, topicAdditionSuccess, topicAdditionFail);
}


$(document).ready(function() {
    var cke = initCkEditor("ckeditor", 200);
    checkSubmitAvailable();

    CKEDITOR.instances.ckeditor.on('change', function(evt) {
        checkSubmitAvailable();
    });
    $('#topic_name').bind('textchange', function(event, previousText) {
        checkSubmitAvailable();
    });
    $('.topic-divided')
    .click(function(e) {
        var target = $(this).attr("target");
        var neighbor = $(this).attr("neighbor");
        var targetElm =  $('#' + target);
        targetElm.toggleClass('hide-me')
        if(targetElm.hasClass('hide-me'))
            targetElm.removeClass('half');
        else
            targetElm.addClass('half');
        var neighborElm = $('#' + neighbor);
        if(!neighborElm.hasClass('hide-me'))
            neighborElm.toggleClass('half');
        
        e.stopPropagation();
    });


    /*var submitButton = $('#addTopicButton');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();*/


});
