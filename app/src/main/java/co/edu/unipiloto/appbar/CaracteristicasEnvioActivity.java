package co.edu.unipiloto.appbar;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CaracteristicasEnvioActivity extends AppCompatActivity {

    public static final String EXTRA_REMITENTE = "remitente";
    public static final String EXTRA_DESTINATARIO = "destinatario";
    public static final String EXTRA_DESTINO = "destino";
    public static final String EXTRA_CONTENIDO = "contenido";
    public static final String EXTRA_PESO = "peso";
    public static final String EXTRA_EXPRESS = "express";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caracteristicas_envio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        boolean express = getIntent().getBooleanExtra(EXTRA_EXPRESS, false);
        String peso = valor(EXTRA_PESO);

        mostrar(R.id.text_remitente, valor(EXTRA_REMITENTE));
        mostrar(R.id.text_destinatario, valor(EXTRA_DESTINATARIO));
        mostrar(R.id.text_destino, valor(EXTRA_DESTINO));
        mostrar(R.id.text_contenido, valor(EXTRA_CONTENIDO));
        mostrar(R.id.text_peso, peso.equals("—") ? peso : peso + " kg");
        mostrar(R.id.text_tipo_envio, express ? "Express" : "Normal");
        mostrar(R.id.text_tiempo_entrega, express ? "1 a 2 días hábiles" : "3 a 5 días hábiles");
    }

    // Devuelve el extra o un guion si el campo del formulario estaba vacío
    private String valor(String extra) {
        String texto = getIntent().getStringExtra(extra);
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals(",")) {
            return "—";
        }
        return texto;
    }

    private void mostrar(int id, String texto) {
        ((TextView) findViewById(id)).setText(texto);
    }
}
