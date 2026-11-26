
package com.example.actividadlistas;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        List<Producto> productos = new ArrayList<>();

        productos.add(new Producto("Laptop", "Computadora portátil", "$12,000", "💻"));
        productos.add(new Producto("Celular", "Teléfono inteligente", "$5,500", "📱"));
        productos.add(new Producto("Audífonos", "Accesorio de audio", "$850", "🎧"));
        productos.add(new Producto("Teclado", "Accesorio de computadora", "$450", "⌨️"));
        productos.add(new Producto("Mouse", "Ratón inalámbrico", "$320", "🖱️"));
        productos.add(new Producto("Monitor", "Pantalla de 24 pulgadas", "$3,200", "🖥️"));
        productos.add(new Producto("Cámara", "Cámara digital", "$4,600", "📷"));
        productos.add(new Producto("Bocina", "Altavoz Bluetooth", "$750", "🔊"));
        productos.add(new Producto("Impresora", "Impresora multifuncional", "$2,400", "🖨️"));
        productos.add(new Producto("Memoria USB", "Almacenamiento de 64 GB", "$180", "💾"));

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        ProductoAdapter adapter = new ProductoAdapter(productos);

        recyclerView.setAdapter(adapter);
    }
}
