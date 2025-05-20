package com.burc.novadiveplannerupdated.data.repository;

import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.repository.ActiveDivePlanRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@Singleton
public class ActiveDivePlanRepositoryImpl implements ActiveDivePlanRepository {

    // BehaviorSubject, en son yayınlanan değeri saklar ve yeni abonelere hemen bu değeri iletir.
    // Başlangıçta bir değer olmayabilir, bu yüzden null ile başlatılabilir veya
    // ilk DivePlan set edildiğinde oluşturulabilir.
    // Eğer null bir planın yayınlanması istenmiyorsa,subject.hide() kullanılabilir veya
    // null kontrolleri ile sarmalanabilir.
    private final BehaviorSubject<DivePlan> activeDivePlanSubject = BehaviorSubject.create();

    @Inject
    public ActiveDivePlanRepositoryImpl() {
        // Constructor
    }

    @Override
    public Observable<DivePlan> getActiveDivePlan() {
        // activeDivePlanSubject bir Observable olduğu için doğrudan döndürülebilir.
        // Eğer subject null ise ve null item yayınlaması istenmiyorsa,
        // .ofType(DivePlan.class) gibi bir filtre eklenebilir,
        // ancak BehaviorSubject zaten null değerleri de yayınlayabilir.
        return activeDivePlanSubject.hide(); // hide() metodu Subject'in tipini gizleyerek Observable olarak sunar.
    }

    @Override
    public void setActiveDivePlan(DivePlan divePlan) {
        if (divePlan != null) {
            activeDivePlanSubject.onNext(divePlan);
        } else {
            // Eğer null bir plan set edilmeye çalışılırsa ne yapılacağına karar verilmeli.
            // Örneğin, bir hata fırlatılabilir veya konu temizlenebilir (eğer destekleniyorsa).
            // Şimdilik, null ise bir şey yapmayalım veya isteğe bağlı olarak subject'e null gönderilebilir.
            // activeDivePlanSubject.onNext(null); // Bu, null bir DivePlan yayınlar.
            // Ya da boş bırakarak null set edilmesini engelle.
            // Dikkat: Eğer subject'e null gönderilirse, alıcıların null kontrolü yapması gerekir.
        }
    }

    // Opsiyonel getCurrentActiveDivePlan() implementasyonu:
    /*
    @Override
    public DivePlan getCurrentActiveDivePlan() {
        // BehaviorSubject.getValue() en son yayınlanan değeri senkron olarak döndürür.
        // Eğer henüz bir değer yayınlanmadıysa null dönebilir.
        return activeDivePlanSubject.getValue();
    }
    */
} 