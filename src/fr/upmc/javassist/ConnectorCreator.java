package fr.upmc.javassist;

import java.lang.reflect.Method;

import fr.upmc.components.connectors.AbstractConnector;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ConnectorCreator extends ClassCreator{

	public static Class<?> createConnectorImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = createClass(className, interfaceToImplementClass,AbstractConnector.class);
		
		/*CtConstructor constructor = CtNewConstructor.make(null, null, test);
		constructor.setBody("{}");
		System.out.println(constructor);
		test.addConstructor(constructor);*/
		
		
		for(Method m : interfaceToImplementClass.getDeclaredMethods())
		{
			System.out.println(m);
			CtMethod method = copyMethodSignature(m,test);
			
			method.setBody(createBodyOfConnector(interfaceToImplementClass.getCanonicalName(),method));	
			System.out.println(method);
		}
		
		CtMethod toString = new CtMethod(pool.get("java.lang.String"),"toString",null, test);
		toString.setBody("return \"JAVASSIST MASTER RACE\";");
		
		test.addMethod(toString);
		
		Class<?> clazz = pool.toClass(test);
		
		return clazz;

	}
	
	protected static String createBodyOfConnector(String interfaceName,CtMethod method) throws NotFoundException
	{
		return createBodyOfConnectorOrOutboundPort(interfaceName,"this.offering",method);
	}
	
}
