package main

import (
	"github.com/pulumi/pulumi-oci/sdk/v2/go/oci/core"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

const (
	vcnCidr    = "10.0.0.0/16"
	subnetCidr = "10.0.1.0/24"
)

func deployNetwork(ctx *pulumi.Context, compartmentId string) (*core.Subnet, error) {
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

	// Instance 1 (gateway): expose 8080 publicly.
	// Instance 2 (backend): no public ports — only reachable from within VCN.
	// Both: SSH open for admin access; restrict to your IP in production.
	securityList, err := core.NewSecurityList(ctx, "healthcare-sl", &core.SecurityListArgs{
		CompartmentId: pulumi.String(compartmentId),
		VcnId:         vcn.ID(),
		DisplayName:   pulumi.String("healthcare-sl"),
		IngressSecurityRules: core.SecurityListIngressSecurityRuleArray{
			// SSH — admin access both instances
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("6"),
				Source:     pulumi.String("0.0.0.0/0"),
				SourceType: pulumi.String("CIDR_BLOCK"),
				TcpOptions: &core.SecurityListIngressSecurityRuleTcpOptionsArgs{
					Min: pulumi.Int(22),
					Max: pulumi.Int(22),
				},
			},
			// Gateway public endpoint
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("6"),
				Source:     pulumi.String("0.0.0.0/0"),
				SourceType: pulumi.String("CIDR_BLOCK"),
				TcpOptions: &core.SecurityListIngressSecurityRuleTcpOptionsArgs{
					Min: pulumi.Int(8080),
					Max: pulumi.Int(8080),
				},
			},
			// Internal VCN — instance 1 routes to instance 2 services
			&core.SecurityListIngressSecurityRuleArgs{
				Protocol:   pulumi.String("all"),
				Source:     pulumi.String(vcnCidr),
				SourceType: pulumi.String("CIDR_BLOCK"),
			},
		},
		EgressSecurityRules: core.SecurityListEgressSecurityRuleArray{
			// All outbound — Docker pulls, Vertex AI calls, GCP logging
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
