package com.example.jogathonc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private RadioGroup gB;
    private RadioButton radioButtonNb;
    TextView idTxt;
    int idRemove;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    public int n = 1;
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() == null){
            Toast.makeText(this, " (YOU WORK OFFLINE) : Silahkan hubungkan jaringan internet anda tanpa keluar dari Aplikasi agar data otomatis diupdate ke database",
                    Toast.LENGTH_LONG).show();
        }
        return cm.getActiveNetworkInfo() != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getImei(){
        // get imei
        TelephonyManager telephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "0";
        }
        String uid = telephony.getImei();
        return uid;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getSupportActionBar().setTitle(" Jog A Thon C"); // set the top title
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        String uid = getImei();


        //init animasi loading
        final ProgressBar loading = (ProgressBar)findViewById(R.id.progressBar);
        final Animation animHide = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.fadeout);
        final Animation animShow = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.fadein);
        final Button btn = (Button)findViewById(R.id.buttonAddNew);

        //button menampilkan dialog + baru
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        // check connection
        if (isNetworkConnected()){
            //show animasi loading
            loading.setVisibility(View.INVISIBLE);
            loading.startAnimation( animShow );
        }

        //mengambil data dari firebase
        DatabaseReference myRef = database.getReference(uid);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //show animasi loading
                loading.setVisibility(View.VISIBLE);
                loading.startAnimation( animShow );

                String x = dataSnapshot.getValue(String.class);
                submit(dataSnapshot.getKey(),Integer.valueOf(x));

                // check connection
                boolean con = isNetworkConnected();
                // validasi apakah ada koneksi
                if (con==true) {
                    //alert anda terhubung
                    Toast.makeText(getApplication(), " (YOU WORK ONLINE) : data otomatis diupdate ke database",
                            Toast.LENGTH_LONG).show();
                    //hide animasi loading
                    loading.setVisibility(View.INVISIBLE);
                    loading.startAnimation(animHide);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //show animasi loading
                loading.setVisibility(View.VISIBLE);
                loading.startAnimation( animShow );

                String x = dataSnapshot.getValue(String.class);
                idTxt.setText(x);
                // check connection
                boolean con = isNetworkConnected();
                // validasi apakah ada koneksi
                if (con==true) {
                    //hide animasi loading
                    loading.setVisibility(View.INVISIBLE);
                    loading.startAnimation(animHide);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //show animasi loading
                loading.setVisibility(View.VISIBLE);
                loading.startAnimation( animShow );

                final LinearLayout layout = (LinearLayout)findViewById(R.id.buttonAppend);
                layout.removeView(findViewById(idRemove));

                // check connection
                boolean con = isNetworkConnected();
                // validasi apakah ada koneksi
                if (con==true) {
                    //hide animasi loading
                    loading.setVisibility(View.INVISIBLE);
                    loading.startAnimation(animHide);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void removeBib(LinearLayout layout, int d, TextView txt) {
        idRemove = d;
        String uid = getImei();
        DatabaseReference myRef = database.getReference(uid+"/"+txt.getText());
        myRef.removeValue();
        DatabaseReference myRefTotal = database.getReference("Total"+"/"+txt.getText());
        myRefTotal.removeValue();
        // check connection
        isNetworkConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void minusBib(TextView txtVal, TextView txt) {
        String uid = getImei();

        //validasi
        if (Integer.parseInt(txtVal.getText().toString())-1>=0) {
            idTxt = txtVal;
            // Write a message to the database
            DatabaseReference myRef = database.getReference(uid+"/"+txt.getText());
            myRef.setValue(String.valueOf(Integer.parseInt(txtVal.getText().toString()) - 1));
            DatabaseReference myRefTotal = database.getReference("Total"+"/"+txt.getText());
            myRefTotal.setValue(String.valueOf(Integer.parseInt(txtVal.getText().toString()) - 1));
            // check connection
            isNetworkConnected();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void plusBib(TextView txtVal, TextView txt){
        String uid = getImei();

        //validasi value <100
        if (Integer.parseInt(txtVal.getText().toString())+1<100) {
            idTxt = txtVal;
            // Write a message to the database
            DatabaseReference myRef = database.getReference(uid+"/"+txt.getText());
            myRef.setValue(String.valueOf(Integer.parseInt(txtVal.getText().toString()) + 1));
            DatabaseReference myRefTotal = database.getReference("Total"+"/"+txt.getText());
            myRefTotal.setValue(String.valueOf(Integer.parseInt(txtVal.getText().toString()) + 1));
            // check connection
            isNetworkConnected();
        }
    }

    @SuppressLint("NewApi")
    public void submit(String text,int nilai){
        final Animation animHide = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.fadeout);
        final Animation animShow = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.fadein);

        // mengambil ukuran frame layar
        final float scale = getResources().getDisplayMetrics().density;

        // append child untuk vertical layout
        final LinearLayout layout = (LinearLayout)findViewById(R.id.buttonAppend);
        layout.setOrientation(LinearLayout.VERTICAL);
        // paramater vertical layout untuk append BIB
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin=(int)(6*scale);
        layoutParams.bottomMargin=(int)(2*scale);

        // membuat BIB
        final TextView txt = new TextView(this);
        txt.setId(R.id.bib+n);
        // set BIB number
        txt.setText(text);
        txt.setTextSize(17*scale);
        txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txt.setBackgroundResource(R.drawable.bib_layout);


        // membuat nilai BIB
        final TextView txtValue = new TextView(this);
        txtValue.setId(R.id.bibValue+n);
        // set nilai = 0
        txtValue.setText(String.valueOf(nilai));
        txtValue.setTextSize(17*scale);
        txtValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtValue.setBackgroundResource(R.drawable.number_layout);



        // membuat layout horizontal button
        final LinearLayout r1 = new LinearLayout(this);
        r1.setId(R.id.layoutR+n);
        r1.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout.LayoutParams layoutR1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,(int)(50*scale));
        LinearLayout.LayoutParams layoutR2 = new LinearLayout.LayoutParams((int)(50*scale),(int)(50*scale));

        layoutR1.rightMargin=(int)(6*scale);layoutR1.topMargin=(int)(5*scale);
        layoutR2.rightMargin=(int)(6*scale);layoutR2.bottomMargin=(int)(7*scale);


        // membuat button +
        Button btnPlus = new Button(this);
        btnPlus.setBackgroundResource(R.drawable.plus);
        btnPlus.setId(R.id.plus+n);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusBib(txtValue,txt);
            }
        });

        // membuat button -
        Button btnMinus = new Button(this);
        btnMinus.setBackgroundResource(R.drawable.minus);
        btnMinus.setId(R.id.minus+n);
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusBib(txtValue,txt);
            }
        });

        // membuat button remove
        Button btnRemove = new Button(this);
        btnRemove.setBackgroundResource(R.drawable.remove);
        btnRemove.setId(R.id.remove+n);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBib(layout,r1.getId(),txt);
            }
        });

        // append button dan bib ke layout horizontal
        r1.addView(txt,layoutR1);
        r1.addView(btnMinus,layoutR2);
        r1.addView(txtValue,layoutR1);
        r1.addView(btnPlus,layoutR2);
        r1.addView(btnRemove,layoutR2);

        //append layout horizontal ke layout vertical
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.addView(r1,layoutParams);

        //animation
        r1.startAnimation( animShow );
        n=n+1;
    }

    private void showDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this,R.style.AlertDialogCustom);

        // set title dialog
        alertDialogBuilder.setTitle("Masukan BIB baru");
        final LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogform, null);
        // set pesan dari dialog
        alertDialogBuilder
                .setIcon(R.drawable.logo)
                .setView(dialogView);

        // membuat alert dialog dari builder
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // menampilkan alert dialog
        alertDialog.show();

        final EditText newBib    = (EditText) dialogView.findViewById(R.id.newBib);
        Button submit = (Button) dialogView.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // validasi BIB tidak boleh kosong
                if (newBib.getText().toString().length()!=0) {
                    //mengambil gender

                    //mengambil data dari firebase
                    String uid = getImei();
                    DatabaseReference myRef = database.getReference(uid + "/" + newBib.getText().toString());
                    myRef.setValue("0");
                    DatabaseReference myRefTotal = database.getReference("Total" + "/" + newBib.getText().toString());
                    myRefTotal.setValue("0");
                    // check connection
                    isNetworkConnected();

                    alertDialog.dismiss();
                } else {
                    //alert message
                    Toast.makeText(getApplication(), "Gagal Menambahkan , BIB tidak boleh kosong",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }
}
