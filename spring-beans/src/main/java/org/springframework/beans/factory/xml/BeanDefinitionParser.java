package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;

/**
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 *
 * @author wangheng
 * @date 2019/08/16
 */
public interface BeanDefinitionParser {

	@Nullable
	BeanDefinition parse(Element element, ParserContext parserContext);

}
