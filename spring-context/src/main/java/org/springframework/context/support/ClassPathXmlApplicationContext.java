package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author wangheng
 * @date 2019/09/24
 * @see #getResource
 * @see #getResourceByPath
 * @see GenericApplicationContext
 * @date 20200324
 * 从类的跟路径下加载配置文件,推荐使用这种
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	@Nullable
	private Resource[] configResources;


	public ClassPathXmlApplicationContext() {
	}

	/**
	 * 	如果已经有 ApplicationContext 并需要配置成父子关系，那么调用这个构造方法
 	 */
	public ClassPathXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}

	public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}

	public ClassPathXmlApplicationContext(String[] configLocations, @Nullable ApplicationContext parent)
			throws BeansException {

		this(configLocations, true, parent);
	}

	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}

	/**
	 * 方法的参数很容易理解，configLocations指Spring的xml配置文件；refresh指是否需要刷新，
	 * 这个refresh决定了是否进行bean解析、注册及实例化；parent指父ApplicationContext
	 * setConfigLocations方法就是设置框架要加载的资源文件的位置。
	 * 进入refresh方法，这个方法继承自AbstractApplicationContext，所以具体实现在AbstractApplicationContext类中
	 * @param configLocations
	 * @param refresh
	 * @param parent
	 * @throws BeansException
	 */
	public ClassPathXmlApplicationContext(
			String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
			throws BeansException {

		super(parent);
		// 根据提供的路径，处理成配置文件数组(以分号、逗号、空格、tab、换行符分割)
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();// 核心方法
		}
	}


	public ClassPathXmlApplicationContext(String path, Class<?> clazz) throws BeansException {
		this(new String[] {path}, clazz);
	}

	public ClassPathXmlApplicationContext(String[] paths, Class<?> clazz) throws BeansException {
		this(paths, clazz, null);
	}

	public ClassPathXmlApplicationContext(String[] paths, Class<?> clazz, @Nullable ApplicationContext parent)
			throws BeansException {

		super(parent);
		Assert.notNull(paths, "Path array must not be null");
		Assert.notNull(clazz, "Class argument must not be null");
		this.configResources = new Resource[paths.length];
		for (int i = 0; i < paths.length; i++) {
			this.configResources[i] = new ClassPathResource(paths[i], clazz);
		}
		refresh();
	}


	@Override
	@Nullable
	protected Resource[] getConfigResources() {
		return this.configResources;
	}

}
