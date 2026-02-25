package com.mikey1201.managers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

public class DatabaseManagerTest {

    private DatabaseManager databaseManager;
    private Connection mockConnection;
    private Logger mockLogger;

    @Before
    public void setUp() throws Exception {
        mockLogger = mock(Logger.class);
        databaseManager = new DatabaseManager(new File("."), mockLogger);
        mockConnection = mock(Connection.class);

        Field field = DatabaseManager.class.getDeclaredField("connection");
        field.setAccessible(true);
        field.set(databaseManager, mockConnection);
    }

    @Test
    public void testHasAccount_True() throws Exception {
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);

        assertTrue(databaseManager.hasAccount(UUID.randomUUID()));
    }

    @Test
    public void testGetBalance() throws Exception {
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getDouble("balance")).thenReturn(123.45);

        assertEquals(123.45, databaseManager.getBalance(UUID.randomUUID()), 0.001);
    }
}
