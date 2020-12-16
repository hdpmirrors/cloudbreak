package com.sequenceiq.freeipa.converter.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.common.api.cloudstorage.old.AdlsGen2CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.GcsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.S3CloudStorageV1Parameters;
import com.sequenceiq.common.api.telemetry.request.BackupRequest;
import com.sequenceiq.common.api.telemetry.request.TelemetryRequest;
import com.sequenceiq.freeipa.api.model.Backup;
import com.sequenceiq.freeipa.configuration.BackupConfiguration;

@Component
public class BackupConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupConverter.class);

    private final boolean freeIpaBackupEnabled;

    private final BackupConfiguration backupConfiguration;

    public BackupConverter(BackupConfiguration backupConfiguration,
            @Value("${freeipa.backup.enabled:true}") boolean freeIpaBackupEnabled) {
        this.backupConfiguration = backupConfiguration;
        this.freeIpaBackupEnabled = freeIpaBackupEnabled;
    }

    public Backup convert(TelemetryRequest request) {
        Backup backup = null;
        if (freeIpaBackupEnabled && request != null && request.getBackup() != null) {
            backup = new Backup();
            backup.setMonthlyFullEnabled(backupConfiguration.isMonthlyFullEnabled());
            backup.setInitialFullEnabled(backupConfiguration.isRunInitialFullAfterInstall());
            backup.setHourlyEnabled(backupConfiguration.isHourlyEnabled());
            decorateBackupFromBackupRequest(backup, request.getBackup());
        }
        return backup;
    }

    private void decorateBackupFromBackupRequest(Backup backup, BackupRequest backupRequest) {
        if (backupRequest != null) {
            backup.setStorageLocation(backupRequest.getStorageLocation());
            if (backupRequest.getS3() != null) {
                S3CloudStorageV1Parameters s3Params = new S3CloudStorageV1Parameters();
                s3Params.setInstanceProfile(backupRequest.getS3().getInstanceProfile());
                backup.setS3(s3Params);
            } else if (backupRequest.getAdlsGen2() != null) {
                AdlsGen2CloudStorageV1Parameters adlsGen2Params = new AdlsGen2CloudStorageV1Parameters();
                AdlsGen2CloudStorageV1Parameters adlsGen2FromRequest = backupRequest.getAdlsGen2();
                adlsGen2Params.setAccountKey(adlsGen2FromRequest.getAccountKey());
                adlsGen2Params.setAccountName(adlsGen2FromRequest.getAccountName());
                adlsGen2Params.setSecure(adlsGen2FromRequest.isSecure());
                adlsGen2Params.setManagedIdentity(adlsGen2FromRequest.getManagedIdentity());
                backup.setAdlsGen2(adlsGen2Params);
            } else if (backupRequest.getGcs() != null) {
                GcsCloudStorageV1Parameters gcsParams = new GcsCloudStorageV1Parameters();
                GcsCloudStorageV1Parameters gcsFromRequest = backupRequest.getGcs();
                gcsParams.setServiceAccountEmail(gcsFromRequest.getServiceAccountEmail());
                backup.setGcs(gcsParams);
            }
        }
    }
}
