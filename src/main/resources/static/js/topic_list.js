$(document).ready(function() {
    initCkEditor("ckeditor", 200);
    var sumbitButton = $('#submitcke');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();

});