package org.springframework.beans.factory.config;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.lang.Nullable;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 *
 * @author wangheng
 * @date 2019/08/19
 */
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

	@Override
	@Nullable
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	@Nullable
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	@Nullable
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		return null;
	}

	@Deprecated
	@Override
	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
