     //GLOBAL VARIABLES
        var nextPage = 1;
        var categoriesUpdatingInProgress = false;
        ///////////////////

function checkSubmitAvailable() {
    var submitButton = $('#addTopicButton');
    if (CKEDITOR.instances.ckeditor==null || CKEDITOR.instances.ckeditor.getData() === '' || $('#topic_name').val() === '')
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

function categoryAdditionSuccess(categoryId) {
    Materialize.toast('Категорію успішно додано', 4000, 'green-toast') // 4000 is the duration of the toast
    GoToUrl(serverPrefix + '/view/category/' + categoryId);
}

function categoryAdditionFail() {
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
    submitForm('#addCategoryForm', url, categoryAdditionSuccess, categoryAdditionFail);
}

   function requestCategories(page){
     var path = serverPrefix+"/api/category/get/sub_categories?page="+page;
     if (currentCategory!=null) path += "&categoryId="+currentCategory;
            $.ajax({
  url:path,
  method:'GET',
  beforeSend:function(){
    categoriesUpdatingInProgress = true;
  }
}).done(function(data){
    if (data!=null){
        var categories = data;
       /* for (var i = 0; i < categories.length; i++){
                   $('ul#categories').append(categories[i].name);
        }*/
        if (categories.length>0)
        processMustacheTemplate("categorie_template",categories,addCategoriesToList)
        else {
            if(page==1){
                   processMustacheTemplate("no_categorie_template",categories,addCategoriesToList);
                    if(!$("#categories").hasClass("half"))
                    $("#categories").addClass("half");
               }
        }
       
       categoriesUpdatingInProgress=false;
    }
});
        }
    function processMustacheTemplate(templateName,data, callback){
        var result = null;
        $.get(serverPrefix+'/mst/'+templateName+".mst", function(template, textStatus, jqXhr) {
        callback(template,data);
        });
    }
    function addCategoriesToList(template,categories){
        var data= {"categories":categories,"serverPrefix":serverPrefix};
        var compiledTemplate = Handlebars.compile(template);
        var html = compiledTemplate(data);
        var output = $('#categories');
        output.append(html);
    }


$(document).ready(function() {
    var cke = initCkEditor("ckeditor", 200);
    checkSubmitAvailable();
    if (CKEDITOR.instances.ckeditor!=null)
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
            var targetElm = $('#' + target);
            var neighborElm = $('#' + neighbor);

            targetElm.toggleClass('hide-me')
            if (targetElm.hasClass('hide-me')) {
                targetElm.removeClass('half');
                neighborElm.removeClass('half');
            } else {
                if (!neighborElm.hasClass('hide-me')) {
                    targetElm.addClass('half');
                    neighborElm.addClass('half');
                }
            }
            e.stopPropagation();
        });

        requestCategories(nextPage++);





     
        $('ul#categories').scroll(function(){
            console.log('windows is scrolled');
        if($('ul#categories').scrollTop()+$('ul#categories').height() >= $('ul#categories').height() - 200 && !categoriesUpdatingInProgress){
console.log('updating categories records');
requestCategories(nextPage++);
}
        });
  
        //$('categories')
    /*var submitButton = $('#addTopicButton');
    if (typeof sumbitButton.val() != 'undefined' && sumbitButton.val().length == 0)
        sumbitButton.hide();*/


});

