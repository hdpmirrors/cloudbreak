package com.sequenceiq.environment.environment.v1.converter;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.telemetry.TelemetryConfiguration;
import com.sequenceiq.common.api.cloudstorage.old.AdlsGen2CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.GcsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.S3CloudStorageV1Parameters;
import com.sequenceiq.common.api.telemetry.model.CloudwatchParams;
import com.sequenceiq.common.api.telemetry.model.Features;
import com.sequenceiq.common.api.telemetry.request.BackupRequest;
import com.sequenceiq.common.api.telemetry.request.FeaturesRequest;
import com.sequenceiq.common.api.telemetry.request.LoggingRequest;
import com.sequenceiq.common.api.telemetry.request.TelemetryRequest;
import com.sequenceiq.common.api.telemetry.request.WorkloadAnalyticsRequest;
import com.sequenceiq.common.api.telemetry.response.FeaturesResponse;
import com.sequenceiq.common.api.telemetry.response.LoggingResponse;
import com.sequenceiq.common.api.telemetry.response.TelemetryResponse;
import com.sequenceiq.common.api.telemetry.response.WorkloadAnalyticsResponse;
import com.sequenceiq.environment.environment.dto.telemetry.EnvironmentBackup;
import com.sequenceiq.environment.environment.dto.telemetry.EnvironmentFeatures;
import com.sequenceiq.environment.environment.dto.telemetry.EnvironmentLogging;
import com.sequenceiq.environment.environment.dto.telemetry.EnvironmentTelemetry;
import com.sequenceiq.environment.environment.dto.telemetry.EnvironmentWorkloadAnalytics;
import com.sequenceiq.environment.environment.dto.telemetry.S3CloudStorageParameters;

@Component
public class TelemetryApiConverter {

    private final boolean clusterLogsCollection;

    private final boolean monitoringEnabled;

    private final boolean useSharedAltusCredential;

    public TelemetryApiConverter(TelemetryConfiguration configuration) {
        this.clusterLogsCollection = configuration.getClusterLogsCollectionConfiguration().isEnabled();
        this.monitoringEnabled = configuration.getMonitoringConfiguration().isEnabled();
        this.useSharedAltusCredential = configuration.getAltusDatabusConfiguration().isUseSharedAltusCredential();
    }

    public EnvironmentTelemetry convert(TelemetryRequest request, Features accountFeatures) {
        EnvironmentTelemetry telemetry = null;
        if (request != null) {
            telemetry = new EnvironmentTelemetry();
            telemetry.setLogging(createLoggingFromRequest(request.getLogging()));
            telemetry.setBackup(createBackupFromRequest(request.getBackup()));
            telemetry.setWorkloadAnalytics(createWorkloadAnalyticsFromRequest(request.getWorkloadAnalytics()));
            telemetry.setFeatures(createEnvironmentFeaturesFromRequest(request.getFeatures(), accountFeatures));
            telemetry.setFluentAttributes(new HashMap<>(request.getFluentAttributes()));
        }
        return telemetry;
    }

    public TelemetryResponse convert(EnvironmentTelemetry telemetry) {
        TelemetryResponse response = null;
        if (telemetry != null) {
            response = new TelemetryResponse();
            response.setLogging(createLoggingResponseFromSource(telemetry.getLogging()));
            response.setWorkloadAnalytics(createWorkloadAnalyticsResponseFromSource(telemetry.getWorkloadAnalytics()));
            response.setFluentAttributes(telemetry.getFluentAttributes());
            response.setFeatures(createFeaturesResponseFromSource(telemetry.getFeatures()));
            response.setFluentAttributes(telemetry.getFluentAttributes());
        }
        return response;
    }

    public TelemetryRequest convertToRequest(EnvironmentTelemetry telemetry) {
        TelemetryRequest telemetryRequest = null;
        if (telemetry != null) {
            telemetryRequest = new TelemetryRequest();
            telemetryRequest.setFluentAttributes(telemetry.getFluentAttributes());
            telemetryRequest.setLogging(createLoggingRequestFromEnvSource(telemetry.getLogging()));
            telemetryRequest.setBackup(createBackupRequestFromEnvSource(telemetry.getBackup()));
            telemetryRequest.setFeatures(createFeaturesRequestEnvSource(telemetry.getFeatures()));
        }
        return telemetryRequest;
    }

    private LoggingRequest createLoggingRequestFromEnvSource(EnvironmentLogging logging) {
        LoggingRequest loggingRequest = null;
        if (logging != null) {
            loggingRequest = new LoggingRequest();
            loggingRequest.setStorageLocation(logging.getStorageLocation());
            loggingRequest.setS3(convertS3(logging.getS3()));
            loggingRequest.setAdlsGen2(convertAdlsV2(logging.getAdlsGen2()));
            loggingRequest.setGcs(convertGcs(logging.getGcs()));
            loggingRequest.setCloudwatch(CloudwatchParams.copy(logging.getCloudwatch()));
        }
        return loggingRequest;
    }

    private BackupRequest createBackupRequestFromEnvSource(EnvironmentBackup backup) {
        BackupRequest backupRequest = null;
        if (backup != null) {
            backupRequest = new BackupRequest();
            backupRequest.setStorageLocation(backup.getStorageLocation());
            backupRequest.setS3(convertS3(backup.getS3()));
            backupRequest.setAdlsGen2(convertAdlsV2(backup.getAdlsGen2()));
            backupRequest.setGcs(convertGcs(backup.getGcs()));
            backupRequest.setCloudwatch(CloudwatchParams.copy(backup.getCloudwatch()));
        }
        return backupRequest;
    }

    private FeaturesRequest createFeaturesRequestEnvSource(EnvironmentFeatures features) {
        FeaturesRequest featuresRequest = null;
        if (features != null) {
            featuresRequest = new FeaturesRequest();
            featuresRequest.setClusterLogsCollection(features.getClusterLogsCollection());
            featuresRequest.setMonitoring(features.getMonitoring());
        }
        return featuresRequest;
    }

    private FeaturesResponse createFeaturesResponseFromSource(EnvironmentFeatures features) {
        FeaturesResponse featuresResponse = null;
        if (features != null) {
            featuresResponse = new FeaturesResponse();
            featuresResponse.setClusterLogsCollection(features.getClusterLogsCollection());
            featuresResponse.setMonitoring(features.getMonitoring());
            featuresResponse.setWorkloadAnalytics(features.getWorkloadAnalytics());
            featuresResponse.setUseSharedAltusCredential(features.getUseSharedAltusCredential());
        }
        return featuresResponse;
    }

    private EnvironmentLogging createLoggingFromRequest(LoggingRequest loggingRequest) {
        EnvironmentLogging logging = null;
        if (loggingRequest != null) {
            logging = new EnvironmentLogging();
            logging.setStorageLocation(loggingRequest.getStorageLocation());
            logging.setS3(convertS3(loggingRequest.getS3()));
            logging.setAdlsGen2(convertAdlsV2(loggingRequest.getAdlsGen2()));
            logging.setGcs(convertGcs(loggingRequest.getGcs()));
            logging.setCloudwatch(CloudwatchParams.copy(loggingRequest.getCloudwatch()));
        }
        return logging;
    }

    private EnvironmentBackup createBackupFromRequest(BackupRequest backupRequest) {
        EnvironmentBackup backup = null;
        if (backupRequest != null) {
            backup = new EnvironmentBackup();
            backup.setStorageLocation(backupRequest.getStorageLocation());
            backup.setS3(convertS3(backupRequest.getS3()));
            backup.setAdlsGen2(convertAdlsV2(backupRequest.getAdlsGen2()));
            backup.setGcs(convertGcs(backupRequest.getGcs()));
            backup.setCloudwatch(CloudwatchParams.copy(backupRequest.getCloudwatch()));
        }
        return backup;
    }

    private EnvironmentFeatures createEnvironmentFeaturesFromRequest(FeaturesRequest featuresRequest, Features accountFeatures) {
        EnvironmentFeatures features = null;
        if (featuresRequest != null) {
            features = new EnvironmentFeatures();
            if (useSharedAltusCredential) {
                features.addUseSharedAltusredential(true);
            }
            setClusterLogsCollectionFromAccountAndRequest(featuresRequest, accountFeatures, features);
            setMonitoringFromAccountAndRequest(featuresRequest, accountFeatures, features);
            if (accountFeatures.getWorkloadAnalytics() != null) {
                features.setWorkloadAnalytics(accountFeatures.getWorkloadAnalytics());
            }
            if (featuresRequest.getWorkloadAnalytics() != null) {
                features.setWorkloadAnalytics(featuresRequest.getWorkloadAnalytics());
            }
        }
        return features;
    }

    private void setClusterLogsCollectionFromAccountAndRequest(FeaturesRequest featuresRequest, Features accountFeatures, EnvironmentFeatures features) {
        if (clusterLogsCollection) {
            if (accountFeatures.getClusterLogsCollection() != null) {
                features.setClusterLogsCollection(accountFeatures.getClusterLogsCollection());
            }
            if (featuresRequest.getClusterLogsCollection() != null) {
                features.setClusterLogsCollection(featuresRequest.getClusterLogsCollection());
            } else {
                features.addClusterLogsCollection(false);
            }
        }
    }

    private void setMonitoringFromAccountAndRequest(FeaturesRequest featuresRequest, Features accountFeatures, EnvironmentFeatures features) {
        if (monitoringEnabled) {
            if (accountFeatures.getMonitoring() != null) {
                features.setMonitoring(featuresRequest.getMonitoring());
            }
            if (featuresRequest.getMonitoring() != null) {
                features.setMonitoring(featuresRequest.getMonitoring());
            } else {
                features.addMonitoring(false);
            }
        }
    }

    private EnvironmentWorkloadAnalytics createWorkloadAnalyticsFromRequest(WorkloadAnalyticsRequest request) {
        EnvironmentWorkloadAnalytics wa = null;
        if (request != null) {
            wa = new EnvironmentWorkloadAnalytics();
            wa.setAttributes(request.getAttributes());
        }
        return wa;
    }

    private S3CloudStorageParameters convertS3(S3CloudStorageV1Parameters s3) {
        S3CloudStorageParameters s3CloudStorageParameters = null;
        if (s3 != null) {
            s3CloudStorageParameters = new S3CloudStorageParameters();
            s3CloudStorageParameters.setInstanceProfile(s3.getInstanceProfile());
            return s3CloudStorageParameters;
        }
        return s3CloudStorageParameters;
    }

    private WorkloadAnalyticsResponse createWorkloadAnalyticsResponseFromSource(EnvironmentWorkloadAnalytics workloadAnalytics) {
        WorkloadAnalyticsResponse waResponse = null;
        if (workloadAnalytics != null) {
            waResponse = new WorkloadAnalyticsResponse();
            waResponse.setAttributes(workloadAnalytics.getAttributes());
        }
        return waResponse;
    }

    private LoggingResponse createLoggingResponseFromSource(EnvironmentLogging logging) {
        LoggingResponse loggingResponse = null;
        if (logging != null) {
            loggingResponse = new LoggingResponse();
            loggingResponse.setStorageLocation(logging.getStorageLocation());
            loggingResponse.setS3(convertS3(logging.getS3()));
            loggingResponse.setAdlsGen2(convertAdlsV2(logging.getAdlsGen2()));
            loggingResponse.setGcs(convertGcs(logging.getGcs()));
            loggingResponse.setCloudwatch(CloudwatchParams.copy(logging.getCloudwatch()));
        }
        return loggingResponse;
    }

    private S3CloudStorageV1Parameters convertS3(S3CloudStorageParameters s3) {
        S3CloudStorageV1Parameters s3CloudStorageV1Parameters = null;
        if (s3 != null) {
            s3CloudStorageV1Parameters = new S3CloudStorageV1Parameters();
            s3CloudStorageV1Parameters.setInstanceProfile(s3.getInstanceProfile());
            return s3CloudStorageV1Parameters;
        }
        return s3CloudStorageV1Parameters;
    }

    private AdlsGen2CloudStorageV1Parameters convertAdlsV2(AdlsGen2CloudStorageV1Parameters adlsV2) {
        AdlsGen2CloudStorageV1Parameters adlsGen2CloudStorageV1Parameters = null;
        if (adlsV2 != null) {
            adlsGen2CloudStorageV1Parameters = new AdlsGen2CloudStorageV1Parameters();
            adlsGen2CloudStorageV1Parameters.setAccountKey(adlsV2.getAccountKey());
            adlsGen2CloudStorageV1Parameters.setAccountName(adlsV2.getAccountName());
            adlsGen2CloudStorageV1Parameters.setManagedIdentity(adlsV2.getManagedIdentity());
            adlsGen2CloudStorageV1Parameters.setSecure(adlsV2.isSecure());
        }
        return adlsGen2CloudStorageV1Parameters;
    }

    private GcsCloudStorageV1Parameters convertGcs(GcsCloudStorageV1Parameters gcs) {
        GcsCloudStorageV1Parameters gcsCloudStorageV1Parameters = null;
        if (gcs != null) {
            gcsCloudStorageV1Parameters = new GcsCloudStorageV1Parameters();
            gcsCloudStorageV1Parameters.setServiceAccountEmail(gcs.getServiceAccountEmail());
        }
        return gcsCloudStorageV1Parameters;
    }
}