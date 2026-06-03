package main

import (
	"encoding/base64"
	"fmt"

	"github.com/pulumi/pulumi-oci/sdk/v2/go/oci/core"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

const shape    = "VM.Standard.E2.1.Micro"
const shapeA1  = "VM.Standard.A1.Flex"

const cloudInitTpl = `#cloud-config
runcmd:
  - echo 'opc:%s' | chpasswd
  - systemctl disable --now firewalld || true
`

func deployCompute(
	ctx *pulumi.Context,
	compartmentId, availabilityDomain, imageId, sshPublicKey, vmPassword string,
	subnet *core.Subnet,
) (pulumi.StringOutput, pulumi.StringOutput, pulumi.StringOutput, pulumi.StringOutput, error) {

	userData := base64.StdEncoding.EncodeToString([]byte(fmt.Sprintf(cloudInitTpl, vmPassword)))

	instance1, err := core.NewInstance(ctx, "healthcare-instance-1", &core.InstanceArgs{
		CompartmentId:      pulumi.String(compartmentId),
		AvailabilityDomain: pulumi.String(availabilityDomain),
		Shape:              pulumi.String(shape),
		DisplayName:        pulumi.String("healthcare-instance-1"),
		CreateVnicDetails: &core.InstanceCreateVnicDetailsArgs{
			SubnetId:       subnet.ID(),
			AssignPublicIp: pulumi.String("true"),
			DisplayName:    pulumi.String("healthcare-vnic-1"),
		},
		SourceDetails: &core.InstanceSourceDetailsArgs{
			SourceType: pulumi.String("image"),
			SourceId:   pulumi.String(imageId),
		},
		Metadata: pulumi.StringMap{
			"ssh_authorized_keys": pulumi.String(sshPublicKey),
			"user_data":           pulumi.String(userData),
		},
	}, pulumi.DeleteBeforeReplace(true))
	if err != nil {
		return pulumi.StringOutput{}, pulumi.StringOutput{}, pulumi.StringOutput{}, pulumi.StringOutput{}, err
	}

	instance2, err := core.NewInstance(ctx, "healthcare-instance-2", &core.InstanceArgs{
		CompartmentId:      pulumi.String(compartmentId),
		AvailabilityDomain: pulumi.String(availabilityDomain),
		Shape:              pulumi.String(shape),
		DisplayName:        pulumi.String("healthcare-instance-2"),
		CreateVnicDetails: &core.InstanceCreateVnicDetailsArgs{
			SubnetId:       subnet.ID(),
			AssignPublicIp: pulumi.String("true"),
			DisplayName:    pulumi.String("healthcare-vnic-2"),
		},
		SourceDetails: &core.InstanceSourceDetailsArgs{
			SourceType: pulumi.String("image"),
			SourceId:   pulumi.String(imageId),
		},
		Metadata: pulumi.StringMap{
			"ssh_authorized_keys": pulumi.String(sshPublicKey),
			"user_data":           pulumi.String(userData),
		},
	}, pulumi.DeleteBeforeReplace(true))
	if err != nil {
		return pulumi.StringOutput{}, pulumi.StringOutput{}, pulumi.StringOutput{}, pulumi.StringOutput{}, err
	}

	return instance1.PublicIp, instance2.PublicIp, instance1.PrivateIp, instance2.PrivateIp, nil
}

// deployA1 creates a single Ampere A1 Flex instance (always-free: 4 OCPUs, 24GB RAM).
// Gated by pulumi config: pulumi config set a1Enabled true
func deployA1(
	ctx *pulumi.Context,
	compartmentId, availabilityDomain, imageId, sshPublicKey, vmPassword string,
	subnet *core.Subnet,
) (pulumi.StringOutput, error) {
	userData := base64.StdEncoding.EncodeToString([]byte(fmt.Sprintf(cloudInitTpl, vmPassword)))

	instance, err := core.NewInstance(ctx, "healthcare-a1", &core.InstanceArgs{
		CompartmentId:      pulumi.String(compartmentId),
		AvailabilityDomain: pulumi.String(availabilityDomain),
		Shape:              pulumi.String(shapeA1),
		ShapeConfig: &core.InstanceShapeConfigArgs{
			Ocpus:       pulumi.Float64(4),
			MemoryInGbs: pulumi.Float64(24),
		},
		DisplayName: pulumi.String("healthcare-a1"),
		CreateVnicDetails: &core.InstanceCreateVnicDetailsArgs{
			SubnetId:       subnet.ID(),
			AssignPublicIp: pulumi.String("true"),
			DisplayName:    pulumi.String("healthcare-vnic-a1"),
		},
		SourceDetails: &core.InstanceSourceDetailsArgs{
			SourceType: pulumi.String("image"),
			SourceId:   pulumi.String(imageId),
		},
		Metadata: pulumi.StringMap{
			"ssh_authorized_keys": pulumi.String(sshPublicKey),
			"user_data":           pulumi.String(userData),
		},
	}, pulumi.DeleteBeforeReplace(true))
	if err != nil {
		return pulumi.StringOutput{}, err
	}

	return instance.PublicIp, nil
}
