package eu.cessda.cmv.server;

import static eu.cessda.cmv.server.Server.ALLOWED_CLI_OPTION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

public class ServerTest
{
	@Test
	public void validateArgs()
	{
		assertDoesNotThrow( () -> Server.validateArgs() );
		assertDoesNotThrow( () -> Server.validateArgs( ALLOWED_CLI_OPTION ) );
		assertThrows( IllegalArgumentException.class, () -> Server.validateArgs( "--not-allowed" ) );
		assertThrows( IllegalArgumentException.class, () -> Server.validateArgs( "--not-allowed", "--not-allowed" ) );
	}
}
