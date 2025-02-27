package com.cordaInteropApp.states

import com.cordaInteropApp.contracts.ExternalStateContract
import com.google.protobuf.ByteString
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

/**
 * A representation of state and proof retrieved from an external network.
 *
 * @property state The state data from the source network, including proofs.
 * @property linearId The unique identifier for the state.
 * @property participants The list of parties that are participants of the state
 * TODO: determine how we should represent the state in the vault.
 */
@BelongsToContract(ExternalStateContract::class)
data class ExternalState(
        val state: ByteArray,
        val meta: ByteArray,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<Party> = listOf()
) : LinearState
