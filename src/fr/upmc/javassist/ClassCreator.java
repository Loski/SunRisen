package fr.upmc.javassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import fr.upmc.components.ComponentI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public abstract class ClassCreator {

	public static void main (String[] args)
	{		
		try {
				Constructor ctr = createConnectorImplementingInterface("Test",RequestNotificationI.class).getDeclaredConstructor();
				AbstractConnector instance = (AbstractConnector) ctr.newInstance();
			
				System.out.println(instance);

				Class myPort = createOutboundPortImplementingInterface("TestPort",RequestNotificationI.class);
				Class[] type = {String.class,ComponentI.class};
				Constructor cons = myPort.getConstructor(type);
				Object[] obj = {"uri",null};
				System.out.println(cons.newInstance(obj));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static CtClass createClass(String className,Class<?> interfaceToImplementClass, Class<?> superClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass extendsClass = pool.getCtClass(superClass.getName());
		
		CtClass test = pool.makeClass(className);
		
		test.setSuperclass(extendsClass);
		
		CtClass interfaceToImplement = pool.get(interfaceToImplementClass.getCanonicalName());
		
		test.addInterface(interfaceToImplement);
		
		return test;
	}
	
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
	
	public static Class<?> createOutboundPortImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = createClass(className, interfaceToImplementClass,AbstractOutboundPort.class);
		
		//TODO : constructeurs
		
		Constructor[] ctrs = TemplateOutboundPort.class.getConstructors();
		for(Constructor ctr : ctrs)
		{
			CtConstructor ctrCopy = copyConstructor(ctr,test);
			System.out.println("CTR COPIED :"+ctrCopy);
			
			test.addConstructor(ctrCopy);
		}
		
		
		for(Method m : interfaceToImplementClass.getDeclaredMethods())
		{
			System.out.println(m);
			CtMethod method = copyMethodSignature(m,test);
			
			method.setBody(createBodyOfOutboundPort(interfaceToImplementClass.getCanonicalName(),method));	
			System.out.println(method);
		}
		
		CtMethod toString = new CtMethod(pool.get("java.lang.String"),"toString",null, test);
		toString.setBody("return \"JAVASSIST MASTER RACE\";");
		
		test.addMethod(toString);
		
		Class<?> clazz = pool.toClass(test);
		
		return clazz;

	}
	
	private static CtConstructor copyConstructor(Constructor ctr, CtClass test) throws Exception {
		
		//TODO : Exceptions
		
		ClassPool pool = ClassPool.getDefault();
		
		 Class[] parametersOfCtr = ctr.getParameterTypes();
		 
		 CtClass[] parameters = new CtClass[parametersOfCtr.length];
		 
		 int i = 0;
		 for(Class parameterClass:parametersOfCtr)
		 {
			 parameters[i]=pool.get(parameterClass.getCanonicalName());
			 i++;
		 }
		
		CtConstructor constructor = CtNewConstructor.make(parameters, null, test);
		
		if(parametersOfCtr.length==2)
			constructor.setBody(String.format("super($1,%s,$2);",test.getName()+".class"));
		else if(parametersOfCtr.length==1)
			constructor.setBody(String.format("super(%s,$1);",test.getName()+".class"));
		
		System.out.println(constructor);
		
		return constructor;
	}

	private static CtMethod copyMethodSignature(Method m,CtClass clazz) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		 CtMethod method = new CtMethod(pool.get("void"),m.getName(),null, clazz);
		 
		 for(Class clazzParameter : m.getParameterTypes())
		 {
			 method.addParameter(pool.get(clazzParameter.getCanonicalName()));
		 }	 
		 
		 Class[] exceptionsToThrow = m.getExceptionTypes();
		 
		 CtClass[] exceptions = new CtClass[exceptionsToThrow.length];
		 
		 int i = 0;
		 for(Class clazzException:exceptionsToThrow)
		 {
			 exceptions[i]=pool.get(clazzException.getCanonicalName());
			 i++;
		 }
		 
		 method.setExceptionTypes(exceptions);		 
		 
		 return method;
	}
	
	private static String createBodyOfConnectorOrOutboundPort(String interfaceName,String variableName,CtMethod method) throws NotFoundException
	{
		//TODO : "return" if not void
		
		String s = String.format("( ( %s ) %s).%s(",interfaceName,variableName,method.getName());
		
		 for(int i=1;i<=method.getParameterTypes().length;i++)
		 {
			 
			 s+= "$"+i;
			 i++;
			 
			 if(i<=method.getParameterTypes().length)
				 s+=", ";
		 }
		 
		 s+=");";
				 
		return s;
	}
	
	private static String createBodyOfConnector(String interfaceName,CtMethod method) throws NotFoundException
	{
		return createBodyOfConnectorOrOutboundPort(interfaceName,"this.offering",method);
	}
	
	private static String createBodyOfOutboundPort(String interfaceName,CtMethod method) throws NotFoundException
	{
		return createBodyOfConnectorOrOutboundPort(interfaceName,"this.connector",method);
	}
}
