package com.sequenceiq.cloudbreak.core.flow2.cluster.provision;

import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.AVAILABLE;
import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.CREATE_FAILED;
import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.UPDATE_IN_PROGRESS;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.common.model.OrchestratorType;
import com.sequenceiq.cloudbreak.common.service.TransactionService.TransactionRuntimeExecutionException;
import com.sequenceiq.cloudbreak.core.bootstrap.service.OrchestratorTypeResolver;
import com.sequenceiq.cloudbreak.core.flow2.stack.CloudbreakFlowMessageService;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.view.OrchestratorView;
import com.sequenceiq.cloudbreak.domain.view.StackView;
import com.sequenceiq.cloudbreak.message.Msg;
import com.sequenceiq.cloudbreak.service.CloudbreakException;
import com.sequenceiq.cloudbreak.service.StackUpdater;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.cluster.flow.ClusterTerminationService;
import com.sequenceiq.cloudbreak.service.publicendpoint.ClusterPublicEndpointManagementService;
import com.sequenceiq.cloudbreak.service.stack.flow.TerminationFailedException;
import com.sequenceiq.cloudbreak.util.StackUtil;

@Component
public class ClusterCreationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterCreationService.class);

    @Inject
    private StackUpdater stackUpdater;

    @Inject
    private CloudbreakFlowMessageService flowMessageService;

    @Inject
    private OrchestratorTypeResolver orchestratorTypeResolver;

    @Inject
    private ClusterService clusterService;

    @Inject
    private ClusterTerminationService clusterTerminationService;

    @Inject
    private StackUtil stackUtil;

    @Inject
    private ClusterPublicEndpointManagementService clusterPublicEndpointManagementService;

    public void bootstrappingMachines(Stack stack) {
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.BOOTSTRAPPING_MACHINES);
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_INFRASTRUCTURE_BOOTSTRAP, UPDATE_IN_PROGRESS.name());
    }

    public void registeringToClusterProxy(Stack stack) {
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.REGISTERING_TO_CLUSTER_PROXY);
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_INFRASTRUCTURE_CLUSTER_PROXY, UPDATE_IN_PROGRESS.name());
    }

    public void registeringGatewayToClusterProxy(Stack stack) {
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.REGISTERING_GATEWAY_TO_CLUSTER_PROXY);
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_INFRASTRUCTURE_CLUSTER_PROXY_GATEWAY_REGISTRATION, UPDATE_IN_PROGRESS.name());
    }

    public void collectingHostMetadata(Stack stack) {
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.COLLECTING_HOST_METADATA);
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_INFRASTRUCTURE_METADATA_SETUP, UPDATE_IN_PROGRESS.name());
    }

    public void bootstrapPublicEndpoints(Stack stack) {
        boolean success = clusterPublicEndpointManagementService.provision(stack);
        if (!success) {
            flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_GATEWAY_CERTIFICATE_CREATE_FAILED, UPDATE_IN_PROGRESS.name());
        }
    }

    public void startingClusterServices(StackView stack) throws CloudbreakException {
        OrchestratorView orchestrator = stack.getOrchestrator();
        OrchestratorType orchestratorType = orchestratorTypeResolver.resolveType(orchestrator.getType());
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.STARTING_AMBARI_SERVICES, "Running cluster services.");
        if (orchestratorType.containerOrchestrator()) {
            flowMessageService.fireEventAndLog(stack.getId(), Msg.CLUSTER_RUN_CONTAINERS, UPDATE_IN_PROGRESS.name());
        } else if (orchestratorType.hostOrchestrator()) {
            flowMessageService.fireEventAndLog(stack.getId(), Msg.CLUSTER_RUN_SERVICES, UPDATE_IN_PROGRESS.name());
        } else {
            String message = String.format("Please implement %s orchestrator because it is not on classpath.", orchestrator.getType());
            LOGGER.error(message);
            throw new CloudbreakException(message);
        }
    }

    public void startingAmbari(long stackId) {
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.CLUSTER_OPERATION, "Ambari cluster is now starting.");
        clusterService.updateClusterStatusByStackId(stackId, UPDATE_IN_PROGRESS);
    }

    public void installingCluster(StackView stack) {
        String clusterManagerIP = stackUtil.extractClusterManagerIp(stack);
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.CLUSTER_OPERATION,
                String.format("Building the cluster. Cluster manager ip:%s", clusterManagerIP));
        flowMessageService.fireEventAndLog(stack.getId(), Msg.CLUSTER_BUILDING, UPDATE_IN_PROGRESS.name(), clusterManagerIP);
    }

    public void clusterInstallationFinished(StackView stackView) {
        String clusterManagerIp = stackUtil.extractClusterManagerIp(stackView);
        clusterService.updateClusterStatusByStackId(stackView.getId(), AVAILABLE);
        stackUpdater.updateStackStatus(stackView.getId(), DetailedStackStatus.AVAILABLE, "Cluster creation finished.");
        flowMessageService.fireEventAndLog(stackView.getId(), Msg.CLUSTER_BUILT, AVAILABLE.name(), clusterManagerIp);
    }

    public void handleClusterCreationFailure(StackView stackView, Exception exception) {
        if (stackView.getClusterView() != null) {
            Cluster cluster = clusterService.getById(stackView.getClusterView().getId());
            String errorMessage = getErrorMessageFromException(exception);
            clusterService.updateClusterStatusByStackId(stackView.getId(), CREATE_FAILED, errorMessage);
            stackUpdater.updateStackStatus(stackView.getId(), DetailedStackStatus.AVAILABLE);
            flowMessageService.fireEventAndLog(stackView.getId(), Msg.CLUSTER_CREATE_FAILED, CREATE_FAILED.name(), errorMessage);
            try {
                OrchestratorType orchestratorType = orchestratorTypeResolver.resolveType(stackView.getOrchestrator().getType());
                if (cluster != null && orchestratorType.containerOrchestrator()) {
                    clusterTerminationService.deleteClusterContainers(cluster);
                }
            } catch (CloudbreakException | TerminationFailedException ex) {
                LOGGER.info("Cluster containers could not be deleted, preparation for reinstall failed: ", ex);
            }
        } else {
            LOGGER.info("Cluster was null. Flow action was not required.");
        }
    }

    private String getErrorMessageFromException(Exception exception) {
        if (exception instanceof TransactionRuntimeExecutionException && exception.getCause() != null && exception.getCause().getCause() != null) {
            return exception.getCause().getCause().getMessage();
        } else {
            return exception instanceof CloudbreakException && exception.getCause() != null
                    ? exception.getCause().getMessage() : exception.getMessage();
        }
    }

    public void mountDisks(Stack stack) {
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.MOUNTING_DISKS);
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_INFRASTRUCTURE_DISK_MOUNT, UPDATE_IN_PROGRESS.name());
    }
}
