package fr.upmc.javassist;

import java.lang.reflect.Method;

import fr.upmc.components.connectors.AbstractConnector;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class RequestDispatcherCreator extends ClassCreator{

	public static Class<?> createRequestDispatcher(String className,Class<?> dispatcher,Class<?> submissionInterface) throws Exception{
		
		System.out.println("CREATING NEW JAVASSIST DISPATCHER FOR :"+submissionInterface.getName());
		
		ClassPool pool = ClassPool.getDefault();
		
		CtClass test = pool.get(dispatcher.getCanonicalName());
		test.defrost();
		
		CtClass inport = null;
		
		try
		{
			inport = pool.get("newRequestSubmissionerPort_"+className);
									
		}catch(Exception e)
		{
			inport = pool.makeClass("newRequestSubmissionerPort_"+className);
		}
		
		test.addField(new CtField(inport, "newInBoundPort_"+className, test));
		test.setName(className);
		
		Class<?> clazz = pool.toClass(test);
		
		return clazz;
	}
}
