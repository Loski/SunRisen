package fr.upmc.components.exceptions;

/**
 * The exception <code>ConfigurationException</code> is thrown when some
 * something unexpected happen or an unacceptable state is reached during
 * the BCM configuration.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 26 oct. 2017</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class				 ConfigurationException
extends		Exception
{

	private static final long serialVersionUID = 1L;

	public 				ConfigurationException()
	{
		super() ;
	}

	public				ConfigurationException(String message)
	{
		super(message);
	}

	public				ConfigurationException(Throwable cause)
	{
		super(cause);
	}

	public				ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public				ConfigurationException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace
		)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
