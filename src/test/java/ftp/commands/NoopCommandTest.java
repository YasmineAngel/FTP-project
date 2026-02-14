package ftp.commands;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ftp.model.UserSession;

public class NoopCommandTest {
	@Test
	public void testNoopCommand() throws Exception {
		UserSession mockSession = mock(UserSession.class);
		PrintWriter mockWriter = mock(PrintWriter.class);
		NoopCommand command = new NoopCommand();

		command.execute(null, mockSession, mockWriter);

		verify(mockWriter).print("200 NOOP ok.\r\n");
		verify(mockWriter).flush();
	}
}
