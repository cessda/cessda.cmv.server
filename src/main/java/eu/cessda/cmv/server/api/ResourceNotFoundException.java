package eu.cessda.cmv.server.api;

import static java.lang.String.format;

public class ResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 9203612830248772991L;

	public ResourceNotFoundException( String httpMethod, String requestPath )
	{
		super( format( "No handler found for %s /%s", httpMethod, requestPath ) );
	}
}
