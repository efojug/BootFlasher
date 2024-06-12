package com.efojug.bootflasher;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.efojug.bootflasher.Utils.FileUtil;
import com.efojug.bootflasher.Utils.SystemPropertiesUtils;
import com.efojug.bootflasher.databinding.FragmentFirstBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
            binding.flashCustomPartition.setEnabled(false);
        }

        binding.unlock.setOnClickListener(v -> new MaterialAlertDialogBuilder(getContext()).setTitle("注意").setMessage("这只适用于一些使用Magisk作为Root权限管理器时，Magisk可能会自动伪装BootLoader解锁状态\n您已经被警告过了").setPositiveButton("确定", (dialogInterface, i) -> {
            binding.unlock.setEnabled(false);
            binding.unlock.setVisibility(View.GONE);
            binding.blNotice.setVisibility(View.GONE);
            binding.bootaFlash.setEnabled(true);
            binding.bootbFlash.setEnabled(true);
            binding.flashCustomPartition.setEnabled(true);
            outputLog("已解锁受限功能");

        }).setNegativeButton("我没有解锁Bootloader", (dialogInterface, i) -> {
        }).show());

        if (getRoot()) {
            binding.slot.setText("当前槽位：" + SystemPropertiesUtils.getProperty("ro.boot.slot_suffix", "未知"));
            try {
                if (Aonly) {
                    boot_a = getPartition("boot");
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText("boot分区：" + boot_a);
                    binding.bootaDump.setText("导出boot");
                    binding.bootaFlash.setText("刷入boot");
                } else {
                    boot_a = getPartition("boot_a");
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText("boot_a分区：" + boot_a);
                }
            } catch (Exception e) {
                outputLog(Aonly ? "获取boot分区失败" : "获取boot_a分区失败");
            }

            try {
                if (!Aonly) {
                    boot_b = getPartition("boot_b");
                    boot_b = boot_b.substring(0, boot_b.length() - 1);
                    binding.bootB.setText("boot_b分区：" + boot_b);
                } else {
                    binding.bootB.setVisibility(View.GONE);
                    binding.bootBOperate.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                outputLog("获取boot_b分区失败");
            }

        } else {
            Toast.makeText(getContext(), "未检测到root权限，请给予权限后重试", Toast.LENGTH_LONG).show();
            System.exit(0);
        }

        binding.bootaDump.setOnClickListener(view1 -> dumpImg(null, "boot_a"));
        binding.bootbDump.setOnClickListener(view1 -> dumpImg(null, "boot_b"));

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

        binding.flashCustomPartition.setOnClickListener(view1 -> {
            EditText CustomPartitionName = new EditText(getContext());
            CustomPartitionName.setSingleLine();
            CustomPartitionName.setHint("请填写分区名称");
            CustomPartitionName.requestFocus();
            CustomPartitionName.setFocusable(true);
            new MaterialAlertDialogBuilder(getContext()).setTitle("写入分区").setView(CustomPartitionName).setPositiveButton("确定", (dialog, which) -> {
                String content = CustomPartitionName.getText().toString();
                if (content.isBlank()) {
                    Toast.makeText(getContext(), "请填写分区名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    targetPath = getPartition(CustomPartitionName.getText().toString());
                } catch (Exception e) {
                    outputLog("获取" + CustomPartitionName.getText().toString() + "分区失败：" + e);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "选择镜像文件"), 3);
            }).setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });

        binding.dumpCustomPartition.setOnClickListener(view1 -> {
            EditText CustomPartitionName = new EditText(getContext());
            CustomPartitionName.setSingleLine();
            CustomPartitionName.setHint("请填写分区名称");
            CustomPartitionName.requestFocus();
            CustomPartitionName.setFocusable(true);
            new MaterialAlertDialogBuilder(getContext()).setTitle("导出分区").setView(CustomPartitionName).setPositiveButton("确定", (dialog, which) -> {
                String content = CustomPartitionName.getText().toString();
                if (content.isBlank()) {
                    Toast.makeText(getContext(), "请填写分区名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    dumpImg(CustomPartitionName.getText().toString(), getPartition(CustomPartitionName.getText().toString()));
                } catch (Exception e) {
                    outputLog("获取" + CustomPartitionName.getText().toString() + "分区失败：" + e);
                }
            }).setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });

        binding.confirmFlashCustomPartition.setOnClickListener(view1 -> {
            binding.confirmFlashCustomPartition.setVisibility(View.GONE);
            binding.showFlashCustomPartition.setVisibility(View.VISIBLE);
        });

        binding.listAllPartitions.setOnClickListener(view1 -> {
            int progress = 10;
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
            LinearProgressIndicator linearProgressIndicator = new LinearProgressIndicator(getContext());
            linearProgressIndicator.setProgress(progress);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("检索分区列表");
            alertDialogBuilder.setView(linearProgressIndicator);
            alertDialogBuilder.setMessage("请等待...");
            Dialog dialog = alertDialogBuilder.show();
            try {
                String str = exeCmd("ls -l /dev/block/by-name", false);
                StringBuilder tmp = new StringBuilder();
                int space = 0;
                boolean output = false;
                for (int i = 0; i < str.length(); i++) {
                    if (!output && space >= 7 && ((Character) str.charAt(i)).toString().equals(" ")) output = true;
                    else space++;
                    if (output) {
                        if (str.charAt(i) == '\n') {
                            outputLog(tmp.toString());
                            space = 0;
                            progress += 5;
                            linearProgressIndicator.setProgress(progress);
                        }
                        tmp.append(str.charAt(i));
                    }
                }
                dialog.dismiss();
            } catch (Exception e) {
                outputLog(e.toString());
                dialog.dismiss();
            }
        });
    }

    String imgPath;
    String targetPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == 1) {
                    imgPath = FileUtil.getPath(getContext(), data.getData());
                    targetPath = boot_a;
                } else if (requestCode == 2) {
                    imgPath = FileUtil.getPath(getContext(), data.getData());
                    targetPath = boot_b;
                } else if (requestCode == 3){
                    imgPath = FileUtil.getPath(getContext(), data.getData());
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
                new MaterialAlertDialogBuilder(getContext()).setTitle("没有正确获取到文件的路径").setMessage("这可能是您选择了一个已经被删除的文件\n这似乎是Android文件选择器的Bug，您可以在选择时点击左上角使用其他文件选择器来选择\n一般情况下，重启手机会刷新Android文件选择器的缓存").setPositiveButton("确定", null).show();
            }
        }

    }

    public void dumpImg(String Name, String Partition) {
        try {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            if (Objects.equals(Partition, "boot_a")) {
                exeCmd("blockdev --setrw " + boot_a, false);
                if (Aonly) {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_" + date + ".img bs=4M;sync", true);
                    outputLog("导出到/Download/boot_" + date + ".img");
                } else {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_a_" + date + ".img bs=4M;sync", true);
                    outputLog("导出到/Download/boot_a_" + date + ".img");
                }
            } else if (Objects.equals(Partition, "boot_b")) {
                exeCmd("blockdev --setrw " + boot_b, false);
                exeCmd("dd if=" + boot_b + " of=" + "/storage/emulated/0/Download/boot_b_" + date + ".img bs=4M;sync", true);
                outputLog("导出到/Download/boot_b_" + date + ".img");
            } else {
                exeCmd("blockdev --setrw " + Partition, false);
                exeCmd("dd if=" + Partition + " of=" + "/storage/emulated/0/Download/" + Name + "_" + date + ".img bs=4M;sync", true);
                outputLog("导出到/Download/" + Name + "_" + date + ".img");
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

    public String getPartition(String partitionName) throws Exception {
        String res = exeCmd("readlink -f /dev/block/by-name/" + partitionName, false);
        if (!res.contains("by-name")) return res;
        throw new Exception("无法获取正确的分区路径");
    }

    public String exeCmd(String command, boolean log) throws InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> futureResult = executor.submit(() -> {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su -c " + command);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new SequenceInputStream(process.getInputStream(), process.getErrorStream()),
                        StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (log) {
                        outputLog(line);
                    }
                    sb.append(line).append("\n");
                    if (progressDialog != null && progressDialog.isShowing()) {
                        int progress = Math.min(progressDialog.getProgress() + 10, 100);
                        progressDialog.setProgress(progress);
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("发生运行时错误：" + exitCode);
                }
                br.close();
            } catch (IOException | InterruptedException e) {
                if (process != null) {
                    process.destroy();
                }
                throw new RuntimeException("Error executing command: " + command, e);
            } finally {
                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                if (Looper.myLooper() == Looper.getMainLooper())
                    binding.logScrollview.post(() -> binding.logScrollview.fullScroll(View.FOCUS_DOWN));
            }
            return sb.toString();
        });
        executor.shutdown();
        if (log) outputLog("完成");
        return futureResult.get();
    }
}