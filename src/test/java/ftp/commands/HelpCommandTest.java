package ftp.commands;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ftp.model.UserSession;

public class HelpCommandTest {
	@Test
	public void testHelpCommand() throws Exception {
	    UserSession mockSession = mock(UserSession.class);
	    PrintWriter mockWriter = mock(PrintWriter.class);
	    HelpCommand command = new HelpCommand();

	    command.execute(null, mockSession, mockWriter);

	    // Verify the start and end of the multi-line response
	    verify(mockWriter).print("214- The following commands are recognized:\r\n");
	    verify(mockWriter).print("214 Help OK.\r\n");
	    verify(mockWriter).flush();
	}
}
