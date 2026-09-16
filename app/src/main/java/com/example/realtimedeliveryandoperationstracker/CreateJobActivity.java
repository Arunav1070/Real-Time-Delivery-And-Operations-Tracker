package com.example.realtimedeliveryandoperationstracker;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateJobActivity extends AppCompatActivity {

    private EditText etJobTitle, etJobDescription;
    private Button btnSubmitJob;
    private DatabaseReference mDatabase;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        etJobTitle = findViewById(R.id.etJobTitle);
        etJobDescription = findViewById(R.id.etJobDescription);
        btnSubmitJob = findViewById(R.id.btnSubmitJob);

        mDatabase = FirebaseDatabase.getInstance("https://realtimedelivery-46afd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("jobs");

        btnSubmitJob.setOnClickListener(v -> {
            if (!isSubmitting) {
                createJob();
            }
        });
    }

    private void createJob() {
        String title = etJobTitle.getText().toString().trim();
        String description = etJobDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a job title", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitting = true;
        btnSubmitJob.setEnabled(false);

        String jobId = mDatabase.push().getKey();
        if (jobId == null) {
            isSubmitting = false;
            btnSubmitJob.setEnabled(true);
            Toast.makeText(this, "Error generating job ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Job newJob = new Job(jobId, title, description, "Assigned");

        mDatabase.child(jobId).setValue(newJob).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                syncToRestApi(newJob);
            } else {
                isSubmitting = false;
                btnSubmitJob.setEnabled(true);
                Toast.makeText(this, "Failed to create job in Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncToRestApi(Job job) {
        ApiClient.JobApiService apiService = ApiClient.getClient().create(ApiClient.JobApiService.class);
        apiService.createJob(job).enqueue(new Callback<Job>() {
            @Override
            public void onResponse(Call<Job> call, Response<Job> response) {
                Toast.makeText(CreateJobActivity.this, "Job Created & Synced Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Job> call, Throwable t) {
                Toast.makeText(CreateJobActivity.this, "Job Saved to Firebase (REST API offline)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}



