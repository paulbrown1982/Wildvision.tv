(function() {
    if (!$ || !wildvision) {
        console.log('No jQuery or wildvision base, exiting.');
    }

    function postFilmImpressionInsight() {
        if (!wildvision.film) return;

        $.ajax('/film/impression/' + wildvision.film, {
            method: 'POST'
        });
    }

    function postPresenterImpressionInsight() {
        if (!wildvision.presenter) return;

        $.ajax('/presenter/impression/' + wildvision.presenter, {
            method: 'POST'
        });
    }

    function postNewsletterImpressionInsight() {
        if (!wildvision.newsletter) return;

        $.ajax('/newsletter/impression/' + wildvision.newsletter, {
            method: 'POST'
        });
    }

    function setupFilmViewInsight() {
        if (!wildvision.film) return;

        var overIframe = false,
            posted = false;

        $("film iframe").each(function(index, el) {
            var film = $(el);
            film.on("mouseenter", function() {
                overIframe = true;
            });
            film.on("mouseleave", function() {
                overIframe = false;
            });
        });
        $(window).blur(function(event) {
            if (overIframe && !posted) {
                $.ajax('/film/view/' + wildvision.film, {
                    method: 'POST'
                });
                posted = true;
            }
        });
    }

    function setIframeHeights() {
        var filmIframes = $("film iframe"),
            newsletterIframes = $("newsletter iframe");

        $(window).resize(function() {
            filmIframes.each(function() {
                var iframe = $(this);
                iframe.height(iframe.width() * 9/16);
            });
            newsletterIframes.each(function() {
                var iframe = $(this);
                iframe.height(iframe.width() * 16/9);
            });
        });
        $(window).trigger('resize');
    }

    $(function() {
        postFilmImpressionInsight();
        postPresenterImpressionInsight();
        postNewsletterImpressionInsight();
        setupFilmViewInsight();
        setIframeHeights();
    });

}());