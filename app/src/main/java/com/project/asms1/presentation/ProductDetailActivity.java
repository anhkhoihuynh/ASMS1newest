package com.project.asms1.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.asms1.R;
import com.project.asms1.model.Product;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        TextView viewName = (TextView) findViewById(R.id.txtProductDetailNameProduct);
        TextView viewPrice = (TextView) findViewById(R.id.txtProductDetailPrice);
        TextView viewCategory = (TextView) findViewById(R.id.txtProductDetailCategory);
        TextView viewQuantity = (TextView) findViewById(R.id.txtProductDetailQuantity);

        Intent intent = getIntent();
        Product product = (Product) intent.getSerializableExtra("product");

        viewName.setText(product.getName());
        viewPrice.setText(String.valueOf(product.getPrice()));
        viewCategory.setText(product.getCategoryID());
        viewQuantity.setText(String.valueOf(product.getQuantity()));

    }

    public void clickToGoBack(View view) {
        finish();
    }
}