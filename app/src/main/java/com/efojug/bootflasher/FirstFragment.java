package com.efojug.bootflasher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.efojug.bootflasher.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!getRoot()) binding.flash.setEnabled(false);
        else binding.root.setVisibility(View.GONE);
        binding.flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo
                Snackbar.make(view, "刷入中", Snackbar.LENGTH_LONG).show();
            }
        });

        if (getRoot()) binding.buttonFirst.setVisibility(View.GONE);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getRoot()) {
                    binding.flash.setEnabled(true);
                    binding.root.setVisibility(View.GONE);
                    binding.buttonFirst.setVisibility(View.GONE);
                } else {
                    Snackbar.make(view, "失败", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
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
        } catch (IOException ignored) {
            return false;
        }
    }

}