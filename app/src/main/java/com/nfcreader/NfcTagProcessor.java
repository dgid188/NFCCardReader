package com.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

public class NfcTagProcessor {

    private static final String TAG = "NfcTagProcessor";

    public static CardInfo processTag(Tag tag) {
        CardInfo cardInfo = new CardInfo();

        // Get UID
        byte[] id = tag.getId();
        cardInfo.setUid(bytesToHex(id));
        cardInfo.setUidFormatted(formatUid(id));

        // Get tech list
        String[] techList = tag.getTechList();
        StringBuilder techBuilder = new StringBuilder();
        for (String tech : techList) {
            if (techBuilder.length() > 0) techBuilder.append(", ");
            techBuilder.append(tech.substring(tech.lastIndexOf('.') + 1));
        }
        cardInfo.setTechnology(techBuilder.toString());

        // Process based on technology
        if (hasTech(techList, MifareClassic.class.getName())) {
            processMifareClassic(tag, cardInfo);
        } else if (hasTech(techList, MifareUltralight.class.getName())) {
            processMifareUltralight(tag, cardInfo);
        } else if (hasTech(techList, IsoDep.class.getName())) {
            processIsoDep(tag, cardInfo);
        } else if (hasTech(techList, NfcA.class.getName())) {
            processNfcA(tag, cardInfo);
        } else if (hasTech(techList, NfcB.class.getName())) {
            processNfcB(tag, cardInfo);
        } else if (hasTech(techList, NfcF.class.getName())) {
            processNfcF(tag, cardInfo);
        } else if (hasTech(techList, NfcV.class.getName())) {
            processNfcV(tag, cardInfo);
        } else {
            cardInfo.setCardType(CardInfo.CardType.UNKNOWN);
            cardInfo.setCardTypeName("Unknown Card");
            cardInfo.setManufacturer(getManufacturerFromUID(id));
        }

        // Set card label
        cardInfo.setCardLabel(generateCardLabel(cardInfo));

        return cardInfo;
    }

    private static void processMifareClassic(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.MIFARE_CLASSIC);

        MifareClassic mifare = MifareClassic.get(tag);
        if (mifare == null) {
            cardInfo.setCardTypeName("MIFARE Classic");
            return;
        }

        int type = mifare.getType();
        switch (type) {
            case MifareClassic.TYPE_CLASSIC:
                cardInfo.setCardTypeName("MIFARE Classic");
                break;
            case MifareClassic.TYPE_PLUS:
                cardInfo.setCardTypeName("MIFARE Plus");
                break;
            case MifareClassic.TYPE_PRO:
                cardInfo.setCardTypeName("MIFARE Pro");
                break;
            default:
                cardInfo.setCardTypeName("MIFARE Classic");
        }

        int size = mifare.getSize();
        int sectors = mifare.getSectorCount();
        int blocks = mifare.getBlockCount();

        cardInfo.setSector(sectors);
        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        StringBuilder sb = new StringBuilder();
        sb.append("Memory Size: ").append(size).append(" bytes\n");
        sb.append("Sectors: ").append(sectors).append("\n");
        sb.append("Blocks: ").append(blocks).append("\n");

        // Try to read sector 0 with default key
        try {
            mifare.connect();
            boolean auth = mifare.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT);
            if (auth) {
                sb.append("\n[Sector 0 Data]\n");
                for (int block = 0; block < mifare.getBlockCountInSector(0); block++) {
                    int blockIndex = mifare.sectorToBlock(0) + block;
                    byte[] data = mifare.readBlock(blockIndex);
                    sb.append("Block ").append(blockIndex).append(": ")
                            .append(bytesToHex(data)).append("\n");
                }
            } else {
                sb.append("\nSektor dienkripsi (akses terbatas)");
            }
            mifare.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading MIFARE Classic", e);
            sb.append("\nError membaca data kartu");
        }

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("Kartu akses MIFARE Classic - Kartu kontrol akses paling umum digunakan");
    }

    private static void processMifareUltralight(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.MIFARE_ULTRALIGHT);

        MifareUltralight mifare = MifareUltralight.get(tag);
        if (mifare == null) {
            cardInfo.setCardTypeName("MIFARE Ultralight");
            return;
        }

        int type = mifare.getType();
        switch (type) {
            case MifareUltralight.TYPE_ULTRALIGHT:
                cardInfo.setCardTypeName("MIFARE Ultralight");
                break;
            case MifareUltralight.TYPE_ULTRALIGHT_C:
                cardInfo.setCardTypeName("MIFARE Ultralight C");
                break;
            default:
                cardInfo.setCardTypeName("MIFARE Ultralight");
        }

        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        StringBuilder sb = new StringBuilder();
        try {
            mifare.connect();
            // Read pages 0-3 (manufacturer data)
            sb.append("[Data Halaman]\n");
            for (int page = 0; page < 4; page++) {
                byte[] data = mifare.readPages(page);
                if (data != null && data.length >= 4) {
                    sb.append("Halaman ").append(page).append(": ")
                            .append(bytesToHex(Arrays.copyOf(data, 4))).append("\n");
                }
            }
            mifare.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading MIFARE Ultralight", e);
            sb.append("Error membaca data kartu");
        }

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("MIFARE Ultralight - Kartu tiket / kartu akses sederhana");
    }

    private static void processIsoDep(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.ISO_DEP);
        cardInfo.setCardTypeName("ISO 14443-4 (ISO-DEP)");
        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) return;

        StringBuilder sb = new StringBuilder();
        try {
            isoDep.connect();
            byte[] hiLayerResponse = isoDep.getHiLayerResponse();
            byte[] historicalBytes = isoDep.getHistoricalBytes();

            if (historicalBytes != null) {
                sb.append("Historical Bytes: ").append(bytesToHex(historicalBytes)).append("\n");
            }
            if (hiLayerResponse != null) {
                sb.append("HiLayer Response: ").append(bytesToHex(hiLayerResponse)).append("\n");
            }

            sb.append("Max Transceive Length: ").append(isoDep.getMaxTransceiveLength()).append(" bytes\n");
            isoDep.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading ISO-DEP", e);
        }

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("ISO-DEP - Kartu EMV/e-Money atau kartu akses modern");
    }

    private static void processNfcA(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.NFC_A);
        cardInfo.setCardTypeName("NFC-A (ISO 14443-3A)");
        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        NfcA nfcA = NfcA.get(tag);
        if (nfcA == null) return;

        byte[] atqa = nfcA.getAtqa();
        short sak = nfcA.getSak();

        cardInfo.setAtqa(bytesToLong(atqa));
        cardInfo.setSak(sak);

        StringBuilder sb = new StringBuilder();
        sb.append("ATQA: ").append(bytesToHex(atqa)).append("\n");
        sb.append("SAK: ").append(String.format("0x%02X", sak)).append("\n");
        sb.append("Max Transceive Length: ").append(nfcA.getMaxTransceiveLength()).append(" bytes\n");

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo(interpretNfcASak(sak));
    }

    private static void processNfcB(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.NFC_B);
        cardInfo.setCardTypeName("NFC-B (ISO 14443-3B)");
        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        NfcB nfcB = NfcB.get(tag);
        if (nfcB == null) return;

        byte[] appData = nfcB.getApplicationData();
        byte[] protInfo = nfcB.getProtocolInfo();

        StringBuilder sb = new StringBuilder();
        if (appData != null) sb.append("App Data: ").append(bytesToHex(appData)).append("\n");
        if (protInfo != null) sb.append("Protocol Info: ").append(bytesToHex(protInfo)).append("\n");

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("NFC-B - Kartu identitas atau kartu akses jenis B");
    }

    private static void processNfcF(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.NFC_F);
        cardInfo.setCardTypeName("NFC-F (FeliCa)");

        NfcF nfcF = NfcF.get(tag);
        if (nfcF == null) return;

        byte[] manufacturer = nfcF.getManufacturer();
        byte[] systemCode = nfcF.getSystemCode();

        cardInfo.setManufacturer("Sony (FeliCa)");

        StringBuilder sb = new StringBuilder();
        if (manufacturer != null) sb.append("Manufacturer: ").append(bytesToHex(manufacturer)).append("\n");
        if (systemCode != null) sb.append("System Code: ").append(bytesToHex(systemCode)).append("\n");

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("FeliCa - Kartu akses / transportasi (umum di Jepang)");
    }

    private static void processNfcV(Tag tag, CardInfo cardInfo) {
        cardInfo.setCardType(CardInfo.CardType.NFC_V);
        cardInfo.setCardTypeName("NFC-V (ISO 15693)");
        cardInfo.setManufacturer(getManufacturerFromUID(tag.getId()));

        NfcV nfcV = NfcV.get(tag);
        if (nfcV == null) return;

        byte dsfId = nfcV.getDsfId();
        byte responseFlags = nfcV.getResponseFlags();

        StringBuilder sb = new StringBuilder();
        sb.append("DSF ID: ").append(String.format("0x%02X", dsfId)).append("\n");
        sb.append("Response Flags: ").append(String.format("0x%02X", responseFlags)).append("\n");

        cardInfo.setRawData(sb.toString());
        cardInfo.setAdditionalInfo("ISO 15693 - Kartu jarak jauh (hingga 1 meter)");
    }

    // ===== HELPER METHODS =====

    private static boolean hasTech(String[] techList, String tech) {
        for (String t : techList) {
            if (t.equals(tech)) return true;
        }
        return false;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static String formatUid(byte[] uid) {
        if (uid == null || uid.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uid.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", uid[i]));
        }
        return sb.toString();
    }

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    private static String getManufacturerFromUID(byte[] uid) {
        if (uid == null || uid.length == 0) return "Unknown";

        // NXP Semiconductors
        if (uid[0] == 0x04) return "NXP Semiconductors";
        // STMicroelectronics
        if (uid[0] == (byte) 0xE0 || uid[0] == (byte) 0xE2) return "STMicroelectronics";
        // Texas Instruments
        if ((uid[0] & 0xFF) == 0xE0) return "Texas Instruments";

        // UID length hints
        switch (uid.length) {
            case 4: return "NXP Semiconductors";
            case 7: return "NXP Semiconductors";
            case 10: return "NXP Semiconductors";
            default: return "Unknown";
        }
    }

    private static String interpretNfcASak(short sak) {
        int s = sak & 0xFF;
        if ((s & 0x20) != 0) return "Kartu smart dengan ISO 14443-4 layer";
        if ((s & 0x40) != 0) return "Kartu dengan ISO 18092 (NFC-DEP)";
        if (s == 0x08 || s == 0x88 || s == 0x28) return "MIFARE Classic 1K - Kartu akses umum";
        if (s == 0x18) return "MIFARE Classic 4K - Kartu akses kapasitas besar";
        if (s == 0x00) return "MIFARE Ultralight / NTAG - Kartu tiket sederhana";
        return "Kartu akses kompatibel NFC-A";
    }

    private static String generateCardLabel(CardInfo cardInfo) {
        switch (cardInfo.getCardType()) {
            case MIFARE_CLASSIC:
                return "Kartu Akses";
            case MIFARE_ULTRALIGHT:
                return "Kartu Tiket";
            case ISO_DEP:
                return "Kartu Pintar";
            case NFC_F:
                return "Kartu FeliCa";
            case NFC_V:
                return "Kartu Jarak Jauh";
            default:
                return "Kartu NFC";
        }
    }
}
