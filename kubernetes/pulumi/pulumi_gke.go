package main

import (
	"github.com/pulumi/pulumi-gcp/sdk/v9/go/gcp/container"
	"github.com/pulumi/pulumi-gcp/sdk/v9/go/gcp/serviceaccount"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func setupServiceAccount(ctx *pulumi.Context) (*serviceaccount.Account, error) {
	return serviceaccount.NewAccount(ctx, serviceAccountID, &serviceaccount.AccountArgs{
		AccountId:   pulumi.String(serviceAccountID),
		DisplayName: pulumi.String("Health AI Service Account"),
	})
}

func deployCluster(ctx *pulumi.Context, sa *serviceaccount.Account, cfg ClusterConfig) error {
	cluster, err := container.NewCluster(ctx, cfg.Name, &container.ClusterArgs{
		Name:                  pulumi.String(cfg.Name),
		Location:              pulumi.String(cfg.Zone),
		RemoveDefaultNodePool: pulumi.Bool(true),
		InitialNodeCount:      pulumi.Int(1),
		DeletionProtection:    pulumi.Bool(false),
		// Disable managed logging/monitoring — avoids ~$25/month Cloud Logging charges.
		// eBPF agent handles security event logging independently via Cloud Logging SDK.
		LoggingService:    pulumi.String("none"),
		MonitoringService: pulumi.String("none"),
	})
	if err != nil {
		return err
	}

	nodePool, err := container.NewNodePool(ctx, cfg.Name+"-pool", &container.NodePoolArgs{
		Name:     pulumi.String("health-ai-pool"),
		Location: pulumi.String(cfg.Zone),
		Cluster:  cluster.Name,
		Autoscaling: &container.NodePoolAutoscalingArgs{
			MinNodeCount: pulumi.Int(1),
			MaxNodeCount: pulumi.Int(3),
		},
		NodeConfig: &container.NodePoolNodeConfigArgs{
			Preemptible:    pulumi.Bool(true),
			MachineType:    pulumi.String(machineType),
			ImageType:      pulumi.String(imageType),
			ServiceAccount: sa.Email,
			OauthScopes: pulumi.StringArray{
				pulumi.String(oauthScope),
			},
			Labels: pulumi.StringMap{
				"workload": pulumi.String("health-ai"),
			},
		},
	})
	if err != nil {
		return err
	}

	ctx.Export("clusterName-"+cfg.Region, cluster.Name)
	ctx.Export("clusterZone-"+cfg.Region, pulumi.String(cfg.Zone))
	ctx.Export("nodePoolName-"+cfg.Region, nodePool.Name)

	return nil
}
