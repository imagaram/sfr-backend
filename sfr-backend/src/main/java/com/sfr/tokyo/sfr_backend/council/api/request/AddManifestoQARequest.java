package com.sfr.tokyo.sfr_backend.council.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddManifestoQARequest {
    @NotBlank
    private String question;
    @NotBlank
    private String answer;
}
