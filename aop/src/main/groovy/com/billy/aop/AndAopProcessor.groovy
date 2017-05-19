package com.billy.aop

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Pattern
import java.util.zip.ZipEntry
/**
 *
 * @author billy.qi
 * @since 17/3/20 11:48
 */
class AndAopProcessor {
    static AopExtension extension;
    public static File processJar(File jarFile) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def file = new JarFile(jarFile);
            Enumeration enumeration = file.entries();
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);

                InputStream inputStream = file.getInputStream(jarEntry);
                jarOutputStream.putNextEntry(zipEntry);
                if (shouldProcessClass(entryName)) {
                    println('entryName:' + entryName)
                    def bytes = referHackWhenInit(inputStream);
                    jarOutputStream.write(bytes);
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }
            jarOutputStream.close();
            file.close();

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

    public static boolean shouldProcessPreDexJar(String path) {
        if (!extension || !extension.aopClass || !extension.include)
            return false

        return path.endsWith("classes.jar") && !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    // file in folder like these
    //com/billy/testplugin/Aop.class
    //com/billy/testplugin/BuildConfig.class
    //com/billy/testplugin/R$attr.class
    //com/billy/testplugin/R.class
    // entry in jar like these
    //android/support/v4/BuildConfig.class
    //com/lib/xiwei/common/util/UiTools.class
    static boolean shouldProcessClass(String entryName) {
//        println('classes:' + entryName)
        if (entryName == null || !entryName.endsWith(".class"))
            return false
        entryName = entryName.substring(0, entryName.lastIndexOf('.'))
        if (extension != null) {
            def list = extension.includePatterns
            if (list) {
                def exlist = extension.excludePatterns
                Pattern pattern, p
                for (int i = 0; i < list.size(); i++) {
                    pattern = list.get(i)
                    if(pattern.matcher(entryName).matches()) {
                        if (exlist) {
                            for (int j = 0; j < exlist.size(); j++) {
                                p = exlist.get(j)
                                if(p.matcher(entryName).matches())
                                    return false
                            }
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    static boolean shouldProcessMethod(String name) {
        if ("<init>".equals(name) || "<clinit>".equals(name) || name.startsWith('access$'))
            return false
        return true
    }

    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    public static byte[] processClass(File file) {
        def optClass = new File(file.getParent(), file.name + ".opt")

        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream(optClass)

        def bytes = referHackWhenInit(inputStream);
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }


    //refer hack class when object init
    private static byte[] referHackWhenInit(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    static class MyClassVisitor extends ClassVisitor {
        String className;
        String superName;
        String[] interfaces;
        public MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (shouldProcessMethod(name)) {
                println('aop method:' + name + desc)
                mv = new MyMethodVisitor(Opcodes.ASM5, mv, access, name, desc, signature, exceptions, className);
            }
            return mv;
        }
    }
    static class MyMethodVisitor extends LocalVariablesSorter {
        int access;
        String name, desc, signature, className;
        String[] exceptions;
        int aopVar;

        public MyMethodVisitor(final int api, final MethodVisitor mv
                               , int access , String name, String desc, String signature, String[] exceptions, String className) {
            super(api, access, desc, mv);
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
            this.className = className;
        }

        @Override
        void visitCode() {
            super.visitCode()
            if (extension.aopClass && extension.methodStart) {
                mv.visitLdcInsn(className);//类名
                mv.visitLdcInsn(name)//方法名
                mv.visitLdcInsn(desc)//参数列表及返回值类型
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.aopClass, extension.methodStart, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)L${extension.aopClass};", false);
                aopVar = newLocal(Type.getObjectType(extension.aopClass))
                mv.visitVarInsn(Opcodes.ASTORE, aopVar)
            }
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                    || opcode == Opcodes.ATHROW) {
                if (extension.aopClass && extension.methodEnd) {
                    if (aopVar >= 0) {//在返回之前安插after 代码。
                        mv.visitVarInsn(Opcodes.ALOAD, aopVar)
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.aopClass, extension.methodEnd, "(L${extension.aopClass};)V", false);
                    } else {
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, extension.aopClass, extension.methodEnd, "()V", false);
                    }
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }
}