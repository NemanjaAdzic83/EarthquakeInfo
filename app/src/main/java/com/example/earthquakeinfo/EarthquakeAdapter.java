package com.example.earthquakeinfo;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.earthquakeinfo.databinding.ListItemBinding;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/*
    Adapter is a bridge between the data (list of earthquakes) and the RecyclerView.
    It creates a row (ViewHolder) for each visible earthquake and fills it with data.
    (Adapter je most izmedju podataka i liste koju korisnik vidi.)
 */
public class EarthquakeAdapter extends ListAdapter<Earthquake, EarthquakeAdapter.EarthquakeViewHolder> {

    // Called when the user taps an earthquake in the list
    public interface OnEarthquakeClickListener {
        void onEarthquakeClick(Earthquake earthquake);
    }

    private final LocationFormatter locationFormatter;

    private final OnEarthquakeClickListener clickListener;

    // Formatters are created once and reused for every row (creating them is relatively slow)
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;
    private final DecimalFormat magnitudeFormat = new DecimalFormat("0.0");

    public EarthquakeAdapter(Context context, OnEarthquakeClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        locationFormatter = LocationFormatter.from(context);

        // Date in the order used by the phone's language (e.g. "16. окт 2026." or "Oct 16, 2026")
        Locale locale = Locale.getDefault();
        String datePattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "ddMMMyyyy");
        dateFormat = new SimpleDateFormat(datePattern, locale);
        // Time in 24h or 12h format, following the phone's settings
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

    /*
        Creates a new row view. Called only for the few rows that fit on the screen;
        those rows are then reused (recycled) while scrolling.
     */
    @NonNull
    @Override
    public EarthquakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);
        return new EarthquakeViewHolder(binding);
    }

    /*
        Fills an existing row with the data of the earthquake at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull EarthquakeViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /*
        ViewHolder keeps references to the views of one row,
        so findViewById is called only once per row, not on every scroll.
     */
    class EarthquakeViewHolder extends RecyclerView.ViewHolder {

        private final ListItemBinding binding;

        EarthquakeViewHolder(@NonNull ListItemBinding binding) {

            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onEarthquakeClick(getItem(position));
                }
            });
        }

        void bind(Earthquake earthquake) {
            Context context = itemView.getContext();

            // Magnitude with one decimal, inside a circle colored by intensity.
            // mutate() gives this row its own copy of the circle, so changing its color
            // does not change the color of the circles in other rows.
            binding.textViewMagnitude.setText(magnitudeFormat.format(earthquake.getMagnitude()));
            GradientDrawable magnitudeCircle = (GradientDrawable) binding.textViewMagnitude.getBackground().mutate();
            magnitudeCircle.setColor(MagnitudeColors.get(context,earthquake.getMagnitude()));
            binding.textViewMagnitude.setTextColor(MagnitudeColors.getTextColor(context,earthquake.getMagnitude()));

            LocationFormatter.Parts location = locationFormatter.format(earthquake.getLocation());
            binding.textViewLocationOffset.setText(location.getOffset());
            binding.textViewPrimaryLocation.setText(location.getPrimaryLocation());

            Date date = new Date(earthquake.getTime());
            binding.textViewDate.setText(dateFormat.format(date));
            binding.textViewTime.setText(timeFormat.format(date));
        }
    }

    /*
        Tells the list which earthquakes are the same and whether their data changed,
        so only the changed rows are redrawn (with animation) when a new list arrives.
     */
    private static final DiffUtil.ItemCallback<Earthquake> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Earthquake>() {
                @Override
                public boolean areItemsTheSame(@NonNull Earthquake oldItem, @NonNull Earthquake newItem) {
                    // Every USGS event has its own unique URL
                    return Objects.equals(oldItem.getUrl(), newItem.getUrl());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Earthquake oldItem, @NonNull Earthquake newItem) {
                    return oldItem.getMagnitude() == newItem.getMagnitude()
                            && oldItem.getTime() == newItem.getTime()
                            && Objects.equals(oldItem.getLocation(), newItem.getLocation());
                }
            };
}