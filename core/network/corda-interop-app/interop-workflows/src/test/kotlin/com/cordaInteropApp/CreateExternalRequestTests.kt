package com.cordaInteropApp

import com.cordaInteropApp.flows.CreateAccessControlPolicy
import com.cordaInteropApp.flows.CreateExternalRequest
import com.cordaInteropApp.flows.CreateVerificationPolicyState
import com.cordaInteropApp.flows.verifyNodeSignature
import com.cordaInteropApp.states.Policy
import com.cordaInteropApp.states.VerificationPolicyState
import com.cordaInteropApp.states.Identifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateExternalRequestTests {
    companion object {
        lateinit var network: MockNetwork
        lateinit var partyA: StartedMockNode

        @BeforeClass
        @JvmStatic
        fun setup() {
            network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.cordaInteropApp.contracts"),
                    TestCordapp.findCordapp("com.cordaInteropApp.flows")
            )))
            partyA = network.createPartyNode()
            network.runNetwork()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            network.stopNodes()
            System.setProperty("net.corda.node.dbtransactionsresolver.InMemoryResolutionLimit", "0")
        }
    }

    @Test
    fun `CreateExternalRequest tests`() {
        val verificationPolicy = VerificationPolicyState(
                securityDomain = "network2",
                identifiers =  listOf(
                        Identifier(
                        pattern = "mychannel:mychaincode:chaincodefn:chaincodeargs",
                        policy = Policy("Signature", listOf("org1.example.com"))
                ),
                        Identifier(
                                pattern = "mychannel:mychaincode:otherChaincodeFn:*",
                                policy = Policy("Signature", listOf("org2.example.com"))
                        ))
        )
        partyA.startFlow(CreateVerificationPolicyState(verificationPolicy))
        network.runNetwork()

        // Happy case with valid address
        val address = "localhost:9080/network2/mychannel:mychaincode:chaincodefn:chaincodeargs"
        val externalRequestFuture = partyA.startFlow(CreateExternalRequest(address))
        network.runNetwork()
        val externalRequest = externalRequestFuture.getOrThrow()
        assertTrue(externalRequest.isRight())
        externalRequest.map {
            assertEquals("org1.example.com", it.policy[0])
            val isValidRequestSignature = verifyNodeSignature(it.certificate, it.signature, (address + it.nonce).toByteArray())
            assertTrue(isValidRequestSignature.isRight())
        }

        // Happy case with valid address without args
        val addressNoCCArgsInVerPolicy = "localhost:9080/network2/mychannel:mychaincode:otherChaincodeFn:chaincodeargs"
        val externalRequestFutureTwo = partyA.startFlow(CreateExternalRequest(addressNoCCArgsInVerPolicy))
        network.runNetwork()
        val externalRequestTwo = externalRequestFutureTwo.getOrThrow()
        assertTrue(externalRequestTwo.isRight())
        externalRequestTwo.map {
            assertEquals("org2.example.com", it.policy[0])
            val isValidRequestSignature = verifyNodeSignature(it.certificate, it.signature, (addressNoCCArgsInVerPolicy + it.nonce).toByteArray())
            assertTrue(isValidRequestSignature.isRight())
        }

            // Happy case with valid address with multiple args when view address in verification policy has no args
//        val multiArgAddressNoCCArgsInVerPolicy = "localhost:9080/network2/mychannel:mychaincode:otherChaincodeFn:chaincodeargs:otherChaincodeArg"
//        val externalRequestFutureThree = partyA.startFlow(CreateExternalRequest(multiArgAddressNoCCArgsInVerPolicy))
//        network.runNetwork()
//        val externalRequestThree = externalRequestFutureTwo.getOrThrow()
//        assertTrue(externalRequestThree.isRight())
//        externalRequestThree.map {
//            assertEquals("org2.example.com", it.policy[0])
//            val isValidRequestSignature = verifyNodeSignature(it.certificate, it.signature, addressNoCCArgsInVerPolicy + it.nonce)
//            assertTrue(isValidRequestSignature.isRight())
//        }

        // Unhappy case with invalid networkId
        val invalidNetworkAddress = "localhost:9080/invalidNetwork/mychannel:mychaincode:chaincodefn:chaincodeargs"
        val invalidNetworkFuture = partyA.startFlow(CreateExternalRequest(invalidNetworkAddress))
        network.runNetwork()
        val invalidNetworkError = invalidNetworkFuture.getOrThrow()
        assertTrue(invalidNetworkError.isLeft())

        // Unhappy case with invalid view address
        val invalidViewAddress = "localhost:9080/network2/mychannel:mychaincode:invalidArgs"
        val invalidViewAddressFuture = partyA.startFlow(CreateExternalRequest(invalidViewAddress))
        network.runNetwork()
        val invalidViewAddressError = invalidViewAddressFuture.getOrThrow()
        assertTrue(invalidViewAddressError.isLeft())
    }
}