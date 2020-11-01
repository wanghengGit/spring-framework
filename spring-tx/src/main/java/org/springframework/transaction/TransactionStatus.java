package org.springframework.transaction;

import java.io.Flushable;

/**
 * @author Juergen Hoeller
 * @date 2019/10/09
 * @since 27.03.2003
 * @see #setRollbackOnly()
 * @see PlatformTransactionManager#getTransaction
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()
 * @date 20200717
 */
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {

	boolean hasSavepoint();

	@Override
	void flush();

}
