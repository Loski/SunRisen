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
 * The class <code>Tuple</code>
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
public class			Tuple
implements	Serializable
{
	private static final long serialVersionUID = 1L;
	protected final String	functor ;
	protected final Field[]	fields ;

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
	public				Tuple(
		String functor,
		int nargs
		)
	{
		super() ;
		this.functor = new String(functor) ;
		this.fields = new Field[nargs] ;
	}

	/**
	 * @return the functor
	 */
	public String	getFunctor() {
		return new String(this.functor) ;
	}

	public int		getNumberOfArguments()
	{
		return this.fields.length ;
	}

	public Field	getField(int i)
	{
		assert	i >= 0 && i < this.fields.length ;

		return this.fields[i] ;
	}

	public Class<?>	getFieldType(int i)
	{
		assert	i >= 0 && i < this.fields.length ;

		return this.fields[i].getType() ;
	}

	public Object	getFieldValue(int i)
	{
		assert	i >= 0 && i < this.fields.length ;

		return this.fields[i].getValue() ;
	}

	public void		setFieldValue(int i , Object value)
	{
		assert	i >= 0 && i < this.fields.length ;
		assert	value != null ;

		this.fields[i].setValue(value) ;
	}

	public boolean	match(Tuple t)
	{
		assert	t != null ;

		boolean ret = false ;
		if (this.functor.equals(t.getFunctor())) {
			ret = true ;
			for(int i = 0 ; ret && i < this.getNumberOfArguments() ; i++) {
				ret = this.getField(i).match(t.getField(i)) ;
			}
		}
		return ret ;
	}

	public Tuple	unify(Tuple t)
	{
		assert	this.match(t) ;

		Tuple ret = new Tuple(this.getFunctor(), this.getNumberOfArguments()) ;
		for(int i = 0 ; i < this.getNumberOfArguments() ; i++) {
			ret.fields[i] = this.getField(i).unify(t.getField(i)) ;
		}
		return ret ;
	}
}
