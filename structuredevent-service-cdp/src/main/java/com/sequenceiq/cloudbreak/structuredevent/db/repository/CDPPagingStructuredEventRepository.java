package com.sequenceiq.cloudbreak.structuredevent.db.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.sequenceiq.cloudbreak.structuredevent.db.domain.CDPStructuredEventEntity;
import com.sequenceiq.cloudbreak.structuredevent.event.StructuredEventType;
import com.sequenceiq.cloudbreak.workspace.repository.EntityType;

@EntityType(entityClass = CDPStructuredEventEntity.class)
@Transactional(Transactional.TxType.REQUIRED)
public interface CDPPagingStructuredEventRepository extends PagingAndSortingRepository<CDPStructuredEventEntity, Long> {

    Page<CDPStructuredEventEntity> findByEventTypeAndResourceTypeAndResourceId(StructuredEventType eventType,
        String resourceType,
        Long resourceId,
        Pageable pageable);
}