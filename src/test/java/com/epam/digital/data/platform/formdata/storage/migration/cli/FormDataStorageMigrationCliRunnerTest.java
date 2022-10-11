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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.formdata.storage.migration.cli.config.ValidationConfig;
import com.epam.digital.data.platform.formdata.storage.migration.cli.dto.ArgsDto;
import com.epam.digital.data.platform.formdata.storage.migration.cli.validator.FormDataKeyValidator;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormDataStorageMigrationCliRunnerTest {

  @Mock
  private FormDataStorageService cephStorage;
  @Mock
  private FormDataStorageService redisStorage;

  private final FormDataKeyValidator validator = new FormDataKeyValidator(ValidationConfig.PATTERNS);
  private final FormDataKeyProvider keyProvider = new FormDataKeyProviderImpl();

  @Test
  void shouldMigrateData() {
    var args = buildArgs(true, true);
    var runner = new FormDataStorageMigrationCliRunner(args, validator, cephStorage, redisStorage);
    var key = keyProvider.generateKey("piid", "taskid");
    var keys = Set.of(key, "invalidKey");
    var formData = FormDataDto.builder()
        .data(new LinkedHashMap<>(Map.of("name", "John")))
        .build();

    when(cephStorage.keys()).thenReturn(keys);
    when(cephStorage.getFormData(key)).thenReturn(Optional.of(formData));

    runner.run();

    verify(redisStorage, times(1)).putFormData(key, formData);
    verify(cephStorage, times(1)).delete(keys);
  }

  @Test
  void shouldMigrateAndDeleteOnlyValid() {
    var args = buildArgs(true, false);
    var runner = new FormDataStorageMigrationCliRunner(args, validator, cephStorage, redisStorage);
    var key = keyProvider.generateKey("piid", "taskid");
    var keys = Set.of(key, "invalidKey");
    var formData = FormDataDto.builder()
        .data(new LinkedHashMap<>(Map.of("name", "John")))
        .build();

    when(cephStorage.keys()).thenReturn(keys);
    when(cephStorage.getFormData(key)).thenReturn(Optional.of(formData));

    runner.run();

    verify(redisStorage, times(1)).putFormData(key, formData);
    verify(cephStorage, times(1)).delete(Set.of(key));
  }

  @Test
  void shouldMigrateWhenException() {
    var args = buildArgs(true, true);
    var runner = new FormDataStorageMigrationCliRunner(args, validator, cephStorage, redisStorage);
    var key = keyProvider.generateKey("piid", "taskid");
    var failKey = keyProvider.generateKey("piid", "failTaskid");
    var keys = Set.of(failKey, key, "invalidKey");
    var formData = FormDataDto.builder()
        .data(new LinkedHashMap<>(Map.of("name", "John")))
        .build();

    when(cephStorage.keys()).thenReturn(keys);
    when(cephStorage.getFormData(key)).thenReturn(Optional.of(formData));
    when(cephStorage.getFormData(failKey)).thenReturn(Optional.of(formData));
    doThrow(new RuntimeException("error message")).when(redisStorage).putFormData(failKey, formData);

    runner.run();

    verify(redisStorage, times(1)).putFormData(key, formData);
    verify(cephStorage, times(1)).delete(keys);
  }

  @Test
  void shouldNotMigrateWhenPresentInRedis() {
    var args = buildArgs(true, true);
    var runner = new FormDataStorageMigrationCliRunner(args, validator, cephStorage, redisStorage);
    var key = keyProvider.generateKey("piid", "taskid");
    var redisKey = keyProvider.generateKey("piid1", "taskid2");
    var keys = Set.of(redisKey, key, "invalidKey");
    var formData = FormDataDto.builder()
        .data(new LinkedHashMap<>(Map.of("name", "John")))
        .build();

    when(cephStorage.keys()).thenReturn(keys);
    when(cephStorage.getFormData(key)).thenReturn(Optional.of(formData));
    when(redisStorage.getFormData(redisKey)).thenReturn(Optional.of(formData));
    when(redisStorage.getFormData(key)).thenReturn(Optional.empty());

    runner.run();

    verify(redisStorage, times(1)).putFormData(key, formData);
    verify(redisStorage, times(0)).putFormData(redisKey, formData);
    verify(cephStorage, times(1)).delete(keys);
  }

  private ArgsDto buildArgs(boolean deleteAfterMigration, boolean deleteUnexpected) {
    return ArgsDto.builder()
        .deleteAfterMigration(deleteAfterMigration)
        .deleteInvalidData(deleteUnexpected)
        .build();
  }
}