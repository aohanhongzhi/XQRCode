/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.xqrcodedemo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageSimpleListFragment;
import com.xuexiang.xpage.utils.TitleBar;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcode.util.QRCodeAnalyzeUtils;
import com.xuexiang.xqrcodedemo.R;
import com.xuexiang.xqrcodedemo.activity.CustomCaptureActivity;
import com.xuexiang.xqrcodedemo.util.PathUtils;
import com.xuexiang.xutil.app.IntentUtils;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.xuexiang.xaop.consts.PermissionConsts.CAMERA;
import static com.xuexiang.xaop.consts.PermissionConsts.STORAGE;
import static com.xuexiang.xqrcode.XQRCode.KEY_IS_REPEATED;
import static com.xuexiang.xqrcode.XQRCode.KEY_SCAN_INTERVAL;

/**
 * 二维码扫描
 *
 * @author xuexiang
 * @since 2019/1/16 下午11:56
 */
@Page(name = "二维码扫描  XQRCode")
public class MainFragment extends XPageSimpleListFragment {
    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE = 111;
    /**
     * 选择系统图片Request Code
     */
    public static final int REQUEST_IMAGE = 112;
    /**
     * 定制化扫描界面Request Code
     */
    public static final int REQUEST_CUSTOM_SCAN = 113;

    /**
     * 初始化例子
     *
     * @param lists
     * @return
     */
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("默认扫描界面");
        lists.add("默认扫描界面(自定义主题)");
        lists.add("定制化扫描界面(单次）");
        lists.add("定制化扫描界面(多次）");
        lists.add("远程扫描界面");
        lists.add("生成二维码图片");
        lists.add("选择二维码进行解析");
        return lists;
    }

    /**
     * 条目点击
     *
     * @param position
     */
    @Override
    protected void onItemClick(int position) {
        switch (position) {
            case 0:
                startScan(ScanType.DEFAULT);
                break;
            case 1:
                startScan(ScanType.DEFAULT_Custom);
                break;
            case 2:
                startScan(ScanType.CUSTOM_SINGLE);
                break;
            case 3:
                startScan(ScanType.CUSTOM_MULTIPLE);
                break;
            case 4:
                startScan(ScanType.REMOTE);
                break;
            case 5:
                openPage(QRCodeProduceFragment.class);
                break;
            case 6:
                selectQRCode();
                break;
            default:
                break;
        }
    }

    @Permission(STORAGE)
    private void selectQRCode() {
        startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.IMAGE), REQUEST_IMAGE);
    }

    /**
     * 开启二维码扫描
     */
    @Permission(CAMERA)
    private void startScan(ScanType scanType) {
        switch (scanType) {
            case DEFAULT:
                XQRCode.startScan(this, REQUEST_CODE);
                break;
            case DEFAULT_Custom:
                CustomCaptureActivity.start(this, REQUEST_CODE, R.style.XQRCodeTheme_Custom);
                break;
            case CUSTOM_SINGLE:
                openPageForResult(CustomCaptureFragment.class, getScanParam(false, 0), REQUEST_CUSTOM_SCAN);
                break;
            case CUSTOM_MULTIPLE:
                openPage(CustomCaptureFragment.class, getScanParam(true, 1000));
                break;
            case REMOTE:
                Intent intent = new Intent(XQRCode.ACTION_DEFAULT_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    /**
     * 获取扫描参数
     *
     * @param isRepeated
     * @param scanInterval
     * @return
     */
    private Bundle getScanParam(boolean isRepeated, long scanInterval) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_IS_REPEATED, isRepeated);
        bundle.putLong(KEY_SCAN_INTERVAL, scanInterval);
        return bundle;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CUSTOM_SCAN && resultCode == RESULT_OK) {
            handleScanResult(data);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理二维码扫描结果
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            //处理扫描结果（在界面上显示）
            handleScanResult(data);
        }

        //选择系统图片并解析
        else if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                getAnalyzeQRCodeResult(uri);
            }
        }
    }

    /**
     * 进行二维码解析
     *
     * @param uri
     */
    @SuppressLint("MissingPermission")
    private void getAnalyzeQRCodeResult(Uri uri) {
        XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(getContext(), uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                ToastUtils.toast("解析结果:" + result, Toast.LENGTH_LONG);
            }

            @Override
            public void onAnalyzeFailed() {
                ToastUtils.toast("解析二维码失败", Toast.LENGTH_LONG);
            }
        });
    }


    /**
     * 处理二维码扫描结果
     *
     * @param data
     */
    private void handleScanResult(Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_SUCCESS) {
                    String result = bundle.getString(XQRCode.RESULT_DATA);
                    ToastUtils.toast("解析结果:" + result, Toast.LENGTH_LONG);
                } else if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_FAILED) {
                    ToastUtils.toast("解析二维码失败", Toast.LENGTH_LONG);
                }
            }
        }
    }

    @Override
    protected TitleBar initTitleBar() {
        return super.initTitleBar().setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickUtils.exitBy2Click();
            }
        });
    }


    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click();
        }
        return true;
    }

    /**
     * 二维码扫描类型
     */
    public enum ScanType {
        /**
         * 默认
         */
        DEFAULT,
        /**
         * 默认(修改主题）
         */
        DEFAULT_Custom,
        /**
         * 远程
         */
        REMOTE,
        /**
         * 自定义(单次）
         */
        CUSTOM_SINGLE,
        /**
         * 自定义(多次）
         */
        CUSTOM_MULTIPLE
    }


}
