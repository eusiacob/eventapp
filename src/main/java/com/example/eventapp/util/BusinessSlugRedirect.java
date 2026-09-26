package com.example.eventapp.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.RedirectView;

public final class BusinessSlugRedirect {
    private BusinessSlugRedirect() {}

    public static RedirectView to(String path) {
        RedirectView view = new RedirectView(path, true);
        view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        view.setExposeModelAttributes(false);
        view.setPropagateQueryParams(true);
        return view;
    }
}
