/*
 * Jvm.java
 * Copyright: TsingSoft (c) 2015
 * Company: 北京清软创新科技有限公司
 */
package tech01JVM;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * JVM 的基本堆栈案例
 * 程序：
 * http://blog.csdn.net/cutesource/article/details/5906705
 * 讲解：
 * http://blog.csdn.net/cutesource/article/details/5904501
 * http://blog.csdn.net/cutesource/article/details/5904542
 * http://blog.csdn.net/cutesource/article/details/5906705
 * http://blog.csdn.net/cutesource/article/details/5907418
 * @author LT
 * @version 1.0, 2015年9月7日
 */
public class Jvm {
	public static void main(String[] args) throws Throwable{
		//测试java堆溢出：new类对象过多
//		HeapOOM.heapOOM();
		
		//测试方法区1：类太多
		MethodAreaOOM.methodAreaOOM();
		//测试方法区2：静态变量、常量过多
//		ConstantOOM.constantOOM();
		
		//测试栈1：调用层过多
//		StackOOMLayer.stackOOM();
		//测试栈2：调用线程过多（可能会造成系统假死，需要重启系统）
//		StackOOMThread.stackOOM();
	}
	/**
	 * 测试String的Intern方法
	 */
	public static void testIntern(){
		String str1="hello";
		String str2=new String("hello");
		//System.out.println(str1==str2);
		System.out.println(str1==str1.intern());
		System.out.println(str1==str2.intern());
		System.out.println(str2==str2.intern());
	}
}

/**
 * java堆-内存溢出
 * new 对象实例，堆大小由 -Xmx和-Xms调节
 * @author LT
 * @version 1.0, 2015年9月8日
 */
class HeapOOM{
	static class OOMObject{}
	/**
	 * 在eclipse的Console 中加此命令会快速报错
	 * 待定：在eclipse的jar命令处配置  defualt VM agruments 
	 * 初始堆大小 最大堆大小 垃圾回收信息输出 新生代的eden和两个survivor区的分配比例，如为8，则是8：2 自动生成Dump
	 * -verbose:gc -Xms10M -Xmx10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+HeapDumpOnOutOfMemoryError
	 */
	public static void heapOOM(){
		List<OOMObject> list = new ArrayList<OOMObject>();
		while(true){
			list.add(new OOMObject());
		}
	}
}
/**
 * 方法区-内存溢出，存放虚拟机加载类的相关信息，如类、静态变量和常量，用-XX:PermSize和-XX:MaxPermSize调节
 * 1.类太多
 * 设置：-XX:PermSize=10M -XX:MaxPermSize=10M
 * Exception OutOfMemoryError:PermGen space
 * @author LT
 * @version 1.0, 2015年9月8日
 */
class MethodAreaOOM{
	static class OOMObject{}
	public static void methodAreaOOM(){
		while(true){
			Enhancer eh = new Enhancer();
			eh.setSuperclass(OOMObject.class);
//			eh.setUseCache(false);
			eh.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object proxy, Method method, Object[] params,
						MethodProxy methodProxy) throws Throwable {
					return methodProxy.invokeSuper(proxy, params);
				}
			});
			eh.create();
		}
	}
}
/**
 * 方法区-内存溢出
 * 2.静态变量或常量
 * 设置：-XX:PermSize=10M -XX:MaxPermSize=10M
 * Exception： OutOfMemoryError PermGen space
 * @author LT
 * @version 1.0, 2015年9月8日
 */
class ConstantOOM{
	public static void constantOOM(){
		List<String> list = new ArrayList<String>();
		int i = 0;
		while(true){
			list.add(String.valueOf(i++).intern());
		}
	}
}
/**
 * java栈和本地方法栈（native方法）-内存溢出，存放局部变量表、操作、方法出口等与方法执行相关信息，有Xss调节
 * 1.方法调用层次太多
 * 设置： -Xss128k
 * Exception:java.lang.StackOverflowError
 * @author LT
 * @version 1.0, 2015年9月8日
 */
class StackOOMLayer{
	private int stackLength=1;
	private void stackLeak(){
		stackLength++;
		stackLeak();
	}
	public  static  void stackOOM() throws Throwable{
		StackOOMLayer sta = new StackOOMLayer();
		try{
			sta.stackLeak();
		}catch(Throwable th){
			System.out.println("num:"+sta.stackLength);
			throw th;
		}
	}
}
/**
 * java栈和本地方法栈-内存溢出
 * 2.线程太多
 * 设置：-Xss128k
 * Exception OutOfMemoryError : unable to create new native thread
 * @author LT
 * @version 1.0, 2015年9月8日
 */
class StackOOMThread{
	private int len = 1;
	private void sleep(){
		while(true){
			try{Thread.sleep(1000);}catch(Exception e){}
		}
	}
	private void stackSleep(){
		while(true){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					sleep();
				}
			});
			t.start();
			len++;
		}
	}
	public static void stackOOM() throws Exception{
		StackOOMThread s = new StackOOMThread();
		try{
		s.stackSleep();
		}catch(Exception e){
			System.out.println("num:"+s.len);
			throw e;
		}
	}
}