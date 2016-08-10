var initSpoilers = function() {
    $('div.spoiler-title').click(function() {
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
