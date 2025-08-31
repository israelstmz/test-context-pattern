package com.israelstz;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Scanner {

    private final VulnerabilitiesApi vulnerabilitiesApi;
    private final Notifier notifier;
    private final ResponseBuilder responseBuilder;

    public Uni<Response> scan(Request request) {
        return analyze(request)
                .call(vulnerabilities -> notifyIfNeeded(request, vulnerabilities))
                .map(this::buildResponse);
    }

    private Uni<List<String>> analyze(Request request) {
        return vulnerabilitiesApi.analyze(request.getLanguage(), request.getName());
    }

    private Uni<Void> notifyIfNeeded(Request request, List<String> vulnerabilities) {
        return notifier.notify(request, vulnerabilities);
    }

    private Response buildResponse(List<String> vulnerabilities) {
        return responseBuilder.buildFor(vulnerabilities);
    }

}
