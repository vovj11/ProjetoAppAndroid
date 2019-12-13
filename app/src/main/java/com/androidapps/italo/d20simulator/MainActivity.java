package com.androidapps.italo.d20simulator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.SeekBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /* CONSTANTES */
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /* VARIÁVEIS */
    int diceType;
    boolean stopAnim = false;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SimpleDateFormat date;
    private Vibrator deviceVib;
    private ProgressDialog Loading;
    private FusedLocationProviderClient mLocationClient;
    double mLatitude = 0;
    double mLongitude = 0;



    /* Chama a tela de login */
    private void callLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /* Chama a tela de minhas jogadas */
    private void callRollsScreen() {
        Intent intent = new Intent(MainActivity.this, RollsActivity.class);
        startActivity(intent);
    }

    /* Faz signout do app e retorna para a tela de login */
    public void signOut() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(R.string.logout_message).setCancelable(true)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        callLoginScreen();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.setTitle(R.string.logout_title);
        alert.show();
    }

    /* Adiciona o menu na toolbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                signOut();
                break;

            case R.id.view_rolls:
                callRollsScreen();
                break;

            default:
                //faznada
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        /* GEOLOCALIZAÇÃO */
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /* VIBRAÇÃO */
        deviceVib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        /* TOOLBAR */
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* ANIMACOES */
        final Animation diceshake = AnimationUtils.loadAnimation(this, R.anim.diceshake);

        /* COMPONENTES */
        final TextView diceTypeLabel = (TextView) findViewById(R.id.diceTypeLabel);
        final TextView diceValueLabel = (TextView) findViewById(R.id.diceVal);
        final SeekBar diceTypeSeek = (SeekBar) findViewById(R.id.diceTypeScroll);

        /* INICIALIZA A SEEKBAR DO TIPO DO DADO */
        diceTypeSeek.setMax(8);
        diceTypeSeek.setProgress(6);

        diceTypeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                diceType = setDiceType(diceTypeSeek.getProgress());
                diceTypeLabel.setText(getString(R.string.dice_type_label) + " D" + diceType);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /* INICIALIZA A LABEL DO TIPO DO DADO */
        diceType = setDiceType(diceTypeSeek.getProgress());
        diceTypeLabel.setText("Tipo do dado: D" + diceType);

        /* INICIALIZA ROLL DO DADO */
        diceValueLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceValueLabel.startAnimation(diceshake);
                if (Build.VERSION.SDK_INT >= 26) {
                    deviceVib.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    deviceVib.vibrate(150);
                }
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Random random = new Random();
                                int rollResult = random.nextInt(diceType);

                                diceValueLabel.setText(" " + Integer.toString(rollResult + 1));

                                saveRollToDatabase(rollResult + 1, diceType);
                            }
                        }, 150);
            }
        });
    }

    /* Ao iniciar a activity solicita o uso de geolocalização */
    @Override public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    /* Checa as permissões de GPS */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /* Solicita permissão de GPS */
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            startLocationPermissionRequest();
        } else {
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /* Callback da solicitação de permissão */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                /* request foi cancelado */
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /* permissão concedida */
                getLastLocation();
            }
        }
    }

    /* Obtém a localização mais recente */
    private void getLastLocation() {
        mLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLatitude = task.getResult().getLatitude();
                            mLongitude = task.getResult().getLongitude();
                        } else {
                            mLatitude = 0;
                            mLongitude = 0;
                        }
                    }
                });
    }

    /* Salva a jogada feita no banco de dados */
    private void saveRollToDatabase(int rollResult, int diceValue) {
        String userKey = mAuth.getUid();
        String rollId = mDatabase.push().getKey();
        String nowDate = date.format(new Date());
        double rollLat = mLatitude;
        double rollLng = mLongitude;
        Rolls newRoll = new Rolls(rollId, diceValue, rollResult, nowDate, rollLat, rollLng);

        mDatabase.child(userKey).child(rollId).setValue(newRoll);
    }

    private int setDiceType(int value) {
        int diceValue;

        switch(value) {
            case 0:
                diceValue = 4;
                break;
            case 1:
                diceValue = 6;
                break;
            case 2:
                diceValue = 8;
                break;
            case 3:
                diceValue = 10;
                break;
            case 4:
                diceValue = 12;
                break;
            case 5:
                diceValue = 16;
                break;
            case 6:
                diceValue = 20;
                break;
            case 7:
                diceValue = 30;
                break;
            case 8:
                diceValue = 100;
                break;
            default:
                diceValue = 6;
        }
        return diceValue;
    }
}
