package com.unilarm.newscast.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unilarm.newscast.R;
import com.unilarm.newscast.handle.DownloadHandle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DownloadHandle.MEDIA_FOLDER;

public class PersonalActivity extends AppCompatActivity {

    /*UI for Bar Button*/
    private ImageView iv_channel_personal;
    private ImageView iv_calendar_personal;
    private ImageView iv_favourite_personal;
    private ImageView iv_person_personal;
    private ConstraintLayout cl_core_personal;
    private LinearLayout ll_toolbar_personal;

    /*UI Logic*/
    private boolean isKeyboardShown;
    private int thisHeightOfCoreFrame;
    private int lastHeightOfCoreFrame;


    /* UI for FireBase */

    private static final String TAG = "EmailPassword";

    private ImageView firebaseImageLogo;
    private TextView fireBaseTextUserInfo;
    private TextView firebaseTextUserID;

    private ImageView firebaseImageBackup;
    private ImageView firebaseImageRestore;

    private ProgressBar firebaseProgressBar;

    private EditText firebaseEditEmail;
    private EditText firebaseEditPassword;
    private LinearLayout firebaseInputBar;
    private LinearLayout firebaseStatusBar;

    private Button firebaseButtonSignIn;
    private Button firebaseButtonCreateAccount;
    private Button firebaseButtonSignOut;
    private Button firebaseButtonVerifyEmail;

    // [START declare_auth]
    private FirebaseAuth authentication;
    private FirebaseUser userInfo;
    private String userID;
    private String squliteFileName = "newscast.sqlite"; //refer to DaoHandle
    private UploadTask uploadTask;
    private Task<Uri> uriDownloadTask;
    // [END declare_auth]

    /*HANDLE*/
    private DownloadHandle downloadHandle;

    /*RECEIVER*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        initFireBase();

        initView();

        initReceiver();

        initHandle();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
    }


    @Override
    protected void onResume() {
        super.onResume();

        buttonEnabled(true);

        userInfo = authentication.getCurrentUser();

        updateUI(userInfo);

        ll_toolbar_personal.setVisibility(VISIBLE);

        hideKeyboard(PersonalActivity.this);

        cl_core_personal.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Log.v("PersonalActivity", "core frame trigger");

                thisHeightOfCoreFrame = cl_core_personal.getMeasuredHeight();

                Log.v("PersonalActivity", "core frame height, is: " + thisHeightOfCoreFrame + ", was: " + lastHeightOfCoreFrame);

                if (thisHeightOfCoreFrame - lastHeightOfCoreFrame > 300)
                {
                    Log.v("PersonalActivity", "core frame growing");

                    if(isKeyboardShown)
                    {
                        isKeyboardShown = false;
                    }

                    if(ll_toolbar_personal.getVisibility() != VISIBLE)
                    {
                        ll_toolbar_personal.setVisibility(VISIBLE);
                    }
                }
                else if(thisHeightOfCoreFrame - lastHeightOfCoreFrame < -300)
                {
                    Log.v("PersonalActivity", "core frame shrinking");

                    if(!isKeyboardShown)
                    {
                        isKeyboardShown = true;
                    }

                    if(ll_toolbar_personal.getVisibility() != GONE)
                    {
                        ll_toolbar_personal.setVisibility(GONE);
                    }
                }
                else
                {
                    Log.v("PersonalActivity", "core frame stable");
                }

                lastHeightOfCoreFrame = thisHeightOfCoreFrame;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); <-- this will be used later...

        alertLeaving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        uploadTask = null;
        uriDownloadTask = null;
        downloadHandle = null;
    }

    /*
     *   FireBase METHODS
     *
     * */

    private void initFireBase()
    {
        /*
         *   FireBase CLASS
         *
         * */

        // [START initialize_auth]
        // Initialize FireBase Auth
        authentication = FirebaseAuth.getInstance();
        // [END initialize_auth]

    }

    private void initView() {

        /*SET Bar Head Button from here*/

        iv_channel_personal = findViewById(R.id.iv_channel_personal);
        iv_calendar_personal = findViewById(R.id.iv_calendar_personal);
        iv_favourite_personal = findViewById(R.id.iv_favourite_personal);
        iv_person_personal = findViewById(R.id.iv_person_personal);
        ll_toolbar_personal = findViewById(R.id.ll_toolbar_personal);
        cl_core_personal = findViewById(R.id.cl_core_personal);

        /*
         *   FireBase UI
         *
         * */

        // Text Fields
        fireBaseTextUserInfo = findViewById(R.id.firebase_text_user_info);
        firebaseTextUserID = findViewById(R.id.firebase_text_user_id);

        // Edit Text
        firebaseStatusBar = findViewById(R.id.firebase_status_bar);
        firebaseInputBar = findViewById(R.id.firebase_input_bar);
        firebaseEditEmail = findViewById(R.id.firebase_edit_email);
        firebaseEditPassword = findViewById(R.id.firebase_edit_password);

        // Logo
        firebaseImageLogo = findViewById(R.id.firebase_image_logo);

        // Buttons
        firebaseButtonSignIn = findViewById(R.id.firebase_btn_sign_in);
        firebaseButtonCreateAccount = findViewById(R.id.firebase_btn_create_account);
        firebaseButtonSignOut = findViewById(R.id.firebase_btn_sign_out);
        firebaseButtonVerifyEmail = findViewById(R.id.firebase_btn_verify_email);

        // Image Button
        firebaseImageBackup = findViewById(R.id.firebase_image_backup);
        firebaseImageRestore = findViewById(R.id.firebase_image_restore);

        // Progress Bar
        firebaseProgressBar = findViewById(R.id.firebase_progress_bar);
        firebaseProgressBar.setVisibility(GONE);
    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(PersonalActivity.this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("DOWNLOAD_STATUS"))
                {
                    String status = intent.getStringExtra("DOWNLOAD_STATUS");

                    Log.v("PLAYBACK_ACTIVITY", "DOWNLOAD_STATUS: " + status);

                    switch(status)
                    {
                        case "File_Downloaded":
                        case "File_Existed":

                            //File file = PersonalActivity.this.getDatabasePath(squliteFileName); //local file name

                            String directory = context.getFilesDir().getAbsolutePath() + "/" + MEDIA_FOLDER;

                            File inputFile = new File(directory + "/" + squliteFileName);

                            File outputFile = PersonalActivity.this.getDatabasePath(squliteFileName);

                            InputStream input;
                            OutputStream output;

                            try
                            {
                                input = new FileInputStream(inputFile);
                                output = new FileOutputStream(outputFile);

                                byte[] buffer = new byte[1024];

                                int read = input.read(buffer);

                                while (read != -1)
                                {
                                    output.write(buffer, 0, read);

                                    read = input.read(buffer);
                                }

                                output.flush();
                                output.close();
                                input.close();

                                setFirebaseButtons(true);
                                hideProgressDialog();

                                Toast.makeText(PersonalActivity.this, "Restore from Firebase, successful", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e)
                            {
                                Log.v("PERSONAL_ACTVITY", "file copy error: " + e.toString());

                                setFirebaseButtons(true);
                                hideProgressDialog();

                                Toast.makeText(PersonalActivity.this, "Restore from Firebase, local file not found", Toast.LENGTH_SHORT).show();
                            }
                            finally
                            {
                                output = null;
                                input = null;
                            }
                            break;
                        case "Directory_Error":
                        case "Download_Cancelled":

                            setFirebaseButtons(true);
                            hideProgressDialog();

                            Toast.makeText(PersonalActivity.this, "Restore from Firebase, downloading error", Toast.LENGTH_SHORT).show();

                            break;
                        default:

                            setFirebaseButtons(true);
                            hideProgressDialog();

                            Log.v("PERSONAL_ACTIVITY", "Unexpected Error Happened in DownloadHandle");
                            break;
                    }
                }
                else if(intent.hasExtra("DOWNLOAD_PROGRESS"))
                {
                    int progress = intent.getIntExtra("DOWNLOAD_PROGRESS", -1);

                    Log.v("PLAYBACK_ACTIVITY", "DOWNLOAD_PROGRESS: " + progress);
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("PERSONAL_ACTIVITY"));
    }

    private void initHandle() {


        /*
         *   ToolBar HANDLER
         *
         * */

        iv_channel_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(PersonalActivity.this, ChanListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_calendar_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonEnabled(false);

                Intent myIntent = new Intent(PersonalActivity.this, TimeListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_favourite_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(PersonalActivity.this, PlayListActivity.class);

                startActivity(myIntent);

                finish();

            }
        });

        iv_person_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        /*
         *   FireBase HANDLER
         *
         * */

        firebaseButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(PersonalActivity.this);

                signIn(firebaseEditEmail.getText().toString(), firebaseEditPassword.getText().toString());
            }
        });

        firebaseButtonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signOut();
            }
        });

        firebaseButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(PersonalActivity.this);

                createAccount(firebaseEditEmail.getText().toString(), firebaseEditPassword.getText().toString());
            }
        });

        firebaseButtonVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendEmailVerification();
            }
        });

        firebaseImageBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userInfo != null && userID!= null) {

                    setFirebaseButtons(false);

                    showProgressDialog();

                    Toast.makeText(PersonalActivity.this, "Backup to Firebase, uploading", Toast.LENGTH_SHORT).show();

                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    StorageReference storageRef = storage.getReference();

                    StorageReference folderRef = storageRef.child(userInfo.getUid());

                    StorageReference fileRef = folderRef.child(squliteFileName);  //remote file name

                    File file = PersonalActivity.this.getDatabasePath(squliteFileName); //local file name

                    Uri uri = Uri.fromFile(file);

                    uploadTask = fileRef.putFile(uri);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            setFirebaseButtons(true);

                            hideProgressDialog();

                            Toast.makeText(PersonalActivity.this, "Backup to Firebase, successful", Toast.LENGTH_SHORT).show();

                            uploadTask = null;
                        }
                    });

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            setFirebaseButtons(true);

                            hideProgressDialog();

                            Toast.makeText(PersonalActivity.this, "Backup to Firebase, error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            uploadTask = null;
                        }
                    });
                }
            }
        });

        firebaseImageRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userInfo != null && userID!= null) {

                    setFirebaseButtons(false);

                    showProgressDialog();

                    Toast.makeText(PersonalActivity.this, "Restore from Firebase, downloading", Toast.LENGTH_SHORT).show();

                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    StorageReference storageRef = storage.getReference();

                    StorageReference folderRef = storageRef.child(userInfo.getUid());

                    StorageReference fileRef = folderRef.child(squliteFileName);  //remote file name

                    uriDownloadTask = fileRef.getDownloadUrl();

                    uriDownloadTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            if (uri != null) {
                                String uriString = uri.toString();

                                Log.v("PERSONAL_ACTIVITY", "Firebase's download link: " + uriString);

                                downloadHandle = null;
                                downloadHandle = new DownloadHandle(PersonalActivity.this, uriString, squliteFileName, "PERSONAL_ACTIVITY");
                                downloadHandle.proceedTask();
                            } else {
                                setFirebaseButtons(true);

                                hideProgressDialog();

                                Toast.makeText(PersonalActivity.this, "Restore from Firebase, file not found", Toast.LENGTH_SHORT).show();
                            }

                            uriDownloadTask = null;
                        }
                    });
                }
            }
        });
    }


    private void createAccount(String email, String password)
    {
        Log.d(TAG, "createAccount:" + email);

        if (!validateForm())
        {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {

                hideProgressDialog();

                if (task.isSuccessful())
                {
                    // Sign in success, update UI with the signed-in user's information

                    Log.d(TAG, "createUserWithEmail:success");

                    userInfo = authentication.getCurrentUser();

                    updateUI(userInfo);

                }
                else
                {
                    // If sign in fails, display a message to the user.

                    Log.w(TAG, "createUserWithEmail:failure", task.getException());

                    Toast.makeText(PersonalActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                    userInfo = null;

                    updateUI(null);
                }

                // [START_EXCLUDE]
                hideProgressDialog();
                // [END_EXCLUDE]
            }
        });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password)
    {
        Log.d(TAG, "signIn:" + email);

        if (!validateForm())
        {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {

                hideProgressDialog();

                if (task.isSuccessful())
                {
                    // Sign in success, update UI with the signed-in user's information

                    Log.d(TAG, "signInWithEmail:success");

                    Toast.makeText(PersonalActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                    userInfo = authentication.getCurrentUser();

                    updateUI(userInfo);
                }
                else
                {
                    // If sign in fails, display a message to the user.

                    Log.w(TAG, "signInWithEmail:failure", task.getException());

                    Toast.makeText(PersonalActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                    userInfo = null;

                    updateUI(null);
                }

                // [START_EXCLUDE]
                hideProgressDialog();
                // [END_EXCLUDE]
            }
        });
        // [END sign_in_with_email]
    }

    private void signOut()
    {
        authentication.signOut();

        userInfo = null;

        updateUI(null);
    }

    private void sendEmailVerification()
    {
        showProgressDialog();

        // Disable button
        findViewById(R.id.firebase_btn_verify_email).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        userInfo = authentication.getCurrentUser();

        userInfo.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {

                hideProgressDialog();

                // [START_EXCLUDE]

                // Re-enable button
                findViewById(R.id.firebase_btn_verify_email).setEnabled(true);

                if (task.isSuccessful())
                {
                    Toast.makeText(PersonalActivity.this, "Verification email sent to " + userInfo.getEmail(), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.e(TAG, "sendEmailVerification", task.getException());

                    Toast.makeText(PersonalActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
                // [END_EXCLUDE]
            }
        });
        // [END send_email_verification]
    }

    private boolean validateForm()
    {
        boolean valid = true;

        String email = firebaseEditEmail.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            firebaseEditEmail.setError("Required.");

            valid = false;
        }
        else
        {
            firebaseEditEmail.setError(null);
        }

        String password = firebaseEditPassword.getText().toString();

        if (TextUtils.isEmpty(password))
        {
            firebaseEditPassword.setError("Required.");

            valid = false;
        }
        else
        {
            firebaseEditPassword.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user)
    {
        if (user == null)
        {
            fireBaseTextUserInfo.setText(null);
            firebaseTextUserID.setText(null);
            userID = null;

            firebaseStatusBar.setVisibility(GONE);
            firebaseInputBar.setVisibility(VISIBLE);

            firebaseButtonSignIn.setVisibility(VISIBLE);
            firebaseButtonCreateAccount.setVisibility(VISIBLE);

            firebaseButtonSignOut.setVisibility(GONE);
            firebaseButtonVerifyEmail.setVisibility(GONE);

            setFirebaseBackupButton(false);
            setFirebaseRestoreButton(false);
        }
        else
        {
            fireBaseTextUserInfo.setText(getString(R.string.emailpassword_status_fmt, user.getEmail(), user.isEmailVerified()));
            firebaseTextUserID.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            userID = userInfo.getUid();

            firebaseStatusBar.setVisibility(VISIBLE);
            firebaseInputBar.setVisibility(GONE);

            firebaseButtonSignIn.setVisibility(GONE);
            firebaseButtonCreateAccount.setVisibility(GONE);

            firebaseButtonSignOut.setVisibility(VISIBLE);
            firebaseButtonVerifyEmail.setVisibility(VISIBLE);

            if(user.isEmailVerified())
            {
                firebaseButtonVerifyEmail.setText(R.string.email_verified);
                firebaseButtonVerifyEmail.setEnabled(false);
            }
            else
            {
                firebaseButtonVerifyEmail.setText(R.string.verify_email);
                firebaseButtonVerifyEmail.setEnabled(true);
            }

            setFirebaseBackupButton(true);
            setFirebaseRestoreButton(true);
        }
    }

    private void setFirebaseButtons(boolean enable)
    {
        if(enable)
        {
            firebaseButtonSignIn.setEnabled(true);
            firebaseButtonSignIn.setAlpha(1.0F);

            firebaseButtonSignOut.setEnabled(true);
            firebaseButtonSignOut.setAlpha(1.0F);

            firebaseButtonCreateAccount.setEnabled(true);
            firebaseButtonCreateAccount.setAlpha(1.0F);

            if(userInfo != null)
            {
                if(userInfo.isEmailVerified())
                {
                    firebaseButtonVerifyEmail.setText(R.string.email_verified);
                    firebaseButtonVerifyEmail.setEnabled(false);
                }
                else
                {
                    firebaseButtonVerifyEmail.setText(R.string.verify_email);
                    firebaseButtonVerifyEmail.setEnabled(true);
                }
            }

            firebaseImageBackup.setEnabled(true);
            firebaseImageBackup.setAlpha(1.0F);


            firebaseImageRestore.setEnabled(true);
            firebaseImageRestore.setAlpha(1.0F);
        }
        else
        {
            firebaseButtonSignIn.setEnabled(false);
            firebaseButtonSignIn.setAlpha(0.5F);

            firebaseButtonSignOut.setEnabled(false);
            firebaseButtonSignOut.setAlpha(0.5F);

            firebaseButtonCreateAccount.setEnabled(false);
            firebaseButtonCreateAccount.setAlpha(0.5F);

            firebaseButtonVerifyEmail.setEnabled(false);
            firebaseButtonVerifyEmail.setAlpha(0.5F);

            firebaseImageBackup.setEnabled(false);
            firebaseImageBackup.setAlpha(0.5F);

            firebaseImageRestore.setEnabled(false);
            firebaseImageRestore.setAlpha(0.5F);
        }
    }

    private void setFirebaseBackupButton(boolean enable)
    {
        if(enable)
        {
            firebaseImageBackup.setEnabled(true);
            firebaseImageBackup.setAlpha(1.0F);
        }
        else
        {
            firebaseImageBackup.setEnabled(false);
            firebaseImageBackup.setAlpha(0.5F);
        }
    }

    private void setFirebaseRestoreButton(boolean enable)
    {
        if(enable)
        {
            firebaseImageRestore.setEnabled(true);
            firebaseImageRestore.setAlpha(1.0F);
        }
        else
        {
            firebaseImageRestore.setEnabled(false);
            firebaseImageRestore.setAlpha(0.5F);
        }
    }

    private void showProgressDialog()
    {
        firebaseImageLogo.setVisibility(GONE);
        firebaseProgressBar.setVisibility(VISIBLE);
    }

    private void hideProgressDialog()
    {
        firebaseImageLogo.setVisibility(VISIBLE);
        firebaseProgressBar.setVisibility(GONE);
    }

    private void hideKeyboard(Activity activity)
    {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
        {
            view = new View(activity);
        }

        //If really nothing is found
        if(imm != null)
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void buttonEnabled(boolean enabled) {
        if (enabled) {

            iv_channel_personal.setEnabled(true);
            iv_calendar_personal.setEnabled(true);
            iv_favourite_personal.setEnabled(true);
            iv_person_personal.setEnabled(true);
        } else {

            iv_channel_personal.setEnabled(false);
            iv_calendar_personal.setEnabled(false);
            iv_favourite_personal.setEnabled(false);
            iv_person_personal.setEnabled(false);
        }
    }

    private void alertLeaving() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PersonalActivity.this);

        alert.setTitle(R.string.leaving);
        alert.setMessage(R.string.are_you_sure);
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //the super.xxx was in the very first line of this method of onBackPressed...
                //but move to here to co-op with onClick, but need to address its origin, MainActivity, which is added to the beginning...
                PersonalActivity.super.onBackPressed(); // to do whatever the onBackPressed should do originally
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

}

