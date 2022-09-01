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

import com.epam.digital.data.platform.formdata.storage.migration.cli.dto.ArgsDto;
import com.epam.digital.data.platform.formdata.storage.migration.cli.validator.FormDataKeyValidator;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormDataStorageMigrationCliRunner implements CommandLineRunner {

  private static final String ENABLED = "Enabled";
  private static final String DISABLED = "Disabled";

  private final ArgsDto args;
  private final FormDataKeyValidator validator;
  private final FormDataStorageService cephFormDataStorageService;
  private final FormDataStorageService redisFormDataStorageService;

  @Override
  public void run(String... args) {
    log.info("Start forms data migration");
    log.info("{} deleting processed valid form data after migration from source",
        this.args.isDeleteAfterMigration() ? ENABLED : DISABLED);
    log.info("{} deleting invalid data from source",
        this.args.isDeleteInvalidData() ? ENABLED : DISABLED);
    var keys = cephFormDataStorageService.keys();
    var processed = migrate(keys);
    delete(keys, processed);
    log.info("Forms data migration finished");
  }

  private Set<String> migrate(Set<String> keys) {
    return keys.stream()
        .filter(validator::isValid)
        .peek(key -> {
          log.info("Migration for '{}' key started", key);
          var formData = cephFormDataStorageService.getFormData(key);
          formData.ifPresentOrElse(
              data -> redisFormDataStorageService.putFormData(key, data),
              () -> log.warn("{} not found in storage", key));
          log.info("Migration for '{}' key finished", key);
        }).collect(Collectors.toSet());
  }

  private void delete(Set<String> allKeys, Set<String> processedKeys) {
    var invalidKeys = allKeys.stream().filter(k -> !processedKeys.contains(k))
        .collect(Collectors.toSet());
    log.info("Found {} invalid keys: {}", invalidKeys.size(), invalidKeys);
    if (args.isDeleteAfterMigration() && args.isDeleteInvalidData()) {
      cephFormDataStorageService.delete(allKeys);
      log.info("Migrated and found invalid data were deleted from the source storage");
    } else if (args.isDeleteAfterMigration()) {
      cephFormDataStorageService.delete(processedKeys);
      log.info("Migrated forms were deleted from the source storage");
    }
  }
}
