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

package software.amazon.jdbc;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.jdbc.utilities.CastHelper;
import software.amazon.jdbc.utilities.QueryExecutor;
import software.amazon.jdbc.utilities.SqlError;
import software.amazon.jdbc.utilities.SqlState;
import software.amazon.jdbc.utilities.Warning;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of Statement for JDBC Driver.
 */
public class Statement implements java.sql.Statement {
    private static final Logger LOGGER = LoggerFactory.getLogger(Statement.class);
    private final java.sql.Connection connection;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private int maxFieldSize = 0;
    private long largeMaxRows = 0;
    private boolean shouldCloseOnCompletion = false;
    private SQLWarning warnings;
    private int fetchSize = 0;
    private ResultSet resultSet;

    @Getter
    private final QueryExecutor queryExecutor;

    /**
     * Constructor for seeding the statement with the parent connection.
     *
     * @param connection    The parent connection.
     * @param queryExecutor The query executor.
     * @throws SQLException if error occurs when get type map of connection.
     */
    public Statement(final java.sql.Connection connection, final QueryExecutor queryExecutor) throws SQLException {
        this.connection = connection;
        this.warnings = null;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public void addBatch(final String sql) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public void cancel() throws SQLException {
        verifyOpen();
        queryExecutor.cancelQuery();
    }

    @Override
    public void clearBatch() throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public void clearWarnings() throws SQLException {
        verifyOpen();
        warnings = null;
    }

    @Override
    public void close() throws SQLException {
        if (!this.isClosed.getAndSet(true)) {
            LOGGER.debug("Cancel any running queries.");
            try {
                queryExecutor.cancelQuery();
            } catch (final SQLException ignored) {
            }

            if (this.resultSet != null) {
                LOGGER.debug("Close open result set.");
                this.resultSet.close();
            }
        }
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        verifyOpen();
        this.shouldCloseOnCompletion = true;
    }

    @Override
    public boolean execute(final String sql) throws SQLException {
        this.resultSet = executeQuery(sql);
        return true;
    }

    // Add default execute stubs.
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        // Ignore the auto-generated keys as INSERT is not supported and auto-generated keys are not supported.
        return execute(sql);
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        // Ignore the auto-generated keys as INSERT is not supported and auto-generated keys are not supported.
        return execute(sql);
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        // Ignore the auto-generated keys as INSERT is not supported and auto-generated keys are not supported.
        return execute(sql);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long executeLargeUpdate(final String sql) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long executeLargeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long executeLargeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long executeLargeUpdate(final String sql, final String[] columnNames) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public java.sql.Connection getConnection() throws SQLException {
        verifyOpen();
        return connection;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        verifyOpen();
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        verifyOpen();
        if (direction != ResultSet.FETCH_FORWARD) {
            throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
        }
    }

    @Override
    public int getFetchSize() throws SQLException {
        verifyOpen();
        return fetchSize;
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {
        verifyOpen();
        if (rows < 1) {
            throw SqlError.createSQLException(
                    LOGGER,
                    SqlState.DATA_EXCEPTION,
                    SqlError.INVALID_FETCH_SIZE,
                    rows);
        }

        // Silently truncate to the maximum number of rows that can be retrieved at a time.
        this.fetchSize = Math.min(rows, queryExecutor.getMaxFetchSize());
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        verifyOpen();
        // Maximum result size is 1MB, so therefore a singe row cannot exceed this.
        return largeMaxRows;
    }

    @Override
    public void setLargeMaxRows(final long max) throws SQLException {
        verifyOpen();
        if (max < 0) {
            throw SqlError.createSQLException(
                    LOGGER,
                    SqlState.DATA_EXCEPTION,
                    SqlError.INVALID_LARGE_MAX_ROWS_SIZE,
                    max);
        }

        this.largeMaxRows = max;
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        verifyOpen();

        // Updates are not supported, so always return -1.
        return -1;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        verifyOpen();
        return maxFieldSize;
    }

    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        verifyOpen();

        if (max < 0) {
            throw SqlError.createSQLException(
                    LOGGER,
                    SqlState.DATA_EXCEPTION,
                    SqlError.INVALID_MAX_FIELD_SIZE,
                    max);
        }

        this.maxFieldSize = max;
    }

    @Override
    public int getMaxRows() throws SQLException {
        final long maxRows = getLargeMaxRows();
        if (maxRows > Integer.MAX_VALUE) {
            final String warning = Warning.lookup(Warning.MAX_VALUE_TRUNCATED, maxRows, Integer.MAX_VALUE);
            LOGGER.warn(warning);
            this.addWarning(new SQLWarning(warning));
            return Integer.MAX_VALUE;
        }
        return (int) maxRows;
    }

    @Override
    public void setMaxRows(final int max) throws SQLException {
        setLargeMaxRows(max);
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return getMoreResults(java.sql.Statement.CLOSE_CURRENT_RESULT);
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        verifyOpen();
        if ((java.sql.Statement.KEEP_CURRENT_RESULT != current) && (this.resultSet != null)) {
            this.resultSet.close();
            this.resultSet = null;
        }
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        verifyOpen();
        return resultSet;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        verifyOpen();
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        verifyOpen();
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public int getResultSetType() throws SQLException {
        verifyOpen();
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return (int) this.getLargeUpdateCount();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        verifyOpen();
        return warnings;
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        verifyOpen();
        return shouldCloseOnCompletion;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        verifyOpen();
        // Statement pooling is not supported.
        return false;
    }

    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);

    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return CastHelper.isWrapperFor(iface, this);
    }

    @Override
    public void setCursorName(final String name) throws SQLException {
        verifyOpen();
        throw SqlError.createSQLFeatureNotSupportedException(LOGGER);
    }

    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        verifyOpen();
        // Do nothing, because the driver does not support escape processing.
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return CastHelper.unwrap(iface, LOGGER, this);
    }

    /**
     * Adds a new {@link SQLWarning} to the end of the warning list.
     *
     * @param warning the {@link SQLWarning} to add.
     */
    void addWarning(final SQLWarning warning) {
        if (this.warnings == null) {
            this.warnings = warning;
        } else {
            this.warnings.setNextWarning(warning);
        }
    }

    /**
     * Verify the statement is open.
     *
     * @throws SQLException if the statement is closed.
     */
    protected void verifyOpen() throws SQLException {
        if (isClosed.get()) {
            throw SqlError.createSQLException(
                    LOGGER,
                    SqlState.DATA_EXCEPTION,
                    SqlError.STMT_CLOSED);
        }
    }

    @Override
    public java.sql.ResultSet executeQuery(final String sql) throws SQLException {
        return queryExecutor.executeQuery(sql, this);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        verifyOpen();
        return queryExecutor.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        verifyOpen();
        queryExecutor.setQueryTimeout(seconds);
    }
}
