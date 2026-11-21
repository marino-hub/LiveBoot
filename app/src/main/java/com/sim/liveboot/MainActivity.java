package com.sim.liveboot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private TextView consoleText;
    private ScrollView scrollView;
    private Button rootButton;
    private LinearLayout promptLayout;
    private EditText cmdInput;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private int count = 0;

    private final String[] FOLDERS = {
        "/storage/emulated/0", "/storage/emulated", "/storage",
        "/data/media/0", "/data/data", "/data",
        "/system/priv-app", "/system/bin", "/system",
        "/vendor", "/boot", "/"
    };

    private final String[] TAGS = {"ActivityManager", "SystemServer", "PackageManager", "Zygote64", "SurfaceFlinger", "Magisk", "SELinux", "libprocessgroup", "WindowManager", "AudioFlinger", "dex2oat64", "art", "libc", "vold", "installd"};
    private final String[] MSGS = {
        "Start proc com.android.systemui for service",
        "Preloading classes and shared libraries...",
        "Loaded 14820 classes in 312ms",
        "Granting root permission to uid=10248 (su daemon active)",
        "Mounting /system_root on /system (ro,seclabel,relatime)",
        "type=1400 audit(0.0:42): avc: granted { read write } for pid=1420 comm=magiskd",
        "Verifying boot signature state=ORANGE (unlocked)",
        "Starting service zygote64...",
        "magiskd: * Running post-fs-data.d scripts (uid=0)",
        "magiskd: * Loading modules from /data/adb/modules",
        "vold: Found primary storage at /dev/block/sda34"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.BLACK);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 20, 20, 20);

        consoleText = new TextView(this);
        consoleText.setTypeface(Typeface.MONOSPACE);
        consoleText.setTextSize(10.5f);
        consoleText.setTextColor(Color.parseColor("#CCCCCC"));
        container.addView(consoleText);

        rootButton = new Button(this);
        rootButton.setText("ROOT");
        rootButton.setTextColor(Color.WHITE);
        rootButton.setBackgroundColor(Color.parseColor("#1565C0"));
        rootButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        rootButton.setTextSize(14f);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 24, 0, 24);
        rootButton.setLayoutParams(btnParams);
        rootButton.setVisibility(View.GONE);
        rootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootButton.setVisibility(View.GONE);
                startLiveBootLogs();
            }
        });
        container.addView(rootButton);

        promptLayout = new LinearLayout(this);
        promptLayout.setOrientation(LinearLayout.HORIZONTAL);
        promptLayout.setVisibility(View.GONE);

        TextView promptLabel = new TextView(this);
        promptLabel.setTypeface(Typeface.MONOSPACE);
        promptLabel.setTextSize(11f);
        promptLabel.setTextColor(Color.parseColor("#42A5F5"));
        promptLabel.setText("root@localhost:~# ");
        promptLayout.addView(promptLabel);

        cmdInput = new EditText(this);
        cmdInput.setTypeface(Typeface.MONOSPACE);
        cmdInput.setTextSize(11f);
        cmdInput.setTextColor(Color.WHITE);
        cmdInput.setBackground(null);
        cmdInput.setSingleLine(true);
        cmdInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        cmdInput.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        promptLayout.addView(cmdInput);

        container.addView(promptLayout);
        scrollView.addView(container);
        setContentView(scrollView);

        cmdInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String cmd = cmdInput.getText().toString().trim();
                    cmdInput.setText("");
                    appendColorLine("root@localhost:~# " + cmd, "#42A5F5");
                    if (cmd.equals("whoami")) appendColorLine("root", "#CCCCCC");
                    else if (cmd.equals("id")) appendColorLine("uid=0(root) gid=0(root) context=u:r:magisk:s0", "#CCCCCC");
                    else if (cmd.equals("exit")) finish();
                    else if (!cmd.isEmpty()) appendColorLine("[OK] Executed as uid=0: " + cmd, "#CCCCCC");
                    return true;
                }
                return false;
            }
        });

        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("LiveBoot")
            .setMessage("Introducir comandos de administrador a esta aplicacion")
            .setCancelable(false)
            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    grantFolderPermissions(0);
                }
            })
            .show();
    }

    private void grantFolderPermissions(final int index) {
        if (Integer.compare(index, FOLDERS.length) == -1) {
            appendColorLine("Otorgando permisos a " + FOLDERS[index] + " ...", "#4DD0E1");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    grantFolderPermissions(index + 1);
                }
            }, 220);
        } else {
            appendColorLine("\n[+] Permisos otorgados (uid=0/root).", "#42A5F5");
            rootButton.setVisibility(View.VISIBLE);
            scrollDown();
        }
    }

    private void scrollDown() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void appendColorLine(String text, String hexColor) {
        SpannableString span = new SpannableString(text + "\n");
        span.setSpan(new ForegroundColorSpan(Color.parseColor(hexColor)), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        consoleText.append(span);
        scrollDown();
    }

    private void startLiveBootLogs() {
        appendColorLine("\n[    0.000000] Booting Linux on physical CPU 0x0000000000 [0x412fd050]", "#FFFFFF");
        appendColorLine("[    0.510332] magiskinit: Magisk v28.1 (28100) - LiveBoot surface attached", "#42A5F5");

        Runnable logRunnable = new Runnable() {
            @Override
            public void run() {
                if (Integer.compare(count, 1200) == -1) {
                    String ts = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                    int pid = 100 + random.nextInt(4500);
                    int tid = pid + random.nextInt(60);
                    String tag = TAGS[random.nextInt(TAGS.length)];
                    String msg = MSGS[random.nextInt(MSGS.length)];
                    int r = random.nextInt(20);

                    if (Integer.compare(r, 14) == -1) {
                        appendColorLine(ts + " " + pid + " " + tid + " I " + tag + ": " + msg, "#CCCCCC");
                    } else if (Integer.compare(r, 16) == -1) {
                        appendColorLine(ts + " " + pid + " " + tid + " D " + tag + ": " + msg, "#42A5F5");
                    } else if (Integer.compare(r, 18) == -1) {
                        appendColorLine(ts + " " + pid + " " + tid + " D " + tag + ": " + msg, "#4DD0E1");
                    } else {
                        appendColorLine(ts + " " + pid + " " + tid + " E " + tag + ": set_timerslack_ns write failed: Operation not permitted", "#FF5252");
                    }
                    count++;
                    handler.postDelayed(this, 12);
                } else {
                    appendColorLine("\n[LiveBoot] sys.boot_completed=1 : Root environment ready (uid=0/root).\n", "#4DD0E1");
                    promptLayout.setVisibility(View.VISIBLE);
                    cmdInput.requestFocus();
                }
            }
        };
        handler.postDelayed(logRunnable, 200);
    }
}
