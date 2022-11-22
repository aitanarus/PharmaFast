package com.example.pharmafast.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pharmafast.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CartRepository {
    private static CartRepository instance;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private MutableLiveData<List<Product>> productsCart;


    private CartRepository(){
        ref = database.getReference("cart");
        productsCart = new MutableLiveData<>();

        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> currentCartSneakers = new ArrayList<>();
                try {
                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    DataSnapshot subSnapshot;
                    while (iterator.hasNext()) {
                        subSnapshot = iterator.next();
                        currentCartSneakers.add(subSnapshot.getValue(Product.class));
                    }
                } catch (Exception e) {
                    Log.e("Firebase error", e.getMessage());
                }
                productsCart.setValue(currentCartSneakers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public static synchronized CartRepository getInstance(Application app) {
        if(instance == null)
            instance = new CartRepository();
        return instance;
    }

    public void addProductToCart(Product product){
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cart").child(String.valueOf(product.getProductId())).setValue(product);
    }

    public void deleteProductFromCart(Product product){
        if (product.getNumberInCart()>0)
            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cart").child(String.valueOf(product.getProductId())).setValue(product);
        else
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cart").child(String.valueOf(product.getProductId())).removeValue();
    }

    public LiveData<List<Product>> getCartProducts() {
        return productsCart;
    }
}
