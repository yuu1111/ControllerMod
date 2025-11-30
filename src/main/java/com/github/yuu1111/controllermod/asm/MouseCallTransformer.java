package com.github.yuu1111.controllermod.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Mouse.getX/Y/isButtonDown() 呼び出しを置換するASMトランスフォーマー
 *
 * <p>
 * Minecraftのクラス内での Mouse.getX(), Mouse.getY(), Mouse.isButtonDown() の呼び出しを
 * MouseHook のラッパーメソッドに置き換える
 *
 * <p>
 * 置換対象:
 * <ul>
 * <li>Mouse.getX() → MouseHook.getX()</li>
 * <li>Mouse.getY() → MouseHook.getY()</li>
 * <li>Mouse.isButtonDown(I) → MouseHook.isButtonDown(I)</li>
 * </ul>
 */
public class MouseCallTransformer implements IClassTransformer {

    /** 変換対象のMouseクラス (内部名) */
    private static final String MOUSE_CLASS = "org/lwjgl/input/Mouse";

    /** 置換先のMouseHookクラス (内部名) */
    private static final String HOOK_CLASS = "com/github/yuu1111/controllermod/asm/MouseHook";

    /** 変換をスキップするパッケージ/クラスのプレフィックス */
    private static final String[] SKIP_PREFIXES = { "org.lwjgl", "com.github.yuu1111.controllermod.asm",
        "com.github.yuu1111.controllermod.gui.VirtualCursorManager", "cpw.mods.fml", "net.minecraftforge",
        "org.spongepowered", "java.", "sun.", "javax." };

    /** デバッグログを出力するか */
    private static final boolean DEBUG = false;

    /** 変換されたクラス数 (デバッグ用) */
    private static int transformedCount = 0;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        // スキップ対象のクラスは変換しない
        if (shouldSkip(transformedName)) {
            return basicClass;
        }

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            MouseCallClassVisitor visitor = new MouseCallClassVisitor(writer, transformedName);

            reader.accept(visitor, 0);

            if (visitor.isModified()) {
                transformedCount++;
                if (DEBUG) {
                    System.out.println(
                        "[ControllerMod ASM] Transformed: " + transformedName + " (total: " + transformedCount + ")");
                }
                return writer.toByteArray();
            }
        } catch (Exception e) {
            System.err.println("[ControllerMod ASM] Error transforming " + transformedName + ": " + e.getMessage());
        }

        return basicClass;
    }

    /**
     * 変換をスキップすべきクラスかどうかを判定する
     *
     * @param className クラス名 (ドット区切り)
     * @return スキップすべき場合は {@code true}
     */
    private boolean shouldSkip(String className) {
        if (className == null) {
            return true;
        }

        for (String prefix : SKIP_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Mouse呼び出しを置換するClassVisitor
     */
    private static class MouseCallClassVisitor extends ClassVisitor {

        private final String className;
        private boolean modified = false;

        public MouseCallClassVisitor(ClassVisitor cv, String className) {
            super(Opcodes.ASM5, cv);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MouseCallMethodVisitor(mv, this);
        }

        public boolean isModified() {
            return modified;
        }

        public void setModified() {
            this.modified = true;
        }

        public String getClassName() {
            return className;
        }
    }

    /**
     * Mouse呼び出しを置換するMethodVisitor
     */
    private static class MouseCallMethodVisitor extends MethodVisitor {

        private final MouseCallClassVisitor parent;

        public MouseCallMethodVisitor(MethodVisitor mv, MouseCallClassVisitor parent) {
            super(Opcodes.ASM5, mv);
            this.parent = parent;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            // INVOKESTATIC org/lwjgl/input/Mouse.xxx を検出
            if (opcode == Opcodes.INVOKESTATIC && MOUSE_CLASS.equals(owner)) {
                // Mouse.getX()I → MouseHook.getX()I
                if ("getX".equals(name) && "()I".equals(desc)) {
                    if (DEBUG) {
                        System.out.println("[ControllerMod ASM] Replacing Mouse.getX() in " + parent.getClassName());
                    }
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK_CLASS, "getX", "()I", false);
                    parent.setModified();
                    return;
                }

                // Mouse.getY()I → MouseHook.getY()I
                if ("getY".equals(name) && "()I".equals(desc)) {
                    if (DEBUG) {
                        System.out.println("[ControllerMod ASM] Replacing Mouse.getY() in " + parent.getClassName());
                    }
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK_CLASS, "getY", "()I", false);
                    parent.setModified();
                    return;
                }

                // Mouse.isButtonDown(I)Z → MouseHook.isButtonDown(I)Z
                if ("isButtonDown".equals(name) && "(I)Z".equals(desc)) {
                    if (DEBUG) {
                        System.out
                            .println("[ControllerMod ASM] Replacing Mouse.isButtonDown() in " + parent.getClassName());
                    }
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK_CLASS, "isButtonDown", "(I)Z", false);
                    parent.setModified();
                    return;
                }
            }

            // 置換対象でない場合はそのまま
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
