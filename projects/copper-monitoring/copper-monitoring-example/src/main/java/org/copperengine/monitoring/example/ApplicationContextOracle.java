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
package org.copperengine.monitoring.example;

import java.beans.PropertyVetoException;

import org.copperengine.core.common.WorkflowRepository;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.OracleDialect;
import org.copperengine.core.persistent.StandardJavaSerializer;
import org.copperengine.monitoring.server.persistent.DatabaseMonitoringDialect;
import org.copperengine.monitoring.server.persistent.OracleMonitoringDbDialect;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ApplicationContextOracle extends ApplicationContext {

    @Override
    protected DatabaseUtil setupDatabase(WorkflowRepository workflowRepository, RuntimeStatisticsCollector runtimeStatisticsCollector) {
        ComboPooledDataSource datasource_oracle = new ComboPooledDataSource();
        try {
            datasource_oracle.setDriverClass("oracle.jdbc.OracleDriver");
            datasource_oracle.setJdbcUrl("jdbc:oracle:thin:COPPER2/COPPER2@localhost:1521:HM");
            datasource_oracle.setMinPoolSize(1);
            datasource_oracle.setMaxPoolSize(8);
            datasource_oracle.setPreferredTestQuery("SELECT 1 FROM DUAL");
            datasource_oracle.setIdleConnectionTestPeriod(15);
        } catch (PropertyVetoException e1) {
            throw new RuntimeException(e1);
        }

        final OracleDialect oracleDialect = new OracleDialect();
        oracleDialect.setWfRepository(workflowRepository);
        oracleDialect.setRuntimeStatisticsCollector(runtimeStatisticsCollector);
        oracleDialect.setDbBatchingLatencyMSec(0);
        oracleDialect.setEngineIdProvider(getEngineIdProvider());
        oracleDialect.startup();
        return new DatabaseUtil(oracleDialect, datasource_oracle);
    }

    @Override
    protected DatabaseMonitoringDialect getMonitoringDbDialect() {
        return new OracleMonitoringDbDialect(new StandardJavaSerializer(), messagePostProcessor, auditTrail);
    }

}
