package com.example.fitzone.activites;

import static com.example.fitzone.common_functions.StaticFunctions.getApiToken;
import static com.example.fitzone.common_functions.StaticFunctions.getBaseUrl;
import static com.example.fitzone.common_functions.StaticFunctions.getDayData;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.fitzone.handelers.HandleRequests;
import com.example.fitzone.retrofit_requists.ApiInterface;
import com.example.fitzone.retrofit_requists.data_models.exercise_data.ExerciseData;
import com.example.fitzone.retrofit_requists.data_models.record_sesponse.RecordResponse;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimerActivity extends AppCompatActivity {

    private TextView timerTextView;

    private String trainingName;
    private int trainingReps,
            trainingSets,
            trainingSetNumber,
            lastTime;

    private Button yes, no;

    private ToggleButton toggleButton;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = null;

    private Retrofit retrofit;
    private ApiInterface apiInterface;


    //timer that take a time and wait for it
    private void waitTime(int limit) {
        timerRunnable = new Runnable() {
            int seconds;
            final long startTime = System.currentTimeMillis();

            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                seconds = (int) (millis / 1000);

                if (seconds >= limit) {
                    timerHandler.removeCallbacks(this);
                    goToLivePreviewActivity();
                    return;
                }

                timerTextView.setText(String.format("%02d", limit - seconds));

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void goToLivePreviewActivity() {
        Intent intent = new Intent(getApplicationContext(), LivePreviewActivity.class);
        intent.putExtra("TName", trainingName);
        intent.putExtra("TReps", trainingReps);
        intent.putExtra("TSets", trainingSets);
        intent.putExtra("setNumber", trainingSetNumber);
        intent.putExtra("lastTime", lastTime);
        startActivity(intent);
        finish();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        //inflate elements
        TextView trainingData = findViewById(R.id.training_name);
        timerTextView = findViewById(R.id.timer);
        toggleButton = findViewById(R.id.timer_toggle_button);

        toggleButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                timerHandler.removeCallbacks(timerRunnable);
            } else {
                timerHandler.postDelayed(timerRunnable, 0);
            }
        });

        Intent intent = getIntent();
        trainingName = intent.getStringExtra("TName");
        trainingReps = intent.getIntExtra("TReps", 10);
        trainingSets = intent.getIntExtra("TSets", 3);
        trainingSetNumber = intent.getIntExtra("setNumber", 1);
        lastTime = intent.getIntExtra("lastTime", 0);//for store last set taken time


        //retrofit builder
        retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl(this))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);


        if (trainingSetNumber > 1) {
            if (trainingSetNumber > trainingSets) {
                timerHandler.removeCallbacks(timerRunnable);
                timerTextView.setTextSize(20);
                trainingData.setText(trainingName);
                toggleButton.setVisibility(View.GONE);
                timerTextView.setText(String.format("%d %s in %d seconds.",
                        trainingReps * trainingSets,
                        trainingName,
                        lastTime));

                // send completed data to server
                sendCompletedResult(trainingName,
                        trainingReps * trainingSets,
                        lastTime);

//                askToShareOrNot(trainingName,
//                        String.format("%d X %d " + getString(R.string.in) + " %d " + getString(R.string.seconds), trainingReps, trainingSets, lastTime),
//                        findViewById(R.id.timer_main_view));

            } else {
                waitTime(30);//wait 30 seconds then move to @link{LivePreviewActivity}
            }

        } else {
            waitTime(15);
        }

        String trainData = "";
        if (trainingSetNumber <= trainingSets) {
            trainData = String.format("\n%s %d : %d %s",
                    getResources().getString(R.string.group_number),
                    trainingSetNumber,
                    trainingReps,
                    getResources().getString(R.string.times));
        }

        trainingData.setText(trainingName + trainData);

        GifImageView gifImageView = findViewById(R.id.training_gif_file);

        //check and assign image to each training
        if (trainingName.equals("squat"))
            gifImageView.setImageResource(R.drawable.dynamic_squat);
        else if (trainingName.equals("push up"))
            gifImageView.setImageResource(R.drawable.dynamic_push_ups);
        else
            gifImageView.setImageResource(R.drawable.dynamic_squat);
    }

    //send completed exercise data to server
    private void sendCompletedResult(String exerciseName, int exerciseCount, int exerciseDuration) {
        if (apiInterface == null)
            return;

        Call<RecordResponse> call = apiInterface.sendFinishedTrainingData("Bearer " + getApiToken(this),
                exerciseName,
                exerciseCount,
                exerciseDuration);

        call.enqueue(new Callback<RecordResponse>() {
            @Override
            public void onResponse(Call<RecordResponse> call, Response<RecordResponse> response) {
                if (response.body() == null)
                    return;

                askToShareOrNot(trainingName,
                        String.format("%d X %d " + getString(R.string.in) + " %d " + getString(R.string.seconds), trainingReps, trainingSets, lastTime),
                        findViewById(R.id.timer_main_view));
            }

            @Override
            public void onFailure(Call<RecordResponse> call, Throwable t) {
                Toast.makeText(TimerActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timerRunnable != null)
            timerHandler.removeCallbacks(timerRunnable);
        finish();
    }

    //share results after finishes
    public void askToShareOrNot(String caption, String content, View view) {

        PopupWindow askPopup;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.ask_if_yes_or_no, null);///

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        askPopup = new PopupWindow(popupView, width, height, true);

        //put the message
        ((TextView) popupView.findViewById(R.id.messageQ)).setText(getResources().getString(R.string.share_mesege) + content + " ?");

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        askPopup.showAtLocation(view, Gravity.CENTER, 0, 0);

        yes = popupView.findViewById(R.id.yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String apiToken = getApiToken(TimerActivity.this);
                HandleRequests handleRequests = new HandleRequests(TimerActivity.this);

                int postType = 1;//for training not regular post
                handleRequests.addPost(caption, content, postType, apiToken,
                        new HandleRequests.VolleyResponseListener() {
                            @Override
                            public void onResponse(boolean status, JSONObject jsonObject) {
                                if (status) {
                                    finish();

                                    askPopup.dismiss();
                                } else {
                                    Snackbar.make(view, "something wrong with your internet connection", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        });
            }
        });

        no = popupView.findViewById(R.id.no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPopup.dismiss();
                finish();
            }
        });

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //askPopup.dismiss();
                return true;
            }
        });
    }
}