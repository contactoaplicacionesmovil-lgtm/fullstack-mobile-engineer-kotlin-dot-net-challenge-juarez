package com.fieldops.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText txtUser;
    EditText txtPass;
    Button btnLogin;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUser = findViewById(R.id.txtUser);
        txtPass = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.btnLogin);

        SharedPreferences prefs = getSharedPreferences("fieldops", MODE_PRIVATE);

        // si ya hay sesion entra directo
        String token = prefs.getString("token", null);
        if (token != null) {
            Session.token = token;
            Session.userId = prefs.getInt("userId", 0);
            Session.fullName = prefs.getString("fullName", "");
            startActivity(new Intent(this, OrdersActivity.class));
            finish();
            return;
        }

        // recordar los datos del ultimo ingreso
        txtUser.setText(prefs.getString("username", ""));
        txtPass.setText(prefs.getString("password", ""));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = txtUser.getText().toString();
                String p = txtPass.getText().toString();
                if (u.equals("") || p.equals("")) {
                    Toast.makeText(LoginActivity.this, "Ingrese usuario y clave", Toast.LENGTH_SHORT).show();
                    return;
                }
                new LoginTask().execute(u, p);
            }
        });
    }

    class LoginTask extends AsyncTask<String, Void, String> {

        String user;
        String pass;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(LoginActivity.this, "", "Ingresando...");
        }

        @Override
        protected String doInBackground(String... params) {
            user = params[0];
            pass = params[1];
            try {
                URL url = new URL("http://10.0.2.2:5080/api/auth/login");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                String body = "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
                OutputStream os = con.getOutputStream();
                os.write(body.getBytes());
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            try {
                JSONObject json = new JSONObject(result);
                if (json.getBoolean("ok")) {
                    String token = json.getString("token");
                    JSONObject u = json.getJSONArray("user").getJSONObject(0);

                    Log.d("FieldOps", "login ok user=" + user + " token=" + token);

                    Session.token = token;
                    Session.userId = u.getInt("id");
                    Session.fullName = u.getString("fullName");

                    SharedPreferences.Editor ed = getSharedPreferences("fieldops", MODE_PRIVATE).edit();
                    ed.putString("token", token);
                    ed.putInt("userId", Session.userId);
                    ed.putString("fullName", Session.fullName);
                    ed.putString("username", user);
                    ed.putString("password", pass);
                    ed.commit();

                    startActivity(new Intent(LoginActivity.this, OrdersActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, json.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
