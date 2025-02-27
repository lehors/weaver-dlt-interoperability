package main

import (
	"encoding/json"
	"fmt"
	"testing"

	"github.com/stretchr/testify/require"
	"github.ibm.com/dlt-interoperability/fabric-interop-cc/contracts/interop/protos-go/common"
)

var identifier = common.Identifier{
	Pattern: "Identifier",
	Policy:  &policy,
}
var policy = common.Policy{
	Criteria: []string{"criteria"},
	Type:     "policytype",
}

var verificationPolicyAsset = common.VerificationPolicy{
	SecurityDomain: "2345",
	Identifiers:    []*common.Identifier{&identifier},
}

func TestGetVerificationPolicyBySecurityDomain(t *testing.T) {
	ctx, chaincodeStub, interopcc := prepMockStub()

	// Case when no VerificationPolicy is found
	acString, err := interopcc.GetVerificationPolicyBySecurityDomain(ctx, "2345")
	require.EqualError(t, err, fmt.Sprintf("VerificationPolicy with id: %s does not exist", "2345"))
	require.Equal(t, acString, "")
	value, err := json.Marshal(&verificationPolicyAsset)
	require.NoError(t, err)
	chaincodeStub.GetStateReturns(value, nil)
	// Case when VerificationPolicy is found
	assetRead, err := interopcc.GetVerificationPolicyBySecurityDomain(ctx, "2345")
	require.NoError(t, err)
	require.Equal(t, assetRead, string(value))
}
func TestCreateVerificationPolicy(t *testing.T) {
	ctx, chaincodeStub, interopcc := prepMockStub()

	verificationPolicyBytes, err := json.Marshal(&verificationPolicyAsset)
	require.NoError(t, err)
	// Case when no VerificationPolicy is found
	err = interopcc.CreateVerificationPolicy(ctx, string(verificationPolicyBytes))
	require.NoError(t, err)
	// Invalid Input check
	err = interopcc.CreateVerificationPolicy(ctx, "Invalid Input")
	require.EqualError(t, err, fmt.Sprintf("Unmarshal error: invalid character 'I' looking for beginning of value"))

	// VerificationPolicy already exists
	chaincodeStub.GetStateReturns([]byte{}, nil)
	err = interopcc.CreateVerificationPolicy(ctx, string(verificationPolicyBytes))
	require.EqualError(t, err, fmt.Sprintf("VerificationPolicy already exists with id: %s", verificationPolicyAsset.SecurityDomain))

}

func TestUpdateVerificationPolicy(t *testing.T) {
	ctx, chaincodeStub, interopcc := prepMockStub()

	verificationPolicyBytes, err := json.Marshal(&verificationPolicyAsset)
	require.NoError(t, err)
	// Case when no VerificationPolicy is found
	err = interopcc.UpdateVerificationPolicy(ctx, string(verificationPolicyBytes))
	require.EqualError(t, err, fmt.Sprintf("VerificationPolicy with id: %s does not exist", verificationPolicyAsset.SecurityDomain))
	// Invalid JSON check
	chaincodeStub.GetStateReturns(verificationPolicyBytes, nil)
	err = interopcc.UpdateVerificationPolicy(ctx, "Invalid Input")
	require.EqualError(t, err, fmt.Sprintf("Unmarshal error: invalid character 'I' looking for beginning of value"))
	// VerificationPolicy already exists update the VerificationPolicy
	chaincodeStub.GetStateReturns(verificationPolicyBytes, nil)
	err = interopcc.UpdateVerificationPolicy(ctx, string(verificationPolicyBytes))
	require.NoError(t, err)

}

func TestDeleteVerificationPolicy(t *testing.T) {
	ctx, chaincodeStub, interopcc := prepMockStub()

	// Case when a VerificationPolicy exists
	chaincodeStub.GetStateReturns([]byte{}, nil)
	err := interopcc.DeleteVerificationPolicy(ctx, "2343")
	require.NoError(t, err)

	// Case when no VerificationPolicy is found
	chaincodeStub.GetStateReturns(nil, nil)
	err = interopcc.DeleteVerificationPolicy(ctx, "2343")
	require.EqualError(t, err, fmt.Sprintf("VerificationPolicy with id: %s does not exist", "2343"))

	// Handle GetState Error
	chaincodeStub.GetStateReturns(nil, fmt.Errorf("unable to retrieve asset"))
	err = interopcc.DeleteVerificationPolicy(ctx, "2343")
	require.EqualError(t, err, fmt.Sprintf("unable to retrieve asset"))
}
