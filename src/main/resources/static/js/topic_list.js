$(document).ready(function() {
    initCkEditor("ckeditor", 200);
    var sumbitButtonCke = $('#submitcke');
        if (typeof sumbitButtonCke.val() != 'undefined' && sumbitButtonCke.val().length == 0)
        sumbitButtonCke.hide();
   
    /*var submitButton = $('#addTopicButton');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();*/


});