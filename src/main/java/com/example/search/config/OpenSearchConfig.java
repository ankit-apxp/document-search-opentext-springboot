package com.example.search.config;

import lombok.Data;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@Configuration
@ConfigurationProperties(prefix = "opensearch")
@Data
public class OpenSearchConfig {

    private String scheme = "http";
    private String host = "localhost";
    private int port = 9200;

    private String username = "";
    private String password = "";

    private String index = "articles";
    private boolean createIndexOnStart = true;

    // for https with demo security and self-signed certs (local only)
    private boolean insecureSsl = true;

    @Bean
    public OpenSearchClient openSearchClient() {
        final HttpHost httpHost = new HttpHost(scheme, host, port);
        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(httpHost);

        // Basic auth if username provided
        if (username != null && !username.isBlank()) {
            final BasicCredentialsProvider credentials = new BasicCredentialsProvider();
            credentials.setCredentials(new AuthScope(httpHost), new UsernamePasswordCredentials(username, password.toCharArray()));
            builder.setHttpClientConfigCallback(clientBuilder -> clientBuilder.setDefaultCredentialsProvider(credentials));
        }

        // Insecure SSL for local demo only (self-signed), when using https
        if ("https".equalsIgnoreCase(scheme) && insecureSsl) {
            try {
                SSLContext sslContext = SSLContextBuilder.create()
                        .loadTrustMaterial(null, (chains, authType) -> true) // trust all
                        .build();

                builder.setHttpClientConfigCallback(httpClientBuilder -> {
                    final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                            .setSslContext(sslContext)
                            .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                                @Override
                                public TlsDetails create(SSLEngine engine) {
                                    return new TlsDetails(engine.getSession(), engine.getApplicationProtocol());
                                }
                            })
                            .build();

                    final PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder
                            .create()
                            .setTlsStrategy(tlsStrategy)
                            .build();

                    return httpClientBuilder.setConnectionManager(cm);
                });
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize insecure SSL for demo", e);
            }
        }

        final OpenSearchTransport transport = builder.build();
        return new OpenSearchClient(transport);
    }
}
