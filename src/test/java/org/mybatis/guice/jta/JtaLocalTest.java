/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.guice.jta;

import static org.junit.Assert.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.util.Providers;

import java.util.List;

import javax.sql.DataSource;

import org.apache.aries.transaction.AriesTransactionManager;
import org.apache.aries.transaction.internal.AriesTransactionManagerImpl;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mybatis.guice.MyBatisJtaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JtaLocalTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(JtaLocalTest.class);

  static AriesTransactionManager manager;
  static DataSource dataSource1;
  static DataSource dataSource2;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
    LogFactory.useSlf4jLogging();

    manager = new AriesTransactionManagerImpl();

    dataSource1 = BaseDB.createLocalDataSource(BaseDB.NAME_DB1, BaseDB.URL_DB1, manager);
    dataSource2 = BaseDB.createLocalDataSource(BaseDB.NAME_DB2, BaseDB.URL_DB2, manager);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    BaseDB.dropTable(BaseDB.URL_DB1);
    BaseDB.dropTable(BaseDB.URL_DB2);
  }

  @Rule
  public TestName testName = new TestName();
  private Injector injector;

  JtaProcess process;

  @Before
  public void setup() throws Exception {
    LOGGER.info("********************************************************************************");
    LOGGER.info("Testing: " + testName.getMethodName() + "(" + getClass().getName() + ")");
    LOGGER.info("********************************************************************************");
    LogFactory.useSlf4jLogging();

    LOGGER.info("create injector");
    injector = Guice.createInjector(new PrivateModule() {

      @Override
      protected void configure() {
        install(new MyBatisJtaModule(manager) {

          @Override
          protected void initialize() {
            environmentId("db1");
            bindDataSourceProvider(Providers.of(dataSource1));
            bindDefaultTransactionProvider();
            bindDatabaseIdProvider(new VendorDatabaseIdProvider());

            addMapperClass(JtaMapper.class);

            bind(JtaService1Impl.class);
          }
        });

        expose(JtaService1Impl.class);
      };
    }, new PrivateModule() {

      @Override
      protected void configure() {
        install(new MyBatisJtaModule(manager) {

          @Override
          protected void initialize() {
            environmentId("db2");
            bindDataSourceProvider(Providers.of(dataSource2));
            bindDefaultTransactionProvider();
            bindDatabaseIdProvider(new VendorDatabaseIdProvider());

            addMapperClass(JtaMapper.class);

            bind(JtaService2Impl.class);
            bind(JtaProcess.class);
          }
        });

        expose(JtaService2Impl.class);
        expose(JtaProcess.class);
      };
    });

    injector.injectMembers(this);
    process = injector.getInstance(JtaProcess.class);
  }

  @After
  public void tearDown() throws Exception {
    BaseDB.clearTable(BaseDB.URL_DB1);
    BaseDB.clearTable(BaseDB.URL_DB2);

    LOGGER.info("********************************************************************************");
    LOGGER.info("Testing done: " + testName.getMethodName() + "(" + getClass().getName() + ")");
    LOGGER.info("********************************************************************************");
  }

  /**
   * begin REQUIRED
   *   insert(id=1)
   * commit REQUIRED
   * 
   * have 1 rows
   */
  @Test
  public void testRequired() throws Exception {
    process.required(1);
    checkCountRows(1);
  }

  /**
   * begin REQUIRES_NEW
   *   insert(id=1)
   * commit REQUIRES_NEW
   * 
   * have 1 rows
   */
  @Test
  public void testRequiresNew() throws Exception {
    process.requiresNew(1);
    checkCountRows(1);
  }

  /**
   * begin REQUIRED
   *   insert(id=1)
   * roll back REQUIRED
   * 
   * have 0 rows
   */
  @Test
  public void testRequiredAndRollback() throws Exception {
    try {
      process.requiredAndRollback(1);
    } catch (JtaRollbackException e) {
    }
    checkCountRows(0);
  }

  /**
   * begin REQUIRES_NEW
   *   insert(id=1)
   * roll back REQUIRES_NEW
   * 
   * have 0 rows
   */
  @Test
  public void testRequiresNewAndRollback() throws Exception {
    try {
      process.requiresNewAndRollback(1);
    } catch (JtaRollbackException e) {
    }
    checkCountRows(0);
  }

  /**
   * begin REQUIRED
   *   insert(id=1)
   *   begin REQUIRES_NEW
   *      insert(id=2)
   *   commit REQUIRES_NEW
   * commit REQUIRED
   * 
   * have 2 rows
   */
  @Test
  public void testRequiredAndRequiresNew() throws Exception {
    process.requiredAndRequiresNew();
    checkCountRows(2);
  }

  /**
   * begin REQUIRED
   *   begin REQUIRES_NEW
   *      insert(id=2)
   *   commit REQUIRES_NEW
   *   insert(id=1)
   * commit REQUIRED
   * 
   * have 2 rows
   */
  @Test
  public void testRequiresNewAndRequired() throws Exception {
    process.requiresNewAndRequired();
    checkCountRows(2);
  }

  /**
   * begin REQUIRED
   *   insert(id=1)
   *   begin REQUIRES_NEW
   *      insert(id=2)
   *   roll back REQUIRES_NEW
   * commit REQUIRED
   * 
   * have 1 rows and id=1 (from commited REQUIRED)
   */
  @Test
  public void testRollbackInternalRequiresNew() throws Exception {
    try {
      process.rollbackInternalRequiresNew();
    } catch (JtaRollbackException e) {
    }
    checkCountRowsAndIndex(1, 1);
  }

  /**
   * begin REQUIRED
   *   begin REQUIRES_NEW
   *      insert(id=1)
   *   roll back REQUIRES_NEW
   *   insert(id=2)
   * commit REQUIRED
   * 
   * have 1 rows and id=2 (from commited REQUIRED) 
   */
  @Test
  public void testRollbackInternalRequiresNew2() throws Exception {
    try {
      process.rollbackInternalRequiresNew2();
    } catch (JtaRollbackException e) {
    }
    checkCountRowsAndIndex(1, 2);
  }

  /**
   * begin REQUIRED
   *   begin REQUIRES_NEW
   *      insert(id=1)
   *   commit REQUIRES_NEW
   *   insert(id=2)
   * roll back REQUIRED
   * 
   * have 1 rows and id=1 (from commited REQUIRES_NEW) 
   */
  @Test
  public void testRollbackExternalRequired() throws Exception {
    try {
      process.rollbackExternalRequired();
    } catch (JtaRollbackException e) {
    }
    checkCountRowsAndIndex(1, 1);
  }

  /**
   * begin REQUIRED
   *   insert(id=1)
   *   begin REQUIRES_NEW
   *      insert(id=2)
   *   commit REQUIRES_NEW
   * roll back REQUIRED
   * 
   * have 1 rows and id=2 (from commited REQUIRES_NEW) 
   */
  @Test
  public void testRollbackExternalRequired2() throws Exception {
    try {
      process.rollbackExternalRequired2();
    } catch (JtaRollbackException e) {
    }
    checkCountRowsAndIndex(1, 2);
  }

  private void checkCountRows(int count) throws Exception {
    String name = testName.getMethodName();
    List<Integer> readRows;
    readRows = BaseDB.readRows(BaseDB.URL_DB1, BaseDB.NAME_DB1);
    LOGGER.info("db1 check count rows {}:{}", count, readRows.size());

    assertEquals(name + " db1 count rows", count, readRows.size());

    readRows = BaseDB.readRows(BaseDB.URL_DB2, BaseDB.NAME_DB2);
    LOGGER.info("db2 check count rows {}:{}", count, readRows.size());

    assertEquals(name + " db2 count rows", count, readRows.size());
  }

  private void checkCountRowsAndIndex(int count, int index) throws Exception {
    String name = testName.getMethodName();
    List<Integer> readRows;
    readRows = BaseDB.readRows(BaseDB.URL_DB1, BaseDB.NAME_DB1);

    LOGGER.info("{} db1 check count rows {}:{}", new Object[] { name, count, readRows.size() });
    LOGGER.info("{} db1 check row id {}:{}", new Object[] { name, index, readRows.get(0).intValue() });

    assertEquals(name + " db1 count rows", count, readRows.size());
    assertEquals(name + " db1 row id", index, readRows.get(0).intValue());

    readRows = BaseDB.readRows(BaseDB.URL_DB2, BaseDB.NAME_DB2);
    LOGGER.info("{} db2 check count rows {}:{}", new Object[] { name, count, readRows.size() });
    LOGGER.info("{} db2 check row id {}:{}", new Object[] { name, index, readRows.get(0).intValue() });

    assertEquals(name + " db2 count rows", count, readRows.size());
    assertEquals(name + " db2 row id", index, readRows.get(0).intValue());
  }
}
