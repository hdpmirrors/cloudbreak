package com.sequenceiq.cloudbreak.cloud.yarn;

import static com.sequenceiq.cloudbreak.cloud.model.Orchestrator.orchestrator;
import static com.sequenceiq.cloudbreak.cloud.model.VmType.vmType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sequenceiq.cloudbreak.api.model.SpecialParameters;
import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import com.sequenceiq.cloudbreak.cloud.PlatformParametersConsts;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZones;
import com.sequenceiq.cloudbreak.cloud.model.DiskType;
import com.sequenceiq.cloudbreak.cloud.model.DiskTypes;
import com.sequenceiq.cloudbreak.cloud.model.PlatformOrchestrator;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.Regions;
import com.sequenceiq.cloudbreak.cloud.model.ScriptParams;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.cloud.model.TagSpecification;
import com.sequenceiq.cloudbreak.cloud.model.VmRecommendations;
import com.sequenceiq.cloudbreak.cloud.model.VmType;
import com.sequenceiq.cloudbreak.cloud.model.VmTypes;
import com.sequenceiq.cloudbreak.cloud.service.CloudbreakResourceReaderService;
import com.sequenceiq.cloudbreak.common.type.OrchestratorConstants;

@Service
public class YarnPlatformParameters implements PlatformParameters {
    // There is no need to initialize the disk on ycloud
    private static final ScriptParams SCRIPT_PARAMS = new ScriptParams("nonexistent_device", 97);

    @Inject
    private CloudbreakResourceReaderService cloudbreakResourceReaderService;

    @Override
    public ScriptParams scriptParams() {
        return SCRIPT_PARAMS;
    }

    @Override
    public DiskTypes diskTypes() {
        return new DiskTypes(Collections.emptyList(), DiskType.diskType(""), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public Regions regions() {
        return new Regions(Lists.newArrayList(), Region.region(""), Maps.newHashMap());
    }

    @Override
    public VmTypes vmTypes(Boolean extended) {
        return new VmTypes(virtualMachines(extended), defaultVirtualMachine());
    }

    @Override
    public Map<AvailabilityZone, VmTypes> vmTypesPerAvailabilityZones(Boolean extended) {
        return Maps.newHashMap();
    }

    @Override
    public AvailabilityZones availabilityZones() {
        return new AvailabilityZones(Maps.newHashMap());
    }

    @Override
    public String resourceDefinition(String resource) {
        return cloudbreakResourceReaderService.resourceDefinition("yarn", resource);
    }

    @Override
    public List<StackParamValidation> additionalStackParameters() {
        List<StackParamValidation> additionalStackParameterValidations = Lists.newArrayList();
        additionalStackParameterValidations.add(new StackParamValidation(YarnConstants.YARN_LIFETIME_PARAMETER, false, Integer.class, Optional.empty()));
        additionalStackParameterValidations.add(new StackParamValidation(YarnConstants.YARN_QUEUE_PARAMETER, false, String.class, Optional.empty()));
        return additionalStackParameterValidations;
    }

    @Override
    public PlatformOrchestrator orchestratorParams() {
        return new PlatformOrchestrator(
                Collections.singletonList(orchestrator(OrchestratorConstants.SALT)),
                orchestrator(OrchestratorConstants.SALT));
    }

    @Override
    public TagSpecification tagSpecification() {
        return null;
    }

    @Override
    public VmRecommendations recommendedVms() {
        return null;
    }

    @Override
    public String getDefaultRegionsConfigString() {
        return "";
    }

    @Override
    public String getDefaultRegionString() {
        return "";
    }

    @Override
    public String platforName() {
        return YarnConstants.YARN_PLATFORM.value();
    }

    @Override
    public SpecialParameters specialParameters() {
        Map<String, Boolean> specialParameters = Maps.newHashMap();
        specialParameters.put(PlatformParametersConsts.CUSTOM_INSTANCETYPE, Boolean.TRUE);
        specialParameters.put(PlatformParametersConsts.NETWORK_IS_MANDATORY, Boolean.FALSE);
        specialParameters.put(PlatformParametersConsts.SCALING_SUPPORTED, Boolean.FALSE);
        specialParameters.put(PlatformParametersConsts.STARTSTOP_SUPPORTED, Boolean.FALSE);
        return new SpecialParameters(specialParameters);
    }

    private Collection<VmType> virtualMachines(Boolean extended) {
        return new ArrayList<>();
    }

    private VmType defaultVirtualMachine() {
        return vmType("");
    }
}
