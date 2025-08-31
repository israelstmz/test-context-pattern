package com.israelstz;

import lombok.Builder;
import lombok.Getter;

@Builder(builderClassName = "Builder")
@Getter
public class Request {

    private final Lang language;
    private final String name;

}
