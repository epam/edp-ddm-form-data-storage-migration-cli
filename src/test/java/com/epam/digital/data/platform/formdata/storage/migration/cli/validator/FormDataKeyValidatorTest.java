package com.epam.digital.data.platform.formdata.storage.migration.cli.validator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.epam.digital.data.platform.formdata.storage.migration.cli.config.ValidationConfig;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormDataKeyValidatorTest {

  private final FormDataKeyProvider keyProvider = new FormDataKeyProviderImpl();
  private final FormDataKeyValidator validator = new FormDataKeyValidator(
      ValidationConfig.PATTERNS);

  @Test
  void successfulValidation() {
    var keys = List.of(
        keyProvider.generateKey("taskDefKey", UUID.randomUUID().toString()),
        keyProvider.generateStartFormKey("processDefKey", UUID.randomUUID().toString()),
        keyProvider.generateKeyForExternalSystem("processDefKey", UUID.randomUUID().toString()),
        keyProvider.generateSystemSignatureKey(UUID.randomUUID().toString(),
            UUID.randomUUID().toString()),
        keyProvider.generateBatchSystemSignatureKey(UUID.randomUUID().toString(), 0)
    );

    assertThat(keys.stream().allMatch(validator::isValid)).isTrue();
  }

  @Test
  void failedValidation() {
    assertThat(validator.isValid("invalid")).isFalse();
  }
}