package com.israelstz;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface VulnerabilitiesApi {

    Uni<List<String>> analyze(Lang language, String lib);

}
