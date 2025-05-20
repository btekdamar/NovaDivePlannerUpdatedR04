package com.burc.novadiveplannerupdated.presentation.ui.segments;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.burc.novadiveplannerupdated.databinding.FragmentSegmentsBinding;
import com.burc.novadiveplannerupdated.presentation.ui.segments.edit.AddEditSegmentDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SegmentsFragment extends Fragment {

    private static final String TAG = "SegmentsFragment";
    private static final String DIALOG_TAG_ADD_SEGMENT = "AddEditSegmentDialog_Add";
    private static final String DIALOG_TAG_EDIT_SEGMENT = "AddEditSegmentDialog_Edit";

    private FragmentSegmentsBinding binding;
    private SegmentsViewModel viewModel;
    private SegmentAdapter segmentAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SegmentsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSegmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        segmentAdapter = new SegmentAdapter(segmentItem -> {
            if (segmentItem.originalSegment != null) {
                viewModel.editSegmentClicked(segmentItem.originalSegment);
            }
        });
        binding.recyclerViewSegments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewSegments.setAdapter(segmentAdapter);

        binding.buttonAddSegment.setOnClickListener(v -> viewModel.addSegmentClicked());
    }

    private void observeViewModel() {
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                Log.w(TAG, "UI State is null");
                return;
            }

            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

            if (state.getErrorMessage() != null) {
                Toast.makeText(getContext(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                viewModel.onErrorMessageShown();
            }

            segmentAdapter.submitList(state.getDisplayableSegments());
            boolean showEmptyText = state.getDisplayableSegments().isEmpty() && !state.isLoading();
            binding.recyclerViewSegments.setVisibility(showEmptyText ? View.GONE : View.VISIBLE);
            binding.textViewEmptySegments.setVisibility(showEmptyText ? View.VISIBLE : View.GONE);

            binding.buttonAddSegment.setEnabled(state.isAddSegmentEnabled());

            if (state.isNavigateToAddSegmentTrigger()) {
                Log.d(TAG, "Triggering Add Segment Dialog");
                AddEditSegmentDialogFragment.newInstance(null)
                        .show(getParentFragmentManager(), DIALOG_TAG_ADD_SEGMENT);
                viewModel.onAddSegmentNavigationConsumed();
            }

            if (state.getNavigateToEditSegmentFor() != null) {
                Integer segmentNumberToEdit = state.getNavigateToEditSegmentFor();
                Log.d(TAG, "Triggering Edit Segment Dialog for segment: " + segmentNumberToEdit);
                AddEditSegmentDialogFragment.newInstance(segmentNumberToEdit)
                        .show(getParentFragmentManager(), DIALOG_TAG_EDIT_SEGMENT);
                viewModel.onEditSegmentNavigationConsumed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerViewSegments.setAdapter(null);
            binding = null;
        }
    }
} 