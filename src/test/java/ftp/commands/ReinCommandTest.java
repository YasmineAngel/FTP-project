package ftp.commands;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import ftp.model.UserSession;

public class ReinCommandTest {
	@Test
	public void testReinCommand() throws Exception {
	    UserSession mockSession = mock(UserSession.class);
	    PrintWriter mockWriter = mock(PrintWriter.class);
	    ReinCommand command = new ReinCommand();

	    command.execute(null, mockSession, mockWriter);

	    // Verify the session was actually logged out
	    verify(mockSession).logout();
	    // Verify the 220 greeting was sent
	    verify(mockWriter).print("220 Service ready for new user.\r\n");
	    verify(mockWriter).flush();
	}
}
