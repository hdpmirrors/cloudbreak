package com.sequenceiq.redbeams.converter.spi;

import org.springframework.stereotype.Component;

import com.sequenceiq.common.model.EndpointType;
import com.sequenceiq.common.api.type.ServiceEndpointCreation;

@Component
public class ServiceEndpointCreationToEndpointTypeConverter {

    public EndpointType convert(ServiceEndpointCreation serviceEndpointCreation) {
        if (serviceEndpointCreation == null) {
            return EndpointType.USE_SERVICE_ENDPOINT;
        }
        switch (serviceEndpointCreation) {
            case ENABLED_PRIVATE_ENDPOINT:
                return EndpointType.USE_PRIVATE_ENDPOINT;
            case ENABLED:
            case DISABLED:
            default:
                return EndpointType.USE_SERVICE_ENDPOINT;
        }
    }
}