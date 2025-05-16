package com.burc.novadiveplannerupdated.data.repository;

import com.burc.novadiveplannerupdated.data.mapper.GasMapper;
import com.burc.novadiveplannerupdated.data.room.dao.GasDao;
import com.burc.novadiveplannerupdated.data.room.entity.GasEntity;
import com.burc.novadiveplannerupdated.domain.entity.Gas;
import com.burc.novadiveplannerupdated.domain.repository.GasRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Implementation of the GasRepository interface.
 * This class is responsible for interacting with the GasDao and mapping
 * between GasEntity (data layer) and Gas (domain layer) models.
 */
@Singleton // Hilt tarafından Singleton olarak yönetilecek
public class GasRepositoryImpl implements GasRepository {

    private final GasDao gasDao;
    // GasMapper statik metotlar içerdiği için doğrudan çağrılacak, enjekte edilmesine gerek yok.

    @Inject // Hilt için constructor enjeksiyonu
    public GasRepositoryImpl(GasDao gasDao) {
        this.gasDao = gasDao;
    }

    @Override
    public Flowable<List<Gas>> getGasesStream() {
        return gasDao.getAllGasesSorted()
                .map(GasMapper::toDomainList) // entity listesini domain listesine map et
                .subscribeOn(Schedulers.io()); // Veritabanı işlemleri IO thread'inde yapılmalı
    }

    @Override
    public Maybe<Gas> getGasBySlotNumber(int slotNumber) {
        return gasDao.getGasBySlotNumber(slotNumber)
                .map(GasMapper::toDomain) // entity'yi domain modeline map et
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable saveGas(Gas gas) {
        if (gas == null) {
            return Completable.error(new IllegalArgumentException("Gas to save cannot be null"));
        }
        GasEntity gasEntity = GasMapper.toEntity(gas);
        if (gasEntity == null) {
            // Bu durum GasMapper'ın null gasDomain için null döndürmesinden kaynaklanabilir,
            // ancak yukarıdaki ilk kontrol bunu engellemeli.
            return Completable.error(new IllegalStateException("Failed to map Gas to GasEntity"));
        }
        return gasDao.insertOrUpdateGas(gasEntity)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable updateGasEnabledState(int slotNumber, boolean isEnabled) {
        return gasDao.updateGasEnabledState(slotNumber, isEnabled)
                .subscribeOn(Schedulers.io());
    }
} 