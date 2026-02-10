package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.io.StringWriter;

import ftp.model.UserSession;

public class TypeCommandTest {

    private TypeCommand typeCommand;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        typeCommand = new TypeCommand();
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testTypeAscii() throws Exception {
        typeCommand.execute("A", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("200"), "Should return 200");
        assertTrue(output.contains("ASCII"), "Should mention ASCII");
        
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType(), 
            "Session should be set to ASCII");
    }

    @Test
    void testTypeAsciiLowerCase() throws Exception {
        typeCommand.execute("a", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("200"), "Should return 200");
        assertTrue(output.contains("ASCII"), "Should mention ASCII");
        
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType(), 
            "Session should be set to ASCII");
    }

    @Test
    void testTypeBinary() throws Exception {
        typeCommand.execute("I", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("200"), "Should return 200");
        assertTrue(output.contains("BINARY"), "Should mention BINARY");
        
        assertEquals(UserSession.TransferType.BINARY, session.getTransferType(), 
            "Session should be set to BINARY");
    }

    @Test
    void testTypeBinaryLowerCase() throws Exception {
        typeCommand.execute("i", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("200"), "Should return 200");
        assertTrue(output.contains("BINARY"), "Should mention BINARY");
        
        assertEquals(UserSession.TransferType.BINARY, session.getTransferType(), 
            "Session should be set to BINARY");
    }

    @Test
    void testTypeNoArguments() throws Exception {
        typeCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for missing arguments");
    }

    @Test
    void testTypeEmptyArguments() throws Exception {
        typeCommand.execute("", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for empty arguments");
    }

    @Test
    void testTypeUnsupported() throws Exception {
        typeCommand.execute("E", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("504"), "Should return 504 for unsupported type");
    }

    @Test
    void testTypeUnsupportedLong() throws Exception {
        typeCommand.execute("IMAGE", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("504"), "Should return 504 for unsupported type");
    }

    @Test
    void testTypeSwitching() throws Exception {
        // Start with default (ASCII)
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType());
        
        // Switch to BINARY
        typeCommand.execute("I", session, printWriter);
        assertEquals(UserSession.TransferType.BINARY, session.getTransferType());
        
        stringWriter.getBuffer().setLength(0);
        
        // Switch back to ASCII
        typeCommand.execute("A", session, printWriter);
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType());
    }

    @Test
    void testTypeCaseInsensitive() throws Exception {
        typeCommand.execute("a", session, printWriter);
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType());
        
        typeCommand.execute("A", session, printWriter);
        assertEquals(UserSession.TransferType.ASCII, session.getTransferType());
        
        typeCommand.execute("i", session, printWriter);
        assertEquals(UserSession.TransferType.BINARY, session.getTransferType());
        
        typeCommand.execute("I", session, printWriter);
        assertEquals(UserSession.TransferType.BINARY, session.getTransferType());
    }
}
