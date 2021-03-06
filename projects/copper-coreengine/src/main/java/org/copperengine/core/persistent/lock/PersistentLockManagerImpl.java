/*
 * Copyright 2002-2014 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent.lock;

import java.sql.Connection;
import java.util.Date;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.PersistentProcessingEngine;
import org.copperengine.core.Response;
import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.TransactionController;

/**
 * Implementation for the {@link PersistentLockManager} interface. It needs a {@link PersistentLockManagerDialect} and
 * a {@link TransactionController} to work.
 * 
 * @author austermann
 * 
 */
public class PersistentLockManagerImpl implements PersistentLockManager {

    private final PersistentProcessingEngine engine;
    private final PersistentLockManagerDialect dialect;
    private final TransactionController transactionController;

    public PersistentLockManagerImpl(PersistentProcessingEngine engine, PersistentLockManagerDialect dialect, TransactionController transactionController) {
        this.engine = engine;
        this.dialect = dialect;
        this.transactionController = transactionController;
    }

    @Override
    public void acquireLock(final String lockId, final String correlationId, final String workflowInstanceId) {
        try {
            transactionController.run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    final String replyCorrelationId = dialect.acquireLock(lockId, workflowInstanceId, correlationId, new Date(), con);
                    if (replyCorrelationId != null) {
                        engine.notify(new Response<PersistentLockResult>(replyCorrelationId, PersistentLockResult.OK, null), con);
                    }
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperRuntimeException("acquireLock failed", e);
        }
    }

    @Override
    public void releaseLock(final String lockId, final String workflowInstanceId) {
        try {
            transactionController.run(new DatabaseTransaction<Void>() {
                @Override
                public Void run(Connection con) throws Exception {
                    final String replyCorrelationId = dialect.releaseLock(lockId, workflowInstanceId, con);
                    if (replyCorrelationId != null) {
                        engine.notify(new Response<PersistentLockResult>(replyCorrelationId, PersistentLockResult.OK, null), con);
                    }
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CopperRuntimeException("acquireLock failed", e);
        }
    }

}
