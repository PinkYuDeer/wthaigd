package com.pinkyudeer.wthaigd.integration.gtnhlib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;

import com.pinkyudeer.wthaigd.Wthaigd;

import cpw.mods.fml.common.Loader;

public final class GtnhLibTeamDataBridge {

    public static final String DATA_KEY = "wthaigd";

    private static final String TEAM_DATA = "com.gtnewhorizon.gtnhlib.teams.ITeamData";
    private static final String TEAM_DATA_REGISTRY = "com.gtnewhorizon.gtnhlib.teams.TeamDataRegistry";
    private static boolean registered;

    private GtnhLibTeamDataBridge() {}

    public static synchronized void register() {
        if (registered || !isGtnhLibLoaded()) return;
        try {
            Class<?> teamDataType = Class.forName(TEAM_DATA);
            Class<?> registryType = Class.forName(TEAM_DATA_REGISTRY);
            Method register = findRegisterMethod(registryType);
            if (register == null) {
                Wthaigd.LOG.warn("GTNHLib TeamDataRegistry register method not found");
                return;
            }

            Supplier<?> supplier = () -> Proxy.newProxyInstance(
                teamDataType.getClassLoader(),
                new Class<?>[] { teamDataType },
                new WthaigdTeamDataHandler());
            register.invoke(null, DATA_KEY, supplier);
            registered = true;
            Wthaigd.LOG.info("Registered wthaigd GTNHLib team data bridge: {}", DATA_KEY);
        } catch (ClassNotFoundException e) {
            // PR #297 尚未合入旧版 GTNHLib 时，静默跳过。
        } catch (Exception e) {
            Wthaigd.LOG.warn("Unable to register GTNHLib team data bridge", e);
        }
    }

    private static Method findRegisterMethod(Class<?> registryType) {
        for (Method method : registryType.getMethods()) {
            if (!("register".equals(method.getName()) || "registerTeamData".equals(method.getName()))) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 2 && String.class.equals(params[0]) && Supplier.class.isAssignableFrom(params[1])) {
                return method;
            }
        }
        return null;
    }

    private static boolean isGtnhLibLoaded() {
        return Loader.isModLoaded("gtnhlib") || Loader.isModLoaded("GTNHLib");
    }

    private static final class WthaigdTeamDataHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("toString".equals(name)) return "WthaigdTeamDataBridge";
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("equals".equals(name)) return proxy == args[0];
            if ("writeToNBT".equals(name)) {
                writeMarker(args);
                return null;
            }
            if ("readFromNBT".equals(name) || "mergeData".equals(name) || "markDirty".equals(name)) return null;
            return null;
        }

        private void writeMarker(Object[] args) {
            if (args == null || args.length == 0 || !(args[0] instanceof NBTTagCompound)) return;
            NBTTagCompound tag = (NBTTagCompound) args[0];
            tag.setString("schema", "external-team-bridge");
        }
    }
}
