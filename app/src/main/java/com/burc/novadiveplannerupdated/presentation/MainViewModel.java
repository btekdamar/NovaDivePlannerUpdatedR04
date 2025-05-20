package com.burc.novadiveplannerupdated.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.burc.novadiveplannerupdated.domain.entity.DivePlan;
import com.burc.novadiveplannerupdated.domain.repository.ActiveDivePlanRepository;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.CreateNewDivePlanUseCase;
import com.burc.novadiveplannerupdated.domain.usecase.diveplan.GetActiveDivePlanUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final CreateNewDivePlanUseCase createNewDivePlanUseCase;
    private final GetActiveDivePlanUseCase getActiveDivePlanUseCase;
    private final ActiveDivePlanRepository activeDivePlanRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<DivePlan> _activeDivePlan = new MutableLiveData<>();
    public LiveData<DivePlan> activeDivePlan = _activeDivePlan;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Throwable> _error = new MutableLiveData<>();
    public LiveData<Throwable> error = _error;

    @Inject
    public MainViewModel(
            CreateNewDivePlanUseCase createNewDivePlanUseCase,
            GetActiveDivePlanUseCase getActiveDivePlanUseCase,
            ActiveDivePlanRepository activeDivePlanRepository
    ) {
        this.createNewDivePlanUseCase = createNewDivePlanUseCase;
        this.getActiveDivePlanUseCase = getActiveDivePlanUseCase;
        this.activeDivePlanRepository = activeDivePlanRepository;
        
        subscribeToActiveDivePlan();
        loadInitialDivePlan();
    }

    private void subscribeToActiveDivePlan() {
        compositeDisposable.add(
            getActiveDivePlanUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    _activeDivePlan::setValue,
                    throwable -> _error.setValue(throwable)
                )
        );
    }

    private void loadInitialDivePlan() {
        _isLoading.setValue(true);
        compositeDisposable.add(
            createNewDivePlanUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    divePlan -> {
                        activeDivePlanRepository.setActiveDivePlan(divePlan);
                        _isLoading.setValue(false);
                    },
                    throwable -> {
                        _error.setValue(throwable);
                        _isLoading.setValue(false);
                    }
                )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
} 