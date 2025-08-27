package com.sfr.tokyo.sfr_backend.council.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpsertManifestoRequest {
    @NotBlank
    @Size(max = 200)
    private String title;
    @Size(max = 500)
    private String summary;
    private List<String> details;
    private List<String> endorsements;
}
