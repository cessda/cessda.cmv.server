package eu.cessda.cmv.server.ui;

import java.io.Serial;

public class CSVException extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = -1872344688892683911L;

	/**
	 * Constructs a new runtime exception with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())}
	 * (which typically contains the class and detail message of {@code cause}).
	 *
	 * @param cause the cause of this exception.
	 */
	public CSVException( Throwable cause )
	{
		super( cause );
	}
}
