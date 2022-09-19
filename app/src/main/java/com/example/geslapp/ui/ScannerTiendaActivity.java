package com.example.geslapp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.geslapp.R;
import com.example.geslapp.core.adapters.RvAdapterListaEtqTienda;
import com.example.geslapp.core.camara.CaptureAct;
import com.example.geslapp.core.clases.BarcodePreferences;
import com.example.geslapp.core.clases.Centro;
import com.example.geslapp.core.clases.ConfigPreferences;
import com.example.geslapp.core.clases.Modelo;
import com.example.geslapp.core.requests.EansRequest;
import com.example.geslapp.core.requests.RecogidasRequest;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;


public class ScannerTiendaActivity extends AppCompatActivity {

    TextView tv_title;
    Button btn_barras, btnCancel, btnSaveBox;
    ImageButton btn_scanner;
    EditText ed_code, edNameBox;
    ImageView btn_enter;
    String nomCaja;
    RecyclerView rv_codes;
    boolean escritura = false;
    private final ConfigPreferences config = new ConfigPreferences();
    private final BarcodePreferences barcodePref = new BarcodePreferences();
    private static String IP, REC;
    String code = "Scanning Code";
    Centro c = new Centro();
    Modelo m = new Modelo();
    ArrayList<String> listBarcodes = new ArrayList<>();
    Drawable btnDraw;
    BottomAppBar bottomAppBar;
    FloatingActionButton fab;
    boolean moverFab = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner_tienda);

        init();
        getRecycler();
        getCode();
        buttonEvents();
        setupBottomMenu();
        visibilityButtonFab();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(listBarcodes.isEmpty()) {
            if(!barcodePref.getListBarcode().isEmpty()) {
                listBarcodes = barcodePref.getListBarcode();
            }
        }
        visibilityButtonFab();
    }

    private void init() {

        getArgs();
        btn_barras = findViewById(R.id.btn_barras);
        btn_scanner = findViewById(R.id.btn_scanner);
        ed_code = findViewById(R.id.ed_code);
        btn_enter = findViewById(R.id.btn_enter);
        rv_codes = findViewById(R.id.rv_codes);
        tv_title = findViewById(R.id.tv_title);
        fab = findViewById(R.id.fab);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        setSupportActionBar(bottomAppBar);
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomMenu() {

        bottomAppBar.setNavigationOnClickListener(v -> showSettings());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_nav_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.Home) {
            showHome();
        }
        if (item.getItemId() == R.id.Search) {
            showSearch();
        }
        if (item.getItemId() == R.id.List) {
            showList();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHome() {

        Intent i = new Intent(this, ScannerTiendaActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showSettings() {

        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showList() {

        Intent i = new Intent(this, ListBarcodesActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showSearch() {

        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    private void visibilityButtonFab() {

        if(listBarcodes.size() == 3){
            fab.show();

        } else {
            fab.hide();
        }
    }

    private boolean moverButtonFab() {

        if(moverFab){
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            moverFab = false;

        } else {
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            moverFab = true;
        }
        return false;
    }

    private void getArgs() {

        IP = config.getIP(getApplicationContext());
        REC = config.getRec(getApplicationContext());
    }

    private void buttonEvents() {

        buttonBarras();
        buttonScanner();
        buttonEnter();
    }

    private void buttonBarras() {

        btn_barras.setOnClickListener(v -> {
            escritura = !escritura;
            int color;

            if(escritura) {
                color = Color.parseColor("#04A800");

            } else {
                color = Color.parseColor("#535353");
            }
            btnDraw = btn_barras.getBackground();
            btnDraw = DrawableCompat.wrap(btnDraw);
            DrawableCompat.setTint(btnDraw,color);
        });
    }

    private void buttonScanner() {

        btn_scanner.setOnClickListener(view -> scanCode());
    }

    private void buttonFAB() {

        fab.setOnClickListener(view -> dialogSaveBox());
    }

    private void ButtonMovePositionFAB() {

        fab.setOnLongClickListener(v -> moverButtonFab());
    }

    private void dialogSaveBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_save_box, null);
        builder.setView(dialogLayout);
        final AlertDialog dialog = builder.create();
        initDialogRemove(dialogLayout);
        nomCaja = edNameBox.getText().toString();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSaveBox.setOnClickListener(v -> saveBox(nomCaja));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void saveBox(String nomCaja) {

        String nombreCompletoCaja = nomCaja + "_" + "codigoCentro.csv";

        Toast.makeText(this, "SIN IMPLEMENTAR", Toast.LENGTH_SHORT).show();
    }

    private void initDialogRemove(View dialogLayout) {

        btnSaveBox = dialogLayout.findViewById(R.id.btnSaveBox);
        btnCancel = dialogLayout.findViewById(R.id.btnCancel);
        edNameBox = dialogLayout.findViewById(R.id.edNameBox);
    }

    private void buttonEnter() {

        btn_enter.setOnClickListener(view -> {
            String text = ed_code.getText().toString();
            String txt2 = text.toUpperCase(Locale.ROOT);
            if(text.equals("")) {
                Toast.makeText(getApplicationContext(),"Campo vacío, por favor introduzca el tracking",Toast.LENGTH_SHORT).show();
            } else trackingCaja(txt2);
        });
    }

    private void getCode(){

        ed_code.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        trackingCaja(ed_code.getText().toString().toUpperCase(Locale.ROOT));
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });
    }

    private void getRecycler() {

        RecyclerView.LayoutManager layoutManagerEtq = new LinearLayoutManager(this);
        rv_codes.setLayoutManager(layoutManagerEtq);
        RecyclerView.Adapter<RvAdapterListaEtqTienda.MyViewHolder> rvAdapterListaEtqs =
                new RvAdapterListaEtqTienda(listBarcodes, ScannerTiendaActivity.this);
        rv_codes.setAdapter(rvAdapterListaEtqs);
    }

    private void scanCode() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(code);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String resultado = result.getContents();

        if (resultado != null) {
            ed_code.setText(resultado);

            if(listBarcodes.isEmpty()) {
                listBarcodes.add(resultado);
                barcodePref.saveBarcodePref(ScannerTiendaActivity.this, resultado);

            } else {
                if(listBarcodes.contains(resultado)){
                    Toast.makeText(this, "Esta etiqueta ya existe en la lista", Toast.LENGTH_SHORT).show();

                } else {
                    listBarcodes.add(resultado);
                    barcodePref.saveBarcodePref(ScannerTiendaActivity.this, resultado);
                    if(listBarcodes.size() == 3) {
                        fab.setVisibility(View.VISIBLE);
                        buttonFAB();
                        ButtonMovePositionFAB();
                        visibilityButtonFab();
                    }
                }
            }

            RecyclerView.Adapter<RvAdapterListaEtqTienda.MyViewHolder> rvAdapterListaEtqs =
                    new RvAdapterListaEtqTienda(listBarcodes, ScannerTiendaActivity.this);
            rv_codes.setAdapter(rvAdapterListaEtqs);
            code = resultado;
            Toast.makeText(getApplicationContext(), resultado, Toast.LENGTH_LONG).show();

            Response.Listener<String> respoListener = response -> {
                try {
                    System.out.println(response);
                    JSONObject jsonResponse = new JSONObject(response);
                    int success = jsonResponse.getInt("success");
                    System.out.println(resultado+"    a");

                    if (success == 1) {
                        Toast.makeText(getApplicationContext(),"Insert realizado exitosamente", Toast.LENGTH_SHORT).show();
                        System.out.println(resultado+"    b");

                    } else {
                        System.out.println(resultado+"    c");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(resultado+"    d");
                }
            };

            ConfigPreferences config = new ConfigPreferences();
            String REC = config.getRec(getApplicationContext());
            String IP = config.getIP(getApplicationContext());
            String[] udata = new String[2];
            udata[0] = config.getUsername(getApplicationContext());
            udata[1] = config.getUip(getApplicationContext());
            EansRequest eansRequest = new EansRequest(c.getId(),m.getTamano(), resultado, respoListener,IP,REC,udata[0],udata[1]);

            RequestQueue queue = Volley.newRequestQueue(ScannerTiendaActivity.this);
            queue.add(eansRequest);

            //scanCode();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void trackingCaja(String tracking) {

        @SuppressLint("SetTextI18n") Response.Listener<String> respoListener2 = response2 -> {

            Dialog dialog = new Dialog(ScannerTiendaActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.custom_dialog);

            TextView tvMensaje = dialog.findViewById(R.id.alertMsg);
            ImageView verde = dialog.findViewById(R.id.cajaVerde);
            ImageView rojo = dialog.findViewById(R.id.cajaRoja);
            TextView btScan = dialog.findViewById(R.id.btScan);
            ImageView imgclose = dialog.findViewById(R.id.imgclose);

            if(escritura)btScan.setVisibility(View.INVISIBLE);
            btScan.setOnClickListener(v -> {
                scanCode();
                dialog.dismiss();
            });
            imgclose.setOnClickListener(v -> dialog.dismiss());

            try {
                JSONObject jsonResponse = new JSONObject(response2);
                int success = jsonResponse.getInt("success");
                System.out.println(success);
                System.out.println(response2);

                if (success == 1) {
                    verde.setVisibility(View.VISIBLE);
                    rojo.setVisibility(View.INVISIBLE);
                    btScan.setVisibility(View.INVISIBLE);
                    tvMensaje.setVisibility(View.VISIBLE);

                    tvMensaje.setText("RECOGIDA REGISTRADA EN CENTRAL");
                    dialog.show();

                } else {
                    verde.setVisibility(View.INVISIBLE);
                    rojo.setVisibility(View.VISIBLE);
                    tvMensaje.setVisibility(View.VISIBLE);
                    btScan.setVisibility(View.INVISIBLE);
                    if(!escritura)ed_code.setText("");
                    tvMensaje.setText("NO SE HA ENCONTRADO LA CAJA");
                    dialog.show();
                    Thread t = new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            dialog.dismiss();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();}

            } catch (JSONException e) {
                verde.setVisibility(View.INVISIBLE);
                rojo.setVisibility(View.VISIBLE);
                tvMensaje.setVisibility(View.VISIBLE);
                btScan.setVisibility(View.INVISIBLE);
                if(!escritura)ed_code.setText("");
                tvMensaje.setText("NO SE HA ENCONTRADO LA CAJA");
                dialog.show();
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        dialog.dismiss();

                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
                t.start();
            }
            dialog.show();
        };

        String username = config.getUsername(getApplicationContext());
        String ip = config.getUip(getApplicationContext());
        RecogidasRequest trackingRequest = new RecogidasRequest(respoListener2,tracking, IP, REC, username, ip);
        RequestQueue queue2 = Volley.newRequestQueue(ScannerTiendaActivity.this);
        queue2.add(trackingRequest);
    }
}