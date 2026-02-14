package ftp.commands;

import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ftp.model.UserSession;

public class HelpCommandTest {
	private UserSession mockSession;
	private PrintWriter mockWriter;
	private HelpCommand command;

	@BeforeEach
	public void setUp() {
		mockSession = mock(UserSession.class);
		mockWriter = mock(PrintWriter.class);
		command = new HelpCommand();
	}

	@Test
	public void testGeneralHelpCommand() throws Exception {
		command.execute(null, mockSession, mockWriter);

		verify(mockWriter).print("214-The following commands are recognized:\r\n");
		verify(mockWriter).print("   Access:   USER, PASS, REIN, QUIT\r\n");
		verify(mockWriter).print("   Transfer: PASV, PORT, TYPE, MODE, STRU\r\n");
		verify(mockWriter).print("200 Help OK.\r\n");
		verify(mockWriter, atLeastOnce()).flush();
	}

	@Test
	public void testSpecificCommandHelp() throws Exception {
		command.execute("STOR", mockSession, mockWriter);

		verify(mockWriter).print("200 STOR <filename> - Store (upload) a file\r\n");
		verify(mockWriter).flush();
	}

	@Test
	public void testUnknownCommandHelp() throws Exception {
		command.execute("INVALID_CMD", mockSession, mockWriter);

		verify(mockWriter).print("502 Help not available for: INVALID_CMD\r\n");
		verify(mockWriter).flush();
	}

}
