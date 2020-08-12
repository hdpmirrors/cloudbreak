package com.sequenceiq.cloudbreak.structuredevent.db.domain;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.json.JsonToString;
import com.sequenceiq.cloudbreak.structuredevent.db.converter.CDPStructuredEventTypeConverter;
import com.sequenceiq.cloudbreak.structuredevent.event.StructuredEventType;
import com.sequenceiq.cloudbreak.structuredevent.repository.AccountAwareResource;

@Entity
@Table(name = "cdpstructuredevent")
public class CDPStructuredEventEntity implements AccountAwareResource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cdpstructuredevent_generator")
    @SequenceGenerator(name = "cdpstructuredevent_generator", sequenceName = "cdpstructuredevent_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = CDPStructuredEventTypeConverter.class)
    private StructuredEventType eventType;

    @Column(nullable = false)
    private String resourceType;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false)
    private String resourceCrn;

    @Column(nullable = false)
    private Long timestamp;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json structuredEventJson;

    @Column(nullable = false)
    private String accountId;

    public Long getId() {
        return id;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StructuredEventType getEventType() {
        return eventType;
    }

    public void setEventType(StructuredEventType eventType) {
        this.eventType = eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Json getStructuredEventJson() {
        return structuredEventJson;
    }

    public void setStructuredEventJson(Json structuredEventJson) {
        this.structuredEventJson = structuredEventJson;
    }

    @Override
    public String getName() {
        return resourceType;
    }

    @Override
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getResourceCrn() {
        return resourceCrn;
    }

    public void setResourceCrn(String resourceCrn) {
        this.resourceCrn = resourceCrn;
    }
}
