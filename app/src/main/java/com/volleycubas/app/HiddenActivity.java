package com.volleycubas.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText editTextJson;
    private Button buttonUpload;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Inicializa Firebase
        setContentView(R.layout.activity_hidden);

        editTextJson = findViewById(R.id.editTextJSON);
        buttonUpload = findViewById(R.id.buttonUploadJSON);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true); // Habilita logs de Firebase

        buttonUpload.setOnClickListener(new View.OnClickListener() {
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
