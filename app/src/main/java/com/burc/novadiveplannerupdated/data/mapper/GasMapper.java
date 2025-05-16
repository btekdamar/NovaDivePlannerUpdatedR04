package com.burc.novadiveplannerupdated.data.mapper;

import com.burc.novadiveplannerupdated.data.room.entity.GasEntity;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.model.GasType;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class for converting between Gas domain model and GasEntity data model.
 */
public class GasMapper {

    // Private constructor to prevent instantiation
    private GasMapper() {}

    /**
     * Converts a Gas domain model to a GasEntity data model.
     *
     * @param gasDomain The Gas domain model.
     * @return The corresponding GasEntity data model.
     */
    public static GasEntity toEntity(Gas gasDomain) {
        if (gasDomain == null) {
            return null;
        }
        return new GasEntity(
                gasDomain.getSlotNumber(),
                gasDomain.isEnabled(),
                gasDomain.getGasName(),
                gasDomain.getFo2(),
                gasDomain.getFhe(),
                gasDomain.getPo2Max(),
                gasDomain.getGasType(),
                gasDomain.getTankCapacity(),
                gasDomain.getReservePressurePercentage()
        );
    }

    /**
     * Converts a GasEntity data model to a Gas domain model.
     *
     * @param gasEntity The GasEntity data model.
     * @return The corresponding Gas domain model.
     */
    public static Gas toDomain(GasEntity gasEntity) {
        if (gasEntity == null) {
            return null;
        }
        Gas.Builder builder = new Gas.Builder()
                .slotNumber(gasEntity.slotNumber)
                .isEnabled(gasEntity.isEnabled)
                .gasName(gasEntity.gasName)
                .fo2(gasEntity.fo2)
                .fhe(gasEntity.fhe)
                .gasType(gasEntity.gasType != null ? gasEntity.gasType : GasType.OPEN_CIRCUIT) // Default if null
                .tankCapacity(gasEntity.tankCapacity)
                .reservePressurePercentage(gasEntity.reservePressurePercentage);

        // po2Max can be null for GasEntity (e.g. for CC gases)
        if (gasEntity.po2Max != null) {
            builder.po2Max(gasEntity.po2Max);
        }
        
        return builder.build();
    }

    /**
     * Converts a list of GasEntity data models to a list of Gas domain models.
     *
     * @param entityList The list of GasEntity data models.
     * @return The corresponding list of Gas domain models.
     */
    public static List<Gas> toDomainList(List<GasEntity> entityList) {
        if (entityList == null) {
            return new ArrayList<>(); // Return empty list instead of null for convenience
        }
        List<Gas> domainList = new ArrayList<>(entityList.size());
        for (GasEntity entity : entityList) {
            domainList.add(toDomain(entity));
        }
        return domainList;
    }

    /**
     * Converts a list of Gas domain models to a list of GasEntity data models.
     * (Currently not used but provided for completeness)
     *
     * @param domainList The list of Gas domain models.
     * @return The corresponding list of GasEntity data models.
     */
    public static List<GasEntity> toEntityList(List<Gas> domainList) {
        if (domainList == null) {
            return new ArrayList<>(); // Return empty list instead of null
        }
        List<GasEntity> entityList = new ArrayList<>(domainList.size());
        for (Gas domain : domainList) {
            entityList.add(toEntity(domain));
        }
        return entityList;
    }
} 