package com.efojug.bootflasher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.efojug.bootflasher.Utils.FileUtil;
import com.efojug.bootflasher.Utils.SystemPropertiesUtils;
import com.efojug.bootflasher.databinding.FragmentFirstBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    String boot_a;
    String boot_b;
    Boolean Aonly = false;
    private ProgressDialog progressDialog;

    public void outputLog(String log) {
        binding.log.post(() -> binding.log.setText(binding.log.getText() + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "> " + log + "\n"));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!Objects.equals(SystemPropertiesUtils.getProperty("ro.build.ab_update", ""), "true")) {
            binding.aonlyWarning.setVisibility(View.VISIBLE);
            binding.slot.setVisibility(View.GONE);
            binding.bootbDump.setEnabled(false);
            binding.bootbFlash.setEnabled(false);
            Aonly = true;
        }
        if (SystemPropertiesUtils.getProperty("ro.boot.flash.locked", "1").equals("1") || !SystemPropertiesUtils.getProperty("ro.boot.verifiedbootstate", "green").equals("orange")) {
            binding.notUnlockBootloader.setVisibility(View.VISIBLE);
            binding.blNotice.setVisibility(View.VISIBLE);
            binding.unlock.setVisibility(View.VISIBLE);
            binding.bootaFlash.setEnabled(false);
            binding.bootbFlash.setEnabled(false);
        }
        binding.unlock.setOnClickListener(v -> {
            binding.unlock.setEnabled(false);
            binding.unlock.setText("功能已解锁");
            binding.bootaFlash.setEnabled(true);
            binding.bootbFlash.setEnabled(true);
        });

        if (getRoot()) {
            binding.slot.setText("当前槽位：" + SystemPropertiesUtils.getProperty("ro.boot.slot_suffix", ""));
            try {
                if (Aonly) {
                    boot_a = exeCmd("readlink -f /dev/block/by-name/boot", false).get();
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText("boot分区：" + boot_a);
                    binding.bootaDump.setText("导出boot");
                    binding.bootaFlash.setText("刷入boot");
                } else {
                    boot_a = exeCmd("readlink -f /dev/block/by-name/boot_a", false).get();
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText("boot_a分区：" + boot_a);
                }
            } catch (Exception e) {
                outputLog("获取boot_a分区失败 " + e);
                binding.bootA.setText("失败");
            }
            try {
                if (!Aonly) {
                    boot_b = exeCmd("readlink -f /dev/block/by-name/boot_b", false).get();
                    boot_b = boot_b.substring(0, boot_b.length() - 1);
                    binding.bootB.setText("boot_b分区：" + boot_b);
                } else {
                    binding.bootB.setText("无boot_b分区");
                }

            } catch (Exception e) {
                outputLog("获取boot_b分区失败 " + e);
                binding.bootB.setText("失败");
            }
        } else {
            Toast.makeText(getContext(), "未检测到root权限，请给予权限后重试", Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        binding.bootaDump.setOnClickListener(view1 -> dumpImg("a"));
        binding.bootbDump.setOnClickListener(view1 -> dumpImg("b"));

        binding.flash.setOnClickListener(view1 -> new MaterialAlertDialogBuilder(getContext()).setTitle("确认").setMessage("您将要把\n" + imgPath + "\n刷入到\n" + targetPath + "\n请注意：此操作不可逆！").setPositiveButton("确定", (dialogInterface, i) -> {
            binding.flash.setEnabled(false);
            outputLog("开始刷写");
            flashImg(imgPath, targetPath);
        }).setNegativeButton("取消", (dialogInterface, i) -> {
        }).show());

        binding.bootaFlash.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "选择镜像文件"), 1);
        });

        binding.bootbFlash.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "选择镜像文件"), 2);
        });
    }

    String imgPath;
    String targetPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == 1) {
                    imgPath = FileUtil.getPath(getActivity().getApplicationContext(), data.getData());
                    targetPath = boot_a;
                } else if (requestCode == 2) {
                    imgPath = FileUtil.getPath(getActivity().getApplicationContext(), data.getData());
                    targetPath = boot_b;
                }
            } catch (Exception e) {
                outputLog("获取路径失败 " + e);
            }
            if (imgPath.contains("/")) {
                binding.flash.setEnabled(true);
                binding.source.setText("源：" + imgPath);
                binding.target.setText("目标：" + targetPath);
                outputLog(imgPath + " -> " + targetPath);
            } else {
                new MaterialAlertDialogBuilder(getContext()).setTitle("注意！").setMessage("没有正确获取到文件的路径\n这可能是您选择了一个已经被删除的文件\n这似乎是Android文件选择器的Bug，您可以在选择时点击左上角使用其他文件选择器来选择\n一般情况下，重启手机会刷新Android文件选择器的缓存").setPositiveButton("确定", null).show();
            }
        }

    }

    public void dumpImg(String boot_partition) {
        try {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            if (Objects.equals(boot_partition, "a")) {
                exeCmd("blockdev --setrw " + boot_a, false);
                if (Aonly) {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_" + date + ".img bs=4M;sync", true);
                    outputLog("导出到/Download/boot_" + date + ".img");
                } else {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_a_" + date + ".img bs=4M;sync", true);
                    outputLog("导出到/Download/boot_a_" + date + ".img");
                }
            } else if (Objects.equals(boot_partition, "b")) {
                exeCmd("blockdev --setrw " + boot_b, false);
                exeCmd("dd if=" + boot_b + " of=" + "/storage/emulated/0/Download/boot_b_" + date + ".img bs=4M;sync", true);
                outputLog("导出到/Download/boot_b_" + date + ".img");
            }
        } catch (Exception e) {
            outputLog("导出失败 " + e);
        }
    }

    public void flashImg(String imgPath, String targetPath) {
        try {
            exeCmd("blockdev --setrw " + targetPath, false);
            exeCmd("dd if=" + imgPath + " of=" + targetPath + " bs=4M;sync", true);
            binding.source.setText("源：未选择");
            binding.target.setText("目标：未选择");
        } catch (Exception e) {
            outputLog("刷入失败 " + e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public boolean getRoot() {
        try {
            Runtime.getRuntime().exec("su");
            return true;
        } catch (IOException i) {
            outputLog(i.toString());
            return false;
        }
    }

    public Future<String> exeCmd(String command, Boolean log) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setTitle("执行中...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        } else progressDialog.setProgressStyle(progressDialog.getProgress() + 10);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> callable = () -> {
            StringBuilder sb = new StringBuilder();
            Process process = Runtime.getRuntime().exec("su -c " + command);
            BufferedReader br = new BufferedReader(new InputStreamReader(new SequenceInputStream(process.getInputStream(), process.getErrorStream()), StandardCharsets.UTF_8));
            String s;
            while ((s = br.readLine()) != null) {
                outputLog(s);
                sb.append(s).append("\n");
                progressDialog.setProgress(Math.min(progressDialog.getProgress() + 10, 100));
            }
            process.waitFor();
            Thread.sleep(1000);
            progressDialog.dismiss();
            binding.logScrollview.post(() -> binding.logScrollview.fullScroll(View.FOCUS_DOWN));
            return sb.toString();
        };
        Future<String> futureResult = executor.submit(callable);
        executor.shutdown();
        if (log) outputLog("完成");
        return futureResult;
    }
}