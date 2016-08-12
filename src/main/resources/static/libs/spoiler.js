var initSpoilers = function(el) {
    if(el == undefined || el == null)
        el = $('div.spoiler-title');
    else
        el = $(el).find('div.spoiler-title');
    
    el.click(function() {
        $(this)
            .children()
            .first()
            .toggleClass('show-icon')
            .toggleClass('hide-icon');
        $(this)
            .parent().children().last().toggleClass('closed_spoiler_content');
    });
    $('div.spoiler-title').each(function() {
        $(this).children()
            .first()
            .toggleClass('show-icon')
            .toggleClass('hide-icon');
        $(this).parent().children().last().toggleClass('closed_spoiler_content');
    });
};
