package com.burc.novadiveplannerupdated.data.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.burc.novadiveplannerupdated.data.room.converter.GasTypeConverter;
import com.burc.novadiveplannerupdated.domain.model.GasType;

@Entity(tableName = "gases")
@TypeConverters(GasTypeConverter.class) // GasType enum'ı için TypeConverter ekliyoruz
public class GasEntity {

    @PrimaryKey
    @ColumnInfo(name = "slot_number")
    public int slotNumber; // 1-10 arası

    @ColumnInfo(name = "is_enabled")
    public boolean isEnabled;

    @ColumnInfo(name = "gas_name")
    public String gasName;

    @ColumnInfo(name = "fo2")
    public double fo2; // Oksijen oranı (0.07 - 1.0)

    @ColumnInfo(name = "fhe")
    public double fhe; // Helyum oranı (0.0 - 0.93)

    @ColumnInfo(name = "po2_max")
    public Double po2Max; // OC gazlar için PPO2 üst limiti, CC için null olabilir

    @ColumnInfo(name = "gas_type")
    public GasType gasType; // OPEN_CIRCUIT veya CLOSED_CIRCUIT

    @ColumnInfo(name = "tank_capacity")
    public double tankCapacity;

    @ColumnInfo(name = "reserve_pressure_percentage")
    public double reservePressurePercentage;

    // Room için gerekli boş constructor
    public GasEntity() {}

    // Tüm alanları içeren constructor
    public GasEntity(int slotNumber, boolean isEnabled, String gasName, 
                     double fo2, double fhe, Double po2Max, GasType gasType,
                     double tankCapacity, double reservePressurePercentage) {
        this.slotNumber = slotNumber;
        this.isEnabled = isEnabled;
        this.gasName = gasName;
        this.fo2 = fo2;
        this.fhe = fhe;
        this.po2Max = po2Max;
        this.gasType = gasType;
        this.tankCapacity = tankCapacity;
        this.reservePressurePercentage = reservePressurePercentage;
    }
} 