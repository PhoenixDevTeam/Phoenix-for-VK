package com.google.android.vending.licensing.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by golde on 01.02.2017.
 */

public class URLUtils {
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";

    public static List<NameValuePair> parse(final URI uri, final String encoding) {
        List<NameValuePair> result = Collections.emptyList();
        final String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            result = new ArrayList<>();
            parse(result, new Scanner(query), encoding);
        }
        return result;
    }

    public static void parse(final List<NameValuePair> parameters, final Scanner scanner, final String encoding) {
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2)
                throw new IllegalArgumentException("bad parameter");

            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2)
                value = decode(nameValue[1], encoding);
            parameters.add(new NameValuePair(name, value));
        }
    }


    private static String decode (final String content, final String encoding) {
        try {
            return URLDecoder.decode(content, encoding != null ? encoding : DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }
}

