package com.israelstz.helpers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestContextExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == TestContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return orchestrateContext();
    }

    private TestContext orchestrateContext() {
        var vulnerabilitiesMock = new VulnerabilitiesApiMock();
        var notifierMock = new NotifierMock();
        return new TestContext(vulnerabilitiesMock, notifierMock);
    }

}
