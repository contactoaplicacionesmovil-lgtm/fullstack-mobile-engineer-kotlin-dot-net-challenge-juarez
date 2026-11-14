package com.fieldops.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrdersActivity extends AppCompatActivity {

    static final String[] FILTER_LABELS = {"Todas", "Pendientes", "En progreso", "Completadas"};
    static final String[] FILTER_VALUES = {"", "PENDING", "IN_PROGRESS", "COMPLETED"};

    ListView listOrders;
    TextView lblHeader;
    TextView lblEmpty;
    Spinner spnStatus;
    ProgressDialog progress;

    ArrayList<JSONObject> orders = new ArrayList<JSONObject>();
    OrdersAdapter adapter;
    String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        setTitle("Mis órdenes");

        listOrders = findViewById(R.id.listOrders);
        lblHeader = findViewById(R.id.lblHeader);
        lblEmpty = findViewById(R.id.lblEmpty);
        spnStatus = findViewById(R.id.spnStatus);

        adapter = new OrdersAdapter();
        listOrders.setAdapter(adapter);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, FILTER_LABELS);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(filterAdapter);
        spnStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = FILTER_VALUES[position];
                loadOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        listOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showStatusDialog(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Cerrar sesión");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            SharedPreferences.Editor ed = getSharedPreferences("fieldops", MODE_PRIVATE).edit();
            ed.remove("token");
            ed.commit();
            Session.token = null;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void loadOrders() {
        new LoadOrdersTask().execute();
    }

    void showStatusDialog(int position) {
        final JSONObject order = orders.get(position);
        final String[] labels = {"Pendiente", "En progreso", "Completada"};
        final String[] values = {"PENDING", "IN_PROGRESS", "COMPLETED"};

        new AlertDialog.Builder(this)
                .setTitle(order.optString("code") + " - Cambiar estado")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new UpdateStatusTask().execute(String.valueOf(order.optInt("id")), values[which]);
                    }
                })
                .show();
    }

    class LoadOrdersTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            // a veces se quedaba pegado el "Cargando..." (reportado por soporte)
            if (progress != null) {
                progress.dismiss();
            }
            progress = ProgressDialog.show(OrdersActivity.this, "", "Cargando órdenes...");
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String u = Session.BASE_URL + "/api/workorders?technicianId=" + Session.userId;
                if (!currentFilter.equals("")) {
                    u = u + "&status=" + currentFilter;
                }
                URL url = new URL(u);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Bearer " + Session.token);

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
            orders.clear();
            try {
                JSONArray arr = new JSONArray(result);
                for (int i = 0; i < arr.length(); i++) {
                    orders.add(arr.getJSONObject(i));
                }
            } catch (Exception e) {
            }
            adapter.notifyDataSetChanged();

            int pending = 0;
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).optString("status").equals("PENDING")) {
                    pending++;
                }
            }
            lblHeader.setText("Hola, " + Session.fullName + "  ·  " + pending + " pendiente(s)");

            if (orders.size() == 0) {
                lblEmpty.setVisibility(View.VISIBLE);
            } else {
                lblEmpty.setVisibility(View.GONE);
            }
        }
    }

    class UpdateStatusTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL(Session.BASE_URL + "/api/workorders/" + params[0] + "/status");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("PUT");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + Session.token);
                con.setDoOutput(true);

                String body = "{\"status\":\"" + params[1] + "\"}";
                OutputStream os = con.getOutputStream();
                os.write(body.getBytes());
                os.close();

                con.getResponseCode();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(OrdersActivity.this, "Estado actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OrdersActivity.this, "No se pudo actualizar", Toast.LENGTH_SHORT).show();
            }
            loadOrders();
        }
    }

    class OrdersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            return orders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.item_order, parent, false);

            TextView lblCode = v.findViewById(R.id.lblCode);
            TextView lblStatus = v.findViewById(R.id.lblStatus);
            TextView lblTitle = v.findViewById(R.id.lblTitle);
            TextView lblCustomer = v.findViewById(R.id.lblCustomer);
            TextView lblAddress = v.findViewById(R.id.lblAddress);
            TextView lblDate = v.findViewById(R.id.lblDate);

            JSONObject o = orders.get(position);
            try {
                lblCode.setText(o.getString("code"));
                lblTitle.setText(o.getString("title"));
                lblCustomer.setText(o.getString("customerName"));
                lblAddress.setText(o.getString("address"));

                String status = o.getString("status");
                if (status.equals("PENDING")) {
                    lblStatus.setText("PENDIENTE");
                    lblStatus.setTextColor(Color.parseColor("#E65100"));
                } else if (status.equals("IN_PROGRESS")) {
                    lblStatus.setText("EN PROGRESO");
                    lblStatus.setTextColor(Color.parseColor("#1565C0"));
                } else {
                    lblStatus.setText("COMPLETADA");
                    lblStatus.setTextColor(Color.parseColor("#2E7D32"));
                }

                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date scheduled = in.parse(o.getString("scheduledAt"));
                lblDate.setText("Programada: " + out.format(scheduled));

                // una orden esta vencida si ya paso su fecha y no se completo
                if (scheduled.before(new Date()) && !status.equals("COMPLETED")) {
                    lblDate.setText("Programada: " + out.format(scheduled) + "   VENCIDA");
                    lblDate.setTextColor(Color.RED);
                }

                // las de prioridad alta se resaltan
                if (o.getString("priority").equals("HIGH")) {
                    v.setBackgroundColor(Color.parseColor("#FFF3E0"));
                }
            } catch (Exception e) {
            }

            return v;
        }
    }
}
