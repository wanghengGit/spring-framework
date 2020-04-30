package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 *
 * @date 2019/08/16
 */
public interface BeanPostProcessor {

	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 该方法Spring IOC过程中最后一个常用的扩展点，用于 bean 初始化之后的后置处理。
	 * IOC 流程执行到此处，一个完整的 bean 已经创建结束，可在此处对 bean 进行包装或者代理。
	 * Spring AOP 原理便是基于此扩展点实现，实现方式在AbstractAutoProxyCreator中：
	 */
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
