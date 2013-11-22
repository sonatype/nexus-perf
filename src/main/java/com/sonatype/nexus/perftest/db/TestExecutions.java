/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package com.sonatype.nexus.perftest.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;

public class TestExecutions {

  static {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      // this is going to be fun to debug
      throw new LinkageError("Could not load required class", e);
    }
  }

  private static void createTables(Connection conn) throws SQLException {
    try (Statement statement = conn.createStatement()) {
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS executions (" //
          + " test_id VARCHAR(255)," //
          + " execution_id VARCHAR(255)," //
          + " metric_id VARCHAR(255)," //
          + " value DOUBLE," //
          + " PRIMARY KEY(test_id, execution_id, metric_id)" //
          + ")");
    }
  }

  private static boolean delete(Connection conn, TestExecution execution) throws SQLException {
    try (PreparedStatement statement =
        conn.prepareStatement("DELETE FROM executions WHERE test_id = ? AND execution_id = ?")) {
      statement.setString(1, execution.getTestId());
      statement.setString(2, execution.getExecutionId());
      return statement.executeUpdate() > 0;
    }
  }

  private static void insert(Connection conn, TestExecution execution) throws SQLException {
    try (PreparedStatement statement =
        conn.prepareStatement("INSERT INTO executions (test_id, execution_id, metric_id, value) VALUES (?, ?, ?, ?)")) {
      for (String metricId : execution.getMetricIds()) {
        statement.setString(1, execution.getTestId());
        statement.setString(2, execution.getExecutionId());
        statement.setString(3, metricId);
        statement.setDouble(4, execution.getMetric(metricId));
        statement.executeUpdate();
      }
    }
  }

  private static TestExecution select(Connection conn, String testId, String executionId) throws SQLException {
    try (PreparedStatement statement =
        conn.prepareStatement("SELECT metric_id, value FROM executions WHERE test_id = ? AND execution_id = ?")) {
      statement.setString(1, testId);
      statement.setString(2, executionId);
      try (ResultSet resultSet = statement.executeQuery()) {
        TestExecution execution = new TestExecution(testId, executionId);
        while (resultSet.next()) {
          execution.addMetric(resultSet.getString(1), resultSet.getDouble(2));
        }
        return execution.getMetricIds().isEmpty() ? null : execution;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    TestExecution execution = new TestExecution("test", "execution");
    execution.addMetric("foo", 123.45);
    try (Connection conn = getConnection()) {
      createTables(conn);
      System.err.println(delete(conn, execution));
      insert(conn, execution);

      select(conn, execution.getTestId(), execution.getExecutionId());

      System.err.println(delete(conn, execution));
    }
  }

  private static Connection getConnection() throws SQLException {
    Connection conn = DriverManager.getConnection("jdbc:h2:~/nexusperftest");
    createTables(conn);
    return conn;
  }

  public static void assertUnique(String testId, String executionId) {
    try (Connection conn = getConnection()) {
      Assert.assertNull(select(conn, testId, executionId));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static TestExecution select(String testId, String executionId) {
    try (Connection conn = getConnection()) {
      return select(conn, testId, executionId);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void insert(TestExecution execution) {
    try (Connection conn = getConnection()) {
      delete(conn, execution); // uniqueness is enforced at test level
      insert(conn, execution);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertPerformance(Collection<PerformanceMetricDescriptor> metrics, TestExecution baseline,
      TestExecution actual) {
    ArrayList<String> errors = new ArrayList<>();
    for (PerformanceMetricDescriptor metric : metrics) {
      String error = metric.assertPerformance(baseline, actual);
      if (error != null) {
        errors.add(error);
      }
    }
    if (!errors.isEmpty()) {
      throw new PerformanceAssertionFailure(baseline, actual, errors);
    }
  }

}
