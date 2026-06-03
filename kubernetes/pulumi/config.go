package main

const (
	projectID        = "ebpfagent"
	serviceAccountID = "health-ai-sa"
)

const (
	clusterName = "health-ai-cluster"
	namespace   = "health-ai"
)

// ClusterConfig holds per-region deployment parameters.
type ClusterConfig struct {
	Name   string
	Region string
	Zone   string
}

var clusters = []ClusterConfig{
	{Name: "health-ai-cluster-us-west1", Region: "us-west1", Zone: "us-west1-a"},
}

const (
	machineType = "e2-standard-2"
	// UBUNTU_CONTAINERD required for BTF/eBPF agent support.
	imageType  = "UBUNTU_CONTAINERD"
	oauthScope = "https://www.googleapis.com/auth/cloud-platform"
)

const (
	svcGateway  = "gateway"
	svcAuth     = "auth-service"
	svcProvider = "provider-service"
	svcAI       = "ai-service"
)

const (
	portGateway  = 8080
	portAuth     = 8082
	portProvider = 8083
	portAI       = 8085
)

const (
	arRegion     = "us-west1"
	arRepository = "health-ai"
	arEbpfRepo   = "ebpf-edr"
)

const (
	imagePrefix   = "us-west1-docker.pkg.dev/" + projectID + "/" + arRepository + "/"
	imageGateway  = imagePrefix + "gateway:latest"
	imageAuth     = imagePrefix + "auth-service:latest"
	imageProvider = imagePrefix + "provider-service:latest"
	imageAI       = imagePrefix + "ai-service:latest"
	imageEbpfEdr  = "us-west1-docker.pkg.dev/" + projectID + "/" + arEbpfRepo + "/ebpf-edr:latest"
)
