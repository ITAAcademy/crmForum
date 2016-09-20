$(document).ready(function() {
    $('.modal-trigger').leanModal({
        dismissible: true, // Modal can be dismissed by clicking outside of the modal
        opacity: .5, // Opacity of modal background
        in_duration: 300, // Transition in duration
        out_duration: 200, // Transition out duration
        starting_top: '4%', // Starting top style attribute
        ending_top: '10px;', // Ending top style attribute
        ready: function() {}, // Callback for Modal open
        complete: function() {} // Callback for Modal close
    });
      $('select').material_select();

      $('.tool-bar-wraper select')
    .focus(function() {
      debugger
        $('.tool-bar-wraper').css( "max-height", "400px" );
    })
    .blur(function() {
      $('.tool-bar-wraper').css( "max-height", "40px" );
    })

       $(".tool-bar-wraper").mousewheel(function(event, delta) {

      this.scrollLeft -= (delta * 30);
    
      event.preventDefault();
      debugger;

   });
});
