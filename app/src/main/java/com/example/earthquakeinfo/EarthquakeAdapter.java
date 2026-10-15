package com.example.earthquakeinfo;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;



import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
        In Android, Adapter is a bridge between UI component and data source that helps us to fill data in UI component.
        It holds the data and send the data to an Adapter view then view can takes the data from the adapter view
        and shows the data on different views like as ListView, GridView, Spinner etc.
        (Adapter se koristi kao most između UI komponenata  i podataka ,
        podatke koje preuzimamo , preko adaptera prikazujemo korisniku pomoću UI)
 */

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    // with this constants we split location of the earthquake in primary and offset location
    private static final String LOCATION_SEPARATOR = " of ";


    public EarthquakeAdapter(Context context, List<Earthquake> earthquakes) {
        super(context, 0, earthquakes);
    }

    /*
        getView display data at the specific  position in the data set
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // If there is not any existing view, than we inflate layout list_item.xml ,that represent one earthquake in a list
        View listItemView = convertView;
        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }

        // find the earthquake at the given position
        Earthquake currentEarthquake = getItem(position);


        // textview for the magnitude info
        TextView tvMag = listItemView.findViewById(R.id.textView_magnitude);
        // format magnitude (method below)
        String formattedMagnitude = formatMagnitude(currentEarthquake.getMagnitude());
        // set formatted magnitude to text view
        tvMag.setText(formattedMagnitude);

        // fetch the background from text view with GradientDrawable, and set proper color to the background
        // wit method getMagnitudeColor
        GradientDrawable magnitudeCircle = (GradientDrawable) tvMag.getBackground();
        int magnitudeColor = getMagnitudeColor(currentEarthquake.getMagnitude());
        magnitudeCircle.setColor(magnitudeColor);


        // full location of the earthquake that we get from USGS
        String originalLocation = currentEarthquake.getLocation();
        // create two string for separation location into primary and offset location
        String primaryLocation;
        String locationOffset;

        // if original location contains separator then we split string into two parts
        // offset = first part + separator
        if (originalLocation.contains(LOCATION_SEPARATOR)){
            String[] parts = originalLocation.split(LOCATION_SEPARATOR);
            locationOffset = parts[0] + LOCATION_SEPARATOR;
            primaryLocation = parts[1];
            // if there is not separator then instead put the "Near the" string
        }else {
            locationOffset = getContext().getString(R.string.near_the);
            primaryLocation = originalLocation;
        }

        // find textview for primary location and set text
        TextView tvPrimaryLocation = listItemView.findViewById(R.id.textView_primaryLocation);
        tvPrimaryLocation.setText(primaryLocation);

        // find textview for offset location and set text
        TextView tvLocationOffset = listItemView.findViewById(R.id.textView_locationOffset);
        tvLocationOffset.setText(locationOffset);

        // create new Date object, get a time from USGS and formatted it
        Date dateObject = new Date(currentEarthquake.getTime());


        TextView tvDate = listItemView.findViewById(R.id.textView_date);
        // format the date string
        String formattedDate = formatDate(dateObject);
        tvDate.setText(formattedDate);


        TextView tvTime = listItemView.findViewById(R.id.textView_time);
        // format the time string
        String formattedTime = formatTime(dateObject);
        tvTime.setText(formattedTime);


        return listItemView;


    }

    // format date string from the Date object
    private String formatDate(Date dateObject){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd LLL, yyyy");
        return dateFormat.format(dateObject);
    }

    // format time string from the Date object
    private String formatTime(Date dateObject){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(dateObject);
    }

    // format magnitude to show one decimal place
    private String formatMagnitude(double magnitude){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return decimalFormat.format(magnitude);
    }

    // return the color for the magnitude circle based on the intensity of the earthquake
    // (pomocu switch izraza i medode floor iz Math klase podesavamo boju u zavisnosti od jacine magnitude)
    private int getMagnitudeColor(double magnitude){
        int magnitudeColorResourceID;
        // use Math class - "floor" ,means finding the closest integer less than the decimal value (1.2 is 1)
        int magnitudeFloor = (int) Math.floor(magnitude);
        switch (magnitudeFloor){
            case 0:
            case 1:
                magnitudeColorResourceID = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceID = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceID = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceID = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceID = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceID = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceID = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceID = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceID = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceID = R.color.magnitude10plus;
                break;

        }

        // ContextCompat.getColor convert the color resource ID into an actual integer color value
        // ContextCompat just returns an integer representation of your color
        return ContextCompat.getColor(getContext(), magnitudeColorResourceID);
    }
}
