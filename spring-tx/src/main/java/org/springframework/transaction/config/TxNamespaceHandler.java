package org.springframework.transaction.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 *
 * @author wangheng
 * @date 2019/08/19
 */
public class TxNamespaceHandler extends NamespaceHandlerSupport {

	static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";


	static String getTransactionManagerName(Element element) {
		return (element.hasAttribute(TRANSACTION_MANAGER_ATTRIBUTE) ?
				element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE) : DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
	}


	@Override
	public void init() {
		registerBeanDefinitionParser("advice", new TxAdviceBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		registerBeanDefinitionParser("jta-transaction-manager", new JtaTransactionManagerBeanDefinitionParser());
	}

}
