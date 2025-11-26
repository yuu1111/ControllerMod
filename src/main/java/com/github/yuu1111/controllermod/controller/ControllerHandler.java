package com.github.yuu1111.controllermod.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.github.yuu1111.controllermod.ControllerMod;

import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

/**
 * Handles controller input using SDL2 via sdl2gdx.
 */
public class ControllerHandler implements ControllerListener {

    private SDL2ControllerManager controllerManager;
    private boolean initialized = false;

    public void init() {
        try {
            controllerManager = new SDL2ControllerManager();
            controllerManager.addListenerAndRunForConnectedControllers(this);
            initialized = true;
            ControllerMod.LOG.info("SDL2 Controller system initialized");
        } catch (Exception e) {
            ControllerMod.LOG.error("Failed to initialize SDL2 controller system", e);
        }
    }

    public void update() {
        if (!initialized || controllerManager == null) {
            return;
        }

        try {
            controllerManager.pollState();
        } catch (Exception e) {
            ControllerMod.LOG.error("Error polling controller state", e);
        }
    }

    public void shutdown() {
        if (controllerManager != null) {
            controllerManager.close();
            controllerManager = null;
            initialized = false;
            ControllerMod.LOG.info("SDL2 Controller system shut down");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ControllerListener implementation

    @Override
    public void connected(Controller controller) {
        ControllerMod.LOG.info("Controller connected: {}", controller.getName());
    }

    @Override
    public void disconnected(Controller controller) {
        ControllerMod.LOG.info("Controller disconnected: {}", controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        ControllerMod.LOG.info("Button DOWN: {} on {}", buttonCode, controller.getName());
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        ControllerMod.LOG.info("Button UP: {} on {}", buttonCode, controller.getName());
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        // Only log significant axis movement to avoid spam
        if (Math.abs(value) > 0.5f) {
            ControllerMod.LOG.info("Axis {}: {} on {}", axisCode, value, controller.getName());
        }
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        if (value != PovDirection.center) {
            ControllerMod.LOG.info("POV {}: {} on {}", povCode, value, controller.getName());
        }
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
