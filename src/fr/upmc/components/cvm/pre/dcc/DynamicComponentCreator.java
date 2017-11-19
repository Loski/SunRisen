package fr.upmc.components.cvm.pre.dcc;

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

import java.lang.reflect.Constructor;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationInboundPort;
import fr.upmc.components.ports.PortI;

/**
 * The class <code>DynamicComponentCreator</code> defines components that will
 * be automatically added in each of the sites of a distributed component
 * assembly to allow for the dynamic remote creation of components on the
 * virtual where the component is running.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 13 mars 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			DynamicComponentCreator
extends		AbstractComponent
{
	/**
	 * create the component, publish its offered interface and its inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	dynamicComponentCreationInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param dynamicComponentCreationInboundPortURI	URI of the port offering the service
	 * @throws Exception
	 */
	public				DynamicComponentCreator(
		String dynamicComponentCreationInboundPortURI
		) throws Exception
	{
		super(1, 0) ;

		assert	dynamicComponentCreationInboundPortURI != null ;

		this.addOfferedInterface(DynamicComponentCreationI.class) ;
		PortI p = new DynamicComponentCreationInboundPort(
								dynamicComponentCreationInboundPortURI, this) ;
		this.addPort(p) ;
		if (AbstractCVM.isDistributed) {
			p.publishPort() ;
		} else {
			p.localPublishPort() ;
		}

		if (AbstractCVM.DEBUG) {
			System.out.println(
				"DynamicComponentCreator created with inbound port: " +
				dynamicComponentCreationInboundPortURI) ;
		}
	}

	/**
	 * create and start a component instantiated from the class of the given
	 * class name and initialised by the constructor which parameters are given.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflInboundPortURI != null && classname != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param classname				name of the class from which the component is created.
	 * @param constructorParams		parameters to be passed to the constructor.
	 * @throws Exception			if the creation did not succeed.
	 */
	public void		createComponent(
		String classname,
		Object[] constructorParams
		) throws Exception
	{
		assert	classname != null ;

		if (AbstractCVM.DEBUG) {
			System.out.println("DynamicComponentCreator creates: " + classname) ;
		}

		Class<?> cl = Class.forName(classname) ;
		assert	cl != null ;
		Class<?>[] parameterTypes = new Class[constructorParams.length] ;
		for (int i = 0 ; i < constructorParams.length ; i++) {
			parameterTypes[i] = constructorParams[i].getClass() ;
		}
		Constructor<?> cons = cl.getConstructor(parameterTypes) ;
		assert	cons != null ;
		AbstractComponent component =
					(AbstractComponent) cons.newInstance(constructorParams) ;
		AbstractCVM.theCVM.addDeployedComponent(component) ;
		component.start() ;
	}
}
