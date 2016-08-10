function checkSubmitAvailable(){
       var submitButton = $('#addTopicButton');
if ( CKEDITOR.instances.ckeditor.getData() ==='' ||  $('#topic_name').val()==='')
    submitButton.hide();
else submitButton.show();
}

$(document).ready(function() {
    var cke = initCkEditor("ckeditor", 200);
    checkSubmitAvailable();

        CKEDITOR.instances.ckeditor.on('change', function(evt) { 
 checkSubmitAvailable();
});
        $('#topic_name').bind('textchange', function (event, previousText) {
    checkSubmitAvailable();
});
   
    /*var submitButton = $('#addTopicButton');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();*/


});