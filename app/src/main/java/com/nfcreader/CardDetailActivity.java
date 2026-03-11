package com.nfcreader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class CardDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_INFO = "extra_card_info";

    private CardInfo cardInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        cardInfo = (CardInfo) getIntent().getSerializableExtra(EXTRA_CARD_INFO);
        if (cardInfo == null) {
            finish();
            return;
        }

        setupToolbar();
        populateData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Kartu");
        }

        // Set toolbar color based on card type
        int color = ContextCompat.getColor(this, cardInfo.getCardColorRes());
        toolbar.setBackgroundColor(color);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    private void populateData() {
        // Card label
        TextView tvCardLabel = findViewById(R.id.tv_card_label_detail);
        tvCardLabel.setText(cardInfo.getCardLabel());

        // Card type chip
        Chip chipCardType = findViewById(R.id.chip_card_type);
        chipCardType.setText(cardInfo.getCardTypeName());
        int color = ContextCompat.getColor(this, cardInfo.getCardColorRes());
        chipCardType.setChipBackgroundColor(
                android.content.res.ColorStateList.valueOf(color)
        );

        // UID
        TextView tvUidValue = findViewById(R.id.tv_uid_value);
        tvUidValue.setText(cardInfo.getUidFormatted());

        // UID length
        TextView tvUidLength = findViewById(R.id.tv_uid_length);
        if (cardInfo.getUid() != null) {
            int bytes = cardInfo.getUid().length() / 2;
            tvUidLength.setText(bytes + " bytes (" + bytes * 8 + " bit)");
        }

        // Technology
        TextView tvTechValue = findViewById(R.id.tv_tech_value);
        tvTechValue.setText(cardInfo.getTechnology());

        // Manufacturer
        TextView tvManufacturerValue = findViewById(R.id.tv_manufacturer_value);
        tvManufacturerValue.setText(cardInfo.getManufacturer() != null ?
                cardInfo.getManufacturer() : "Tidak diketahui");

        // Timestamp
        TextView tvTimestampValue = findViewById(R.id.tv_timestamp_value);
        tvTimestampValue.setText(cardInfo.getTimestamp());

        // Additional info
        TextView tvAdditionalInfo = findViewById(R.id.tv_additional_info);
        if (cardInfo.getAdditionalInfo() != null && !cardInfo.getAdditionalInfo().isEmpty()) {
            tvAdditionalInfo.setText(cardInfo.getAdditionalInfo());
            tvAdditionalInfo.setVisibility(View.VISIBLE);
        } else {
            tvAdditionalInfo.setVisibility(View.GONE);
        }

        // Raw data
        TextView tvRawData = findViewById(R.id.tv_raw_data);
        if (cardInfo.getRawData() != null && !cardInfo.getRawData().isEmpty()) {
            tvRawData.setText(cardInfo.getRawData());
        } else {
            tvRawData.setText("Data tidak tersedia");
        }

        // Copy UID button
        MaterialButton btnCopyUid = findViewById(R.id.btn_copy_uid);
        btnCopyUid.setOnClickListener(v -> copyToClipboard("UID Kartu", cardInfo.getUidFormatted()));

        // Copy all button
        MaterialButton btnCopyAll = findViewById(R.id.btn_copy_all);
        btnCopyAll.setOnClickListener(v -> copyToClipboard("Info Kartu NFC", buildFullInfo()));
    }

    private String buildFullInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== INFO KARTU NFC ===\n");
        sb.append("Label: ").append(cardInfo.getCardLabel()).append("\n");
        sb.append("Tipe: ").append(cardInfo.getCardTypeName()).append("\n");
        sb.append("UID: ").append(cardInfo.getUidFormatted()).append("\n");
        sb.append("Teknologi: ").append(cardInfo.getTechnology()).append("\n");
        sb.append("Produsen: ").append(cardInfo.getManufacturer()).append("\n");
        sb.append("Waktu Baca: ").append(cardInfo.getTimestamp()).append("\n");
        if (cardInfo.getRawData() != null) {
            sb.append("\n=== DATA ===\n").append(cardInfo.getRawData());
        }
        return sb.toString();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Disalin ke clipboard!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
