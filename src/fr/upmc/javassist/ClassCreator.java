package fr.upmc.javassist;

import java.lang.reflect.Method;

import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public abstract class ClassCreator {

	public static void main (String[] args)
	{		
		try {
		
		ClassPool pool = ClassPool.getDefault();
		CtClass test = pool.makeClass("Test");
		
		CtConstructor constructor = new CtConstructor(new CtClass[0], test);
		constructor.setBody("return $0;");
		test.addConstructor(constructor);
		
		CtClass interfaceToImplement = pool.get(RequestNotificationI.class.getCanonicalName());

		test.addInterface(interfaceToImplement);
		
		System.out.println(RequestNotificationI.class.getCanonicalName());
		
		Method m = RequestNotificationI.class.getDeclaredMethods()[0];
		
		System.out.println(m);
		
		 CtMethod method = new CtMethod(pool.get("void"),m.getName(),null, test);
		 method.addParameter(pool.get(m.getParameterTypes()[0].getCanonicalName()));
		 
		 Class[] exceptionsToThrow = m.getExceptionTypes();
		 
		 CtClass[] exceptions = new CtClass[exceptionsToThrow.length];
		 
		 exceptions[0]=pool.get(exceptionsToThrow[0].getCanonicalName());
		 
		 method.setExceptionTypes(exceptions);
		 
		 System.out.println(method);

	       method.setBody(String.format("return null;"));

	       test.addMethod(method);
			
			Class<?> clazz = pool.toClass(test);
		
			RequestNotificationI obj = (RequestNotificationI) clazz.newInstance();
			
			System.out.println(obj);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
