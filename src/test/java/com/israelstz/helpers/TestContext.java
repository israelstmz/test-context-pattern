package com.israelstz.helpers;

import com.israelstz.*;
import lombok.RequiredArgsConstructor;

public class TestContext {

    private final Request.Builder requestBuilder;
    private final VulnerabilitiesApiMock vulnerabilitiesApiMock;
    private final NotifierMock notifierMock;
    private final Scanner scanner;
    private Response response;

    public TestContext(VulnerabilitiesApiMock vulnerabilitiesApiMock, NotifierMock notifierMock) {
        this.notifierMock = notifierMock;
        this.requestBuilder = Request.builder();
        this.vulnerabilitiesApiMock = vulnerabilitiesApiMock;
        this.scanner = new Scanner(vulnerabilitiesApiMock, notifierMock, new ResponseBuilder());
    }

    private void run() {
        var request = requestBuilder.build();
        response = scanner.scan(request)
                .await().indefinitely();
    }

    public static Given given(TestContext context) {
        return new Given(context);
    }

    public static When when(TestContext context) {
        return new When(context);
    }

    public static Then then(TestContext context) {
        return new Then(context);
    }
    @RequiredArgsConstructor
    public static class Given {


        private final TestContext context;

        public Given ofJavaLibrary() {
            context.requestBuilder.language(Lang.JAVA);
            return this;
        }

        public Given withKnownVulnerability() {
            context.vulnerabilitiesApiMock.mockVulnerability("CVE-2023-12345");
            return this;
        }

        public Given withoutVulnerabilities() {
            context.vulnerabilitiesApiMock.mockNoVulnerabilities();
            return this;
        }
    }
    @RequiredArgsConstructor
    public static class When {


        private final TestContext context;

        public void runScan() {
            context.run();
        }


    }
    @RequiredArgsConstructor
    public static class Then {

        private final TestContext context;

        public ResponseAsserter response() {
            return new ResponseAsserter(context.response);
        }

        public NotificationAsserter notification() {
            return new NotificationAsserter(context.notifierMock);
        }

    }
}
