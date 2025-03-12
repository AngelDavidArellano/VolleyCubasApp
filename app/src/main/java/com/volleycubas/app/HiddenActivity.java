package com.volleycubas.app;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HiddenActivity extends AppCompatActivity {

    private EditText editTextJson, editTextTitle, editTextContent;
    private TextView predefined_horarios, predefined_actualizacion;
    private ImageView iconCopyTitle, iconCopyContent;
    private Button buttonUploadJson, buttonUploadDocument;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Inicializa Firebase
        setContentView(R.layout.activity_hidden);

        editTextJson = findViewById(R.id.editTextJSON);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        buttonUploadJson = findViewById(R.id.buttonUploadJSON);
        buttonUploadDocument = findViewById(R.id.buttonUploadDocument);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true); // Habilita logs de Firebase

        iconCopyTitle = findViewById(R.id.iconCopyTitle);
        iconCopyContent = findViewById(R.id.iconCopyContent);

        predefined_horarios = findViewById(R.id.predefined_horarios);
        predefined_actualizacion = findViewById(R.id.predefined_actualizacion);

        buttonUploadJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonInput = editTextJson.getText().toString();

                if (TextUtils.isEmpty(jsonInput)) {
                    Toast.makeText(HiddenActivity.this, "Por favor, ingrese un JSON válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(jsonInput);
                    uploadJsonToFirestore(jsonObject);
                } catch (JSONException e) {
                    Toast.makeText(HiddenActivity.this, "Error al parsear el JSON", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonUploadDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = editTextTitle.getText().toString().trim();
                String contenido = editTextContent.getText().toString().trim();

                if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(contenido)) {
                    Toast.makeText(HiddenActivity.this, "Por favor, ingrese un título y contenido", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadAnnouncementToFirestore(titulo, contenido);
            }
        });


        // 🔥 Icono para copiar el título al portapapeles
        iconCopyTitle.setOnClickListener(v -> copyToClipboard(String.valueOf(editTextTitle.getText()), String.valueOf(editTextContent.getText())));

        // 🔥 Icono para copiar el contenido al portapapeles
        iconCopyContent.setOnClickListener(v -> copyToClipboard("Contenido", "Consulta los próximos partidos de tus equipos en el apartado 'Próximo partido'"));

        predefined_horarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = "¡Nuevos horarios publicados!";
                String contenido = "Consulta los próximos partidos de tus equipos en el apartado 'Próximo partido'";

                editTextTitle.setText(titulo);
                editTextContent.setText(contenido);
            }
        });
        predefined_actualizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = "¡Actualización disponible!";
                String contenido = "Puedes descargar la nueva versión acercando la tarjeta de acceso o pulsando AQUÍ";

                editTextTitle.setText(titulo);
                editTextContent.setText(contenido);
            }
        });
    }

    private void uploadAnnouncementToFirestore(String titulo, String contenido) {
        Map<String, Object> anuncioData = new HashMap<>();
        anuncioData.put("titulo", titulo);
        anuncioData.put("contenido", contenido);

        firestore.collection("anuncios").document("anuncio_principal").set(anuncioData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HiddenActivity.this, "Anuncio actualizado correctamente", Toast.LENGTH_SHORT).show();
                    editTextTitle.setText("");
                    editTextContent.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(HiddenActivity.this, "Error al actualizar el anuncio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void copyToClipboard(String label, String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, label + " está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, label + " copiado al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void uploadJsonToFirestore(JSONObject jsonObject) {
        try {
            // ✅ Convertir TODO el JSON a un Map ANTES de subirlo, permitiendo `null`
            Map<String, Object> data = convertJsonToMap(jsonObject);

            // Obtener las fechas y crear el ID del documento
            Map<String, Object> finDeSemana = (Map<String, Object>) data.get("fin_de_semana");
            if (finDeSemana == null) {
                Toast.makeText(this, "Error: No se encontró 'fin_de_semana'", Toast.LENGTH_SHORT).show();
                return;
            }

            String inicio = (String) finDeSemana.get("inicio");
            String fin = (String) finDeSemana.get("fin");

            if (inicio == null || fin == null) {
                Toast.makeText(this, "Error: Fechas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear ID del documento sin "/"
            String documentTitle = inicio.replace("/", "-") + "_" + fin.replace("/", "-");

            firestore.collection("horarios").document(documentTitle).set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(HiddenActivity.this, "JSON subido con éxito", Toast.LENGTH_SHORT).show();
                        editTextJson.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(HiddenActivity.this, "Error al subir el JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar el JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Método para convertir JSONObject en un Map<String, Object> permitiendo `null`
    private Map<String, Object> convertJsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = convertJsonToMap((JSONObject) value); // Convertir recursivamente
            } else if (value instanceof JSONArray) {
                value = convertJsonArrayToList((JSONArray) value); // Convertir a lista de mapas
            } else if (value == JSONObject.NULL) {
                value = null; // 🔥 Se subirá como `null` en Firestore
            }

            map.put(key, value);
        }

        return map;
    }

    // ✅ Método para convertir un JSONArray en una lista de mapas
    private List<Map<String, Object>> convertJsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            list.add(convertJsonToMap(obj)); // Convertir cada objeto JSON a Map<String, Object>
        }
        return list;
    }
}
