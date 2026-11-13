package com.simpleeconomy.api;

import java.util.Objects;

import com.simpleeconomy.economy.EconomyData;

import net.minecraft.server.level.ServerPlayer;

/**
 * Public server-side API for other mods that integrate with Simple Economy.
 *
 * Call economy methods only from the logical server thread.
 */
public final class EconomyAPI {

    private EconomyAPI() {
    }

    /**
     * Pays a player for completing a contract.
     *
     * @param player the player receiving the reward
     * @param amount positive amount of money to award
     * @param contractName display name saved to transaction history
     * @return the player's updated balance
     * @throws IllegalArgumentException if the reward is not positive or the
     *                                  balance would overflow
     */
    public static long addContractReward(
            ServerPlayer player,
            long amount,
            String contractName
    ) {
        Objects.requireNonNull(player, "player");

        return EconomyData.get(player.server).addContractReward(
                player.getUUID(),
                amount,
                contractName
        );
    }
}
