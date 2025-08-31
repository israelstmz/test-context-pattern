package com.israelstz;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Response {

    private final Verdict verdict;
    private final String reason;

}
