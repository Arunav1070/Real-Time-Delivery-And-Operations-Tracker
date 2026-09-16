package com.example.realtimedeliveryandoperationstracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvJobTitle, tvJobDescription, tvStatus, tvUserEmail;
    private Button btnAccept, btnStart, btnComplete, btnFail;
    private FloatingActionButton fabCreateJob;
    private MaterialToolbar topAppBar;
    private Spinner spinnerJobs;

    private DatabaseReference jobsDatabaseRef;
    private ValueEventListener allJobsListener;
    private FirebaseAuth mAuth;

    private List<Job> jobList = new ArrayList<>();
    private List<String> jobTitles = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private Job selectedJob = null;
    private String currentStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://realtimedelivery-46afd-default-rtdb.asia-southeast1.firebasedatabase.app");
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception ignored) {
        }


        jobsDatabaseRef = database.getReference("jobs");


        topAppBar = findViewById(R.id.topAppBar);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvStatus = findViewById(R.id.tvStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnStart = findViewById(R.id.btnStart);
        btnComplete = findViewById(R.id.btnComplete);
        btnFail = findViewById(R.id.btnFail);
        fabCreateJob = findViewById(R.id.fabCreateJob);
        spinnerJobs = findViewById(R.id.spinnerJobs);

        if (currentUser.getEmail() != null) {
            tvUserEmail.setText("Operator: " + currentUser.getEmail());
        }


        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jobTitles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJobs.setAdapter(spinnerAdapter);


        spinnerJobs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < jobList.size()) {
                    selectedJob = jobList.get(position);
                    currentStatus = selectedJob.getStatus() != null ? selectedJob.getStatus() : "Assigned";
                    tvJobTitle.setText(selectedJob.getTitle());
                    tvJobDescription.setText(selectedJob.getDescription() != null ? selectedJob.getDescription() : "");
                    updateUI(currentStatus);
                    saveLocalState(selectedJob.getId(), currentStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnMainLogout).setOnClickListener(v -> logoutUser());

        fabCreateJob.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateJobActivity.class))
        );


        btnAccept.setOnClickListener(v -> updateSelectedJobStatus("Accepted"));
        btnStart.setOnClickListener(v -> updateSelectedJobStatus("In Progress"));
        btnComplete.setOnClickListener(v -> updateSelectedJobStatus("Completed"));
        btnFail.setOnClickListener(v -> updateSelectedJobStatus("Failed"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRealtimeJobsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachRealtimeJobsListener();
    }

    private void attachRealtimeJobsListener() {
        if (allJobsListener == null) {
            allJobsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    jobList.clear();
                    jobTitles.clear();

                    for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                        Job job = jobSnapshot.getValue(Job.class);
                        if (job != null) {
                            jobList.add(job);
                            String displayTitle = (job.getTitle() != null ? job.getTitle() : "Job")
                                    + " (" + (job.getStatus() != null ? job.getStatus() : "N/A") + ")";
                            jobTitles.add(displayTitle);
                        }
                    }

                    spinnerAdapter.notifyDataSetChanged();

                    if (!jobList.isEmpty()) {
                        if (selectedJob != null) {
                            for (int i = 0; i < jobList.size(); i++) {
                                if (jobList.get(i).getId() != null && jobList.get(i).getId().equals(selectedJob.getId())) {
                                    spinnerJobs.setSelection(i);
                                    selectedJob = jobList.get(i);
                                    currentStatus = selectedJob.getStatus();
                                    tvJobTitle.setText(selectedJob.getTitle());
                                    tvJobDescription.setText(selectedJob.getDescription() != null ? selectedJob.getDescription() : "");
                                    updateUI(currentStatus);
                                    break;
                                }
                            }
                        } else {
                            spinnerJobs.setSelection(0);
                        }
                    } else {
                        tvJobTitle.setText("No jobs found. Create one with +");
                        tvJobDescription.setText("");
                        tvStatus.setText("Status: --");
                        disableAllButtons();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Database Sync Error", Toast.LENGTH_SHORT).show();
                }
            };
            jobsDatabaseRef.addValueEventListener(allJobsListener);
        }
    }

    private void detachRealtimeJobsListener() {
        if (allJobsListener != null) {
            jobsDatabaseRef.removeEventListener(allJobsListener);
            allJobsListener = null;
        }
    }

    private void updateSelectedJobStatus(String newStatus) {
        if (selectedJob == null || selectedJob.getId() == null) {
            Toast.makeText(this, "No job selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isValidTransition(currentStatus, newStatus)) {
            jobsDatabaseRef.child(selectedJob.getId()).child("status").setValue(newStatus);
            currentStatus = newStatus;
            selectedJob.setStatus(newStatus);
            updateUI(newStatus);
            saveLocalState(selectedJob.getId(), newStatus);
        } else {
            Toast.makeText(this, "Invalid status transition", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidTransition(String from, String to) {
        if (from == null) return false;
        if (from.equals("Assigned") && to.equals("Accepted")) return true;
        if (from.equals("Accepted") && to.equals("In Progress")) return true;
        if (from.equals("In Progress") && (to.equals("Completed") || to.equals("Failed"))) return true;
        return false;
    }
    private void updateUI(String status) {
        if(status == null){
            disableAllButtons();
            return;
        }
        tvStatus.setText("Status: " + status);
        btnAccept.setEnabled(status.equals("Assigned"));
        btnStart.setEnabled(status.equals("Accepted"));
        btnComplete.setEnabled(status.equals("In Progress"));
        btnFail.setEnabled(status.equals("In Progress"));
    }

    private void disableAllButtons(){
        btnAccept.setEnabled(false);
        btnStart.setEnabled(false);
        btnComplete.setEnabled(false);
        btnFail.setEnabled(false);
    }

    private void saveLocalState(String jobId, String status){
        SharedPreferences pref = getSharedPreferences("JobCache", Context.MODE_PRIVATE);
        pref.edit().putString("active_job_id", jobId).putString("job_status", status).apply();
    }

    private void logoutUser(){
        detachRealtimeJobsListener();
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}




