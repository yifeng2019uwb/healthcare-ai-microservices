package main

import (
	"encoding/base64"

	"github.com/pulumi/pulumi-oci/sdk/v2/go/oci/core"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

const shape = "VM.Standard.E2.1.Micro"

// cloudInitBase installs Docker on Ubuntu 22.04 and is shared by both instances.
const cloudInitBase = `#!/bin/bash
set -e
apt-get update -y
apt-get install -y docker.io docker-compose-plugin curl
systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu
`

// cloudInitInstance2 extends the base with PostgreSQL data directory setup.
// /data/postgres is a bind-mount target for the postgres container.
// UID 999 is the postgres user inside the official postgres Docker image.
const cloudInitInstance2 = cloudInitBase + `
hostnamectl set-hostname healthcare-backend
mkdir -p /data/postgres
chown 999:999 /data/postgres
chmod 700 /data/postgres
`

const cloudInitInstance1 = cloudInitBase + `
hostnamectl set-hostname healthcare-gateway
`

func deployCompute(
	ctx *pulumi.Context,
	compartmentId, availabilityDomain, ubuntuImageId, sshPublicKey string,
	subnet *core.Subnet,
) (pulumi.StringOutput, pulumi.StringOutput, error) {

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
			SourceId:   pulumi.String(ubuntuImageId),
		},
		// gateway + auth-service
		Metadata: pulumi.StringMap{
			"ssh_authorized_keys": pulumi.String(sshPublicKey),
			"user_data":           pulumi.String(base64.StdEncoding.EncodeToString([]byte(cloudInitInstance1))),
		},
	})
	if err != nil {
		return pulumi.StringOutput{}, pulumi.StringOutput{}, err
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
			SourceId:   pulumi.String(ubuntuImageId),
		},
		// provider-service + PostgreSQL + ai-service
		// /data/postgres bind-mounted by Docker Compose for PostgreSQL persistence
		Metadata: pulumi.StringMap{
			"ssh_authorized_keys": pulumi.String(sshPublicKey),
			"user_data":           pulumi.String(base64.StdEncoding.EncodeToString([]byte(cloudInitInstance2))),
		},
	})
	if err != nil {
		return pulumi.StringOutput{}, pulumi.StringOutput{}, err
	}

	ip1 := instance1.PublicIp
	ip2 := instance2.PublicIp

	return ip1, ip2, nil
}
