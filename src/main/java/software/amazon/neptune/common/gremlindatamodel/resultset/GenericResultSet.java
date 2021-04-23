/*
 * Copyright <2020> Amazon.com, final Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, final Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, final WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, final either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package software.amazon.neptune.common.gremlindatamodel.resultset;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic ResultSet class.
 */
public abstract class GenericResultSet extends software.amazon.jdbc.ResultSet implements java.sql.ResultSet {
    /**
     * OpenCypherResultSet constructor, initializes super class.
     *
     * @param statement Statement Object.
     * @param columns   Columns for result.
     * @param rowCount  Row count for result.
     */
    public GenericResultSet(final java.sql.Statement statement, final List<String> columns, final int rowCount) {
        super(statement, columns, rowCount);
    }

    @Override
    protected void doClose() {
    }

    @Override
    protected int getDriverFetchSize() {
        // Do we want to update this or statement?
        return 0;
    }

    @Override
    protected void setDriverFetchSize(final int rows) {
        // Do we want to update this or statement?
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
}
