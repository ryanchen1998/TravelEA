/*
 * Changing the Navbar colour depending on the situation
 */
$(document).ready(function() {
    var scroll_start = 0;
    var startchange = $('#startchange');
    var offset = startchange.offset();
    $(document).scroll(function() {
        scroll_start = $(this).scrollTop();
        if(scroll_start > offset.top  - $('#navbar').outerHeight()) {
            $('#navbar').css('background-color', '#5283B7');
        } else {
            $('#navbar').css('background-color', 'transparent');
        }
    });
});

$('.collapse').on('show.bs.collapse', function () {
    $('#navbar').css('background-color', '#5283B7');
})

$('.collapse').on('hide.bs.collapse', function () {
    $('#navbar').css('background-color', 'transparent');
})