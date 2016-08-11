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
function topicAdditionSuccess(topicId){
  Materialize.toast('Тему успішно додано', 4000,'green-toast') // 4000 is the duration of the toast
  GoToUrl(serverPrefix+'/view/topic/'+topicId);
}
function topicAdditionFail(){
 Materialize.toast('Не вдалось додати тему', 4000,'red-toast') // 4000 is the duration of the toast
}
function addTopic(event,url){
    event.preventDefault();
submitForm('#addTopicForm',url,topicAdditionSuccess,topicAdditionFail);
}