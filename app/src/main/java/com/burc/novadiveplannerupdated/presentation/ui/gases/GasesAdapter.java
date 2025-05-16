package com.burc.novadiveplannerupdated.presentation.ui.gases;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.burc.novadiveplannerupdated.R;
import com.burc.novadiveplannerupdated.presentation.ui.gases.state.GasRowDisplayData;

import java.util.Locale;

public class GasesAdapter extends ListAdapter<GasRowDisplayData, GasesAdapter.GasViewHolder> {

    private OnGasEnabledChangedListener gasEnabledChangedListener;
    private OnEditGasClickListener editGasClickListener;

    public interface OnGasEnabledChangedListener {
        void onEnabledChanged(int slotNumber, boolean isEnabled);
    }

    public interface OnEditGasClickListener {
        void onEditClicked(int slotNumber);
    }

    public GasesAdapter(@NonNull DiffUtil.ItemCallback<GasRowDisplayData> diffCallback) {
        super(diffCallback);
    }

    public void setOnGasEnabledChangedListener(OnGasEnabledChangedListener listener) {
        this.gasEnabledChangedListener = listener;
    }

    public void setOnEditGasClickListener(OnEditGasClickListener listener) {
        this.editGasClickListener = listener;
    }

    @NonNull
    @Override
    public GasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gas_row_item, parent, false);
        return new GasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GasViewHolder holder, int position) {
        GasRowDisplayData currentGas = getItem(position);
        holder.bind(currentGas, gasEnabledChangedListener, editGasClickListener);
    }

    static class GasViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewGasSlotAndTypeLabel;
        private final CheckBox checkBoxGasEnabled;
        private final TextView textViewGasName;
        private final TextView textViewModValue;
        private final TextView textViewHtValue;
        private final TextView textViewEndValue;
        private final TextView textViewWobValue;
        private final ImageView imageViewEditGas;

        GasViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGasSlotAndTypeLabel = itemView.findViewById(R.id.textViewGasSlotAndTypeLabel);
            checkBoxGasEnabled = itemView.findViewById(R.id.checkBoxGasEnabled);
            textViewGasName = itemView.findViewById(R.id.textViewGasName);
            textViewModValue = itemView.findViewById(R.id.textViewModValue);
            textViewHtValue = itemView.findViewById(R.id.textViewHtValue);
            textViewEndValue = itemView.findViewById(R.id.textViewEndValue);
            textViewWobValue = itemView.findViewById(R.id.textViewWobValue);
            imageViewEditGas = itemView.findViewById(R.id.imageViewEditGas);
        }

        void bind(final GasRowDisplayData gasData,
                  final OnGasEnabledChangedListener enabledListener,
                  final OnEditGasClickListener editListener) {

            textViewGasSlotAndTypeLabel.setText(String.format(Locale.getDefault(),
                    "GAS %d (%s)", gasData.getSlotNumber(), gasData.getGasTypeShortText()));

            // Temporarily remove the listener to prevent firing during programmatic change
            checkBoxGasEnabled.setOnCheckedChangeListener(null);
            checkBoxGasEnabled.setChecked(gasData.isEnabled());
            checkBoxGasEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (enabledListener != null) {
                    enabledListener.onEnabledChanged(gasData.getSlotNumber(), isChecked);
                }
            });
            
            String displayName = gasData.getUserDefinedGasName() != null && !gasData.getUserDefinedGasName().isEmpty() ?
                                 gasData.getUserDefinedGasName() : gasData.getCalculatedStandardGasName();
            textViewGasName.setText(displayName);

            textViewModValue.setText(gasData.getModText());
            textViewHtValue.setText(gasData.getHtText());
            textViewEndValue.setText(gasData.getEndLimitText());
            textViewWobValue.setText(gasData.getWobLimitText());

            imageViewEditGas.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEditClicked(gasData.getSlotNumber());
                }
            });
        }
    }

    public static class GasDiffCallback extends DiffUtil.ItemCallback<GasRowDisplayData> {
        @Override
        public boolean areItemsTheSame(@NonNull GasRowDisplayData oldItem, @NonNull GasRowDisplayData newItem) {
            return oldItem.getSlotNumber() == newItem.getSlotNumber();
        }

        @Override
        public boolean areContentsTheSame(@NonNull GasRowDisplayData oldItem, @NonNull GasRowDisplayData newItem) {
            return oldItem.equals(newItem);
        }
    }
} 