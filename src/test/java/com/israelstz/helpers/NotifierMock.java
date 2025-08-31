package com.israelstz.helpers;

import com.israelstz.Notifier;
import com.israelstz.Request;
import io.smallrye.mutiny.Uni;
import lombok.Getter;

import java.util.List;

public class NotifierMock implements Notifier {

    @Getter
    private String notification;

    @Override
    public Uni<Void> notify(Request request, List<String> vulnerabilities) {
        notification = vulnerabilities != null && !vulnerabilities.isEmpty() ?
                "Known vulnerability" : null;
        return Uni.createFrom().voidItem();
    }

}
