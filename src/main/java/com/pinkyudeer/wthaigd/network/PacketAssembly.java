package com.pinkyudeer.wthaigd.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import com.pinkyudeer.wthaigd.Wthaigd;

public final class PacketAssembly {

    public static final PacketAssembly INSTANCE = new PacketAssembly();
    private static final int BUFFER_SIZE = 20480;
    private static final long ASSEMBLY_TIMEOUT_MS = 30000L;

    private final HashMap<String, PendingAssembly> buffers = new HashMap<>();
    private final AtomicInteger nextAssemblyId = new AtomicInteger();

    private PacketAssembly() {}

    public List<NBTTagCompound> splitPacket(NBTTagCompound payload) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(payload, out);
            out.flush();
            byte[] data = out.toByteArray();
            out.close();

            int required = MathHelper.ceiling_float_int(data.length / (float) BUFFER_SIZE);
            int assemblyId = nextAssemblyId.incrementAndGet();
            List<NBTTagCompound> packets = new ArrayList<>(required);
            for (int packetIndex = 0; packetIndex < required; packetIndex++) {
                int index = packetIndex * BUFFER_SIZE;
                int size = Math.min(data.length - index, BUFFER_SIZE);
                byte[] part = new byte[size];
                System.arraycopy(data, index, part, 0, size);

                NBTTagCompound container = new NBTTagCompound();
                container.setInteger("assembly", assemblyId);
                container.setInteger("size", data.length);
                container.setInteger("index", index);
                container.setInteger("parts", required);
                container.setInteger("part", packetIndex);
                container.setTag("data", new NBTTagByteArray(part));
                packets.add(container);
            }
            return packets;
        } catch (Exception e) {
            Wthaigd.LOG.error("Unable to split wthaigd packet", e);
            return Collections.emptyList();
        }
    }

    public NBTTagCompound assemblePacket(UUID owner, NBTTagCompound packet) {
        cleanupExpired();
        int assemblyId = packet.getInteger("assembly");
        int size = packet.getInteger("size");
        int index = packet.getInteger("index");
        int parts = packet.hasKey("parts") ? packet.getInteger("parts")
            : MathHelper.ceiling_float_int(size / (float) BUFFER_SIZE);
        int part = packet.hasKey("part") ? packet.getInteger("part") : index / BUFFER_SIZE;
        byte[] data = packet.getByteArray("data");

        if (size <= 0 || parts <= 0 || part < 0 || part >= parts || index < 0 || index + data.length > size) {
            Wthaigd.LOG.error("Invalid wthaigd packet fragment: size={}, index={}, part={}, parts={}", size, index, part, parts);
            return null;
        }

        String key = key(owner, assemblyId);
        PendingAssembly assembly;
        synchronized (buffers) {
            assembly = buffers.get(key);
            if (assembly == null) {
                assembly = new PendingAssembly(size, parts);
                buffers.put(key, assembly);
            } else if (assembly.size != size || assembly.parts != parts) {
                Wthaigd.LOG.error("Unexpected wthaigd packet assembly shape for {}", key);
                buffers.remove(key);
                return null;
            }

            assembly.lastAccess = System.currentTimeMillis();
            if (!assembly.received.get(part)) {
                System.arraycopy(data, 0, assembly.buffer, index, data.length);
                assembly.received.set(part);
                assembly.receivedParts++;
            }
            if (assembly.receivedParts < assembly.parts) return null;
            buffers.remove(key);
        }

        try {
            DataInputStream in = new DataInputStream(
                new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(assembly.buffer))));
            NBTTagCompound tags = CompressedStreamTools.read(in);
            in.close();
            return tags;
        } catch (Exception e) {
            throw new RuntimeException("Unable to assemble wthaigd packet", e);
        }
    }

    private String key(UUID owner, int assemblyId) {
        return (owner == null ? "client" : owner.toString()) + ":" + assemblyId;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        synchronized (buffers) {
            buffers.entrySet()
                .removeIf(entry -> now - entry.getValue().lastAccess > ASSEMBLY_TIMEOUT_MS);
        }
    }

    private static final class PendingAssembly {

        private final int size;
        private final int parts;
        private final byte[] buffer;
        private final BitSet received;
        private int receivedParts;
        private long lastAccess = System.currentTimeMillis();

        private PendingAssembly(int size, int parts) {
            this.size = size;
            this.parts = parts;
            this.buffer = new byte[size];
            this.received = new BitSet(parts);
        }
    }
}
