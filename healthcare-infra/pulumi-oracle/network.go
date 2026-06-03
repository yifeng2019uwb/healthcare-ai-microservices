package main

import (
	"github.com/pulumi/pulumi-oci/sdk/v2/go/oci/core"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

const (
	vcnCidr    = "10.0.0.0/16"
	subnetCidr = "10.0.1.0/24"
)

func deployNetwork(ctx *pulumi.Context, compartmentId string) (*core.Subnet, error) {
	cfg := config.New(ctx, "")
	// sshAllowedCidr restricts SSH access. Default 0.0.0.0/0 (open).
	// Set to your IP: pulumi config set sshAllowedCidr <your-ip>/32
	sshAllowedCidr := cfg.Get("sshAllowedCidr")
	if sshAllowedCidr == "" {
		sshAllowedCidr = "0.0.0.0/0"
	}
	vcn, err := core.NewVcn(ctx, "healthcare-vcn", &core.VcnArgs{
		CompartmentId: pulumi.String(compartmentId),
		CidrBlock:     pulumi.String(vcnCidr),
		DisplayName:   pulumi.String("healthcare-vcn"),
	})
	if err != nil {
		return nil, err
	}

	igw, err := core.NewInternetGateway(ctx, "healthcare-igw", &core.InternetGatewayArgs{
		CompartmentId: pulumi.String(compartmentId),
		VcnId:         vcn.ID(),
		DisplayName:   pulumi.String("healthcare-igw"),
		Enabled:       pulumi.Bool(true),
	})
	if err != nil {
		return nil, err
	}

	routeTable, err := core.NewRouteTable(ctx, "healthcare-rt", &core.RouteTableArgs{
		CompartmentId: pulumi.String(compartmentId),
		VcnId:         vcn.ID(),
		DisplayName:   pulumi.String("healthcare-rt"),
		RouteRules: core.RouteTableRouteRuleArray{
			&core.RouteTableRouteRuleArgs{
				NetworkEntityId: igw.ID(),
				Destination:     pulumi.String("0.0.0.0/0"),
				DestinationType: pulumi.String("CIDR_BLOCK"),
			},
		},
	})
	if err != nil {
		return nil, err
	}

	// All ingress/egress rules for the healthcare VCN are defined here.
	// To add a new port: add a rule below and re-run make oracle-up.
	securityList, err := core.NewSecurityList(ctx, "healthcare-sl", &core.SecurityListArgs{
		CompartmentId: pulumi.String(compartmentId),
		VcnId:         vcn.ID(),
		DisplayName:   pulumi.String("healthcare-sl"),
		IngressSecurityRules: core.SecurityListIngressSecurityRuleArray{
			// SSH — restricted to sshAllowedCidr (set via: pulumi config set sshAllowedCidr <ip>/32)
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("6"),
				Source:     pulumi.String(sshAllowedCidr),
				SourceType: pulumi.String("CIDR_BLOCK"),
				TcpOptions: &core.SecurityListIngressSecurityRuleTcpOptionsArgs{
					Min: pulumi.Int(22),
					Max: pulumi.Int(22),
				},
			},
			// Gateway public endpoint (instance-1)
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("6"),
				Source:     pulumi.String("0.0.0.0/0"),
				SourceType: pulumi.String("CIDR_BLOCK"),
				TcpOptions: &core.SecurityListIngressSecurityRuleTcpOptionsArgs{
					Min: pulumi.Int(8080),
					Max: pulumi.Int(8080),
				},
			},
			// Internal VCN — instance-1 routes to instance-2 services (8083, 8085)
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("all"),
				Source:     pulumi.String(vcnCidr),
				SourceType: pulumi.String("CIDR_BLOCK"),
			},
		},
		EgressSecurityRules: core.SecurityListEgressSecurityRuleArray{
			// All outbound — Docker pulls, Gemini API calls
			&core.SecurityListEgressSecurityRuleArgs{
				Protocol:        pulumi.String("all"),
				Destination:     pulumi.String("0.0.0.0/0"),
				DestinationType: pulumi.String("CIDR_BLOCK"),
			},
		},
	})
	if err != nil {
		return nil, err
	}

	subnet, err := core.NewSubnet(ctx, "healthcare-subnet", &core.SubnetArgs{
		CompartmentId:  pulumi.String(compartmentId),
		VcnId:          vcn.ID(),
		CidrBlock:      pulumi.String(subnetCidr),
		DisplayName:    pulumi.String("healthcare-subnet"),
		RouteTableId:   routeTable.ID(),
		SecurityListIds: pulumi.StringArray{securityList.ID()},
		DhcpOptionsId:  vcn.DefaultDhcpOptionsId,
	})
	if err != nil {
		return nil, err
	}

	return subnet, nil
}
