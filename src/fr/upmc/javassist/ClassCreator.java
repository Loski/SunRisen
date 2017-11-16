package fr.upmc.javassist;

import java.lang.reflect.Method;

import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import javassist.CannotCompileException;
import javassist.ClassMap;
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

		RequestNotificationI obj = (RequestNotificationI) createConnectorImplementingInterface("Test",RequestNotificationI.class).newInstance();
			
		System.out.println(obj);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Class<?> createConnectorImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = pool.makeClass("Test");
		
		CtClass interfaceToImplement = pool.get(interfaceToImplementClass.getCanonicalName());
		
		test.addInterface(interfaceToImplement);
		
		CtConstructor constructor = new CtConstructor(new CtClass[0], test);
		constructor.setBody("return $0;");//Mais que fais $0 ?
		test.addConstructor(constructor);
		
		
		for(Method m : interfaceToImplementClass.getDeclaredMethods())
		{
			System.out.println(m);
			CtMethod method = copyMethodSignature(m,test);
			
			method.setBody(createBodyOfConnector());	
		}
		
		CtMethod toString = new CtMethod(pool.get("java.lang.String"),"toString",null, test);
		toString.setBody("return \"JAVASSIST MASTER RACE\";");
		
		test.addMethod(toString);
		
		Class<?> clazz = pool.toClass(test);

		return clazz;

	}
	
	private static CtMethod copyMethodSignature(Method m,CtClass clazz) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		 CtMethod method = new CtMethod(pool.get("void"),m.getName(),null, clazz);
		 method.addParameter(pool.get(m.getParameterTypes()[0].getCanonicalName()));
		 
		 Class[] exceptionsToThrow = m.getExceptionTypes();
		 
		 CtClass[] exceptions = new CtClass[exceptionsToThrow.length];
		 
		 int i = 0;
		 for(Class clazzException:exceptionsToThrow)
		 {
			 exceptions[i]=pool.get(clazzException.getCanonicalName());
			 i++;
		 }
		 
		 method.setExceptionTypes(exceptions);
		 
		 System.out.println(method);
		 
		 return method;
	}
	
	private static String createBodyOfConnector()
	{
		return "return null;";
	}
}
