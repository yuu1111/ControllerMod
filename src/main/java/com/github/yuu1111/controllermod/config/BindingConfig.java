package com.github.yuu1111.controllermod.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.yuu1111.controllermod.ControllerMod;
import com.github.yuu1111.controllermod.constants.Reference;
import com.github.yuu1111.controllermod.input.Keybind;
import com.github.yuu1111.controllermod.input.KeybindRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * コントローラーバインド設定の保存/読み込み
 *
 * <p>
 * JSON形式でバインド設定を永続化する。
 * Minecraftの設定ディレクトリに保存される。
 * 
 */
public final class BindingConfig {

    /** Gson インスタンス */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();

    /** 設定ファイル */
    private static File configFile;

    private BindingConfig() {
        // ユーティリティクラス
    }

    /**
     * 設定ディレクトリを初期化
     *
     * @param configDir Minecraftの設定ディレクトリ
     */
    public static void init(File configDir) {
        configFile = new File(configDir, Reference.CONFIG_FILE_BINDINGS);
        load();
    }

    /**
     * バインド設定を読み込む
     */
    public static void load() {
        if (configFile == null || !configFile.exists()) {
            ControllerMod.LOG.info("Binding config not found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            BindingData data = GSON.fromJson(reader, BindingData.class);
            if (data != null && data.bindings != null) {
                for (Map.Entry<String, Integer> entry : data.bindings.entrySet()) {
                    Keybind binding = KeybindRegistry.get(entry.getKey());
                    if (binding != null) {
                        binding.setButton(entry.getValue());
                    }
                }
                ControllerMod.LOG.info("Loaded {} binding(s) from config", data.bindings.size());
            }
        } catch (IOException e) {
            ControllerMod.LOG.error("Failed to load binding config", e);
        }
    }

    /**
     * バインド設定を保存する
     */
    public static void save() {
        if (configFile == null) {
            ControllerMod.LOG.warn("Config file not initialized");
            return;
        }

        BindingData data = new BindingData();
        data.bindings = new HashMap<>();

        // 変更されたバインドのみ保存
        for (Keybind binding : KeybindRegistry.getAll()) {
            if (binding.isModified()) {
                data.bindings.put(binding.getId(), binding.getButton());
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(data, writer);
            ControllerMod.LOG.info("Saved {} modified binding(s) to config", data.bindings.size());
        } catch (IOException e) {
            ControllerMod.LOG.error("Failed to save binding config", e);
        }
    }

    /**
     * 全バインドをリセットして保存
     */
    public static void resetAndSave() {
        KeybindRegistry.resetAll();
        save();
    }

    /**
     * JSON保存用データクラス
     */
    private static class BindingData {

        Map<String, Integer> bindings;
    }
}
