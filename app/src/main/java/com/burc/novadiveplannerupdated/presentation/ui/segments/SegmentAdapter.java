package com.burc.novadiveplannerupdated.presentation.ui.segments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.burc.novadiveplannerupdated.databinding.ListItemSegmentBinding;
import com.burc.novadiveplannerupdated.presentation.ui.segments.SegmentsViewModel.DisplayableSegmentItem;

public class SegmentAdapter extends ListAdapter<DisplayableSegmentItem, SegmentAdapter.SegmentViewHolder> {

    private final OnSegmentEditClickListener editClickListener;

    public interface OnSegmentEditClickListener {
        void onEditClicked(DisplayableSegmentItem segmentItem);
    }

    public SegmentAdapter(OnSegmentEditClickListener listener) {
        super(DIFF_CALLBACK);
        this.editClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<DisplayableSegmentItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DisplayableSegmentItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull DisplayableSegmentItem oldItem, @NonNull DisplayableSegmentItem newItem) {
            // Genellikle segment numarası veya benzersiz bir ID kullanılır.
            // DisplayableSegmentItem içinde originalSegment.getSegmentNumber() gibi bir alan varsa o kullanılır.
            // Şimdilik objelerin kendisini karşılaştırıyoruz, ama daha spesifik bir ID daha iyi olur.
            return oldItem.originalSegment.getSegmentNumber() == newItem.originalSegment.getSegmentNumber();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DisplayableSegmentItem oldItem, @NonNull DisplayableSegmentItem newItem) {
            return oldItem.equals(newItem); // DisplayableSegmentItem'da equals() override edilmeli
        }
    };

    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemSegmentBinding binding = ListItemSegmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SegmentViewHolder(binding, editClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        DisplayableSegmentItem currentSegment = getItem(position);
        holder.bind(currentSegment);
    }

    static class SegmentViewHolder extends RecyclerView.ViewHolder {
        private final ListItemSegmentBinding binding;
        private final OnSegmentEditClickListener editClickListener;

        public SegmentViewHolder(ListItemSegmentBinding binding, OnSegmentEditClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.editClickListener = listener;
        }

        public void bind(DisplayableSegmentItem segmentItem) {
            binding.textViewSegmentNumber.setText(segmentItem.segmentNumberText);
            binding.textViewDepthValue.setText(segmentItem.depthText);
            binding.textViewTimeValue.setText(segmentItem.timeText);
            binding.textViewGasValue.setText(segmentItem.gasText);
            binding.textViewSpValue.setText(segmentItem.spText);

            binding.imageButtonEditSegment.setVisibility(segmentItem.canEdit ? View.VISIBLE : View.GONE);
            if (segmentItem.canEdit) {
                binding.imageButtonEditSegment.setOnClickListener(v -> editClickListener.onEditClicked(segmentItem));
            } else {
                binding.imageButtonEditSegment.setOnClickListener(null);
            }
        }
    }
} 