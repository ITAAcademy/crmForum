   
  
function getXmlHttp() {
    var xmlhttp;
    try {
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
        try {
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (E) {
            xmlhttp = false;
        }
    }
    if ((!xmlhttp || !xmlhttp.upload) && typeof XMLHttpRequest != 'undefined') {
        xmlhttp = new XMLHttpRequest();
    }
    return xmlhttp;
}

   function uploadXhr(files, urlpath, successCallback, errorCallback, onProgress) {

    var xhr = getXmlHttp();

    //  обработчик для закачки
    xhr.upload.onprogress = function(event) {
        //console.log(event.loaded + ' / ' + event.total);
        onProgress(event, xhr.upload.loaded);
    }

    //  обработчики успеха и ошибки
    //  если status == 200, то это успех, иначе ошибка
    xhr.onload = xhr.onerror = function() {
        if (this.status == 200) {
            console.log("SUCCESS:" + xhr.responseText);
            successCallback(xhr.responseText);
        } else {
            console.log("error " + this.status);
            errorCallback(xhr);
        }
    };

    xhr.open("POST", urlpath);
    var boundary = String(Math.random()).slice(2);
    //  xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
    var formData = new FormData();

    for (var i = 0; i < files.length; i++) {
        formData.append("file" + i, files[i]);
    }
    xhr.send(formData);

}
 function getUrlParam( paramName ) {
            var reParam = new RegExp( '(?:[\?&]|&)' + paramName + '=([^&]+)', 'i' );
            var match = window.location.search.match( reParam );

            return ( match && match.length > 1 ) ? match[1] : null;
        }

   var uploadProgress = 0;
   $(document).ready(function() {
    $('a.file_item').click(function(e){
        e.preventDefault();
        insertLinkToFileInCkEditor(e.target.href);
        return false;
    })

   $('#upload_file_form').submit(function(event) {
    event.preventDefault();
         var form = $(this);
        var input = this.elements.file;
        var files = [];
        for (var i = 0; i < input.files.length; i++) files.push(input.files[i]);
        if (files) {
            uploadXhr(files, form.attr('action'),
                function successCallback(data) {
                    uploadProgress = 0;
                    //$scope.sendMessage("я отправил вам файл", JSON.parse(data));
                   form.get(0).reset(); 
                },
                function(xhr) {
                    uploadProgress = 0;
                    alert("SEND FAILED:" + JSON.parse(xhr.response).message);
                },
                function(event, loaded) {
                    console.log(event.loaded + ' / ' + event.totalSize);
                   uploadProgress = Math.floor((event.loaded / event.totalSize) * 100);
                });
        }
        setTimeout(function () { window.location.reload(); }, 500);
        return false;
    });
});
   function insertLinkToFileInCkEditor(link){
     if (window.opener != null && window.opener.CKEDITOR != null)
                   {                
                   var funcNum = getUrlParam( 'CKEditorFuncNum' );
                     window.opener.CKEDITOR.tools.callFunction( funcNum, link );
                    window.close();
                      }
   }