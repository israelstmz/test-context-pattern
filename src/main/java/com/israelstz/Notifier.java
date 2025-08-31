package com.israelstz;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface Notifier {

    Uni<Void> notify(Request request, List<String> vulnerabilities);

}
