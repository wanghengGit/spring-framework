package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.lang.Nullable;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 *
 * @author wangheng
 * @date 2019/08/16
 */
public interface AspectJAdvisorFactory {

	boolean isAspect(Class<?> clazz);

	void validate(Class<?> aspectClass) throws AopConfigException;

	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	@Nullable
	Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName);

	@Nullable
	Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}
