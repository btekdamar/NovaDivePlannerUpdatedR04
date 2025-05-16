package com.burc.novadiveplannerupdated.presentation.ui.gases;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.databinding.FragmentGasesBinding;
import com.burc.novadiveplannerupdated.presentation.ui.gases.edit.EditGasViewModel;
import com.burc.novadiveplannerupdated.presentation.ui.gases.state.GasScreenUiState;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class GasesFragment extends Fragment {

    private static final String TAG = "GasesFragment";

    private FragmentGasesBinding binding;
    private GasViewModel viewModel;
    private GasesAdapter gasesAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GasViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGasesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeUiState();
    }

    private void setupRecyclerView() {
        gasesAdapter = new GasesAdapter(new GasesAdapter.GasDiffCallback());
        binding.recyclerViewGases.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewGases.setAdapter(gasesAdapter);

        gasesAdapter.setOnGasEnabledChangedListener((slotNumber, isEnabled) -> {
            Log.d(TAG, "Gas enabled changed: slot=" + slotNumber + ", enabled=" + isEnabled);
            viewModel.onGasEnabledChanged(slotNumber, isEnabled);
        });

        gasesAdapter.setOnEditGasClickListener(slotNumber -> {
            Log.d(TAG, "Edit gas clicked for slot: " + slotNumber);
            Bundle bundle = new Bundle();
            bundle.putInt(EditGasViewModel.ARG_SLOT_NUMBER, slotNumber);
            NavHostFragment.findNavController(GasesFragment.this)
                    .navigate(R.id.action_navigation_gases_to_editGasDialogFragment, bundle);
        });
    }

    private void observeUiState() {
        compositeDisposable.add(
            viewModel.uiState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleUiState,
                    throwable -> {
                        Log.e(TAG, "Error observing UI state", throwable);
                        binding.progressBarGases.setVisibility(View.GONE);
                        binding.textViewGasesError.setVisibility(View.VISIBLE);
                        binding.textViewGasesError.setText("Error: " + throwable.getMessage());
                    }
                )
        );
    }

    private void handleUiState(GasScreenUiState state) {
        Log.d(TAG, "Handling UI State: isLoading=" + state.isLoading() + ", gasListSize=" + state.getGasList().size() + ", error=" + state.getErrorMessage());
        if (state.isLoading()) {
            binding.progressBarGases.setVisibility(View.VISIBLE);
            binding.textViewGasesError.setVisibility(View.GONE);
            binding.recyclerViewGases.setVisibility(View.GONE);
        } else {
            binding.progressBarGases.setVisibility(View.GONE);
            if (state.getErrorMessage() != null) {
                binding.textViewGasesError.setVisibility(View.VISIBLE);
                binding.textViewGasesError.setText(state.getErrorMessage());
                binding.recyclerViewGases.setVisibility(View.GONE);
            } else {
                binding.textViewGasesError.setVisibility(View.GONE);
                binding.recyclerViewGases.setVisibility(View.VISIBLE);
                gasesAdapter.submitList(state.getGasList());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
        binding.recyclerViewGases.setAdapter(null); // Clear adapter to prevent memory leaks
        binding = null;
    }
} 