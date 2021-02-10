/*
 * Copyright <2020> Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.jdbc.utilities;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Test for ConnectionProperties.
 */
class ConnectionPropertiesTest {
    private ConnectionProperties connectionProperties;

    // TODO - add test for other properties

    @Test
    void testAuthScheme() throws SQLException {
        final List<String> validAuthSchemes = ImmutableList.of(
                "NONE", "none", "IAMSigV4", "iamSIGV4", "IAMRole", "IaMRoLe");
        final List<String> invalidAuthSchemes = ImmutableList.of(
                "-1;", "100;", "46hj7;", "foo;");

        // Verify default property is set.
        connectionProperties = new ConnectionProperties();
        Assertions.assertEquals(ConnectionProperties.DEFAULT_AUTH_SCHEME, connectionProperties.getAuthScheme());

        final Properties properties = new Properties();
        connectionProperties = new ConnectionProperties(properties);
        Assertions.assertEquals(ConnectionProperties.DEFAULT_AUTH_SCHEME, connectionProperties.getAuthScheme());

        // Verify valid property value is set.
        for (final String validAuthScheme : validAuthSchemes) {
            // Convert string to enum.
            Assertions.assertNotNull(
                    AuthScheme.fromString(validAuthScheme)
            );
            // Set properties through constructor.
            properties.put(ConnectionProperties.AUTH_SCHEME_KEY, validAuthScheme);
            Assertions.assertDoesNotThrow(() -> {
                connectionProperties = new ConnectionProperties(properties);
            });
            Assertions.assertEquals(AuthScheme.fromString(validAuthScheme), connectionProperties.getAuthScheme());
            // Set property directly.
            connectionProperties = new ConnectionProperties();
            connectionProperties.setAuthScheme(AuthScheme.fromString(validAuthScheme));
            Assertions.assertEquals(AuthScheme.fromString(validAuthScheme), connectionProperties.getAuthScheme());
        }

        // Verify invalid property value throws error.
        for (final String invalidAuthScheme : invalidAuthSchemes) {
            // Convert string to enum.
            Assertions.assertNull(
                AuthScheme.fromString(invalidAuthScheme)
            );
            // Set properties through constructor.
            properties.setProperty(ConnectionProperties.AUTH_SCHEME_KEY, invalidAuthScheme);
            Assertions.assertThrows(SQLException.class,
                    () -> connectionProperties = new ConnectionProperties(properties));
            // Set property directly.
            Assertions.assertThrows(SQLException.class,
                    () -> connectionProperties.setAuthScheme(AuthScheme.fromString(invalidAuthScheme)));
        }
    }
}