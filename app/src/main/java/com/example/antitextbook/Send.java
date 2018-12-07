package com.example.antitextbook;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_OK;

public class Send extends Fragment {
    private EditText nameOfFeedback;
    private EditText describingOfFeedback;

    String mNameOfFeedback;
    String mDescribingOfFeedback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send, container, false);

        nameOfFeedback = rootView.findViewById(R.id.nameOfFeedback);
        describingOfFeedback = rootView.findViewById(R.id.describingOfFeedback);

        Button sendFeedback = rootView.findViewById(R.id.sendFeedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                mNameOfFeedback = nameOfFeedback.getText().toString();
                mDescribingOfFeedback = describingOfFeedback.getText().toString();

                if("".equals(mDescribingOfFeedback) || "".equals(mNameOfFeedback)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setTitle("Предупреждение")
                            .setMessage("Одно из полей не заполненно. Пожалуйста, заполните все поля и повторите отправку")
                            .setCancelable(false)
                            .setNegativeButton("Ок, закрыть",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else{
                    sendEmail();
                }

            }
        });

        return rootView;
    }

    // метод отправки письма через Mailgun
    private void sendEmail() {
        String to = "justlike3210@gmail.com";
        String from = "meFeedback@gmail.com";
        String subject = nameOfFeedback.getText().toString().trim();
        String message = describingOfFeedback.getText().toString().trim();

        if (subject.isEmpty()) {
            nameOfFeedback.setError("Subject required");
            nameOfFeedback.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            describingOfFeedback.setError("Message required");
            describingOfFeedback.requestFocus();
            return;
        }

        RetrofitClient.getInstance()
                .getApi()
                .sendEmail(from, to, subject, message)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == HTTP_OK) {
                            try {
                                assert response.body() != null;
                                JSONObject obj = new JSONObject(response.body().string());
                                Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        //send email if validation passes

    }
}
