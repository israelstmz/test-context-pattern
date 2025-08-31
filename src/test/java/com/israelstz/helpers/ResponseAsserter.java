package com.israelstz.helpers;

import com.israelstz.Response;
import com.israelstz.Verdict;
import lombok.RequiredArgsConstructor;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class ResponseAsserter {

    private final Response response;

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
