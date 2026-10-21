package com.example.earthquakeinfo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.earthquakeinfo.databinding.BottomSheetEarthquakeDetailsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DecimalFormat;

/*
    Panel that slides up from the bottom when an earthquake in the list is tapped.
    Shows magnitude, place, exact time, depth, coordinates, a tsunami warning,
    and buttons to open the place on a map or the USGS web page.
 */
public class EarthquakeDetailsBottomSheet extends BottomSheetDialogFragment {

    // Tag used to find the panel in the FragmentManager (so it is not opened twice)
    public static final String TAG = "EarthquakeDetails";

    // Keys for the values stored in the arguments Bundle
    private static final String ARG_MAGNITUDE = "magnitude";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_TIME = "time";
    private static final String ARG_URL = "url";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_DEPTH = "depth";
    private static final String ARG_TSUNAMI = "tsunami";

    private BottomSheetEarthquakeDetailsBinding binding;

    /*
        The only correct way to create this panel.
        Android may destroy and recreate a fragment (e.g. on screen rotation) using its empty
        constructor, so the data must be stored in "arguments" - Android keeps them for us.
     */
    public static EarthquakeDetailsBottomSheet newInstance(Earthquake earthquake) {
        Bundle args = new Bundle();
        args.putDouble(ARG_MAGNITUDE, earthquake.getMagnitude());
        args.putString(ARG_LOCATION, earthquake.getLocation());
        args.putLong(ARG_TIME, earthquake.getTime());
        args.putString(ARG_URL, earthquake.getUrl());
        args.putDouble(ARG_LATITUDE, earthquake.getLatitude());
        args.putDouble(ARG_LONGITUDE, earthquake.getLongitude());
        args.putDouble(ARG_DEPTH, earthquake.getDepth());
        args.putBoolean(ARG_TSUNAMI, earthquake.isTsunami());

        EarthquakeDetailsBottomSheet sheet = new EarthquakeDetailsBottomSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEarthquakeDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Earthquake earthquake = readEarthquake(requireArguments());
        LocationFormatter locationFormatter = LocationFormatter.from(requireContext());

        // Big magnitude circle, same colors as in the list
        binding.detailsMagnitude.setText(new DecimalFormat("0.0").format(earthquake.getMagnitude()));
        GradientDrawable circle = (GradientDrawable) binding.detailsMagnitude.getBackground().mutate();
        circle.setColor(MagnitudeColors.get(requireContext(), earthquake.getMagnitude()));
        binding.detailsMagnitude.setTextColor(MagnitudeColors.getTextColor(requireContext(),earthquake.getMagnitude()));

        // "73 km ЗЈЗ од" + "Rumoi, Japan"
        LocationFormatter.Parts location = locationFormatter.format(earthquake.getLocation());
        binding.detailsOffset.setText(location.getOffset());
        binding.detailsPlace.setText(location.getPrimaryLocation());

        // Full date and time in the phone's language and 12/24h setting
        binding.detailsTime.setText(DateUtils.formatDateTime(requireContext(), earthquake.getTime(),
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME));

        // Depth - hidden if USGS did not send it
        if (earthquake.hasDepth()) {
            binding.detailsDepth.setText(getString(R.string.depth_format, earthquake.getDepth()));
        } else {
            binding.detailsDepthLabel.setVisibility(View.GONE);
            binding.detailsDepth.setVisibility(View.GONE);
        }

        // Coordinates and the map button - hidden if the position is unknown
        if (earthquake.hasCoordinates()) {
            binding.detailsCoordinates.setText(
                    locationFormatter.formatCoordinates(earthquake.getLatitude(), earthquake.getLongitude()));
            binding.detailsShowOnMap.setOnClickListener(v -> showOnMap(earthquake));
        } else {
            binding.detailsCoordinatesLabel.setVisibility(View.GONE);
            binding.detailsCoordinates.setVisibility(View.GONE);
            binding.detailsShowOnMap.setVisibility(View.GONE);
        }

        binding.detailsTsunami.setVisibility(earthquake.isTsunami() ? View.VISIBLE : View.GONE);

        binding.detailsOpenUsgs.setOnClickListener(v -> openUrl(Uri.parse(earthquake.getUrl())));

        // Keep the buttons above the navigation bar (edge-to-edge)
        int basePadding = binding.detailsRoot.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailsRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), basePadding + bars.bottom);
            return insets;
        });
    }

    /*
        Opens the epicenter in a map app (Google Maps or any other).
        "geo:" is a standard Android link type that every map app understands.
     */
    private void showOnMap(Earthquake earthquake) {
        String coordinates = earthquake.getLatitude() + "," + earthquake.getLongitude();
        Uri mapUri = Uri.parse("geo:" + coordinates + "?q=" + coordinates
                + "(" + Uri.encode(earthquake.getLocation()) + ")");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
        } catch (ActivityNotFoundException e) {
            // No map app installed - open Google Maps in the browser instead
            openUrl(Uri.parse("https://www.google.com/maps/search/?api=1&query=" + coordinates));
        }
    }

    private void openUrl(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            // No browser on the device - nothing we can do, just ignore the tap
        }
    }

    // Rebuilds the Earthquake from the values saved in newInstance()
    private static Earthquake readEarthquake(Bundle args) {
        return new Earthquake(
                args.getDouble(ARG_MAGNITUDE),
                args.getString(ARG_LOCATION, ""),
                args.getLong(ARG_TIME),
                args.getString(ARG_URL, ""),
                args.getDouble(ARG_LATITUDE, Double.NaN),
                args.getDouble(ARG_LONGITUDE, Double.NaN),
                args.getDouble(ARG_DEPTH, Double.NaN),
                args.getBoolean(ARG_TSUNAMI));
    }

    // The view is destroyed before the fragment, so we drop the binding to avoid a memory leak
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}