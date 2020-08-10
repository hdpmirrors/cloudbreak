package com.sequenceiq.cloudbreak.reactor.handler.cluster.dr.backup;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.auth.altus.UmsRight;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupRequest;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupService;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.common.exception.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.core.bootstrap.service.ClusterDeletionBasedExitCriteriaModel;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.dto.LdapView;
import com.sequenceiq.cloudbreak.ldap.LdapConfigService;
import com.sequenceiq.cloudbreak.orchestrator.host.HostOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltConfig;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteriaModel;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.dr.backup.DatabaseBackupFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.dr.backup.DatabaseBackupRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.dr.backup.DatabaseBackupSuccess;
import com.sequenceiq.cloudbreak.reactor.handler.cluster.dr.BackupRestoreSaltConfigGenerator;
import com.sequenceiq.cloudbreak.service.GatewayConfigService;
import com.sequenceiq.cloudbreak.service.environment.EnvironmentConfigProvider;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.util.StackUtil;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.ExceptionCatcherEventHandler;

@Component
public class DatabaseBackupHandler extends ExceptionCatcherEventHandler<DatabaseBackupRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBackupHandler.class);

    @Inject
    private BackupRestoreSaltConfigGenerator saltConfigGenerator;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private HostOrchestrator hostOrchestrator;

    @Inject
    private StackService stackService;

    @Inject
    private StackUtil stackUtil;

    @Inject
    private VirtualGroupService virtualGroupService;

    @Inject
    private LdapConfigService ldapConfigService;

    @Inject
    private EnvironmentConfigProvider environmentConfigProvider;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(DatabaseBackupRequest.class);
    }

    @Override
    protected Selectable defaultFailureEvent(Long resourceId, Exception e) {
        return new DatabaseBackupFailedEvent(resourceId, e, DetailedStackStatus.DATABASE_BACKUP_FAILED);
    }

    @Override
    protected Selectable doAccept(HandlerEvent event) {
        DatabaseBackupRequest request = event.getData();
        Selectable result;
        Long stackId = request.getResourceId();
        LOGGER.debug("Backing up database on stack {}, backup id {}", stackId, request.getBackupId());
        try {
            Stack stack = stackService.getByIdWithListsInTransaction(stackId);
            Cluster cluster = stack.getCluster();
            InstanceMetaData gatewayInstance = stack.getPrimaryGatewayInstance();
            GatewayConfig gatewayConfig = gatewayConfigService.getGatewayConfig(stack, gatewayInstance, cluster.hasGateway());
            Set<String> gatewayFQDN = Collections.singleton(gatewayInstance.getDiscoveryFQDN());
            ExitCriteriaModel exitModel = ClusterDeletionBasedExitCriteriaModel.clusterDeletionBasedModel(stackId, cluster.getId());
            Optional<LdapView> ldapView = ldapConfigService.get(stack.getEnvironmentCrn(), stack.getName());
            String virtualGroupsEnvironmentCrn = environmentConfigProvider.getParentEnvironmentCrn(stack.getEnvironmentCrn());
            String adminGroup = ldapView.orElseThrow(() -> new CloudbreakServiceException("Ranger admin group not found."))
                    .getAdminGroup();
            LOGGER.error("Admin Group:", adminGroup);
            VirtualGroupRequest virtualGroupRequest = new VirtualGroupRequest(virtualGroupsEnvironmentCrn, adminGroup);
            String rangerAdminGroup = virtualGroupService.getVirtualGroup(virtualGroupRequest, UmsRight.RANGER_ADMIN.getRight());
            SaltConfig saltConfig = saltConfigGenerator.createSaltConfig(request.getBackupLocation(), request.getBackupId(), rangerAdminGroup, stack);
            hostOrchestrator.backupDatabase(gatewayConfig, gatewayFQDN, stackUtil.collectReachableNodes(stack), saltConfig, exitModel);

            result = new DatabaseBackupSuccess(stackId);
        } catch (Exception e) {
            LOGGER.error("Database backup event failed", e);
            result = new DatabaseBackupFailedEvent(stackId, e, DetailedStackStatus.DATABASE_BACKUP_FAILED);
        }
        return result;
    }
}
