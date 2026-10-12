package co.edu.unipiloto.appbar;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "solicitudes_envio";
    private static final int REQUEST_NOTIFICACIONES = 1;

    private EditText editRemitente, editTelefonoRemitente, editDestinatario,
            editDireccion, editCiudad, editContenido, editPeso;
    private RadioGroup radioTipoEnvio;

    // Datos de la última solicitud registrada (para la notificación)
    private int numeroSolicitud = 0;
    private String resumenSolicitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editRemitente = (EditText) findViewById(R.id.edit_remitente);
        editTelefonoRemitente = (EditText) findViewById(R.id.edit_telefono_remitente);
        editDestinatario = (EditText) findViewById(R.id.edit_destinatario);
        editDireccion = (EditText) findViewById(R.id.edit_direccion);
        editCiudad = (EditText) findViewById(R.id.edit_ciudad);
        editContenido = (EditText) findViewById(R.id.edit_contenido);
        editPeso = (EditText) findViewById(R.id.edit_peso);
        radioTipoEnvio = (RadioGroup) findViewById(R.id.radio_tipo_envio);

        crearCanalNotificaciones();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if/else en vez de switch: desde AGP 8 los R.id no son constantes
        if (item.getItemId() == R.id.action_caracteristicas_envio) {
            //Code to run when the Características del envío item is clicked
            Intent intent = new Intent(this, CaracteristicasEnvioActivity.class);
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_REMITENTE, texto(editRemitente));
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_DESTINATARIO, texto(editDestinatario));
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_DESTINO,
                    texto(editDireccion) + ", " + texto(editCiudad));
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_CONTENIDO, texto(editContenido));
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_PESO, texto(editPeso));
            intent.putExtra(CaracteristicasEnvioActivity.EXTRA_EXPRESS, esExpress());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_crear_notificacion) {
            //Code to run when the Crear notificación item is clicked
            crearNotificacion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------------- Formulario ----------------

    private String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    private boolean esExpress() {
        return radioTipoEnvio.getCheckedRadioButtonId() == R.id.radio_express;
    }

    private boolean formularioCompleto() {
        EditText[] campos = {editRemitente, editTelefonoRemitente, editDestinatario,
                editDireccion, editCiudad, editContenido, editPeso};
        for (EditText campo : campos) {
            if (texto(campo).isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                campo.requestFocus();
                return false;
            }
        }
        return true;
    }

    private String textoFormulario() {
        return "Solicitud de envío de encomienda\n"
                + "Remitente: " + texto(editRemitente) + " - Tel. " + texto(editTelefonoRemitente) + "\n"
                + "Destinatario: " + texto(editDestinatario) + "\n"
                + "Dirección: " + texto(editDireccion) + ", " + texto(editCiudad) + "\n"
                + "Contenido: " + texto(editContenido) + "\n"
                + "Peso: " + texto(editPeso) + " kg\n"
                + "Tipo de envío: " + (esExpress() ? "Express" : "Normal");
    }

    public void onClickRegistrar(View view) {
        if (!formularioCompleto()) {
            return;
        }
        numeroSolicitud++;
        resumenSolicitud = texto(editRemitente) + " → " + texto(editDestinatario)
                + " (" + texto(editCiudad) + "), " + texto(editPeso) + " kg, "
                + (esExpress() ? "Express" : "Normal");
        Toast.makeText(this, "Solicitud #" + numeroSolicitud + " registrada", Toast.LENGTH_SHORT).show();
    }

    // Botón compartir: envía el formulario a otra app con un intent ACTION_SEND
    public void onClickCompartir(View view) {
        if (!formularioCompleto()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud de envío de encomienda");
        intent.putExtra(Intent.EXTRA_TEXT, textoFormulario());
        startActivity(Intent.createChooser(intent, "Compartir solicitud con"));
    }

    // ---------------- Notificación ----------------

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Solicitudes de envío", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Avisos de las solicitudes de envío registradas");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private boolean faltaPermisoNotificaciones() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void crearNotificacion() {
        if (numeroSolicitud == 0) {
            Toast.makeText(this, "Primero registre una solicitud", Toast.LENGTH_SHORT).show();
            return;
        }
        if (faltaPermisoNotificaciones()) {
            // Android 13+: se pide el permiso y la notificación sale en onRequestPermissionsResult
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICACIONES);
            return;
        }
        mostrarNotificacion();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICACIONES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarNotificacion();
            } else {
                Toast.makeText(this, "Sin permiso para mostrar notificaciones", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarNotificacion() {
        if (faltaPermisoNotificaciones()) {
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle("Solicitud de envío #" + numeroSolicitud + " registrada")
                .setContentText(resumenSolicitud)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(resumenSolicitud))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(numeroSolicitud, builder.build());
    }
}
