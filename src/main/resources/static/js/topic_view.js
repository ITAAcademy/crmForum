var config = null;
var initMessages = function(editor) {
    var topicContent = $(".card-panel");
    topicContent.each(function(index) {
        var contentElement = $(this);
        var messageSpan = contentElement.find('.topic_message_text');
        messageSpan.each(function(index, spanElement) {
            var spanElement = $(this);
            var bbCodedText = spanElement.text();
            spanElement.removeClass('white-text');
            spanElement.html(getHtmlFrommBBCode(bbCodedText));

        });
        var preloader = contentElement.find(".preloader-wrapper");
        preloader.remove();
        initSpoilers();
    });

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
            //CKEDITOR.instances["ckeditor_edit"].outdent.exec();
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
    initCkEditor("ckeditor", 200);
    var instance = initCkEditor("ckeditor_edit", 280);
    if (instance != null) {
        instance.on("instanceReady", function() {
            config = instance.config;
            initMessages(instance);
        });
    }
    initCkEditor("ckeditor", 200);

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
