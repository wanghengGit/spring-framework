package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @date 2019/09/24
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 * @date 20200403
 * @author kit
 */
public interface ListableBeanFactory extends BeanFactory {

	boolean containsBeanDefinition(String beanName);

	int getBeanDefinitionCount();

	String[] getBeanDefinitionNames();

	String[] getBeanNamesForType(ResolvableType type);

	String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

	String[] getBeanNamesForType(@Nullable Class<?> type);

	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
