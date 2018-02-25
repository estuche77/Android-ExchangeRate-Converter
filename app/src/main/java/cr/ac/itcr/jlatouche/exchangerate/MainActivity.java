package cr.ac.itcr.jlatouche.exchangerate;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Used to get today's date in dd/MM/yyyy format
        String date =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        //This link will be used to fetch an XML from BCCR API
        final String bccrLink =
                "http://indicadoreseconomicos.bccr.fi.cr/indicadoreseconomicos/WebServices/" +
                        "wsIndicadoresEconomicos.asmx/ObtenerIndicadoresEconomicosXML?" +
                        "tcIndicador=318&tcFechaInicio=" + date + "&tcFechaFinal=" + date +
                        "&tcNombre=AndroidApp&tnSubNiveles=n";

        new HttpGetRequest().execute(bccrLink);

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

        protected void onPostExecute(String result){
            super.onPostExecute(result);

        }
    }
}
