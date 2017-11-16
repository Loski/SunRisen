package fr.upmc.components.extensions.coordinators.tuplespace.tuples;

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

import java.io.Serializable;

/**
 * The class <code>Field</code>
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
public class			Field
implements	Serializable
{
	private static final long serialVersionUID = 1L ;
	protected final Class<?>	type ;
	protected Object			value ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	public				Field(Class<?> type)
	{
		super() ;
		this.type = type ;
		this.value = null ;
	}

	/**
	 * @return the value
	 */
	public Object		getValue()
	{
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void			setValue(Object value)
	{
		assert	this.value == null ;
		assert	value != null ;

		this.value = value ;
	}

	/**
	 * @return the type
	 */
	public Class<?>		getType() {
		return this.type;
	}

	public boolean		match(Field f)
	{
		assert	f != null ;

		boolean ret = false ;
		
		if (this.getValue() == null && f.getValue() != null) {
			ret = this.getType().isAssignableFrom(f.getType()) ;
		} else if (this.getValue() != null && f.getValue() == null) {
			ret = f.getType().isAssignableFrom(this.getType()) ;
		} else if (this.getValue() == null && f.getValue() == null) {
			ret = f.getType().isAssignableFrom(this.getType()) ||
							this.getType().isAssignableFrom(f.getType()) ;
		} else {
			assert	this.getValue() != null && f.getValue() != null ;
			ret = f.getType().isAssignableFrom(this.getType()) &&
					this.getType().isAssignableFrom(f.getType()) &&
					this.getValue().equals(f.getValue()) ;
		}
		return ret ;
	}

	public Field		unify(Field f)
	{
		assert	this.match(f) ;

		Field ret = null ;
		if (this.getValue() == null && f.getValue() != null) {
			ret = new Field(f.getType()) ;
			ret.setValue(f.getValue()) ;
		} else if (this.getValue() != null && f.getValue() == null) {
			ret = new Field(this.getType()) ;
			ret.setValue(this.getValue()) ;
		} else if (this.getValue() == null && f.getValue() == null) {
			if (f.getType().isAssignableFrom(this.getType())) {
				ret = new Field(this.getType()) ;
			} else {
				assert	this.getType().isAssignableFrom(f.getType()) ;
				ret = new Field(f.getType()) ;
			}
		} else {
			assert	this.getValue() != null && f.getValue() != null ;
			if (f.getType().isAssignableFrom(this.getType())) {
				ret = new Field(this.getType()) ;
				ret.setValue(this.getValue()) ;
			} else {
				assert	this.getType().isAssignableFrom(f.getType()) ;
				ret = new Field(f.getType()) ;
				ret.setValue(f.getValue()) ;
			}
		}
		return ret ;
	}
}
