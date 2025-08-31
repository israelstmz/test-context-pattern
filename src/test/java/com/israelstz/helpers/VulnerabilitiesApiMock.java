package com.israelstz.helpers;

import com.israelstz.Lang;
import com.israelstz.VulnerabilitiesApi;
import io.smallrye.mutiny.Uni;

import java.util.List;

public class VulnerabilitiesApiMock implements VulnerabilitiesApi {

    private String vulnerability;

    @Override
    public Uni<List<String>> analyze(Lang language, String lib) {
        return Uni.createFrom().item(vulnerability == null ? List.of() : List.of(vulnerability));
    }

    public void mockVulnerability(String vulnerability) {
        this.vulnerability = vulnerability;
    }

    public void mockNoVulnerabilities() {
        this.vulnerability = null;
    }
}
