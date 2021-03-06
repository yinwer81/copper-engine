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
package org.copperengine.monitoring.server.persistent;

import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.MessagePostProcessor;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.Serializer;

/**
 * H2 implementation of the {@link DatabaseDialect} interface.
 *
 * @author dmoebius
 */
public class H2MonitoringDbDialect extends BaseDatabaseMonitoringDialect {

    public H2MonitoringDbDialect(Serializer serializer, MessagePostProcessor messagePostProcessor, BatchingAuditTrail auditTrail) {
        super(serializer, messagePostProcessor, auditTrail);
    }

}
