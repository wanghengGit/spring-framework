package org.springframework.orm.hibernate5;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.lang.Nullable;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author Juergen Hoeller
 * @date 2019/10/09
 * @since 4.2
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * 适用于使用Hibernate进行数据持久化操作的情况
 * @author kit
 * @date 20200911
 */
@SuppressWarnings("serial")
public class HibernateTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, BeanFactoryAware, InitializingBean {

	@Nullable
	private SessionFactory sessionFactory;

	@Nullable
	private DataSource dataSource;

	private boolean autodetectDataSource = true;

	private boolean prepareConnection = true;

	private boolean allowResultAccessAfterCompletion = false;

	private boolean hibernateManagedSession = false;

	@Nullable
	private Object entityInterceptor;

	@Nullable
	private BeanFactory beanFactory;


	public HibernateTransactionManager() {
	}

	public HibernateTransactionManager(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		afterPropertiesSet();
	}


	public void setSessionFactory(@Nullable SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Nullable
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	protected final SessionFactory obtainSessionFactory() {
		SessionFactory sessionFactory = getSessionFactory();
		Assert.state(sessionFactory != null, "No SessionFactory set");
		return sessionFactory;
	}

	public void setDataSource(@Nullable DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy) {
			// If we got a TransactionAwareDataSourceProxy, we need to perform transactions
			// for its underlying target DataSource, else data access code won't see
			// properly exposed transactions (i.e. transactions for the target DataSource).
			this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
		}
		else {
			this.dataSource = dataSource;
		}
	}

	@Nullable
	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void setAutodetectDataSource(boolean autodetectDataSource) {
		this.autodetectDataSource = autodetectDataSource;
	}

	public void setPrepareConnection(boolean prepareConnection) {
		this.prepareConnection = prepareConnection;
	}

	public void setAllowResultAccessAfterCompletion(boolean allowResultAccessAfterCompletion) {
		this.allowResultAccessAfterCompletion = allowResultAccessAfterCompletion;
	}

	public void setHibernateManagedSession(boolean hibernateManagedSession) {
		this.hibernateManagedSession = hibernateManagedSession;
	}

	public void setEntityInterceptorBeanName(String entityInterceptorBeanName) {
		this.entityInterceptor = entityInterceptorBeanName;
	}

	public void setEntityInterceptor(@Nullable Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	@Nullable
	public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException {
		if (this.entityInterceptor instanceof Interceptor) {
			return (Interceptor) this.entityInterceptor;
		}
		else if (this.entityInterceptor instanceof String) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("Cannot get entity interceptor via bean name if no bean factory set");
			}
			String beanName = (String) this.entityInterceptor;
			return this.beanFactory.getBean(beanName, Interceptor.class);
		}
		else {
			return null;
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("Property 'sessionFactory' is required");
		}
		if (this.entityInterceptor instanceof String && this.beanFactory == null) {
			throw new IllegalArgumentException("Property 'beanFactory' is required for 'entityInterceptorBeanName'");
		}

		// Check for SessionFactory's DataSource.
		if (this.autodetectDataSource && getDataSource() == null) {
			DataSource sfds = SessionFactoryUtils.getDataSource(getSessionFactory());
			if (sfds != null) {
				// Use the SessionFactory's DataSource for exposing transactions to JDBC code.
				if (logger.isDebugEnabled()) {
					logger.debug("Using DataSource [" + sfds +
							"] of Hibernate SessionFactory for HibernateTransactionManager");
				}
				setDataSource(sfds);
			}
		}
	}


	@Override
	public Object getResourceFactory() {
		return obtainSessionFactory();
	}

	@Override
	protected Object doGetTransaction() {
		HibernateTransactionObject txObject = new HibernateTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		SessionFactory sessionFactory = obtainSessionFactory();
		SessionHolder sessionHolder =
				(SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound Session [" + sessionHolder.getSession() + "] for Hibernate transaction");
			}
			txObject.setSessionHolder(sessionHolder);
		}
		else if (this.hibernateManagedSession) {
			try {
				Session session = sessionFactory.getCurrentSession();
				if (logger.isDebugEnabled()) {
					logger.debug("Found Hibernate-managed Session [" + session + "] for Spring-managed transaction");
				}
				txObject.setExistingSession(session);
			}
			catch (HibernateException ex) {
				throw new DataAccessResourceFailureException(
						"Could not obtain Hibernate-managed Session for Spring-managed transaction", ex);
			}
		}

		if (getDataSource() != null) {
			ConnectionHolder conHolder = (ConnectionHolder)
					TransactionSynchronizationManager.getResource(getDataSource());
			txObject.setConnectionHolder(conHolder);
		}

		return txObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		return (txObject.hasSpringManagedTransaction() ||
				(this.hibernateManagedSession && txObject.hasHibernateManagedTransaction()));
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		if (txObject.hasConnectionHolder() && !txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
			throw new IllegalTransactionStateException(
					"Pre-bound JDBC Connection found! HibernateTransactionManager does not support " +
					"running within DataSourceTransactionManager if told to manage the DataSource itself. " +
					"It is recommended to use a single HibernateTransactionManager for all transactions " +
					"on a single DataSource, no matter whether Hibernate or JDBC access.");
		}

		Session session = null;

		try {
			if (!txObject.hasSessionHolder() || txObject.getSessionHolder().isSynchronizedWithTransaction()) {
				Interceptor entityInterceptor = getEntityInterceptor();
				Session newSession = (entityInterceptor != null ?
						obtainSessionFactory().withOptions().interceptor(entityInterceptor).openSession() :
						obtainSessionFactory().openSession());
				if (logger.isDebugEnabled()) {
					logger.debug("Opened new Session [" + newSession + "] for Hibernate transaction");
				}
				txObject.setSession(newSession);
			}

			session = txObject.getSessionHolder().getSession();

			boolean holdabilityNeeded = this.allowResultAccessAfterCompletion && !txObject.isNewSession();
			boolean isolationLevelNeeded = (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT);
			if (holdabilityNeeded || isolationLevelNeeded || definition.isReadOnly()) {
				if (this.prepareConnection && isSameConnectionForEntireSession(session)) {
					// We're allowed to change the transaction settings of the JDBC Connection.
					if (logger.isDebugEnabled()) {
						logger.debug("Preparing JDBC Connection of Hibernate Session [" + session + "]");
					}
					Connection con = ((SessionImplementor) session).connection();
					Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
					txObject.setPreviousIsolationLevel(previousIsolationLevel);
					if (this.allowResultAccessAfterCompletion && !txObject.isNewSession()) {
						int currentHoldability = con.getHoldability();
						if (currentHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
							txObject.setPreviousHoldability(currentHoldability);
							con.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
						}
					}
				}
				else {
					// Not allowed to change the transaction settings of the JDBC Connection.
					if (isolationLevelNeeded) {
						// We should set a specific isolation level but are not allowed to...
						throw new InvalidIsolationLevelException(
								"HibernateTransactionManager is not allowed to support custom isolation levels: " +
								"make sure that its 'prepareConnection' flag is on (the default) and that the " +
								"Hibernate connection release mode is set to 'on_close' (the default for JDBC).");
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Not preparing JDBC Connection of Hibernate Session [" + session + "]");
					}
				}
			}

			if (definition.isReadOnly() && txObject.isNewSession()) {
				// Just set to MANUAL in case of a new Session for this transaction.
				session.setFlushMode(FlushMode.MANUAL);
				// As of 5.1, we're also setting Hibernate's read-only entity mode by default.
				session.setDefaultReadOnly(true);
			}

			if (!definition.isReadOnly() && !txObject.isNewSession()) {
				// We need AUTO or COMMIT for a non-read-only transaction.
				FlushMode flushMode = SessionFactoryUtils.getFlushMode(session);
				if (FlushMode.MANUAL.equals(flushMode)) {
					session.setFlushMode(FlushMode.AUTO);
					txObject.getSessionHolder().setPreviousFlushMode(flushMode);
				}
			}

			Transaction hibTx;

			// Register transaction timeout.
			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				// Use Hibernate's own transaction timeout mechanism on Hibernate 3.1+
				// Applies to all statements, also to inserts, updates and deletes!
				hibTx = session.getTransaction();
				hibTx.setTimeout(timeout);
				hibTx.begin();
			}
			else {
				// Open a plain Hibernate transaction without specified timeout.
				hibTx = session.beginTransaction();
			}

			// Add the Hibernate transaction to the session holder.
			txObject.getSessionHolder().setTransaction(hibTx);

			// Register the Hibernate Session's JDBC Connection for the DataSource, if set.
			if (getDataSource() != null) {
				SessionImplementor sessionImpl = (SessionImplementor) session;
				// The following needs to use a lambda expression instead of a method reference
				// for compatibility with Hibernate ORM <5.2 where connection() is defined on
				// SessionImplementor itself instead of on SharedSessionContractImplementor...
				ConnectionHolder conHolder = new ConnectionHolder(() -> sessionImpl.connection());
				if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(timeout);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing Hibernate transaction as JDBC [" + conHolder.getConnectionHandle() + "]");
				}
				TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
				txObject.setConnectionHolder(conHolder);
			}

			// Bind the session holder to the thread.
			if (txObject.isNewSessionHolder()) {
				TransactionSynchronizationManager.bindResource(obtainSessionFactory(), txObject.getSessionHolder());
			}
			txObject.getSessionHolder().setSynchronizedWithTransaction(true);
		}

		catch (Throwable ex) {
			if (txObject.isNewSession()) {
				try {
					if (session != null && session.getTransaction().getStatus() == TransactionStatus.ACTIVE) {
						session.getTransaction().rollback();
					}
				}
				catch (Throwable ex2) {
					logger.debug("Could not rollback Session after failed transaction begin", ex);
				}
				finally {
					SessionFactoryUtils.closeSession(session);
					txObject.setSessionHolder(null);
				}
			}
			throw new CannotCreateTransactionException("Could not open Hibernate Session for transaction", ex);
		}
	}

	@Override
	protected Object doSuspend(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		txObject.setSessionHolder(null);
		SessionHolder sessionHolder =
				(SessionHolder) TransactionSynchronizationManager.unbindResource(obtainSessionFactory());
		txObject.setConnectionHolder(null);
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(sessionHolder, connectionHolder);
	}

	@Override
	protected void doResume(@Nullable Object transaction, Object suspendedResources) {
		SessionFactory sessionFactory = obtainSessionFactory();

		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			// From non-transactional code running in active transaction synchronization
			// -> can be safely removed, will be closed on transaction completion.
			TransactionSynchronizationManager.unbindResource(sessionFactory);
		}
		TransactionSynchronizationManager.bindResource(sessionFactory, resourcesHolder.getSessionHolder());
		if (getDataSource() != null && resourcesHolder.getConnectionHolder() != null) {
			TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
		}
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		Transaction hibTx = txObject.getSessionHolder().getTransaction();
		Assert.state(hibTx != null, "No Hibernate transaction");
		if (status.isDebug()) {
			logger.debug("Committing Hibernate transaction on Session [" +
					txObject.getSessionHolder().getSession() + "]");
		}

		try {
			hibTx.commit();
		}
		catch (org.hibernate.TransactionException ex) {
			// assumably from commit call to the underlying JDBC connection
			throw new TransactionSystemException("Could not commit Hibernate transaction", ex);
		}
		catch (HibernateException ex) {
			// assumably failed to flush changes to database
			throw convertHibernateAccessException(ex);
		}
		catch (PersistenceException ex) {
			if (ex.getCause() instanceof HibernateException) {
				throw convertHibernateAccessException((HibernateException) ex.getCause());
			}
			throw ex;
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		Transaction hibTx = txObject.getSessionHolder().getTransaction();
		Assert.state(hibTx != null, "No Hibernate transaction");
		if (status.isDebug()) {
			logger.debug("Rolling back Hibernate transaction on Session [" +
					txObject.getSessionHolder().getSession() + "]");
		}

		try {
			hibTx.rollback();
		}
		catch (org.hibernate.TransactionException ex) {
			throw new TransactionSystemException("Could not roll back Hibernate transaction", ex);
		}
		catch (HibernateException ex) {
			// Shouldn't really happen, as a rollback doesn't cause a flush.
			throw convertHibernateAccessException(ex);
		}
		catch (PersistenceException ex) {
			if (ex.getCause() instanceof HibernateException) {
				throw convertHibernateAccessException((HibernateException) ex.getCause());
			}
			throw ex;
		}
		finally {
			if (!txObject.isNewSession() && !this.hibernateManagedSession) {
				// Clear all pending inserts/updates/deletes in the Session.
				// Necessary for pre-bound Sessions, to avoid inconsistent state.
				txObject.getSessionHolder().getSession().clear();
			}
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting Hibernate transaction on Session [" +
					txObject.getSessionHolder().getSession() + "] rollback-only");
		}
		txObject.setRollbackOnly();
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void doCleanupAfterCompletion(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		// Remove the session holder from the thread.
		if (txObject.isNewSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(obtainSessionFactory());
		}

		// Remove the JDBC connection holder from the thread, if exposed.
		if (getDataSource() != null) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
		}

		Session session = txObject.getSessionHolder().getSession();
		if (this.prepareConnection && isPhysicallyConnected(session)) {
			// We're running with connection release mode "on_close": We're able to reset
			// the isolation level and/or read-only flag of the JDBC Connection here.
			// Else, we need to rely on the connection pool to perform proper cleanup.
			try {
				Connection con = ((SessionImplementor) session).connection();
				Integer previousHoldability = txObject.getPreviousHoldability();
				if (previousHoldability != null) {
					con.setHoldability(previousHoldability);
				}
				DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
			}
			catch (HibernateException ex) {
				logger.debug("Could not access JDBC Connection of Hibernate Session", ex);
			}
			catch (Throwable ex) {
				logger.debug("Could not reset JDBC Connection after transaction", ex);
			}
		}

		if (txObject.isNewSession()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing Hibernate Session [" + session + "] after transaction");
			}
			SessionFactoryUtils.closeSession(session);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not closing pre-bound Hibernate Session [" + session + "] after transaction");
			}
			if (txObject.getSessionHolder().getPreviousFlushMode() != null) {
				session.setFlushMode(txObject.getSessionHolder().getPreviousFlushMode());
			}
			if (!this.allowResultAccessAfterCompletion && !this.hibernateManagedSession) {
				disconnectOnCompletion(session);
			}
		}
		txObject.getSessionHolder().clear();
	}

	protected void disconnectOnCompletion(Session session) {
		session.disconnect();
	}

	@SuppressWarnings("deprecation")
	protected boolean isSameConnectionForEntireSession(Session session) {
		if (!(session instanceof SessionImplementor)) {
			// The best we can do is to assume we're safe.
			return true;
		}
		ConnectionReleaseMode releaseMode =
				((SessionImplementor) session).getJdbcCoordinator().getConnectionReleaseMode();
		return ConnectionReleaseMode.ON_CLOSE.equals(releaseMode);
	}

	protected boolean isPhysicallyConnected(Session session) {
		if (!(session instanceof SessionImplementor)) {
			// The best we can do is to check whether we're logically connected.
			return session.isConnected();
		}
		return ((SessionImplementor) session).getJdbcCoordinator().getLogicalConnection().isPhysicallyConnected();
	}


	protected DataAccessException convertHibernateAccessException(HibernateException ex) {
		return SessionFactoryUtils.convertHibernateAccessException(ex);
	}


	private class HibernateTransactionObject extends JdbcTransactionObjectSupport {

		@Nullable
		private SessionHolder sessionHolder;

		private boolean newSessionHolder;

		private boolean newSession;

		@Nullable
		private Integer previousHoldability;

		public void setSession(Session session) {
			this.sessionHolder = new SessionHolder(session);
			this.newSessionHolder = true;
			this.newSession = true;
		}

		public void setExistingSession(Session session) {
			this.sessionHolder = new SessionHolder(session);
			this.newSessionHolder = true;
			this.newSession = false;
		}

		public void setSessionHolder(@Nullable SessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = false;
			this.newSession = false;
		}

		public SessionHolder getSessionHolder() {
			Assert.state(this.sessionHolder != null, "No SessionHolder available");
			return this.sessionHolder;
		}

		public boolean hasSessionHolder() {
			return (this.sessionHolder != null);
		}

		public boolean isNewSessionHolder() {
			return this.newSessionHolder;
		}

		public boolean isNewSession() {
			return this.newSession;
		}

		public void setPreviousHoldability(@Nullable Integer previousHoldability) {
			this.previousHoldability = previousHoldability;
		}

		@Nullable
		public Integer getPreviousHoldability() {
			return this.previousHoldability;
		}

		public boolean hasSpringManagedTransaction() {
			return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
		}

		public boolean hasHibernateManagedTransaction() {
			return (this.sessionHolder != null &&
					this.sessionHolder.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE);
		}

		public void setRollbackOnly() {
			getSessionHolder().setRollbackOnly();
			if (hasConnectionHolder()) {
				getConnectionHolder().setRollbackOnly();
			}
		}

		@Override
		public boolean isRollbackOnly() {
			return getSessionHolder().isRollbackOnly() ||
					(hasConnectionHolder() && getConnectionHolder().isRollbackOnly());
		}

		@Override
		public void flush() {
			try {
				getSessionHolder().getSession().flush();
			}
			catch (HibernateException ex) {
				throw convertHibernateAccessException(ex);
			}
			catch (PersistenceException ex) {
				if (ex.getCause() instanceof HibernateException) {
					throw convertHibernateAccessException((HibernateException) ex.getCause());
				}
				throw ex;
			}
		}
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by {@code doSuspend} and {@code doResume}.
	 */
	private static final class SuspendedResourcesHolder {

		private final SessionHolder sessionHolder;

		@Nullable
		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(SessionHolder sessionHolder, @Nullable ConnectionHolder conHolder) {
			this.sessionHolder = sessionHolder;
			this.connectionHolder = conHolder;
		}

		private SessionHolder getSessionHolder() {
			return this.sessionHolder;
		}

		@Nullable
		private ConnectionHolder getConnectionHolder() {
			return this.connectionHolder;
		}
	}

}
