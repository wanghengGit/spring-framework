package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author wangheng
 * @date 2019/09/24
 * @since 07.07.2003
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
 */
public interface HierarchicalBeanFactory extends BeanFactory {

	/**
	 * Return the parent bean factory, or {@code null} if there is none.
	 */
	@Nullable
	BeanFactory getParentBeanFactory();

	boolean containsLocalBean(String name);

}
