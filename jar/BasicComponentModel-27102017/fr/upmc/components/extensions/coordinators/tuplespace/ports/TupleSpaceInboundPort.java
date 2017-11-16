package fr.upmc.components.extensions.coordinators.tuplespace.ports;

//Copyright Jacques Malenfant, Univ. Pierre et Marie Curie.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.concurrent.Future;

import fr.upmc.components.ComponentI;
import fr.upmc.components.extensions.coordinators.tuplespace.components.TupleSpace;
import fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>TupleSpaceInboundPort</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : February 16, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			TupleSpaceInboundPort
extends		AbstractInboundPort
implements	TupleSpaceI
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null
	 * pre	owner != null && owner instanceof TupleSpaceI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri
	 * @param implementedInterface
	 * @param owner
	 * @throws Exception
	 */
	public				TupleSpaceInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, TupleSpaceI.class, owner) ;

		assert	uri != null ;
		assert	owner != null && owner instanceof TupleSpaceI ;
	}

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner != null && owner instanceof TupleSpaceI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param implementedInterface
	 * @param owner
	 * @throws Exception
	 */
	public TupleSpaceInboundPort(ComponentI owner)
	throws Exception
	{
		super(TupleSpaceI.class, owner) ;

		assert	owner != null && owner instanceof TupleSpaceI ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	tuple != null
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI#out(fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI.TupleI)
	 */
	@Override
	public void			out(TupleI tuple)
	throws Exception
	{
		assert	tuple != null ;

		final TupleSpace ts = (TupleSpace) this.owner ;
		final TupleI t = tuple ;
		ts.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						((TupleSpaceI)ts).out(t) ;
						return null;
					}
				});
	}

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	template != null
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI#in(fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI.TupleI)
	 */
	@Override
	public TupleI		in(TupleI template)
	throws Exception
	{
		assert	template != null ;

		final TupleSpace ts = (TupleSpace) this.owner ;
		final TupleI t = template ;
		// Use an explicit future variable to block the calling thread
		// rather than a thread of the called component.
		Future<TupleI> future =
			ts.handleRequest(
				new ComponentI.ComponentService<TupleI>() {
					@Override
					public TupleI call() throws Exception {
						return ((TupleSpaceI)ts).in(t) ;
					}
				}) ;
		// This will block the calling thread until the result of the call
		// becomes available.
		return future.get() ;
	}

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	template != null
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI#read(fr.upmc.components.extensions.coordinators.tuplespace.interfaces.TupleSpaceI.TupleI)
	 */
	@Override
	public TupleI		read(TupleI template)
	throws Exception
	{
		assert	template != null ;

		final TupleSpace ts = (TupleSpace) this.owner ;
		final TupleI t = template ;
		// Use an explicit future variable to block the calling thread
		// rather than a thread of the called component.
		Future<TupleI> future =
			ts.handleRequest(
				new ComponentI.ComponentService<TupleI>() {
					@Override
					public TupleI call() throws Exception {
						return ((TupleSpaceI)ts).read(t) ;
					}
				}) ;
		// This will block the calling thread until the result of the call
		// becomes available.
		return future.get() ;
	}
}
