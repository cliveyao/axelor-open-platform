/*
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.axelor.db.jpa;

import com.google.common.base.Preconditions;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import com.zaxxer.hikari.HikariDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.cfg.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.Map;

/**
 * JPA provider for guice persist.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class JpaPersistModule extends PersistModule {
  private final String jpaUnit;

  private HikariDataSource hikariDataSource;
  private DataSourceTransactionManager dataSourceTransactionManager;


  public JpaPersistModule(String jpaUnit) {
    Preconditions.checkArgument(
        null != jpaUnit && jpaUnit.length() > 0, "JPA unit name must be a non-empty string.");
    this.jpaUnit = jpaUnit;
  }

  private Map<?, ?> properties;
  private MethodInterceptor transactionInterceptor;

  @Override
  protected void configurePersistence() {
    bindConstant().annotatedWith(Jpa.class).to(jpaUnit);

    bind(DataSource.class).toInstance(hikariDataSource);
    bind(PlatformTransactionManager.class).toInstance(dataSourceTransactionManager);

    bind(JpaPersistService.class).in(Singleton.class);

    bind(PersistService.class).to(JpaPersistService.class);
    bind(UnitOfWork.class).to(JpaPersistService.class);
    bind(EntityManager.class).toProvider(JpaPersistService.class);
    bind(EntityManagerFactory.class)
        .toProvider(JpaPersistService.EntityManagerFactoryProvider.class);

    transactionInterceptor = new JpaLocalTxnInterceptor();
    requestInjection(transactionInterceptor);


  }

  @Override
  protected MethodInterceptor getTransactionInterceptor() {
    return transactionInterceptor;
  }

  @Provides
  @Jpa
  Map<?, ?> provideProperties() {
    return properties;
  }

  /**
   * Configures the JPA persistence provider with a set of properties.
   *
   * @param properties A set of name value pairs that configure a JPA persistence provider as per
   *     the specification.
   * @since 4.0 (since 3.0 with a parameter type of {@code java.util.Properties})
   */
  public JpaPersistModule properties(Map<?, ?> properties) {
    this.properties = properties;
    if (properties.get(Environment.DATASOURCE) != null){
      hikariDataSource = (HikariDataSource)properties.get(Environment.DATASOURCE);
      dataSourceTransactionManager = new DataSourceTransactionManager(hikariDataSource);
    }
    return this;
  }

}
