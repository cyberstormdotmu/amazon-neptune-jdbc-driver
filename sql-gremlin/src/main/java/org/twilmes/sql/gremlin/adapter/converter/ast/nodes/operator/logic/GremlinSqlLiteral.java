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

package org.twilmes.sql.gremlin.adapter.converter.ast.nodes.operator.logic;

import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.twilmes.sql.gremlin.adapter.converter.SqlMetadata;
import org.twilmes.sql.gremlin.adapter.converter.ast.nodes.GremlinSqlNode;
import java.sql.SQLException;

/**
 * This module is a GremlinSql equivalent of Calcite's SqlLiteral.
 *
 * @author Lyndon Bauto (lyndonb@bitquilltech.com)
 */
public class GremlinSqlLiteral extends GremlinSqlNode {
    final SqlLiteral sqlLiteral;
    public GremlinSqlLiteral(final SqlLiteral sqlLiteral,
                             final SqlMetadata sqlMetadata) {
        super(sqlLiteral, sqlMetadata);
        this.sqlLiteral = sqlLiteral;
    }

    public void appendTraversal(final GraphTraversal<?, ?> graphTraversal) throws SQLException {
        graphTraversal.constant(getValue());
    }

    public Object getValue() {
        return (sqlLiteral.getTypeName().equals(SqlTypeName.CHAR)) ? sqlLiteral.toValue() : sqlLiteral.getValue();
    }
}
