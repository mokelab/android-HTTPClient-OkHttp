package com.mokelab.http;

import java.util.HashMap;
import java.util.Set;

import okhttp3.Headers;

/**
 * Implementation
 */
public class HeaderImpl extends HashMap<String, String> implements Header {
    public HeaderImpl() {
        super();
    }

    HeaderImpl(Headers originalHeaders) {
        if (originalHeaders == null) { return; }

        Set<String> names = originalHeaders.names();
        for (String name : names) {
            this.put(name, originalHeaders.get(name));
        }
    }
}
