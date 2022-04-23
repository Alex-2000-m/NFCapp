package com.example.nfctest2;


import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends Activity {
    private CheckBox      mWriteData;
    private CheckBox      mCopyData;
    private CheckBox      mStorageData;
    private CheckBox      mReadData;
    private NfcAdapter    mNfcAdapter;
    private PendingIntent mPendingIntent;
    byte[][][] storageTagData = new byte[16][4][];
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取默认的NFC控制器
        mWriteData = (CheckBox) findViewById(R.id.checkbox_write);
//        mCopyData = (CheckBox) findViewById(R.id.checkbox_copy);
        mStorageData = (CheckBox) findViewById(R.id.checkbox_storage);
        mReadData = (CheckBox) findViewById(R.id.checkbox_read);
        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()), 0);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                    null);
    }

    @Override
    public void onNewIntent(Intent intent) {

        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        String[] techList = tag.getTechList();
        boolean haveMifareUltralight = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareClassic") >= 0) {
                haveMifareUltralight = true;
                break;
            }
        }
        if (!haveMifareUltralight) {
            Toast.makeText(this, "不支持MifareClassic", Toast.LENGTH_LONG).show();
            return;
        }
        if (mWriteData.isChecked()) {
            writeTag(tag);
            String data = readTag(tag);
            if (data != null) {
                Log.i(data, "ouput");
                Toast.makeText(this, data, Toast.LENGTH_LONG).show();
            }
        }
        if(mStorageData.isChecked()){
            storageTag(tag);
            String data = storageTag(tag);
            if (data != null) {
                Log.i(data, "ouput");
                Toast.makeText(this, data, Toast.LENGTH_LONG).show();
            }
        }
//        if (mCopyData.isChecked()){
//            writeTag2(tag);
//            String data2 = writeTag2(tag);
//            if (data2 != null) {
//                Log.i(data2, "ouput");
//                Toast.makeText(this, data2, Toast.LENGTH_LONG).show();
//            }
//        }
        if (mReadData.isChecked()) {
            String data = readTag(tag);
            if (data != null) {
                Log.i(data, "ouput");
                Toast.makeText(this, data, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);

    }

    public String storageTag(Tag tag){
        MifareClassic mfc = MifareClassic.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        //读取TAG

        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        storageTagData[j][i]=data;
                        if(storageTagData[j][i]==data) {
                            metaInfo += "Block " + bIndex + " : "
                                    + "存储成功！"+ "\n";
                            bIndex++;
                        }
                        else {
                            Log.d("storageTagData","null!");
                        }
                    }
                    Log.d("???",""+storageTagData[0][0]);
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            return metaInfo;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;
    }

    public void writeTag(Tag tag) {

        MifareClassic mfc = MifareClassic.get(tag);
        Log.d("writeTag","writeTag方法被调用了！");

        try {
            mfc.connect();
            boolean auth = false;
            short sectorAddress = 0;
            auth = mfc.authenticateSectorWithKeyA(sectorAddress,
                    MifareClassic.KEY_DEFAULT);
            if (auth) {
                // 扇区的最后一个块用于KeyA，KeyB不能被覆盖
                //blockindex是要写入的目标块的序号
                byte[] temp=storageTagData[0][0];
                mfc.writeBlock(0,temp);
                mfc.close();
                Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    public String writeTag2(Tag tag) {
//
//        MifareClassic mfc = MifareClassic.get(tag);
//        Log.d("writeTag2","writeTag2方法被调用了！");
//        for (String tech : tag.getTechList()) {
//            System.out.println(tech);
//        }
//        try {
//            String metaInfo2 = "";
//            mfc.connect();
//            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
//            Log.d("sectorCount",""+sectorCount);
//            boolean auth = false;
//
//            for (int j = 0; j < sectorCount; j++) {
//                //Authenticate a sector with key A.
//                auth = mfc.authenticateSectorWithKeyA(j,
//                        MifareClassic.KEY_DEFAULT);
//                int bCount;
//                int bIndex;
//                if (auth) {
//                    metaInfo2 += "Sector " + j + ":验证成功\n";
//                    // 读取扇区中的块
//                    bCount = mfc.getBlockCountInSector(j);
//                    bIndex = mfc.sectorToBlock(j);
//                    for (int i = 0; i < bCount; i++) {
//                        mfc.writeBlock(i,storageTagData[j][i]);
//                        if(mfc.readBlock(i)==storageTagData[j][i]) {
//                            metaInfo2 += "Block " + bIndex + " : "
//                                    + "写入成功！"+ "\n";
//                            bIndex++;
//                        };
//                    }
//                } else {
//                    metaInfo2 += "Sector " + j + ":验证失败\n";
//                }
//            }
//            return  metaInfo2;
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            try {
//                mfc.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    public String readTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        //读取TAG

        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            return metaInfo;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;

    }
}