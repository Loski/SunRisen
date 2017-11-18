package fr.upmc.javassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import fr.upmc.components.ComponentI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public abstract class ClassCreator {

	public static void main (String[] args)
	{		
		/*try {
				Constructor ctr = createConnectorImplementingInterface("Test",RequestNotificationI.class).getDeclaredConstructor();
				AbstractConnector instance = (AbstractConnector) ctr.newInstance();
			
				System.out.println(instance);

				Class myPort = createOutboundPortImplementingInterface("TestPort",RequestNotificationI.class);
				Class[] type = {String.class,ComponentI.class};
				Constructor cons = myPort.getConstructor(type);
				Object[] obj = {"uri",null};
				System.out.println(cons.newInstance(obj));
				
				Class myPort2 = createInboundPortImplementingInterface("TestPortxxx",RequestNotificationI.class);
				Class[] type2 = {String.class,ComponentI.class};
				Constructor cons2 = myPort2.getConstructor(type2);
				Object[] obj2 = {"uri45",null};
				System.out.println(cons2.newInstance(obj2));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	protected static CtClass createClass(String className,Class<?> interfaceToImplementClass, Class<?> superClass) throws Exception
	{		
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = null;
		
		test = pool.makeClass(className);
		
		CtClass extendsClass = null;
		
		if(superClass!=null)
		{
			extendsClass = pool.getCtClass(superClass.getName());
			test.setSuperclass(extendsClass);
		}
		
		CtClass interfaceToImplement = pool.get(interfaceToImplementClass.getName());
		
		test.addInterface(interfaceToImplement);
		
		return test;
	}
	
	
	
	public static Class<?> createOutboundPortImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = createClass(className, interfaceToImplementClass,AbstractOutboundPort.class);
		
		Constructor[] ctrs = TemplateOutboundPort.class.getConstructors();
		for(Constructor ctr : ctrs)
		{
			CtConstructor ctrCopy = copyConstructor(ctr,test);
			System.out.println("CTR COPIED :"+ctrCopy);
			
			if(ctr.getParameterTypes().length==2)
				ctrCopy.setBody(String.format("super($1,%s,$2);",test.getName()+".class"));
			else if(ctr.getParameterTypes().length==1)
				ctrCopy.setBody(String.format("super(%s,$1);",test.getName()+".class"));
			
			test.addConstructor(ctrCopy);
		}
		
		
		for(Method m : interfaceToImplementClass.getDeclaredMethods())
		{
			System.out.println(m);
			CtMethod method = copyMethodSignature(m,test);
			
			System.out.println("YOLO: "+stringOfMethodCall(method));
			
			method.setBody(createBodyOfOutboundPort(interfaceToImplementClass.getCanonicalName(),method));	
			System.out.println(method);
		}
		
		CtMethod toString = new CtMethod(pool.get("java.lang.String"),"toString",null, test);
		toString.setBody("return \"JAVASSIST MASTER RACE\";");
		
		test.addMethod(toString);
		
		Class<?> clazz = pool.toClass(test);
		
		return clazz;

	}
	
	public static Class<?> createInboundPortImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = createClass(className, interfaceToImplementClass,AbstractInboundPort.class);
		
		Constructor[] ctrs = TemplateOutboundPort.class.getConstructors();
		for(Constructor ctr : ctrs)
		{
			CtConstructor ctrCopy = copyConstructor(ctr,test);
			System.out.println("CTR COPIED :"+ctrCopy);
			
			/*String s1 = "assert	$1 != null && $1 instanceof "+interfaceToImplementClass.getCanonicalName();
			String s2 = "assert	$2 != null && $2 instanceof "+interfaceToImplementClass.getCanonicalName();*/
			
			String s1="";
			String s2="";
			
			System.out.println(String.format("{super($1,%s,$2); %s;}",interfaceToImplementClass.getName()+".class",s2));
			
			if(ctr.getParameterTypes().length==2)
				ctrCopy.setBody(String.format("{super($1,%s,$2); %s;}",interfaceToImplementClass.getName()+".class",s2));
			else if(ctr.getParameterTypes().length==1)
				ctrCopy.setBody(String.format("{super(%s,$1); %s;}",interfaceToImplementClass.getName()+".class",s1));
			
			test.addConstructor(ctrCopy);
		}				
		
		/*for(Method m : interfaceToImplementClass.getDeclaredMethods())
		{
			System.out.println(m);
			CtMethod method = copyMethodSignature(m,test);
			
			method.setBody(createBodyOfInboundPort(interfaceToImplementClass,method));	
			System.out.println(method);
		}*/
		
		CtMethod toString = new CtMethod(pool.get("java.lang.String"),"toString",null, test);
		toString.setBody("return \"JAVASSIST MASTER RACE 777\";");
		
		test.addMethod(toString);
		
		Class<?> clazz = pool.toClass(test);
		
		return clazz;

	}
	
	protected static CtConstructor copyConstructor(Constructor ctr, CtClass test) throws Exception {
		
		ClassPool pool = ClassPool.getDefault();
		
		 Class[] parametersOfCtr = ctr.getParameterTypes();
		 
		 CtClass[] parameters = new CtClass[parametersOfCtr.length];
		 
		 int i = 0;
		 for(Class parameterClass:parametersOfCtr)
		 {
			 parameters[i]=pool.get(parameterClass.getCanonicalName());
			 i++;
		 }
		 
		 Class[] exceptionsToThrow = ctr.getExceptionTypes();
		 
		 CtClass[] exceptions = new CtClass[exceptionsToThrow.length];
		 
		 i = 0;
		 for(Class clazzException:exceptionsToThrow)
		 {
			 exceptions[i]=pool.get(clazzException.getCanonicalName());
			 i++;
		 }
		
		CtConstructor constructor = CtNewConstructor.make(parameters, exceptions, test);
		
		System.out.println(constructor);
		
		return constructor;
	}

	protected static CtMethod copyMethodSignature(Method m,CtClass clazz) throws Exception
	{
		 ClassPool pool = ClassPool.getDefault();
		
		 CtMethod method = new CtMethod(pool.get(m.getReturnType().getName()),m.getName(),null, clazz);
		 
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
	
	protected static String stringOfMethodCall(CtMethod method) throws Exception
	{
		String s = method.getName()+"(";
		
		 for(int i=0;i<method.getParameterTypes().length;i++)
		 {
			 s+= "arg"+i;
			 
			 if(i+1<method.getParameterTypes().length)
				 s+=", ";
		 }
		 
		 s+=");";
				 
		return s;
	}
	
	protected static String createBodyOfConnectorOrOutboundPort(String interfaceName,String variableName,CtMethod method) throws NotFoundException
	{
		String s = "";
		
		if(method.getReturnType().getSimpleName()!="void")
			s+="return ";
		
		s += String.format("( ( %s ) %s).%s(",interfaceName,variableName,method.getName());
		
		 for(int i=1;i<=method.getParameterTypes().length;i++)
		 {
			 s+= "$"+i;
			 
			 if(i<method.getParameterTypes().length)
				 s+=", ";
		 }
		 
		 s+=");";
				 
		return s;
	}
	
	protected static String createBodyOfOutboundPort(String interfaceName,CtMethod method) throws NotFoundException
	{
		return createBodyOfConnectorOrOutboundPort(interfaceName,"this.connector",method);
	}
	
	protected static void implementComponentService(CtClass clazz,CtMethod methodTemplate) throws Exception
	{
		 ClassPool pool = ClassPool.getDefault();
		
		//TODO : gérer autre type que Void
		 CtMethod method = new CtMethod(pool.get("void"),"call",null, clazz);
		 method.setBody("operator.accept"+stringOfMethodCall(methodTemplate));
		 
		 clazz.addMethod(method);
	}
	
	protected static String createBodyOfInboundPort(Class<?> interfaceToImplementClass,CtMethod method) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("{");
		
		if(method.getReturnType().getSimpleName()!="void")
			builder.append("return ");
		
		String returnType = getWrapperClass(method.getReturnType());
		
		CtClass anonymousClass = createClass("ComponentAnonymousDynamic", ComponentI.ComponentService.class,null);
		CtClass[] parameters = new CtClass[1];
		
		
		//TODO : à tester en multi-JVM
		CtClass interfaceToImplement = pool.get(interfaceToImplementClass.getCanonicalName());
		
		parameters[0] = interfaceToImplement;
		
		anonymousClass.addField(new CtField(interfaceToImplement, "operator", anonymousClass));
		
		CtConstructor constructor = CtNewConstructor.make(parameters,null,anonymousClass);
		constructor.setBody("this.operator=$1;");
		
		anonymousClass.addConstructor(constructor);
		
		implementComponentService(anonymousClass,method);
		
		Class<?> instanciatedClass = pool.toClass(anonymousClass);
		
		builder.append(String.format("final %s aaa = new %s(( %s ) this.owner);",instanciatedClass.getCanonicalName(),instanciatedClass.getCanonicalName(),interfaceToImplementClass.getCanonicalName()));
		builder.append("this.owner.handleRequestAsync(aaa);}");
		
		System.out.println(builder.toString());
		
		return builder.toString();
	}
	
	protected static String getWrapperClass(CtClass clazz)
	{
		if (clazz.isPrimitive()) {
		   CtPrimitiveType primitive = (CtPrimitiveType) clazz;
		  return primitive.getWrapperName();
		} else {
			return clazz.getName();
		 }
	}
}
