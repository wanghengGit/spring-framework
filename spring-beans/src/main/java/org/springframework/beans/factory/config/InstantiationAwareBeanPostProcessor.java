package org.springframework.beans.factory.config;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.lang.Nullable;

/**
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @date 2019/08/19
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 * @since 1.2
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
	/**
	 * Instantiation 实例化的意思，和Initialization初始化 比较相似，容易混淆。
	 * postProcessBeforeInstantiation用来获取 bean，如果获取到，则不再执行对应 bean的初始化之前流程，直接执行后面要讲的postProcessAfterInitialization方法。
	 * 调用点在AbstractAutowireCapableBeanFactory中
	 */
	@Nullable
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 实例化之后调用的方法，在AbstractAutowireCapableBeanFactory.populateBean()填充方法中会触发。
	 * 该方法默认返回为true，如果返回false，则中断populateBean方法，即不再执行属性注入的过程。
	 * 实际项目中，该扩展方法使用不多
	 */
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	@Nullable
	default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		return null;
	}

	/**
	 * 该方法用于属性注入，在 bean 初始化阶段属性填充时触发。@Autowired，@Resource 等注解原理基于此方法实现。
	 * 具体调用点在AbstractAutowireCapableBeanFactory中populateBean方法
	 */
	@Deprecated
	@Nullable
	default PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}

}
