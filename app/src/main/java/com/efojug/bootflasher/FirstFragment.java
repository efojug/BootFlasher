package com.efojug.bootflasher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.efojug.bootflasher.Utils.SystemPropertiesUtils;
import com.efojug.bootflasher.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    String boot_a;
    String boot_b;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getRoot()) {
            binding.root.setVisibility(View.GONE);
            binding.slot.setVisibility(View.VISIBLE);
            binding.slot.setText("当前槽位：" + SystemPropertiesUtils.getProperty("ro.boot.slot_suffix", ""));
            try {
                boot_a = fsh("ls -l /dev/block/by-name/boot_a").split("-> ")[1];
                binding.bootA.setText("boot_a分区：" + boot_a);
            } catch (Exception e) {
                binding.log.setText(binding.log.getText() + Date() + " 获取boot_a分区失败 " + e + "\n");
                binding.bootA.setText("失败");
            }
            try {
                boot_b = fsh("ls -l /dev/block/by-name/boot_b").split("-> ")[1];
                binding.bootB.setText("boot_b分区：" + boot_b);
            } catch (Exception e) {
                binding.log.setText(binding.log.getText() + Date() + " 获取boot_b失败 " + e + "\n");
                binding.bootB.setText("失败");
            }
        } else {
            System.exit(0);
        }

        binding.bootaDump.setOnClickListener(view1 -> dumpImg("a"));
        binding.bootbDump.setOnClickListener(view1 -> dumpImg("b"));

        binding.flash.setOnClickListener(view1 -> {
            flashImg(imgPath, targetPath);
            Snackbar.make(view1, "刷入中", Snackbar.LENGTH_LONG).show();
            binding.flash.setEnabled(false);
        });

        binding.confirm.setOnCheckedChangeListener((compoundButton, b) -> {
            binding.flash.setEnabled(binding.confirm.isChecked());
        });

        binding.bootaFlash.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "选择您要刷入到boot_a的镜像文件"), 1);
        });

        binding.bootbFlash.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "选择您要刷入到boot_b的镜像文件"), 2);
        });
    }

    String imgPath;
    String targetPath;

    public String Date() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                try {
                    imgPath = "/storage/emulated/0/" + data.getData().getPath().split(":")[1];
                    targetPath = boot_a;
                    binding.confirm.setEnabled(true);
                } catch (Exception e) {
                    binding.log.setText(binding.log.getText() + Date() + " 获取路径失败 " + e + "\n");
                }
            }
            if (requestCode == 2) {
                try {
                    imgPath = "/storage/emulated/0/" + data.getData().getPath().split(":")[1];
                    targetPath = boot_b;
                    binding.confirm.setEnabled(true);
                } catch (Exception e) {
                    binding.log.setText(binding.log.getText() + Date() + " 获取路径失败 " + e + "\n");
                }
            }
            binding.command.setText(imgPath + " -> " + targetPath);
            binding.log.setText(binding.log.getText() + Date() + " " + imgPath + " -> " + targetPath + "\n");
        }
    }

    public void dumpImg(String  boot_partition) {
        try {
            if (Objects.equals(boot_partition, "a")) {
                fsh("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_a_" + (new Random().nextInt(900000) + 100000) + ";sync");
                binding.log.setText(binding.log.getText() + Date() + " 已导出到/Download " + "\n");
            }
            if (Objects.equals(boot_partition, "b")) {
                fsh("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_b_" + (new Random().nextInt(900000) + 100000) + ";sync");
                binding.log.setText(binding.log.getText() + Date() + " 已导出到/Download " + "\n");
            }
        } catch (Exception e) {
            binding.log.setText(binding.log.getText() + Date() + " 导出失败 " + e + "\n");
        }
    }

    public void flashImg(String imgPath, String targetPath) {
        try {
            fsh("dd if=" + imgPath + " of=" + targetPath + ";sync");
        } catch (Exception e) {
            binding.log.setText(binding.log.getText() + Date() + " 刷入失败：" + e + "\n");
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
            binding.log.setText(binding.log.getText() + Date() + " " + i + "\n");
            return false;
        }
    }

    public String fsh(String command) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        OutputStream outputStream = process.getOutputStream();
        outputStream.write(command.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
        int exitCode = process.waitFor();
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        reader.close();
        binding.log.setText(binding.log.getText() + Date() + " " + output + "\n");
        return output.toString();
    }
}