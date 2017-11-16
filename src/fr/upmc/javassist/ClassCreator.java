package fr.upmc.javassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
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
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Class<?> createConnectorImplementingInterface(String className,Class<?> interfaceToImplementClass) throws Exception
	{
		ClassPool pool = ClassPool.getDefault();
		
		CtClass extendsClass = pool.getCtClass(AbstractConnector.class.getName());
		
		CtClass test = pool.makeClass("Test");
		
		test.setSuperclass(extendsClass);
		
		CtClass interfaceToImplement = pool.get(interfaceToImplementClass.getCanonicalName());
		
		test.addInterface(interfaceToImplement);
		
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
	
	private static String createBodyOfConnector(String interfaceName,CtMethod method) throws NotFoundException
	{
		String s = String.format("( ( %s ) this.offering).%s(",interfaceName,method.getName());
		
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
}
