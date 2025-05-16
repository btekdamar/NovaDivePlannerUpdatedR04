package com.burc.novadiveplannerupdated.data.room.converter;

import androidx.room.TypeConverter;
import com.burc.novadiveplannerupdated.domain.model.GasType;

public class GasTypeConverter {

    @TypeConverter
    public static String fromGasType(GasType gasType) {
        return gasType == null ? null : gasType.name();
    }

    @TypeConverter
    public static GasType toGasType(String gasTypeName) {
        if (gasTypeName == null) {
            return null;
        }
        try {
            return GasType.valueOf(gasTypeName);
        } catch (IllegalArgumentException e) {
            // Log error or handle unknown value, e.g., return a default or null
            // For now, returning null if the string doesn't match any enum constant
            return null; 
        }
    }
} 