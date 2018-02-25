package cr.ac.itcr.jlatouche.exchangerate;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static double exchangeRate = 550.0;
    private static String date;
    private static boolean fromDollarsToColones = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Used to get today's date in dd/MM/yyyy format
        date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        //This link will be used to fetch an XML from BCCR API
        final String bccrLink =
                "http://indicadoreseconomicos.bccr.fi.cr/indicadoreseconomicos/WebServices/" +
                        "wsIndicadoresEconomicos.asmx/ObtenerIndicadoresEconomicosXML?" +
                        "tcIndicador=318&tcFechaInicio=" + date + "&tcFechaFinal=" + date +
                        "&tcNombre=AndroidApp&tnSubNiveles=n";

        new HttpGetRequest().execute(bccrLink);

    }

    //This method changes the Edit Text hint
    public void onRadioButtonChanged(View view) {
        RadioButton radioButton = (RadioButton)view;
        EditText numberEditText = findViewById(R.id.numberEditText);

        if (radioButton.getId() == R.id.usd2crcRadioButton) {
            numberEditText.setHint("United States Dollar");
            fromDollarsToColones = true;
        }
        else if (radioButton.getId() == R.id.crc2usdRadioButton) {
            numberEditText.setHint("Costa Rican Colón");
            fromDollarsToColones = false;
        }

        onCalculateButton(view);
    }

    public void onCalculateButton(View view) {
        EditText numberEditText = findViewById(R.id.numberEditText);
        TextView resultTextView = findViewById(R.id.resultTextView);

        //If numberEditText is empty
        if (numberEditText.getText().toString().matches("")) {
            Toast.makeText(this, "You must write a number", Toast.LENGTH_SHORT).show();
            return;
        }

        //Parse the number
        double number = Double.parseDouble(numberEditText.getText().toString());
        double result;

        //Calculate the exchange rate
        if (fromDollarsToColones) {
            result = number * exchangeRate;
            resultTextView.setText(String.valueOf(result) + " CRC");
        }
        else {
            result = number / exchangeRate;
            resultTextView.setText(String.valueOf(result) + " USD");
        }

    }

    //This class will handle the HTTP request
    public class HttpGetRequest extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String data;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);

                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
                        myUrl.openConnection();

                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();

                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());

                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();

                //Set our result equal to our stringBuilder
                data = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                data = null;
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //If no error occurred
            if (result != null) {
                //Find where the exchange rate double is stored
                Pattern pattern = Pattern.compile("(\\d*\\.(\\d){2,})");
                Matcher matcher = pattern.matcher(result);

                while (matcher.find()) {
                    String string = matcher.group();
                    exchangeRate = Double.parseDouble(string);
                }

                //Shows a message to the user
                Toast.makeText(getApplicationContext(), "Today's exchange rate received", Toast.LENGTH_SHORT).show();

                TextView infoTextView = findViewById(R.id.infoTextView);
                infoTextView.setText(date + ": 1 USD = " + exchangeRate + " CRC \n" +
                        "based on Banco Central de Costa Rica");
            }
            else {
                Toast.makeText(getApplicationContext(), "Can't fetch data", Toast.LENGTH_SHORT).show();

                TextView infoTextView = findViewById(R.id.infoTextView);
                infoTextView.setText(date + ": 1 USD = " + exchangeRate + " CRC \n" +
                        "based on a local variable");
            }
        }
    }
}
