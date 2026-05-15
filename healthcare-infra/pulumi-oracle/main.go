package main

import (
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

// Required Pulumi config keys:
//   pulumi config set compartmentId <ocid1.compartment...>
//   pulumi config set availabilityDomain <tenancy-name>:US-ASHBURN-AD-1
//   pulumi config set ubuntuImageId <ocid1.image...>   (Ubuntu 22.04, region-specific)
//   pulumi config set --secret sshPublicKey "ssh-rsa AAAA..."
//
// OCI provider credentials via ~/.oci/config or environment variables:
//   OCI_TENANCY_OCID, OCI_USER_OCID, OCI_FINGERPRINT, OCI_PRIVATE_KEY_PATH, OCI_REGION

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		cfg := config.New(ctx, "")

		compartmentId      := cfg.Require("compartmentId")
		availabilityDomain := cfg.Require("availabilityDomain")
		ubuntuImageId      := cfg.Require("ubuntuImageId")
		sshPublicKey       := cfg.Require("sshPublicKey")

		subnet, err := deployNetwork(ctx, compartmentId)
		if err != nil {
			return err
		}

		instance1Ip, instance2Ip, err := deployCompute(ctx, compartmentId, availabilityDomain, ubuntuImageId, sshPublicKey, subnet)
		if err != nil {
			return err
		}

		ctx.Export("instance1PublicIp", instance1Ip)
		ctx.Export("instance2PublicIp", instance2Ip)

		return nil
	})
}
