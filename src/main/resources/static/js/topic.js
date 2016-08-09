var initCkEditor = function(id, iheight) {
    if ($('#' + id).length == 0)
        return null;
    var editor = CKEDITOR.replace(id, {
        height: iheight,
        //resize_enabled : false,
        // Add plugins providing functionality popular in BBCode environment.
        extraPlugins: 'bbcode,autogrow,smiley,font,colorbutton,save,spoiler',
        // Remove unused plugins.
        //removePlugins: 'filebrowser,format,horizontalrule,pastetext,pastefromword,scayt,showborders,stylescombo,table,tabletools,wsc',
        removePlugins: 'resize,filebrowser,format,horizontalrule,pastetext,pastefromword,scayt,showborders,stylescombo,table,tabletools,wsc',
        // Remove unused buttons.
        removeButtons: 'Anchor,BGColor,Font,Strike,Subscript,Superscript',
        // Width and height are not supported in the BBCode format, so object resizing is disabled.
        disableObjectResizing: true,
        // Define font sizes in percent values.
        fontSize_sizes: "30/30%;50/50%;100/100%;120/120%;150/150%;200/200%;300/300%",
        // Strip CKEditor smileys to those commonly used in BBCode.
        smiley_images: [
            'regular_smile.png', 'sad_smile.png', 'wink_smile.png', 'teeth_smile.png', 'tongue_smile.png',
            'embarrassed_smile.png', 'omg_smile.png', 'whatchutalkingabout_smile.png', 'angel_smile.png',
            'shades_smile.png', 'cry_smile.png', 'kiss.png'
        ],
        smiley_descriptions: [
            'smiley', 'sad', 'wink', 'laugh', 'cheeky', 'blush', 'surprise',
            'indecision', 'angel', 'cool', 'crying', 'kiss'
        ],
        on: {
            change: function(evt) {
                var sumbitButton = $('#submitcke');
                if (evt.editor.getData() === '')
                    sumbitButton.hide();
                else
                    sumbitButton.show();
                //CKEDITOR.dom.element.createFromHtml( '<p style="color:red">Editor contents changed!</p>' ).appendTo( CKEDITOR.document.getBody() );
            }
        }
    });
    return editor;
}
var config = null;
var initMessages = function() {
    var messagesSpans = $(".topic_message_text");
    messagesSpans.each(function(index) {
        var bbCodedText = $(this).text();
        /*var htmlCodedTextObj = XBBCODE.process({
            text: bbCodedText,
            removeMisalignedTags: false,
            addInLineBreaks: false
        });
        $(this).replaceWith('<span>' + htmlCodedTextObj.html + '</span>');*/
        $(this).html(getHtmlFrommBBCode(bbCodedText));
    });
}

function getHtmlFrommBBCode(code) {
    var fragment = CKEDITOR.htmlParser.fragment.fromBBCode(code);
    var writer = new CKEDITOR.htmlParser.basicWriter();
    fragment.writeHtml(writer, CKEDITOR.createBBcodeFilter);
    return writer.getHtml();
}
$(document).ready(function() {
    var instance = initCkEditor("ckeditor_edit", 280);
    if (instance != null) {
        instance.on("instanceReady", function() {
            // insert code to run after editor is ready
            // alert("ckEditor is ready");
            config = instance.config;
            debugger;
            initMessages();
        });
    }

    initCkEditor("ckeditor", 200);
    var sumbitButton = $('#submitcke');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();

});
