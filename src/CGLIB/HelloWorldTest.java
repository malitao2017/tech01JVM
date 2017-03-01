/*
 * CglibProxy.java
 * Copyright: TsingSoft (c) 2015
 * Company: 北京清软创新科技有限公司
 */
package CGLIB;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class HelloWorldTest{
	public static void main(String[] args) {
		HelloWorld hw = new HelloWorld();
		CglibProxy cg = new CglibProxy();
		HelloWorld obj = (HelloWorld)cg.createProxy(hw);
		obj.sayHello();
	}
}

/**
 * 动态代理
 * @author LT
 * @version 1.0, 2015年9月7日
 */
class CglibProxy implements MethodInterceptor{
	private Object obj;
	public Object createProxy(Object target){
		//要代理的原对象
		this.obj=target;
		Enhancer en = new Enhancer();
		en.setSuperclass(this.obj.getClass());//设置代理目标
		en.setCallback(this);//设置回调
		en.setClassLoader(target.getClass().getClassLoader());
		return en.create();
	}
	@Override
	public Object intercept(Object target, Method method, Object[] parm,
			MethodProxy methodproxy) throws Throwable {
		before();
		Object result = null;
		result = methodproxy.invokeSuper(target, parm);
		after();
		return result;
	}
	private void before(){
		System.out.println("before");
	}
	private void after(){
		System.out.println("after");
	}
}
class HelloWorld{
	public void sayHello(){
		System.out.println("hello world");
	}
}

