/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.formdata.storage.migration.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.formdata.storage.migration.cli.config.CephFormDataStorageConfig;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.repository.FormDataRepository;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@SpringBootTest(
    classes = {FormDataStorageMigrationCliRunnerIT.TestConfiguration.class},
    args = {
        "--delete-after-migration=true",
        "--delete-invalid-data=true",

        "--s3.config.client.protocol=http",
        "--s3.config.options.pathStyleAccess=true",

        "--storage.backend.ceph.http-endpoint=http://localhost:8100",
        "--storage.backend.ceph.access-key=key",
        "--storage.backend.ceph.secret-key=key",
        "--storage.backend.ceph.bucket=bucket",

        "--storage.backend.redis.password=",
        "--storage.backend.redis.sentinel.master=mymaster",
        "--storage.backend.redis.sentinel.nodes=127.0.0.1:26379"
    })
class FormDataStorageMigrationCliRunnerIT {

  private static final FormDataKeyProvider KEY_PROVIDER = new FormDataKeyProviderImpl();

  private static final String FORM_DATA_KEY = "process/542d91c9-14d7-11ed-9169-0a580a80281a/task/Activity_10Pjrfg";
  private static final String START_FORM_DATA_KEY = "process-definition/auto_process_SATT/start-form/bb1cd559-bc09-488d-2b15-f44d1216daa8";

  @Autowired
  private FormDataStorageService redisFormDataStorageService;
  @Autowired
  private FormDataStorageService cephFormDataStorageService;

  @Test
  void shouldMigrateData() {
    var formData = redisFormDataStorageService.getFormData(FORM_DATA_KEY);
    var startFormData = redisFormDataStorageService.getFormData(START_FORM_DATA_KEY);

    assertThat(formData).isPresent();
    assertThat(formData.get().getData().get("name")).isEqualTo("John");
    assertThat(startFormData).isPresent();
    assertThat(startFormData.get().getData().get("name")).isEqualTo("Bob");
    assertThat(cephFormDataStorageService.keys()).isEmpty();
  }

  @Configuration
  @ComponentScan(value = "com.epam.digital.data.platform.formdata.storage.migration.cli",
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CephFormDataStorageConfig.class))
  protected static class TestConfiguration {

    @Bean
    public FormDataStorageService cephFormDataStorageService() {
      return FormDataStorageService.builder()
          .keyProvider(KEY_PROVIDER)
          .repository(new FormDataRepository() {

            final Map<String, FormDataDto> storage = new HashMap<>(Map.of(
                FORM_DATA_KEY, FormDataDto.builder()
                    .data(new LinkedHashMap<>(Map.of("name", "John")))
                    .build(),
                START_FORM_DATA_KEY, FormDataDto.builder()
                    .data(new LinkedHashMap<>(Map.of("name", "Bob")))
                    .build()
            ));

            @Override
            public Optional<FormDataDto> getFormData(String key) {
              return Optional.of(storage.get(key));
            }

            @Override
            public void putFormData(String s, FormDataDto formDataDto) {
              throw new UnsupportedOperationException();
            }

            @Override
            public Set<String> getKeys(String s) {
              throw new UnsupportedOperationException();
            }

            @Override
            public void delete(Set<String> keys) {
              storage.keySet().removeAll(keys);
            }

            @Override
            public Set<String> keys() {
              return storage.keySet();
            }
          }).build();
    }
  }
}