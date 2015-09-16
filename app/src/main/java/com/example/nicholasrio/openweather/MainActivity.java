package com.example.nicholasrio.openweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Other functions
    private void initSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String city = readCityPreferences();
        Log.d("TEST", "initSpinner " + city);
        if(city == null){
            city = spinner.getSelectedItem().toString();
            writeCityPreferences(city);
        }

        int spinnerPosition = adapter.getPosition(city);
        spinner.setSelection(spinnerPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                Log.d("TEST", "onItemClick " + selectedCity);
                writeCityPreferences(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
    }

    private void writeCityPreferences(String value){
        SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("city", value);
        editor.commit();
    }

    private String readCityPreferences() {
        SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
        return pref.getString("city",null);
    }
}
