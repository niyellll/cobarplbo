package id.ac.ukdw.www.rpblo.javafx_rplbo.Manager;


import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseDriver {
    Connection getConnection();
    void closeConnection();
    void prepareSchema();
}
