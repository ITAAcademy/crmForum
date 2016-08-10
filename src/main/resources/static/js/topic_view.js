var config = null;
var initMessages = function() {
    var topicContent = $(".card-panel");
    topicContent.each(function(index) {
        var contentElement = $(this);
          var messageSpan = contentElement.find('.topic_message_text');
    messageSpan.each(function(index,spanElement) {
        var spanElement = $(this);
        var bbCodedText = spanElement.text();
        spanElement.removeClass('white-text');
        /*var htmlCodedTextObj = XBBCODE.process({
            text: bbCodedText,
            removeMisalignedTags: false,
            addInLineBreaks: false
        });
        $(this).replaceWith('<span>' + htmlCodedTextObj.html + '</span>');*/
       spanElement.html(getHtmlFrommBBCode(bbCodedText));
       
    });
            var preloader = contentElement.find(".preloader-wrapper");
             preloader.remove();
    });
  
}

function runEditPost(idPost) {
    $.post(URL_PREFIX + "operations/message/" + idPost + "/get", function(data) {
            CKEDITOR.instances["ckeditor_edit"].setData(data);
            $("#edit_form").attr("action", URL_PREFIX + "operations/message/" + idPost + "/update");
            $("#edit").openModal();
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

$(document).ready(function() {
    var instance = initCkEditor("ckeditor_edit", 280);
    if (instance != null) {
        instance.on("instanceReady", function() {
            config = instance.config;
            initMessages();
        });
    }
});
