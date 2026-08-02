package com.ursc.chss.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DatabaseAdapter - the single database abstraction for the CHSS Rejection
 * Letter module.
 *
 * <p>Every SQL query in the module obtains connections through
 * {@link #getConnection()}. This is the ONLY file that needs to be modified to
 * change database connectivity.
 *
 * <p>=======================================================================
 * SANDSH CONNECTION PLACEHOLDER
 * ------------------------------------------------------------------------
 * Copy the database connection mechanism from an existing Sandesh module here.
 *
 * The module does not know how the existing Sandesh project obtains database
 * connections (JNDI DataSource, DriverManager, a shared JDBC helper class,
 * etc.), so no mechanism is implemented yet. Find an existing Sandesh module
 * that reads the employee database, copy its connection code into
 * {@link #getConnection()} below, and return a {@link Connection}.
 *
 * You do NOT need to touch any other file for connectivity.
 * ========================================================================
 */
public final class DatabaseAdapter {

    private DatabaseAdapter() {
    }

    /**
     * Returns a connection to the CHSS application database.
     *
     * @return an open {@link Connection}; callers must close it (use try-with-resources)
     * @throws SQLException if a connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        /*
         * SANDSH CONNECTION PLACEHOLDER - EDIT HERE
         * ----------------------------------------
         * Copy the database connection mechanism from an existing Sandesh
         * module here and return a java.sql.Connection. For example, if Sandesh
         * uses a JNDI DataSource bound in Tomcat:
         *
         *     Context initCtx = new InitialContext();
         *     DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/<name>");
         *     return ds.getConnection();
         *
         * Use exactly the same mechanism an existing Sandesh module uses.
         * Every SQL query in this module goes through this one method.
         */
        throw new UnsupportedOperationException(
                "Copy the database connection mechanism from an existing Sandesh module here.");
    }
}
