/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.twilmes.sql.gremlin.adapter.converter.ast.nodes.operator;

import lombok.AllArgsConstructor;
import org.apache.calcite.sql.SqlOperator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twilmes.sql.gremlin.adapter.converter.SqlMetadata;
import org.twilmes.sql.gremlin.adapter.converter.ast.nodes.GremlinSqlNode;
import org.twilmes.sql.gremlin.adapter.converter.ast.nodes.operands.GremlinSqlIdentifier;
import org.twilmes.sql.gremlin.adapter.converter.ast.nodes.operator.logic.GremlinSqlLiteral;
import org.twilmes.sql.gremlin.adapter.util.SqlGremlinError;

import java.sql.SQLException;
import java.util.List;

/**
 * This abstract class is a GremlinSql equivalent of Calcite's SqlOperator.
 *
 * @author Lyndon Bauto (lyndonb@bitquilltech.com)
 */
@AllArgsConstructor
public abstract class GremlinSqlOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GremlinSqlOperator.class);
    private final SqlOperator sqlOperator;
    private final List<GremlinSqlNode> sqlOperands;
    private final SqlMetadata sqlMetadata;

    protected abstract void appendTraversal(GraphTraversal<?, ?> graphTraversal) throws SQLException;

    public void appendOperatorTraversal(final GraphTraversal<?, ?> graphTraversal) throws SQLException {
        if (sqlOperands.size() > 2) {
            throw SqlGremlinError.create(SqlGremlinError.OPERANDS_MORE_THAN_TWO);
        } else if (sqlOperands.isEmpty()) {
            throw SqlGremlinError.create(SqlGremlinError.OPERANDS_EMPTY);
        }

        appendTraversal(graphTraversal);
    }

    protected String getOperandName(final GremlinSqlNode operand) throws SQLException {
        if (operand instanceof GremlinSqlIdentifier) {
            final GremlinSqlIdentifier gremlinSqlIdentifier = (GremlinSqlIdentifier) operand;
            return gremlinSqlIdentifier.isStar() ? "*" : gremlinSqlIdentifier.getColumn();
        } else if (operand instanceof GremlinSqlLiteral) {
            return ((GremlinSqlLiteral) operand).getValue().toString();
        } else if (operand instanceof GremlinSqlBasicCall) {
            return ((GremlinSqlBasicCall) operand).getRename();
        }
        throw SqlGremlinError.createNotSupported(SqlGremlinError.UNSUPPORTED_OPERAND_TYPE, operand.getClass().getName());
    }
}
