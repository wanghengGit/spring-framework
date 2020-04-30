package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 *
 * @date 2019/08/16
 */
public interface Advisor {

	Advice EMPTY_ADVICE = new Advice() {};


	Advice getAdvice();

	boolean isPerInstance();

}
