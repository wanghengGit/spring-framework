package org.springframework.aop;

/**
 * @author Rod Johnson
 *
 * @author wangheng
 * @date 2019/08/16
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * Get the Pointcut that drives this advisor.
	 */
	Pointcut getPointcut();

}
