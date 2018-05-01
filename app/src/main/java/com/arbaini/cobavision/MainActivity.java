package com.arbaini.cobavision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import kotlin.Pair;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    JSONObject input;
    ClarifaiClient clarifaiClient;
    public final static int MY_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clarifaiClient = new ClarifaiBuilder("REPLACE with your apikey")
                .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
                .buildSync();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK) {

            // Convert image data to bitmap
            Bitmap picture = (Bitmap) data.getExtras().get("data");

            // Set the bitmap as the source of the ImageView
            ((ImageView) findViewById(R.id.previewImage))
                    .setImageBitmap(picture);

            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.JPEG, 90, byteStream);
            String base64Data = Base64.encodeToString(byteStream.toByteArray(),
                    Base64.URL_SAFE);

            Log.d("BASE63", base64Data);


            new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
                @Override
                protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
                    // The default Clarifai model that identifies concepts in images
                    final ConceptModel generalModel = clarifaiClient.getDefaultModels().generalModel();

                    // Use this model to predict, with the image that the user just selected as the input
                    return generalModel.predict()
                            .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(byteStream.toByteArray())))
                            .executeSync();
                }

                @Override
                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {

                    //TODO : This the result of request. You can do whatever you want
                    final List<ClarifaiOutput<Concept>> predictions = response.get();

                    Log.d("HASIL",predictions.get(0).data().toString());


                }
            }.execute();
        }

    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, MY_REQUEST_CODE);
    }
}
