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

package software.aws.neptune.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.aws.neptune.jdbc.helpers.HelperFunctions;
import software.aws.neptune.jdbc.mock.MockResultSetMetaData;
import software.aws.neptune.jdbc.mock.MockStatement;

/**
 * Test for abstract ResultSetMetaData Object.
 */
public class ResultSetMetaDataTest {
    private java.sql.ResultSetMetaData resultSetMetaData;

    @BeforeEach
    void initialize() {
        resultSetMetaData = new MockResultSetMetaData();
    }

    @Test
    void testWrap() {
        HelperFunctions
                .expectFunctionDoesntThrow(() -> resultSetMetaData.isWrapperFor(MockResultSetMetaData.class), true);
        HelperFunctions.expectFunctionDoesntThrow(() -> resultSetMetaData.isWrapperFor(MockStatement.class), false);
        HelperFunctions.expectFunctionDoesntThrow(() -> resultSetMetaData.isWrapperFor(null), false);
        HelperFunctions.expectFunctionDoesntThrow(() -> resultSetMetaData.unwrap(MockResultSetMetaData.class),
                resultSetMetaData);
        HelperFunctions.expectFunctionThrows(() -> resultSetMetaData.unwrap(MockStatement.class));
    }
}
