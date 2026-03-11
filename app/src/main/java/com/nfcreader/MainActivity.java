package com.nfcreader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "NFCCardReader";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private ImageView ivNfcIcon;
    private ImageView ivNfcWave;
    private TextView tvStatus;
    private TextView tvInstruction;
    private CardView cardScanArea;
    private LinearLayout layoutNoNfc;
    private LinearLayout layoutNfcDisabled;
    private RecyclerView rvHistory;
    private TextView tvHistoryEmpty;
    private FloatingActionButton fabClear;

    private CardHistoryAdapter historyAdapter;
    private List<CardInfo> cardHistory = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNfc();
        setupRecyclerView();
        handleIntent(getIntent());
    }

    private void initViews() {
        ivNfcIcon = findViewById(R.id.iv_nfc_icon);
        ivNfcWave = findViewById(R.id.iv_nfc_wave);
        tvStatus = findViewById(R.id.tv_status);
        tvInstruction = findViewById(R.id.tv_instruction);
        cardScanArea = findViewById(R.id.card_scan_area);
        layoutNoNfc = findViewById(R.id.layout_no_nfc);
        layoutNfcDisabled = findViewById(R.id.layout_nfc_disabled);
        rvHistory = findViewById(R.id.rv_history);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        fabClear = findViewById(R.id.fab_clear);

        fabClear.setOnClickListener(v -> clearHistory());
    }

    private void setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            // Device does not support NFC
            cardScanArea.setVisibility(View.GONE);
            layoutNoNfc.setVisibility(View.VISIBLE);
            return;
        }

        // Setup pending intent for NFC foreground dispatch
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        // Setup intent filters
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{tagDetected, ndefDetected, techDetected};

        techListsArray = new String[][]{
                new String[]{IsoDep.class.getName()},
                new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcF.class.getName()},
                new String[]{NfcV.class.getName()},
                new String[]{MifareClassic.class.getName()},
                new String[]{MifareUltralight.class.getName()}
        };
    }

    private void setupRecyclerView() {
        historyAdapter = new CardHistoryAdapter(cardHistory, this::onCardHistoryItemClick);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                layoutNfcDisabled.setVisibility(View.VISIBLE);
                cardScanArea.setVisibility(View.GONE);
            } else {
                layoutNfcDisabled.setVisibility(View.GONE);
                cardScanArea.setVisibility(View.VISIBLE);
                nfcAdapter.enableForegroundDispatch(
                        this, pendingIntent, intentFiltersArray, techListsArray
                );
                startScanAnimation();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        stopScanAnimation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                processTag(tag);
            }
        }
    }

    private void processTag(Tag tag) {
        isScanning = true;
        showScanningState();

        // Process tag in background thread
        new Thread(() -> {
            CardInfo cardInfo = NfcTagProcessor.processTag(tag);

            handler.post(() -> {
                isScanning = false;
                showCardResult(cardInfo);
                addToHistory(cardInfo);
            });
        }).start();
    }

    private void showScanningState() {
        tvStatus.setText("Membaca kartu...");
        tvInstruction.setText("Tahan kartu di posisinya");
        ivNfcIcon.setImageResource(R.drawable.ic_nfc_reading);
    }

    private void showCardResult(CardInfo cardInfo) {
        tvStatus.setText("Kartu Terdeteksi! ✓");
        tvInstruction.setText("Tempelkan kartu lain untuk membaca");
        ivNfcIcon.setImageResource(R.drawable.ic_nfc_success);

        // Show toast
        Toast.makeText(this, "Kartu berhasil dibaca!", Toast.LENGTH_SHORT).show();

        // Reset after delay
        handler.postDelayed(() -> {
            tvStatus.setText("Siap Membaca");
            tvInstruction.setText("Tempelkan kartu akses ke bagian belakang ponsel");
            ivNfcIcon.setImageResource(R.drawable.ic_nfc_scan);
        }, 3000);
    }

    private void addToHistory(CardInfo cardInfo) {
        cardHistory.add(0, cardInfo);
        historyAdapter.notifyItemInserted(0);
        rvHistory.scrollToPosition(0);

        // Update empty state
        tvHistoryEmpty.setVisibility(cardHistory.isEmpty() ? View.VISIBLE : View.GONE);
        fabClear.setVisibility(cardHistory.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void clearHistory() {
        int size = cardHistory.size();
        cardHistory.clear();
        historyAdapter.notifyItemRangeRemoved(0, size);
        tvHistoryEmpty.setVisibility(View.VISIBLE);
        fabClear.setVisibility(View.GONE);
        Toast.makeText(this, "Riwayat dihapus", Toast.LENGTH_SHORT).show();
    }

    private void onCardHistoryItemClick(CardInfo cardInfo) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(CardDetailActivity.EXTRA_CARD_INFO, cardInfo);
        startActivity(intent);
    }

    private void startScanAnimation() {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        ivNfcWave.startAnimation(pulse);
    }

    private void stopScanAnimation() {
        ivNfcWave.clearAnimation();
    }
}
