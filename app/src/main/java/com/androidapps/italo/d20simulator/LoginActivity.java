package com.androidapps.italo.d20simulator;

import android.app.ProgressDialog;
import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    /* Componentes */
    private EditText Name;
    private EditText Password;
    private Button Login;
    private TextView Message;
    private FirebaseAuth mAuth;
    private ProgressDialog Loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Pega referencias dos componentes */
        Name = (EditText)findViewById(R.id.email);
        Password = (EditText)findViewById(R.id.password);
        Login = (Button)findViewById(R.id.email_sign_in_button);
        Message = (TextView)findViewById(R.id.login_message);
        mAuth = FirebaseAuth.getInstance();

        Loading = new ProgressDialog(this);

        Login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Name.getText().toString(), Password.getText().toString());
            }
        });
    }

    /* Ao iniciar a activity */
    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* Chama a tela principal */
    private void callMainScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /* Cria uma nova conta e faz login do usuário */
    private void createAccount(String userName, String userPassword) {
        mAuth.createUserWithEmailAndPassword(userName, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Loading.dismiss();

                        if (task.isSuccessful()) {
                            // Usuário criado
                            FirebaseUser user = mAuth.getCurrentUser();
                            callMainScreen();
                        } else {
                            // Criação de conta falhou por algum motivo
                            String error = task.getException().toString();
                            if (error.indexOf("The given password is invalid.") != -1) {
                                Message.setText(R.string.auth_password_fail);
                            }
                            if (error.indexOf("The email address is badly formatted.") != -1) {
                                Message.setText(R.string.auth_email_fail);
                            }
                            if (error.indexOf("The email address is already in use by another account.") != -1) {
                                Message.setText(R.string.auth_signup_fail);
                            }
                            if (error.indexOf("network error") != -1) {
                                Message.setText(R.string.auth_no_connection);
                            }

                        }
                    }
                });
    }

    /* Faz login do usuário */
    private void signIn(final String userName, final String userPassword) {
        mAuth.signInWithEmailAndPassword(userName, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Login feito com sucesso
                            Loading.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            callMainScreen();
                        } else {
                            //Login falhou por algum motivo
                            String error = task.getException().toString();
                            if (error.indexOf("The email address is badly formatted.") != -1) {
                                Loading.dismiss();
                                Message.setText(R.string.auth_email_fail);
                            }
                            if (error.indexOf("The password is invalid") != -1) {
                                Loading.dismiss();
                                Message.setText(R.string.auth_signup_fail);
                            }
                            if (error.indexOf("There is no user record") != -1) {
                                Loading.setMessage("Cadastrando novo usuário.");
                                createAccount(userName, userPassword);
                            }
                            if (error.indexOf("network error") != -1) {
                                Loading.dismiss();
                                Message.setText(R.string.auth_no_connection);
                            }
                        }
                    }
                });
    }

    /* Verifica os campos de login e senha e procede a autenticação */
    private void validate(String userName, String userPassword) {
        if (!(TextUtils.isEmpty(userName)) && !(TextUtils.isEmpty(userPassword))) {
            Loading.setTitle("Autenticando...");
            Loading.setMessage("Por favor, aguarde.");
            Loading.setCanceledOnTouchOutside(false);
            Loading.show();

            signIn(userName, userPassword);
        } else {
            Message.setText(R.string.auth_null);
        }
    }
}

