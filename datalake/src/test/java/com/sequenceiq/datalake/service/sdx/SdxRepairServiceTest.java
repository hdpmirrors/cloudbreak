package com.sequenceiq.datalake.service.sdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dyngr.core.AttemptResult;
import com.dyngr.core.AttemptState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.StackV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.ClusterRepairV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.StackV4Response;
import com.sequenceiq.cloudbreak.event.ResourceEvent;
import com.sequenceiq.datalake.entity.DatalakeStatusEnum;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.repository.SdxClusterRepository;
import com.sequenceiq.datalake.service.sdx.status.SdxStatusService;
import com.sequenceiq.sdx.api.model.SdxRepairRequest;

@ExtendWith(MockitoExtension.class)
public class SdxRepairServiceTest {

    private static final String USER_CRN = "crn:cdp:iam:us-west-1:cloudera:user:bob@cloudera.com";

    @Mock
    private StackV4Endpoint stackV4Endpoint;

    @Mock
    private SdxClusterRepository sdxClusterRepository;

    @Mock
    private SdxService sdxService;

    @Mock
    private SdxNotificationService notificationService;

    @Mock
    private SdxStatusService sdxStatusService;

    @Captor
    private ArgumentCaptor<ClusterRepairV4Request> captor;

    @InjectMocks
    private SdxRepairService underTest;

    @Test
    public void triggerCloudbreakRepair() {
        SdxCluster cluster = new SdxCluster();
        cluster.setInitiatorUserCrn(USER_CRN);
        cluster.setClusterName("dummyCluster");
        SdxRepairRequest sdxRepairRequest = new SdxRepairRequest();
        sdxRepairRequest.setHostGroupName("master");
        underTest.startRepairInCb(cluster, sdxRepairRequest);
        verify(stackV4Endpoint).repairCluster(eq(0L), eq("dummyCluster"), captor.capture());
        assertEquals("master", captor.getValue().getHostGroups().get(0));
        verify(sdxStatusService, times(1))
                .setStatusForDatalake(DatalakeStatusEnum.REPAIR_IN_PROGRESS, "Datalake repair in progress", cluster);
        verify(notificationService).send(eq(ResourceEvent.SDX_REPAIR_STARTED), any());
    }

    @Test
    public void waitCloudbreakClusterRepair() throws JsonProcessingException {
        SdxCluster cluster = new SdxCluster();
        cluster.setInitiatorUserCrn(USER_CRN);
        cluster.setClusterName("dummyCluster");
        StackV4Response resp = new StackV4Response();
        resp.setStatus(Status.UPDATE_FAILED);
        when(stackV4Endpoint.get(eq(0L), eq("dummyCluster"), any())).thenReturn(resp);
        AttemptResult<StackV4Response> attempt = underTest.checkClusterStatusDuringRepair(cluster);
        assertEquals(AttemptState.BREAK, attempt.getState());
        verify(notificationService).send(eq(ResourceEvent.SDX_REPAIR_FAILED), any());
    }
}
