package com.nt.service_nexus_android;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.nt.service_nexus_android.data.local.SessionManager;
import com.nt.service_nexus_android.data.models.Role;
import com.nt.service_nexus_android.data.models.User;
import com.nt.service_nexus_android.ui.auth.LoginActivity;
import com.nt.service_nexus_android.ui.customer.CustomerMainActivity;
import com.nt.service_nexus_android.ui.worker.WorkerDashboardActivity;
import org.osmdroid.config.Configuration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());

        SessionManager sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            User user = sessionManager.getUser();
            if (user != null && user.getRole() == Role.WORKER) {
                startActivity(new Intent(this, WorkerDashboardActivity.class));
            } else {
                startActivity(new Intent(this, CustomerMainActivity.class));
            }
        }
        finish();
    }
}
