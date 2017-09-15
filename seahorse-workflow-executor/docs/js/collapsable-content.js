$(function() {
    $(".collapsable-content").each(function() {
        var element = this;
        var elementId = $(element).attr("id");

        $(element).hide();
        $(element).wrap(function() {
            return '<div markdown="1"></div>';
        });

        var button = $('<button type="button" class="btn btn-default collapse-trigger">' +
            'Show</button>');
        button.click(function() {
            var content = $(this).text();
            $(this).text(content == "Show" ? "Hide" : "Show");
            $(element).toggle();
        });

        $(element).before(button);
    });
});
