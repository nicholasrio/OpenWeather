package com.example.nicholasrio.openweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
//        Log.d("TEST", "initSpinner " + city);
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
//                Log.d("TEST", "onItemSelected" + selectedCity);
                writeCityPreferences(selectedCity);

                String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + selectedCity + "&mode=json&units=metric&cnt=7";
                new DownloadTask().execute(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    private class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls){
            try{
                return downloadUrl(urls[0]);
            }catch (IOException e){
                return "Unable to download";
            }
        }

        @Override
        protected void onPostExecute(String result){
//            Log.d("TEST", "onPostExecute " + result);
            try {
                double totalDay = 0;
                double totalVariance = 0;

                JSONObject root = new JSONObject(result);
                JSONArray list = root.optJSONArray("list");

                NumberFormat nf = new DecimalFormat("#0.00");
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
                TableLayout tempTable = new TableLayout(getApplicationContext());
                tempTable.setBackgroundColor(Color.BLACK);

                //Create header row
                TextView dateHeaderText = new TextView(getApplicationContext());
                dateHeaderText.setBackgroundColor(Color.WHITE);
                dateHeaderText.setText("Date");
                TextView tempHeaderText = new TextView(getApplicationContext());
                tempHeaderText.setBackgroundColor(Color.WHITE);
                tempHeaderText.setText("Temperature");
                TextView varHeaderText = new TextView(getApplicationContext());
                varHeaderText.setBackgroundColor(Color.WHITE);
                varHeaderText.setText("Variance");

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
                rowParams.setMargins(5, 5, 5, 5);
                rowParams.weight = 1;

                TableRow headerRow = new TableRow(getApplicationContext());
                headerRow.setBackgroundColor(Color.BLACK);
                headerRow.addView(dateHeaderText, rowParams);
                headerRow.addView(tempHeaderText, rowParams);
                headerRow.addView(varHeaderText, rowParams);
                tempTable.addView(headerRow);

                for(int i=0; i < list.length(); i++){
                    JSONObject cur = list.getJSONObject(i);

                    String date = df.format(new Date(cur.getLong("dt") * 1000)).toString();

                    JSONObject temp = cur.optJSONObject("temp");
                    double day = temp.getDouble("day");
                    double variance = temp.getDouble("max") - temp.getDouble("min");
                    totalDay += day;
                    totalVariance += variance;

                    //Setting the table layout
                    TextView dateText = new TextView(getApplicationContext());
                    dateText.setBackgroundColor(Color.WHITE);
                    dateText.setText(date);
                    TextView tempText = new TextView(getApplicationContext());
                    tempText.setBackgroundColor(Color.WHITE);
                    tempText.setText(nf.format(day));
                    TextView varText = new TextView(getApplicationContext());
                    varText.setBackgroundColor(Color.WHITE);
                    varText.setText(nf.format(variance));

                    TableRow row = new TableRow(getApplicationContext());
                    row.setBackgroundColor(Color.BLACK);

                    row.addView(dateText,rowParams);
                    row.addView(tempText,rowParams);
                    row.addView(varText,rowParams);
                    tempTable.addView(row);
                }

                double avgDay = totalDay / 7;
                double avgVariance = totalVariance / 7;

                TextView avgText = new TextView(getApplicationContext());
                avgText.setBackgroundColor(Color.WHITE);
                avgText.setText("Rata-rata ");
                TextView avgDayText = new TextView(getApplicationContext());
                avgDayText.setBackgroundColor(Color.WHITE);
                avgDayText.setText(nf.format(avgDay));
                TextView avgVarText = new TextView(getApplicationContext());
                avgVarText.setBackgroundColor(Color.WHITE);
                avgVarText.setText(nf.format(avgVariance));

                TableRow row = new TableRow(getApplicationContext());
                row.setBackgroundColor(Color.BLACK);

                row.addView(avgText,rowParams);
                row.addView(avgDayText,rowParams);
                row.addView(avgVarText,rowParams);
                tempTable.addView(row);

                layout.removeViewAt(1);
                layout.addView(tempTable);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String downloadUrl(String ori) throws IOException{
            URL url = new URL(ori);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine())!=null){
//                Log.d("TEST", "downloadUrl " + line);
                sb.append(line);
            }

            if(is != null){
                is.close();
            }
            if(br != null){
                br.close();
            }

            return sb.toString();
        }
    }
}
