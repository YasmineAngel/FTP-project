package ftp.commands;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
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
