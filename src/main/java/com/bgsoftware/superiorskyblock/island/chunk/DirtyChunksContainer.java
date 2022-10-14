package com.bgsoftware.superiorskyblock.island.chunk;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class DirtyChunksContainer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final EnumMap<World.Environment, BitSet> dirtyChunks = new EnumMap<>(World.Environment.class);

    private final Island island;
    private final int minChunkX;
    private final int minChunkZ;
    private final int chunksInXAxis;
    private final int totalChunksCount;
    private final boolean shouldSave;

    public DirtyChunksContainer(Island island) {
        this.island = island;

        Location minimum = island.getMinimum();
        this.minChunkX = minimum.getBlockX() >> 4;
        this.minChunkZ = minimum.getBlockZ() >> 4;

        Location maximum = island.getMaximum();
        int maxChunkX = maximum.getBlockX() >> 4;
        int maxChunkZ = maximum.getBlockZ() >> 4;
        int chunksInZAxis = maxChunkZ - this.minChunkZ;

        this.chunksInXAxis = maxChunkX - this.minChunkX;
        this.totalChunksCount = this.chunksInXAxis * chunksInZAxis;

        this.shouldSave = !island.isSpawn();
    }

    public Island getIsland() {
        return island;
    }

    public boolean isMarkedDirty(ChunkPosition chunkPosition) {
        int chunkIndex = getChunkIndex(chunkPosition);

        if (chunkIndex < 0)
            throw new IllegalStateException("Chunk is not inside island boundaries: " + chunkPosition);

        BitSet dirtyChunksBitset = this.dirtyChunks.get(chunkPosition.getWorld().getEnvironment());

        return dirtyChunksBitset != null && !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);
    }

    public void markEmpty(ChunkPosition chunkPosition, boolean save) {
        int chunkIndex = getChunkIndex(chunkPosition);

        if (chunkIndex < 0)
            throw new IllegalStateException("Chunk is not inside island boundaries: " + chunkPosition);

        BitSet dirtyChunksBitset = this.dirtyChunks.get(chunkPosition.getWorld().getEnvironment());

        boolean isMarkedDirty = dirtyChunksBitset != null && !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);

        if (isMarkedDirty) {
            dirtyChunksBitset.clear(chunkIndex);
            if (this.shouldSave && save)
                IslandsDatabaseBridge.saveDirtyChunks(this);
        }
    }

    public void markDirty(ChunkPosition chunkPosition, boolean save) {
        int chunkIndex = getChunkIndex(chunkPosition);

        if (chunkIndex < 0)
            throw new IllegalStateException("Chunk is not inside island boundaries: " + chunkPosition);

        BitSet dirtyChunksBitset = this.dirtyChunks.computeIfAbsent(chunkPosition.getWorld().getEnvironment(),
                e -> new BitSet(this.totalChunksCount));

        boolean isMarkedDirty = !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);

        if (!isMarkedDirty) {
            dirtyChunksBitset.set(chunkIndex);
            if (this.shouldSave && save)
                IslandsDatabaseBridge.saveDirtyChunks(this);
        }
    }

    public List<ChunkPosition> getDirtyChunks() {
        if (this.dirtyChunks.isEmpty())
            return Collections.emptyList();

        List<ChunkPosition> dirtyChunkPositions = new LinkedList<>();

        this.dirtyChunks.forEach(((environment, dirtyChunks) -> {
            if (!dirtyChunks.isEmpty()) {
                World world = plugin.getGrid().getIslandsWorld(island, environment);
                if (world != null) {
                    for (int j = dirtyChunks.nextSetBit(0); j >= 0; j = dirtyChunks.nextSetBit(j + 1)) {
                        int deltaX = j / this.chunksInXAxis;
                        int deltaZ = j % this.chunksInXAxis;
                        dirtyChunkPositions.add(ChunkPosition.of(world, deltaX + this.minChunkX, deltaZ + this.minChunkZ));
                    }
                }
            }
        }));

        return dirtyChunkPositions;
    }

    private int getChunkIndex(ChunkPosition chunkPosition) {
        int deltaX = chunkPosition.getX() - this.minChunkX;
        int deltaZ = chunkPosition.getZ() - this.minChunkZ;
        return deltaX * this.chunksInXAxis + deltaZ;
    }

}