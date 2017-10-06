package biz.dealnote.messenger.media.exo;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource.Factory;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.net.Proxy;

/**
 * A {@link Factory} that produces {@link CustomHttpDataSource} instances.
 */
public final class CustomHttpDataSourceFactory extends BaseFactory {

    private final String userAgent;
    private final TransferListener<? super DataSource> listener;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final boolean allowCrossProtocolRedirects;

    private final Proxy proxy;

    /**
     * Constructs a DefaultHttpDataSourceFactory. Sets {@link
     * CustomHttpDataSource#DEFAULT_CONNECT_TIMEOUT_MILLIS} as the connection timeout, {@link
     * CustomHttpDataSource#DEFAULT_READ_TIMEOUT_MILLIS} as the read timeout and disables
     * cross-protocol redirects.
     *
     * @param userAgent The User-Agent string that should be used.
     */
    public CustomHttpDataSourceFactory(String userAgent, Proxy proxy) {
        this(userAgent, null, proxy);
    }

    /**
     * Constructs a DefaultHttpDataSourceFactory. Sets {@link
     * CustomHttpDataSource#DEFAULT_CONNECT_TIMEOUT_MILLIS} as the connection timeout, {@link
     * CustomHttpDataSource#DEFAULT_READ_TIMEOUT_MILLIS} as the read timeout and disables
     * cross-protocol redirects.
     *
     * @param userAgent The User-Agent string that should be used.
     * @param listener  An optional listener.
     * @see #CustomHttpDataSourceFactory(String, TransferListener, int, int, boolean, Proxy)
     */
    public CustomHttpDataSourceFactory(String userAgent, TransferListener<? super DataSource> listener, Proxy proxy) {
        this(userAgent, listener, CustomHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, CustomHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, false, proxy);
    }

    /**
     * @param userAgent                   The User-Agent string that should be used.
     * @param listener                    An optional listener.
     * @param connectTimeoutMillis        The connection timeout that should be used when requesting remote
     *                                    data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis           The read timeout that should be used when requesting remote data, in
     *                                    milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *                                    to HTTPS and vice versa) are enabled.
     */
    public CustomHttpDataSourceFactory(String userAgent, TransferListener<? super DataSource> listener,
                                       int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects, Proxy proxy) {
        this.userAgent = userAgent;
        this.listener = listener;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
        this.proxy = proxy;
    }

    @Override
    protected CustomHttpDataSource createDataSourceInternal(HttpDataSource.RequestProperties defaultRequestProperties) {
        return new CustomHttpDataSource(userAgent, null, listener, connectTimeoutMillis, readTimeoutMillis, allowCrossProtocolRedirects, defaultRequestProperties, proxy);
    }
}