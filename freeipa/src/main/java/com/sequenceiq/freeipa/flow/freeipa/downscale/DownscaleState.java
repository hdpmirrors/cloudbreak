package com.sequenceiq.freeipa.flow.freeipa.downscale;

import com.sequenceiq.flow.core.FlowState;

public enum DownscaleState implements FlowState {
    INIT_STATE,
    STARTING_DOWNSCALE_STATE,
    DOWNSCALE_CLUSTERPROXY_REGISTRATION_STATE,
    DOWNSCALE_STOP_TELEMETRY_STATE,
    DOWNSCALE_COLLECT_RESOURCES_STATE,
    DOWNSCALE_REMOVE_INSTANCES_STATE,
    DOWNSCALE_REMOVE_SERVERS_STATE,
    DOWNSCALE_REVOKE_CERTS_STATE,
    DOWNSCALE_REMOVE_DNS_ENTRIES_STATE,
    DOWNSCALE_UPDATE_METADATA_STATE,
    DOWNSCALE_REMOVE_HOSTS_FROM_ORCHESTRATION_STATE,
    DOWNSCALE_FINISHED_STATE,
    DOWNSCALE_FAIL_STATE,
    FINAL_STATE;
}