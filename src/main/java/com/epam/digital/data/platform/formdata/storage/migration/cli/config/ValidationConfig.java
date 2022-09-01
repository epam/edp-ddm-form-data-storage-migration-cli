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

package com.epam.digital.data.platform.formdata.storage.migration.cli.config;

import com.epam.digital.data.platform.formdata.storage.migration.cli.dto.ArgsDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

  public static final List<Pattern> PATTERNS = List.of(
      Pattern.compile(
          String.format(FormDataKeyProviderImpl.TASK_FORM_DATA_KEY_FORMAT, "(.*)", "(.*)")),
      Pattern.compile(
          String.format(FormDataKeyProviderImpl.START_FORM_DATA_KEY_FORMAT, "(.*)", "(.*)")),
      Pattern.compile(
          String.format(FormDataKeyProviderImpl.START_FORM_DATA_VALUE_FORMAT, "(.*)", "(.*)")),
      Pattern.compile(
          String.format(FormDataKeyProviderImpl.SYSTEM_SIGNATURE_STORAGE_KEY, "(.*)", "(.*)")),
      Pattern.compile(
          String.format(FormDataKeyProviderImpl.BATCH_SYSTEM_SIGNATURE_STORAGE_KEY, "(.*)", "(.*)"))
  );

  private final ArgsDto args;

  @Bean
  public List<Pattern> patterns() {
    var patterns = new ArrayList<>(PATTERNS);
    patterns.addAll(args.getAdditionalKeyPatterns().stream().map(Pattern::compile)
        .collect(Collectors.toList()));
    return patterns;
  }
}
