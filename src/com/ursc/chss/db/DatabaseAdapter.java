package com.ursc.chss.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseAdapter - the single database abstraction for the CHSS Rejection
 * Letter module.
 *
 * <p>Every SQL query in the module obtains connections through
 * {@link #getConnection()}. This is the ONLY file that needs to be modified to
 * change database connectivity.
 *
 * <p>=========================================================================
 * SANDSH CONNECTION PLACEHOLDER - EDIT HERE
 * --------------------------------------------------------------------------
 * Replace the {@link DriverManager} mechanism below with whatever Sandesh uses
 * internally, for example a JNDI DataSource bound in Tomcat:
 *
 * <pre>
 *     import javax.naming.Context;
 *     import javax.naming.InitialContext;
 *     import javax.sql.DataSource;
 *
 *     Context initCtx = new InitialContext();
 *     DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/chss");
 *     return ds.getConnection();
 * </pre>
 *
 * You do NOT need to touch any other file for connectivity.
 * =========================================================================
 */
public final class DatabaseAdapter {

    /** JDBC URL of the CHSS application database (MySQL). */
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/chss_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";

    /** Database user. */
    private static final String DB_USER = "chss_user";

    /** Database password. */
    private static final String DB_PASSWORD = "chss_password";

    /** JDBC driver class name. */
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found on classpath: " + DB_DRIVER
                            + ". Add mysql-connector-j to the Sandesh lib/ folder.");
        }
    }

    private DatabaseAdapter() {
    }

    /**
     * Returns a connection to the CHSS application database.
     *
     * @return an open {@link Connection}; callers must close it (use try-with-resources)
     * @throws SQLException if a connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        // Replace the following line with the Sandesh connection mechanism.
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
