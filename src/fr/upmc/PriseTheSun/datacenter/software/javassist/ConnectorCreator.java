package fr.upmc.PriseTheSun.datacenter.software.javassist;

import java.lang.reflect.Method;

import fr.upmc.components.connectors.AbstractConnector;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ConnectorCreator extends ClassCreator{

	public static Class<?> createConnectorImplementingInterface(String className,Class<?> submissionInterface) throws Exception
	{
		System.out.println("CREATING NEW JAVASSIST CONNECTOR FOR :"+submissionInterface.getSimpleName());
		
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = createClass(className, submissionInterface,AbstractConnector.class);
		
		/*CtConstructor constructor = CtNewConstructor.make(null, null, test);
		constructor.setBody("{}");
		System.out.println(constructor);
		test.addConstructor(constructor);*/
		
		
		for(Method m : submissionInterface.getDeclaredMethods())
		{
			CtMethod method = copyMethodSignature(m,test);
			
			method.setBody(createBodyOfConnector(submissionInterface.getCanonicalName(),method));	
			
			test.addMethod(method);
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
