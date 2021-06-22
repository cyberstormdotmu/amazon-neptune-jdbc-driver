/*
 * Copyright <2021> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package software.amazon.neptune.sparql;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.neptune.auth.NeptuneApacheHttpSigV4Signer;
import com.amazonaws.neptune.auth.NeptuneSigV4SignerException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.jdbc.utilities.AuthScheme;
import software.amazon.jdbc.utilities.ConnectionProperties;
import java.sql.SQLException;
import java.util.Properties;

public class SparqlManualNeptuneVerificationTest {

    private static final String NEPTUNE_HOSTNAME =
            "https://iam-auth-test-lyndon.cluster-cdubgfjknn5r.us-east-1.neptune.amazonaws.com";
    private static final int NEPTUNE_DEFAULT_PORT = 8182;
    private static final String NEPTUNE_QUERY_ENDPOINT = "sparql";
    private static final String NEPTUNE_DESTINATION_STRING =
            String.format("%s:%d", NEPTUNE_HOSTNAME, NEPTUNE_DEFAULT_PORT);
    private java.sql.Connection authConnection;

    private static Properties authProperties() {
        final Properties properties = new Properties();
        properties.put(ConnectionProperties.AUTH_SCHEME_KEY, AuthScheme.IAMSigV4);
        properties.put(SparqlConnectionProperties.ENDPOINT_KEY, NEPTUNE_HOSTNAME);
        properties.put(SparqlConnectionProperties.PORT_KEY, NEPTUNE_DEFAULT_PORT);
        properties.put(SparqlConnectionProperties.QUERY_ENDPOINT_KEY, NEPTUNE_QUERY_ENDPOINT);
        return properties;
    }

    @BeforeEach
    void initialize() throws SQLException {
        // TODO: add result getters after implementing ResultSet
        authConnection = new SparqlConnection(new SparqlConnectionProperties(authProperties()));
    }

    @Test
    @Disabled
    void testSigV4Auth() throws SQLException {
        // TODO: add additional assertions for result getters
        Assertions.assertTrue(authConnection.isValid(1));
    }

    @Test
    @Disabled
    void testRunBasicAuthConnection() throws NeptuneSigV4SignerException {
        //TODO: remove this test after completing the driver and rest of this test class
        final AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
        final NeptuneApacheHttpSigV4Signer v4Signer =
                new NeptuneApacheHttpSigV4Signer("us-east-1", awsCredentialsProvider);

        final HttpClient v4SigningClient =
                HttpClientBuilder.create().addInterceptorLast(new HttpRequestInterceptor() {

                    @Override
                    public void process(final HttpRequest req, final HttpContext ctx) throws HttpException {
                        if (req instanceof HttpUriRequest) {
                            final HttpUriRequest httpUriReq = (HttpUriRequest) req;
                            try {
                                v4Signer.signRequest(httpUriReq);
                            } catch (final NeptuneSigV4SignerException e) {
                                throw new HttpException("Problem signing the request: ", e);
                            }
                        } else {
                            throw new HttpException("Not an HttpUriRequest"); // this should never happen
                        }
                    }

                }).build();

        final RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .httpClient(v4SigningClient)
                .destination(NEPTUNE_DESTINATION_STRING)
                // Query only.
                .queryEndpoint("sparql");

        final Query query = QueryFactory.create("SELECT * { ?s ?p ?o } LIMIT 100");

        // Whether the connection can be reused depends on the details of the implementation.
        // See example 5.
        try (final RDFConnection conn = builder.build()) {
            System.out.println("connecting");
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }

}