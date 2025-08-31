## Beyond Class-Level Testing - The Orchestration Problem

As experienced developers practicing Test-Driven Development with hexagonal architecture, we've evolved beyond testing individual classes. We understand the need to test broader units of business logic - complete flows from drivers through the domain core to adapters.

However, **the practice of implementing this understanding still weighs heavily on our test classes**:

- **Orchestration complexity**: Setting up the entire flow requires significant boilerplate
- **Mocking ceremony**: Each test method needs extensive mock setup and configuration
- **Framework coupling**: Tests become tied to specific mocking frameworks or paradigms like async programming
- **Noise vs. Signal**: The actual business scenario gets buried in technical setup code

We know *what* to test (business flows, not classes), but the *how* clutters our test methods with orchestration concerns that shouldn't be there.

## A Test Context Pattern: Pure Business Language Testing

A Test Context pattern solves the orchestration problem by **extracting all technical concerns from test methods**. It leverages JUnit 5 extensions and rich context objects to expose only business-domain language, leaving test methods to express pure business scenarios.

### The Core Innovation: Orchestration Extraction

The pattern's breakthrough is moving all orchestration, mocking, and assertion logic **behind the scenes**:

- **JUnit 5 Extension**: Handles context creation and dependency wiring
- **Rich Context Object**: Provides business-domain vocabulary for test scenarios
- **Hidden Complexity**: All technical setup, async handling, and mock configuration disappears
- **Domain Language**: Tests speak only in business terms, not technical implementation

## The Pattern in Action

Let's examine a vulnerability scanning system that demonstrates the Test Context pattern. The (quarkus-mutiny) app gets a request to scan a code library and returns a verdict (Safe or Risky) based on known vulnerabilities. If found risky, it sends a notification.

### The Test Class - Pure Business Logic

```java
@ExtendWith(TestContextExtension.class)
@DisplayName("Vulnerabilities scan")
class VulnerabilityTest {

    @Test
    @DisplayName("When scanning a Java library with a known vulnerability - a 'Risky' verdict should be returned and a notification should be sent")
    void javaLibWithVulnerability(TestContext context) {
        given(context)
            .ofJavaLibrary()
            .withKnownVulnerability();

        when(context).runScan();

        then(context).response()
            .verdictShouldBe(Verdict.RISKY)
            .reasonShouldContain("Known vulnerability");
        then(context).notification()
            .shouldBeSent()
            .containingText("Known vulnerability");
    }

    @Test
    @DisplayName("When scanning a Java library without any known vulnerability - a 'Safe' verdict should be returned without any notification")
    void javaLibWithNoVulnerabilities(TestContext context) {
        given(context)
            .ofJavaLibrary()
            .withoutVulnerabilities();

        when(context).runScan();

        then(context).response()
            .verdictShouldBe(Verdict.SAFE);
        then(context).notification()
            .shouldNotBeSent();
    }
}
```

Notice how the test method contains **zero orchestration code**:

- No mock setup or configuration
- No async handling (Mutiny's `await().indefinitely()` is hidden)
- No framework-specific APIs
- No technical vocabulary

The test speaks purely in **business-domain language**: "Java library", "known vulnerability", "risky verdict", "notification sent". This is the pattern's key achievement - **complete separation of business intent from technical orchestration**.

### The TestContext - Where Orchestration Lives

All the technical complexity that would normally clutter your test methods gets encapsulated in a single TestContext:

```java
public class TestContext {

    private final Request.Builder requestBuilder;
    private final VulnerabilitiesApiMock vulnerabilitiesApiMock;
    private final NotifierMock notifierMock;
    private final Scanner scanner;
    private Response response;

    // Constructor handles all dependency wiring - hidden from tests
    public TestContext(VulnerabilitiesApiMock vulnerabilitiesApiMock, NotifierMock notifierMock) {
        this.notifierMock = notifierMock;
        this.requestBuilder = Request.builder();
        this.vulnerabilitiesApiMock = vulnerabilitiesApiMock;
        // Real business logic assembly - no mocking ceremony needed in tests
        this.scanner = new Scanner(vulnerabilitiesApiMock, notifierMock, new ResponseBuilder());
    }

    // Async complexity hidden here
    private void run() {
        var request = requestBuilder.build();
        response = scanner.scan(request).await().indefinitely();
    }

    // Business-domain vocabulary exposed through fluent API
    public static Given given(TestContext context) {
        return new Given(context);
    }

    public static When when(TestContext context) {
        return new When(context);
    }

    public static Then then(TestContext context) {
        return new Then(context);
    }
}
```

The TestContext becomes the **orchestration layer** - it knows about mocks, async handling, object construction, and framework details. Test methods remain blissfully unaware of these concerns.
```java
    // Given-When-Then as business language constructs
    @RequiredArgsConstructor
    public static class Given {
        private final TestContext context;

        // Business vocabulary - no mention of mocks or technical setup
        public Given ofJavaLibrary() {
            context.requestBuilder.language(Lang.JAVA);
            return this;
        }

        public Given withKnownVulnerability() {
            // Mock orchestration hidden behind business language
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
            // All async complexity hidden here
            context.run();
        }
    }

    @RequiredArgsConstructor
    public static class Then {
        private final TestContext context;

        // Business-focused assertions
        public ResponseAsserter response() {
            return new ResponseAsserter(context.response);
        }

        public NotificationAsserter notification() {
            return new NotificationAsserter(context.notifierMock);
        }
    }
```

And here's the Extension itself - wiring the test context creation to the tests:

```java
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
```

### The Pattern's Brilliance: Business Language DSL

The Given-When-Then classes create a **Domain Specific Language** for your business context. Test authors think in business terms:

- `ofJavaLibrary()` instead of `requestBuilder.language(Lang.JAVA)`  
- `withKnownVulnerability()` instead of mock setup ceremony
- `runScan()` instead of async orchestration details

**The technical how is completely separated from the business what.**

### Domain-Tailored Mocks and Assertions

Instead of generic mocking frameworks that leak technical details into tests, we create **business-focused test doubles**:

```java
// Business-focused mock - no Mockito ceremony
public class VulnerabilitiesApiMock implements VulnerabilitiesApi {
    private String vulnerability;

    @Override
    public Uni<List<String>> analyze(Lang language, String lib) {
        return Uni.createFrom().item(vulnerability == null ? List.of() : List.of(vulnerability));
    }

    // Business vocabulary for test setup
    public void mockVulnerability(String vulnerability) {
        this.vulnerability = vulnerability;
    }

    public void mockNoVulnerabilities() {
        this.vulnerability = null;
    }
}
```

**The mock speaks business language** - no `when().thenReturn()` or generic mocking syntax. Test authors configure behavior using domain concepts like "mock vulnerability" and "mock no vulnerabilities".

### Business-Language Assertions

Custom asserters continue the business language theme:

```java
@RequiredArgsConstructor
public class ResponseAsserter {
    private final Response response;

    // Business assertions - not technical verification
    public ResponseAsserter verdictShouldBe(Verdict expectedVerdict) {
        var actualVerdict = response.getVerdict();
        assertNotNull(actualVerdict);
        assertEquals(expectedVerdict, actualVerdict);
        return this;
    }

    public ResponseAsserter reasonShouldContain(String expectedReason) {
        var actualReason = response.getReason();
        assertNotNull(actualReason);
        assertTrue(actualReason.contains(expectedReason));
        return this;
    }
}
```

Again, **pure business vocabulary**: "verdict should be", "reason should contain". No technical assertion details leak into the test methods.

## The Real Achievement: Orchestration Extraction

What makes this pattern revolutionary for experienced hexagonal architecture practitioners is the **complete separation of concerns**:

**Test Methods**: Express only business scenarios in domain language
**TestContext**: Handles all orchestration, mocking, async complexity  
**JUnit Extension**: Manages context lifecycle and dependency injection

### Before: Orchestration Pollution
```java
// What our tests used to look like
@Test
void scanWithVulnerability() {
    // Orchestration noise cluttering business intent
    var vulnerabilitiesApi = mock(VulnerabilitiesApi.class);
    var notifier = mock(Notifier.class);
    when(vulnerabilitiesApi.analyze(JAVA, "lib"))
        .thenReturn(Uni.createFrom().item(List.of("CVE-123")));
    when(notifier.notify(any(), any()))
        .thenReturn(Uni.createFrom().voidItem());
    
    var scanner = new Scanner(vulnerabilitiesApi, notifier, new ResponseBuilder());
    var request = Request.builder().language(JAVA).name("lib").build();
    
    // Finally - the actual business scenario buried in setup
    var response = scanner.scan(request).await().indefinitely();
    
    assertEquals(Verdict.RISKY, response.getVerdict());
    verify(notifier).notify(request, List.of("CVE-123"));
}
```

**The business intent is drowning in orchestration details.**

### After: Pure Business Language
```java
@Test
void javaLibWithVulnerability() {
    given(context)
        .ofJavaLibrary()
        .withKnownVulnerability();

    when(context).runScan();

    then(context).response()
        .verdictShouldBe(Verdict.RISKY);
    then(context).notification()
        .shouldBeSent();
}
```

**The business scenario is crystal clear - zero orchestration noise.**

## Key Benefits for Advanced Practitioners

### 🎯 **Pure Business Focus**
Test methods become **executable business specifications**. No technical noise distracts from business intent.

### 🚀 **Dramatic Productivity Gains**
Once TestContext infrastructure exists, writing tests becomes incredibly fast - just express business scenarios.

### 🛡️ **True Refactoring Freedom**
Internal refactoring never breaks tests. Change implementations, move classes, restructure packages - tests remain green.

### 🏗️ **Architectural Enforcement**
The pattern naturally enforces clean architecture - it's hard to write bad tests when orchestration is extracted.

### 📚 **Living Documentation**
Tests become the definitive business specification - readable by domain experts, not just developers.

### 🔄 **Framework Independence**
Switch from Mutiny to Reactor, Mockito to custom mocks - test methods remain unchanged.

## When to Use This Pattern

The Test Context pattern is ideal when:

- You're practicing hexagonal architecture
- You have complex business flows spanning multiple classes
- You want test immunity to refactoring
- Your team values readable, maintainable tests
- You're building domain-rich applications

## Conclusion

The Test Context pattern represents a possible evolution in hexagonal architecture testing. By moving all orchestration concerns into a dedicated context and leveraging JUnit 5 extensions, we achieve tests that are:

- **Fast** like unit tests
- **Complete** like integration tests
- **Maintainable** through refactoring immunity
- **Readable** like business specifications

For developers practicing TDD with hexagonal architecture, this pattern offers a compelling path forward - taking your testing game to the next level while dramatically reducing maintenance overhead.

The pattern transforms testing from a necessary overhead into a powerful design tool that drives better architecture and provides living documentation of your business rules. Testing becomes more fun then ever!

---

*Full implementation is available in the [GitHub repository](https://github.com/your-username/test-context-pattern)*