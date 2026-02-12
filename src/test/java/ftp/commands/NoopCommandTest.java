package ftp.commands;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
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
