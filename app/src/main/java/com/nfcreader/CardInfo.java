package com.nfcreader;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CardInfo implements Serializable {

    public enum CardType {
        MIFARE_CLASSIC,
        MIFARE_ULTRALIGHT,
        ISO_DEP,
        NFC_A,
        NFC_B,
        NFC_F,
        NFC_V,
        UNKNOWN
    }

    private String uid;
    private String uidFormatted;
    private CardType cardType;
    private String cardTypeName;
    private String technology;
    private long atqa;
    private int sak;
    private String manufacturer;
    private String timestamp;
    private String rawData;
    private int sector;
    private String additionalInfo;

    // For display
    private String cardLabel;

    public CardInfo() {
        this.timestamp = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss", Locale.getDefault()
        ).format(new Date());
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUidFormatted() { return uidFormatted; }
    public void setUidFormatted(String uidFormatted) { this.uidFormatted = uidFormatted; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public long getAtqa() { return atqa; }
    public void setAtqa(long atqa) { this.atqa = atqa; }

    public int getSak() { return sak; }
    public void setSak(int sak) { this.sak = sak; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public int getSector() { return sector; }
    public void setSector(int sector) { this.sector = sector; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getCardLabel() { return cardLabel; }
    public void setCardLabel(String cardLabel) { this.cardLabel = cardLabel; }

    /**
     * Get color resource based on card type
     */
    public int getCardColorRes() {
        switch (cardType) {
            case MIFARE_CLASSIC: return R.color.card_mifare_classic;
            case MIFARE_ULTRALIGHT: return R.color.card_mifare_ultralight;
            case ISO_DEP: return R.color.card_iso_dep;
            case NFC_A: return R.color.card_nfc_a;
            case NFC_B: return R.color.card_nfc_b;
            case NFC_F: return R.color.card_nfc_f;
            case NFC_V: return R.color.card_nfc_v;
            default: return R.color.card_unknown;
        }
    }

    /**
     * Get icon resource based on card type
     */
    public int getCardIconRes() {
        switch (cardType) {
            case MIFARE_CLASSIC:
            case MIFARE_ULTRALIGHT:
                return R.drawable.ic_card_access;
            case ISO_DEP:
                return R.drawable.ic_card_emv;
            default:
                return R.drawable.ic_card_generic;
        }
    }
}
