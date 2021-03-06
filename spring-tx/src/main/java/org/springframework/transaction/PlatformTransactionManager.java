package org.springframework.transaction;

import org.springframework.lang.Nullable;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @date 2019/10/09
 * @since 16.05.2003
 * @see org.springframework.transaction.support.TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * 用于执行具体的事务操作
 */
public interface PlatformTransactionManager extends TransactionManager {

	TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException;

	void commit(TransactionStatus status) throws TransactionException;

	void rollback(TransactionStatus status) throws TransactionException;

}
