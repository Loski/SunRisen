package fr.upmc.datacenter.software.applicationvm.ports;

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

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;

/**
 * The class <code>ApplicationVMManagementInboundPort</code> implements the
 * inbound port offering the interface <code>ApplicationVMManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		owner instanceof ApplicationVMManagementI
 * </pre>
 * 
 * <p>Created on : August 25, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			ApplicationVMManagementInboundPort
extends		AbstractInboundPort
implements	ApplicationVMManagementI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner			owner component.
	 * @throws Exception
	 */
	public				ApplicationVMManagementInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(ApplicationVMManagementI.class, owner) ;

		assert	owner instanceof ApplicationVMManagementI ;
	}

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null && owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			uri of the port.
	 * @param owner			owner component.
	 * @throws Exception
	 */
	public				ApplicationVMManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ApplicationVMManagementI.class, owner);

		assert	uri != null && owner instanceof ApplicationVMManagementI ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI#allocateCores(fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore[])
	 */
	@Override
	public void			allocateCores(
		final AllocatedCore[] allocatedCores
		) throws Exception
	{
		final ApplicationVMManagementI avm =
									(ApplicationVMManagementI) this.owner ;
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						avm.allocateCores(allocatedCores) ;
						return null;
					}
				}) ;
	}

	@Override
	public void			connectWithRequestSubmissioner(final String rgURI, final String RequestNotificationInboundPortURI) throws Exception {
		// TODO Auto-generated method stub
		final ApplicationVMManagementI avm =
				(ApplicationVMManagementI) this.owner ;
		this.owner.handleRequestSync(
			new ComponentI.ComponentService<Void>() {
			@Override
			public Void call() throws Exception {
				avm.connectWithRequestSubmissioner(rgURI, RequestNotificationInboundPortURI) ;
				return null;
			}
		}) ;
	}

	@Override
	public void desallocateCores(final int nbCore) throws Exception {
		final ApplicationVMManagementI avm = (ApplicationVMManagementI) this.owner ;
		this.owner.handleRequestSync(
			new ComponentI.ComponentService<Void>() {
			@Override
			public Void call() throws Exception {
				avm.desallocateCores(nbCore);
				return null;
			}
		}) ;
	}

	@Override
	public void disconnectWithRequestSubmissioner() throws Exception {
		final ApplicationVMManagementI avm = (ApplicationVMManagementI) this.owner ;
		this.owner.handleRequestSync(
			new ComponentI.ComponentService<Void>() {
			@Override
			public Void call() throws Exception {
				avm.disconnectWithRequestSubmissioner();
				return null;
			}
		}) ;
	}

	@Override
	public AllocatedCore[] desallocateAllCores() throws Exception {
		final ApplicationVMManagementI avm = (ApplicationVMManagementI) this.owner ;
		return this.owner.handleRequestSync(
			new ComponentI.ComponentService<AllocatedCore[]>() {
			@Override
			public AllocatedCore[] call() throws Exception {
				return avm.desallocateAllCores();
			}
		});
	}
}
