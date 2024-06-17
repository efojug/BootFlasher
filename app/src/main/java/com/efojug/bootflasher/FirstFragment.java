package com.efojug.bootflasher;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.efojug.bootflasher.databinding.FragmentFirstBinding;
import com.efojug.bootflasher.utils.FileUtil;
import com.efojug.bootflasher.utils.PartitionUtil;
import com.efojug.bootflasher.utils.SystemPropertiesUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kotlin.Unit;

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
    String sourcePath;
    String targetPath;

    Vector<String> logs = new Vector<>();

    public void outputLog(String log) {
        if (logs.size() > 20) logs.remove(0);
        logs.add(new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(new Date()) + "> " + log + "\n");
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < logs.size(); i++) tmp.append(logs.get(i));
        binding.log.post(() -> binding.log.setText(tmp.toString()));
        binding.logScrollview.fullScroll(View.FOCUS_DOWN);
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.sourceFile.setText(getString(R.string.source_file, getString(R.string.not_selected)));
        binding.targetPartition.setText(getString(R.string.target_partition, getString(R.string.not_selected)));

        if (!Objects.equals(SystemPropertiesUtils.getProperty("ro.build.ab_update", "false"), "true")) {
            binding.aonlyWarning.setVisibility(View.VISIBLE);
            binding.slot.setVisibility(View.GONE);
            binding.extractBootB.setEnabled(false);
            binding.writeBootB.setEnabled(false);
            Aonly = true;
        }

        if (SystemPropertiesUtils.getProperty("ro.boot.flash.locked", "1").equals("1") || SystemPropertiesUtils.getProperty("ro.boot.verifiedbootstate", "green").equals("green")) {
            binding.notUnlockBootloader.setVisibility(View.VISIBLE);
            binding.blNotice.setVisibility(View.VISIBLE);
            binding.unlock.setVisibility(View.VISIBLE);
            binding.writeBootA.setEnabled(false);
            binding.writeBootB.setEnabled(false);
            binding.writeCustomPartition.setEnabled(false);
        }

        binding.unlock.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning)).setMessage(getString(R.string.enable_warn)).setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> {
            binding.unlock.setEnabled(false);
            binding.unlock.setVisibility(View.GONE);
            binding.blNotice.setVisibility(View.GONE);
            binding.writeBootA.setEnabled(true);
            binding.writeBootB.setEnabled(true);
            binding.writeCustomPartition.setEnabled(true);
            outputLog(getString(R.string.restrict_feature_unlocked));
        }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show());

        if (getRoot()) {
            binding.slot.setText(getString(R.string.current_slot) + SystemPropertiesUtils.getProperty("ro.boot.slot_suffix", getString(R.string.unknown)));
            try {
                if (Aonly) {
                    boot_a = getPartition("boot");
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText(getString(R.string.boot_partition) + boot_a);
                    binding.extractBootA.setText(getString(R.string.extract_boot));
                    binding.writeBootA.setText(getString(R.string.write_boot));
                } else {
                    boot_a = getPartition("boot_a");
                    boot_a = boot_a.substring(0, boot_a.length() - 1);
                    binding.bootA.setText(getString(R.string.boot_a_partition) + boot_a);
                }
            } catch (Exception e) {
                outputLog(getString(R.string.get_partiton_failed, Aonly ? "boot" : "boot_a", e));
            }

            try {
                if (!Aonly) {
                    boot_b = getPartition("boot_b");
                    boot_b = boot_b.substring(0, boot_b.length() - 1);
                    binding.bootB.setText(getString(R.string.boot_b_partition) + boot_b);
                } else {
                    binding.bootB.setVisibility(View.GONE);
                    binding.bootBOperate.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                outputLog(getString(R.string.get_partiton_failed, "boot_b", e));
            }

        } else {
            try {
                Toast.makeText(requireContext(), getString(R.string.no_root), Toast.LENGTH_LONG).show();
                sleep(200);
                System.exit(0);
            } catch (InterruptedException ignored) {
            }
        }

        binding.extractBootA.setOnClickListener(view1 -> extractPartition(null, "boot_a"));
        binding.extractBootB.setOnClickListener(view1 -> extractPartition(null, "boot_b"));

        binding.writePartition.setOnClickListener(view1 -> new MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning)).setMessage(getString(R.string.write_confirm, sourcePath, targetPath)).setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> {
            binding.writePartition.setEnabled(false);
            writePartition(sourcePath, targetPath);
        }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show());

        binding.writeBootA.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            selectBootAPartitionFile.launch(intent);
        });

        binding.writeBootB.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            selectBootBPartitionFile.launch(intent);
        });

        binding.writeCustomPartition.setOnClickListener(view1 -> {
            EditText CustomPartitionName = new EditText(requireContext());
            CustomPartitionName.setSingleLine();
            CustomPartitionName.setHint(getString(R.string.input_partition_name_notice));
            CustomPartitionName.requestFocus();
            CustomPartitionName.setFocusable(true);
            new MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.write_partition)).setView(CustomPartitionName).setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                String content = CustomPartitionName.getText().toString();
                if (content.isBlank()) {
                    Toast.makeText(requireContext(), getString(R.string.input_partition_name_notice), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    targetPath = getPartition(content);
                } catch (Exception e) {
                    outputLog(getString(R.string.get_partiton_failed, content, e));
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                selectCustomPartitionFile.launch(intent);
            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });

        binding.extractCustomPartition.setOnClickListener(view1 -> {
            EditText CustomPartitionName = new EditText(requireContext());
            CustomPartitionName.setSingleLine();
            CustomPartitionName.setHint(getString(R.string.input_partition_name_notice));
            CustomPartitionName.requestFocus();
            CustomPartitionName.setFocusable(true);
            new MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.extract_partition)).setView(CustomPartitionName).setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                String content = CustomPartitionName.getText().toString();
                if (content.isBlank()) {
                    Toast.makeText(requireContext(), getString(R.string.input_partition_name_notice), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    extractPartition(CustomPartitionName.getText().toString(), getPartition(CustomPartitionName.getText().toString()));
                } catch (Exception e) {
                    outputLog(getString(R.string.get_partiton_failed, content, e));
                }
            }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });

        binding.confirmWriteCustomPartition.setOnClickListener(view1 -> {
            binding.confirmWriteCustomPartition.setVisibility(View.GONE);
            binding.showWriteCustomPartition.setVisibility(View.VISIBLE);
        });

        binding.listAllPartitions.setOnClickListener(view1 -> showPartitionList(new MaterialAlertDialogBuilder(requireContext()).setCancelable(false).setTitle(getString(R.string.get_partitions_list)).setMessage(getString(R.string.waiting)).show()));
    }

    /*
     * the dialog will automatically close after acquire partition list
     * */
    private void showPartitionList(AlertDialog dialog) {
        PartitionUtil.performIOOperationAsync(partitions -> {
            if (partitions.isBlank()) {
                outputLog(getString(R.string.get_partitons_list_failed));
                dialog.dismiss();
                return Unit.INSTANCE;
            }

            outputLog(getString(R.string.partitions_list, partitions));

            dialog.dismiss();
            return Unit.INSTANCE;
        });
    }

    ActivityResultLauncher<Intent> selectBootAPartitionFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> fileSelectResult(result, boot_a));

    ActivityResultLauncher<Intent> selectBootBPartitionFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> fileSelectResult(result, boot_b));

    ActivityResultLauncher<Intent> selectCustomPartitionFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> fileSelectResult(result, targetPath));

    private void fileSelectResult(ActivityResult result, String target) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                try {
                    sourcePath = FileUtil.getPath(requireContext(), data.getData());
                    targetPath = target;
                } catch (Exception e) {
                    outputLog(getString(R.string.unknown_error) + e);
                }
            }
            if (sourcePath != null && sourcePath.contains("/")) {
                binding.writePartition.setEnabled(true);
                binding.sourceFile.setText(getString(R.string.source_file, sourcePath));
                binding.targetPartition.setText(getString(R.string.target_partition, targetPath));
                outputLog(sourcePath + " -> " + targetPath);
            } else {
                new MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.path_error)).setMessage(getString(R.string.path_error_detail)).setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }
        }
    }

    public void extractPartition(String Name, String Partition) {
        try {
            String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
            if (Objects.equals(Partition, "boot_a")) {
                exeCmd("blockdev --setrw " + boot_a, false);
                if (Aonly) {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_" + date + ".img bs=4M;sync", true);
                    outputLog(getString(R.string.extract_to, "/Download/boot_" + date + ".img"));
                } else {
                    exeCmd("dd if=" + boot_a + " of=" + "/storage/emulated/0/Download/boot_a_" + date + ".img bs=4M;sync", true);
                    outputLog(getString(R.string.extract_to, "/Download/boot_a_" + date + ".img"));
                }
            } else if (Objects.equals(Partition, "boot_b")) {
                exeCmd("blockdev --setrw " + boot_b, false);
                exeCmd("dd if=" + boot_b + " of=" + "/storage/emulated/0/Download/boot_b_" + date + ".img bs=4M;sync", true);
                outputLog(getString(R.string.extract_to, "/Download/boot_b_" + date + ".img"));
            } else {
                exeCmd("blockdev --setrw " + Partition, false);
                exeCmd("dd if=" + Partition + " of=" + "/storage/emulated/0/Download/" + Name + "_" + date + ".img bs=4M;sync", true);
                outputLog(getString(R.string.extract_to, "/Download/" + Name + "_" + date + ".img"));
            }
        } catch (Exception e) {
            outputLog(getString(R.string.extract_failed, e.toString()));
        }
    }

    public void writePartition(String imgPath, String targetPath) {
        try {
            exeCmd("blockdev --setrw " + targetPath, false);
            exeCmd("dd if=" + imgPath + " of=" + targetPath + " bs=4M;sync", true);
            binding.sourceFile.setText(getString(R.string.source_file, getString(R.string.not_selected)));
            binding.targetPartition.setText(getString(R.string.target_partition, getString(R.string.not_selected)));
        } catch (Exception e) {
            outputLog(getString(R.string.write_failed, e.toString()));
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
        throw new Exception(getString(R.string.real_link_error));
    }

    public String exeCmd(String command, boolean log) throws InterruptedException, ExecutionException {
        StringBuilder sb = new StringBuilder();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> futureResult = executor.submit(() -> {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su -c " + command);
                BufferedReader br = new BufferedReader(new InputStreamReader(new SequenceInputStream(process.getInputStream(), process.getErrorStream()), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (log) {
                        outputLog(line);
                    }
                    sb.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new Exception(getString(R.string.unknown_error, "exitCode: " + exitCode));
                }
                br.close();
            } catch (IOException | InterruptedException e) {
                if (process != null) {
                    process.destroy();
                }
                throw new RuntimeException(getString(R.string.unknown_error, e));
            } finally {
                if (log) outputLog(getString(R.string.complete));
            }
            return sb.toString();
        });
        executor.shutdown();
        return futureResult.get();
    }
}