package com.sequenceiq.cloudbreak.api.endpoint.v4.diagnostics.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;
import com.sequenceiq.common.api.diagnostics.BaseDiagnosticsCollectionRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("DiagnosticsCollectionRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticsCollectionRequest extends BaseDiagnosticsCollectionRequest {

    @NotNull
    @ApiModelProperty(ModelDescriptions.StackModelDescription.CRN)
    private String stackCrn;

    public String getStackCrn() {
        return stackCrn;
    }

    public void setStackCrn(String stackCrn) {
        this.stackCrn = stackCrn;
    }
}
