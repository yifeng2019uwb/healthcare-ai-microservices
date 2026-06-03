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

		instance1PublicIp, instance2PublicIp, _, instance2PrivateIp, err := deployCompute(ctx, compartmentId, availabilityDomain, imageId, sshPublicKey, vmPassword, subnet)
		if err != nil {
			return err
		}

		ctx.Export("instance1PublicIp", instance1PublicIp)
		ctx.Export("instance2PublicIp", instance2PublicIp)
		ctx.Export("instance2PrivateIp", instance2PrivateIp)

		// A1 instance — gated by: pulumi config set a1Enabled true
		// Retries via try-a1.sh until Oracle free tier capacity is available
		if cfg.GetBool("a1Enabled") {
			a1Images, err := core.GetImages(ctx, &core.GetImagesArgs{
				CompartmentId:          compartmentId,
				OperatingSystem:        strPtr("Oracle Linux"),
				OperatingSystemVersion: strPtr("9"),
				Shape:                  strPtr("VM.Standard.A1.Flex"),
				SortBy:                 strPtr("TIMECREATED"),
				SortOrder:              strPtr("DESC"),
			}, nil)
			if err != nil {
				return err
			}
			a1ImageId := a1Images.Images[0].Id

			a1Ip, err := deployA1(ctx, compartmentId, availabilityDomain, a1ImageId, sshPublicKey, vmPassword, subnet)
			if err != nil {
				return err
			}
			ctx.Export("a1PublicIp", a1Ip)
		}

		return nil
	})
}

func strPtr(s string) *string { return &s }
