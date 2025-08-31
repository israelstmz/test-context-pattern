package com.israelstz;

import java.util.List;

public class ResponseBuilder {

    public Response buildFor(List<String> vulnerabilities) {
        var verdict = vulnerabilities != null && !vulnerabilities.isEmpty() ?
                Verdict.RISKY : Verdict.SAFE;
        var reason = vulnerabilities != null && !vulnerabilities.isEmpty() ?
                "Known vulnerability" : "No known vulnerabilities";
        return new Response(verdict, reason);
    }

}
