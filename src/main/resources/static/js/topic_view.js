var config = null;
var initMessages = function(editor) {
    var messageSpan = $('.topic_message_text');
    messageSpan.each(function(index, spanElement) {
        $(this).html(getHtmlFrommBBCode($(this).text()));

    });
    messageSpan.removeClass('white-text');
    var preloader = $(".preloader-wrapper");
    preloader.remove();
    setTimeout(initSpoilers, 100);
}

function runEditPost(idPost) {
    $.post(URL_PREFIX + "operations/message/" + idPost + "/get", function(data) {
            CKEDITOR.instances["ckeditor_edit"].setData(data, {
                callback: function() {
                    CKEDITOR.instances.ckeditor_edit.setMode('source');
                    // Switch to "wysiwyg" view and be notified on completion.
                    CKEDITOR.instances.ckeditor_edit.setMode('wysiwyg');
                }
            });
            $("#edit_form").attr("action", URL_PREFIX + "operations/message/" + idPost + "/update");
            $("#edit").openModal();
            if (typeof CKEDITOR.instances["ckeditor_edit"].outdent != 'undefined')
            CKEDITOR.instances["ckeditor_edit"].outdent.exec();
        })
        .fail(function() {

        });
}

function runUpdatePost() {
    $.post($("#edit_form").attr("action"), { msg_body: CKEDITOR.instances["ckeditor_edit"].getData() }, function(data) {
            // $("#edit").closeModal();
            location.reload();
        })
        .fail(function() {
            $("#edir_err").html("Сталася помилка!!!");
        });
}
var processSubmitButtonAvailability = function(text) {
    var sumbitButton = $('#submitcke');
    if (sumbitButton != null) {
        if (text === '')
            sumbitButton.hide();
        else
            sumbitButton.show();
    }
}

$(document).ready(function() {
    processSubmitButtonAvailability('');

    var instance = initCkEditor("ckeditor_edit", 280);
    if (instance != null) {
        instance.on("instanceReady", function() {
            config = instance.config;
            initMessages(instance);
        });
    }
    var ckEditor = initCkEditor("ckeditor", 200).on('change', function(evt) {
            processSubmitButtonAvailability(evt.editor.getData());

        });;




    var offset = 220;
    var duration = 500;
    jQuery(window).scroll(function() {
        if (jQuery(this).scrollTop() > offset) {
            jQuery('.back-to-top').fadeIn(duration);
        } else {
            jQuery('.back-to-top').fadeOut(duration);
        }
    });

    jQuery('.back-to-top').click(function(event) {
        event.preventDefault();
        jQuery('html, body').animate({ scrollTop: 0 }, duration);
        return false;
    })

});
function messageAdditionSuccess(message){
  Materialize.toast('Повідомлення успішно додано', 4000,'green-toast') // 4000 is the duration of the toast
  GoToUrl(serverPrefix+'/view/topic/{0}/{1}'.format(message.topic,message.page));
}
function messageAdditionFail(){
 Materialize.toast('Не вдалось додати повідомлення', 4000,'red-toast') // 4000 is the duration of the toast
}
function addMessage(event,url){
    event.preventDefault();
    for ( instance in CKEDITOR.instances )
    CKEDITOR.instances[instance].updateElement();
submitForm('#addMessageForm',url,messageAdditionSuccess,messageAdditionFail);
}
function quotation(messageIndex,messageAuthorName){
var msgElmSpanName = '#topicMessage'+messageIndex;
var elm = $(msgElmSpanName);
alert('Ви цитуєте:'+elm.html());
//CKEDITOR.instances.ckeditor.setData('');
var prefix = "<blockquote><b>{0}:</b><br/>".format(messageAuthorName);
var suffix = "</blockquote> "//need space to make wrap on new line;
var htmlCode = prefix +elm.html()+ suffix;
CKEDITOR.instances.ckeditor.insertHtml(htmlCode);
}