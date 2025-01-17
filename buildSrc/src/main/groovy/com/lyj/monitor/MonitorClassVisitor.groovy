package com.lyj.monitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class MonitorClassVisitor extends ClassVisitor {
    private ClassVisitor classVisitor;
    //类名
    private String className
    // 父类名
    private String superName
    //该类实现的接口
    private String[] interfaces
    //Activity访问过的方法，在类结束时判断是否需要添加方法
    public HashSet<String> visitedActivityMethods = new HashSet<>()
    //Fragment访问过的方法，在类结束时判断是否需要添加方法
    public HashSet<String> visitedFragmentMethods = new HashSet<>()

    public MonitorClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        this.classVisitor = classVisitor
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.superName = superName
        this.interfaces = interfaces
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        methodVisitor = new AdviceAdapter(Opcodes.ASM6, methodVisitor, access, name, desc) {
            @Override
            protected void onMethodExit(int i) {
                super.onMethodExit(i)
                // 排除系统类
                if (className.startsWith('android')) {
                    return
                }

                if (MonitorUtil.isExtendsActivity(superName)) {
                    handleActivity(name, mv)
                } else if (MonitorUtil.isExtendsFragment(superName)) {
                    handleFragment(name, mv)
                } else if (MonitorUtil.isMatchingInterfaces(interfaces, 'android/view/View$OnClickListener') && "onClick".equals(name)) {
                    handleViewEvent(name, mv)
                }
            }
        }
        return methodVisitor
    }

    /**
     * activity 生命周期埋点
     * @param name
     */
    void handleActivity(String name, MethodVisitor mv) {
        if (MonitorConfig.ACTIVITY_METHOD_ONCREATE == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONCREATE)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityCreate", "(Landroid/app/Activity;)V", false);
        } else if (MonitorConfig.ACTIVITY_METHOD_ONSTART == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONSTART)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityStart", "(Landroid/app/Activity;)V", false);
        } else if (MonitorConfig.ACTIVITY_METHOD_ONSTOP == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONSTOP)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityStop", "(Landroid/app/Activity;)V", false);
        } else if (MonitorConfig.ACTIVITY_METHOD_ONDESTROY == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONDESTROY)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityDestroy", "(Landroid/app/Activity;)V", false);
        } else if (MonitorConfig.ACTIVITY_METHOD_ONRESUME == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONRESUME)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityResume", "(Landroid/app/Activity;)V", false);
        } else if (MonitorConfig.ACTIVITY_METHOD_ONPAUSE == name) {
            visitedActivityMethods.add(MonitorConfig.ACTIVITY_METHOD_ONPAUSE)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/ActivityLifeUtil", "onActivityPause", "(Landroid/app/Activity;)V", false);
        }
    }
    /**
     * fragment 生命周期埋点
     * @param name
     */
    void handleFragment(String name, MethodVisitor mv) {
        //fragment 生命周期埋点
        if (MonitorConfig.FRAGMENT_METHOD_ONRESUME == name) {
            visitedFragmentMethods.add(MonitorConfig.FRAGMENT_METHOD_ONRESUME)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/FragmentLifeUtil", "onFragmentResume", "(Landroid/support/v4/app/Fragment;)V", false);
        } else if (MonitorConfig.FRAGMENT_METHOD_ONPAUSE == name) {
            visitedFragmentMethods.add(MonitorConfig.FRAGMENT_METHOD_ONPAUSE)
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/FragmentLifeUtil", "onFragmentPause", "(Landroid/support/v4/app/Fragment;)V", false);
        } else if (MonitorConfig.FRAGMENT_METHOD_ONHIDDENCHANGED == name) {
            visitedFragmentMethods.add(MonitorConfig.FRAGMENT_METHOD_ONHIDDENCHANGED)
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitVarInsn(Opcodes.ILOAD, 1)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/FragmentLifeUtil", "onFragmentHiddenChanged", "(Landroid/support/v4/app/Fragment;Z)V", false);
        } else if (MonitorConfig.FRAGMENT_METHOD_SETUSERVISIBLEHINT == name) {
            visitedFragmentMethods.add(MonitorConfig.FRAGMENT_METHOD_SETUSERVISIBLEHINT)
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitVarInsn(Opcodes.ILOAD, 1)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/foolchen/lib/tracker/utils/FragmentLifeUtil", "onFragmentSetUserVisibleHint", "(Landroid/support/v4/app/Fragment;Z)V", false);
        }
    }
    /**
     * view点击事件处理
     * @param name
     */
    void handleViewEvent(String name, MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 1)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/paic/zhifu/wallet/activity/myapp/ViewEventUtil", "onClick", "(Landroid/view/View;)V", false);
    }


    @Override
    void visitEnd() {
        if (className.startsWith('android')) {
            return
        }

        if (MonitorUtil.isExtendsActivity(superName)) {
            //是Activity类 查询其中4个方法是否都已调用，没调用的先插入函数再调用
            boolean hasOnCreate = false
            boolean hasOnStart = false
            boolean hasOnStop = false
            boolean hasOnDestory = false
            boolean hasOnResume = false
            boolean hasOnPause = false
            for (int i = 0; i < visitedActivityMethods.size(); i++) {
                if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONCREATE) {
                    hasOnCreate = true
                } else if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONSTART) {
                    hasOnStart = true
                } else if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONSTOP) {
                    hasOnStop = true
                } else if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONDESTROY) {
                    hasOnDestory = true
                } else if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONRESUME) {
                    hasOnResume = true
                } else if (visitedActivityMethods[i] == MonitorConfig.ACTIVITY_METHOD_ONPAUSE) {
                    hasOnPause = true
                }
            }
            if (!hasOnCreate) {
                MonitorUtil.insertActivityOnCreate(classVisitor, superName)
            }
            if (!hasOnStart) {
                MonitorUtil.insertActivityOnStart(classVisitor, superName)
            }
            if (!hasOnStop) {
                MonitorUtil.insertActivityOnStop(classVisitor, superName)
            }
            if (!hasOnDestory) {
                MonitorUtil.insertActivityOnDestroy(classVisitor, superName)
            }
            if (!hasOnResume) {
                MonitorUtil.insertActivityOnResume(classVisitor, superName)
            }
            if (!hasOnPause) {
                MonitorUtil.insertActivityOnPause(classVisitor, superName)
            }
        } else if (MonitorUtil.isExtendsFragment(superName)) {
            //是Fragment类 查询其中4个方法是否都已调用，没调用的先插入函数再调用
            boolean hasOnResume = false
            boolean hasOnPause = false
            boolean hasOnHiddenChanged = false
            boolean hasSetUserVisibleHint = false
            for (int i = 0; i < visitedFragmentMethods.size(); i++) {
                if (visitedFragmentMethods[i] == MonitorConfig.FRAGMENT_METHOD_ONRESUME) {
                    hasOnResume = true
                } else if (visitedFragmentMethods[i] == MonitorConfig.FRAGMENT_METHOD_ONPAUSE) {
                    hasOnPause = true
                } else if (visitedFragmentMethods[i] == MonitorConfig.FRAGMENT_METHOD_ONHIDDENCHANGED) {
                    hasOnHiddenChanged = true
                } else if (visitedFragmentMethods[i] == MonitorConfig.FRAGMENT_METHOD_SETUSERVISIBLEHINT) {
                    hasSetUserVisibleHint = true
                }
            }
            if (!hasOnResume) {
                MonitorUtil.insertFragmentOnResume(classVisitor, superName)
            }
            if (!hasOnPause) {
                MonitorUtil.insertFragmentOnPause(classVisitor, superName)
            }
            if (!hasOnHiddenChanged) {
                MonitorUtil.insertFragmentOnHiddenChanged(classVisitor, superName)
            }
            if (!hasSetUserVisibleHint) {
                MonitorUtil.insertFragmentSetUserVisibleHint(classVisitor, superName)
            }
        }
        super.visitEnd()
    }
}