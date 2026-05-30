package main

import (
	"github.com/pulumi/pulumi-oci/sdk/v2/go/oci/core"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

// Required Pulumi config keys:
//   pulumi config set compartmentId    <ocid1.tenancy...>
//   pulumi config set availabilityDomain <prefix>:US-SANJOSE-1-AD-1
//   pulumi config set --secret sshPublicKey "ssh-rsa AAAA..."
//   pulumi config set --secret vmPassword "your-console-password"
//
// OCI provider credentials via ~/.oci/config:
//   user, fingerprint, tenancy, region, key_file

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		cfg := config.New(ctx, "")

		compartmentId      := cfg.Require("compartmentId")
		availabilityDomain := cfg.Require("availabilityDomain")
		sshPublicKey       := cfg.Require("sshPublicKey")
		vmPassword         := cfg.Require("vmPassword")

		// Look up the latest Oracle Linux 9 platform image for this region automatically.
		// No need to hardcode a region-specific image OCID.
		images, err := core.GetImages(ctx, &core.GetImagesArgs{
			CompartmentId:          compartmentId,
			OperatingSystem:        strPtr("Oracle Linux"),
			OperatingSystemVersion: strPtr("9"),
			Shape:                  strPtr("VM.Standard.E2.1.Micro"),
			SortBy:                 strPtr("TIMECREATED"),
			SortOrder:              strPtr("DESC"),
		}, nil)
		if err != nil {
			return err
		}
		imageId := images.Images[0].Id

		subnet, err := deployNetwork(ctx, compartmentId)
		if err != nil {
			return err
		}

		instance1Ip, instance2Ip, err := deployCompute(ctx, compartmentId, availabilityDomain, imageId, sshPublicKey, vmPassword, subnet)
		if err != nil {
			return err
		}

		ctx.Export("instance1PublicIp", instance1Ip)
		ctx.Export("instance2PublicIp", instance2Ip)

		return nil
	})
}

func strPtr(s string) *string { return &s }
